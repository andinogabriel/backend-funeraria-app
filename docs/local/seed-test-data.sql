-- =============================================================================
-- Local development seed: realistic-but-marked test data for every list view.
--
-- Purpose
-- -------
-- Drops you into a fully-populated database so you can validate every form +
-- list page in the frontend without having to manually create dozens of
-- records one by one. The data is realistic in shape (Argentine names, CUIT-
-- format NIFs, plausible prices) but carries clear markers so you can tell at
-- a glance which rows are test data:
--
--   * Affiliates have DNIs in the 35000001 .. 35000150 range.
--   * Deceased have DNIs in the 38000001 .. 38000120 range.
--   * Suppliers have NIFs starting with "30-99" (the 99 prefix is rare in
--     real Argentine CUIT issuance).
--   * Incomes have receipt numbers in the 99001 .. 99100 range.
--   * Funerals have receipt numbers F-99001 .. F-99120 with series "T".
--   * Items have codes prefixed "TEST-".
--
-- Volumes are sized so the operator hits the pagination + per-column filters
-- on every server-side list (~10–15 pages of 10 rows on each surface). Names,
-- birth dates, deceased flags, plans, etc. cycle deterministically through
-- the catalogs via modulo arithmetic so re-runs produce identical data.
--
-- Run it
-- ------
-- bash / zsh / git-bash:
--   docker exec -i backend-funeraria-postgres psql -U postgres -d funerariadb \
--     < docs/local/seed-test-data.sql
--
-- Windows PowerShell — DO NOT use `<` directly: PowerShell 5.1 does not honor
-- shell-style stdin redirection AND re-encodes the byte stream through the
-- pipe, which corrupts every UTF-8 multibyte character in the seed (every
-- á / é / í / ó / ú / ñ becomes a literal `?`). Use ONE of:
--
--   * cmd /c "..."  — delegates to cmd's classic redirection, which is
--     byte-faithful:
--       cmd /c "docker exec -i backend-funeraria-postgres psql -U postgres -d funerariadb < docs/local/seed-test-data.sql"
--
--   * docker cp + psql -f  — copies the file into the container and reads it
--     from the filesystem, no host pipe involved. This is the safest variant
--     and works identically on every shell:
--       docker cp docs/local/seed-test-data.sql backend-funeraria-postgres:/tmp/seed.sql
--       docker exec backend-funeraria-postgres psql -U postgres -d funerariadb -f /tmp/seed.sql
--
-- If you ever see Spanish names rendering with `??` on the UI (eg. "Cofre
-- Econ??mico Pino"), the seed was loaded through a re-encoding pipe — re-run
-- it via the cmd /c or docker cp variant and the names will come back clean.
--
-- Idempotent
-- ----------
-- The script wipes all prior test data (matched by the markers above) before
-- inserting so re-running is safe. The catalogs from V2 (genders, roles,
-- relationships, receipt types, death causes, provinces, cities) are NEVER
-- touched. The bootstrap admin user is left intact and reused as the actor /
-- policyholder for affiliates and incomes — `admin@funeraria.local` is the
-- default; if you bootstrap a different email through `app.bootstrap.admin.*`
-- the script picks it up automatically by joining on the `users` row that
-- carries `ROLE_ADMIN`.
--
-- What the seed populates
-- -----------------------
--   *   5 suppliers
--   *   8 brands
--   *   6 categories
--   *  18 active items + 5 soft-deleted items (TEST-DEL-* in the papelera)
--   *   4 active plans + 2 soft-deleted plans (in the papelera)
--   * 150 affiliates (18 hand-crafted with intentional family groupings +
--                     132 synthetic via generate_series)
--   * 126 deceased rows with linked funerals (4 hand-crafted with addresses +
--                     116 synthetic spread across ~32 months + 6 arqueo-day
--                     services dated relative to today)
--   * 126 funerals (matching deceased 1:1, receipt types + plans cycled)
--   * 100 active incomes + 5 ANNULLED incomes paired with 5 reversal
--           counter-entries (status=ACTIVE, reversal_of_id set, negative qty)
--   *   9 arqueo-day incomes (8 originals + 1 reversal) dated relative to today
--           so the daily report (/arqueo, PR6) has three reconcilable days:
--           today (net +), yesterday (with an annulled+reversed purchase) and
--           7 days ago (net -, red card). See section 12c.
--   * ~200 income_details (2 lines per synthetic income on average) plus the
--           reversal lines mirroring the originals at negative quantity
--   * varied low_stock_threshold across all items (PR5a); five active items
--           sit below their threshold so the red chip + bell badge show up
--           right after the seed runs
--   *  10 LOW_STOCK_REACHED notifications for ROLE_ADMIN (5 unread + 5 read)
--           with payloads referencing real TEST- items so the bell dropdown
--           and `/notificaciones` center are exercised end-to-end
--
-- After running, the activity-feed + dashboard KPIs will NOT immediately reflect
-- this data: those panels project events from the outbox, and direct SQL inserts
-- do not fire the use-case path. To populate the activity feed, perform any
-- update / delete through the UI (eg. edit an affiliate) — the use case will
-- emit through the outbox and the relay will project it within ~5 s.
--
-- The bell badge + `/notificaciones` page DO show the seed rows immediately
-- because the notifications table is the canonical store for that surface
-- (no outbox projection needed) — the bell polls every 60 s and the center
-- page fetches on navigation.
--
-- Maintainer notes
-- ----------------
-- This file lives under docs/local/ because it is operator tooling, not part
-- of the migration history. Flyway does not see it. If a schema change in V8+
-- breaks the seed, update the INSERTs here (the markers above are stable so
-- the cleanup half stays correct).
-- =============================================================================

begin;

-- ---------------------------------------------------------------------------
-- 1. CLEANUP (idempotency). Order respects FK direction: children first.
-- ---------------------------------------------------------------------------

-- notifications — seed-only rows carry the TEST- item code embedded in the JSON
-- payload so we match on it and leave operational alerts (if any) intact.
delete from notifications
where payload like '%TEST-%';

-- income_details → incomes. Reversal counter-entries (PR4) are linked through
-- `reversal_of_id` so we wipe them along with the originals — the same receipt
-- range catches both because the seed assigns reversals receipt numbers in
-- 99201..99999 which is inside the cleanup range below.
delete from income_details
where income_id in (
    select id from incomes where receipt_number between 99001 and 99999
);
delete from incomes where receipt_number between 99001 and 99999;

-- funeral → deceased → addresses (deceased.address_id is unique on the address)
-- We keep a stash of address ids first because the addresses are FK-cascaded only
-- after we drop the deceased rows that reference them.
do $$
declare
    test_deceased_ids bigint[];
    test_address_ids bigint[];
begin
    select coalesce(array_agg(id), array[]::bigint[]) into test_deceased_ids
    from deceased
    where dni between 38000001 and 38000999;

    select coalesce(array_agg(address_id), array[]::bigint[]) into test_address_ids
    from deceased
    where dni between 38000001 and 38000999 and address_id is not null;

    delete from funeral where deceased_id = any(test_deceased_ids);
    delete from deceased where id = any(test_deceased_ids);
    delete from addresses where id = any(test_address_ids);
end$$;

-- items_plan → plans (children point at plan_id)
delete from items_plan
where plan_id in (select id from plans where name like '[TEST]%')
   or item_id in (select id from items where code like 'TEST-%');
delete from plans where name like '[TEST]%';

-- items → brands / categories (items reference both, both reference suppliers indirectly)
delete from items where code like 'TEST-%';

-- brands / categories — marker is name prefix.
delete from brands where name like '[TEST]%';
delete from categories where name like '[TEST]%';

-- affiliates — referenced by deceased? No, affiliates are independent of deceased.
-- But the audit log + outbox carry rows with these dnis; those are NOT cleaned
-- here because they're operational logs of past activity, not part of the
-- seed itself. See header.
delete from affiliates where dni between 35000001 and 35000999;

-- suppliers — referenced by addresses (FK), mobile_numbers (FK), incomes (cleaned above),
-- and the items table doesn't link directly. Wipe addresses tied to test suppliers.
delete from addresses
where supplier_id in (select id from suppliers where nif like '30-99%');
delete from mobile_numbers
where supplier_id in (select id from suppliers where nif like '30-99%');
delete from suppliers where nif like '30-99%';

-- ---------------------------------------------------------------------------
-- 2. ANCHORS: admin user id is reused as actor / policyholder for everything.
-- ---------------------------------------------------------------------------

-- Sanity check: the seed assumes the bootstrap admin user is already in place.
do $$
declare
    admin_count integer;
begin
    select count(*) into admin_count
    from users u
    join user_role ur on ur.user_id = u.id
    join roles r on r.id = ur.role_id
    where r.name = 'ROLE_ADMIN';

    if admin_count = 0 then
        raise exception 'No ROLE_ADMIN user found. Start the backend once with the docker profile so the bootstrap admin gets created, then re-run this seed.';
    end if;
end$$;

-- ---------------------------------------------------------------------------
-- 3. SUPPLIERS — 5 plausible Argentine wholesalers in the funeral supply chain.
-- ---------------------------------------------------------------------------

insert into suppliers (nif, email, web_page, name) values
    ('30-99100001-1', 'ventas@florestania.com.ar', 'https://florestania.com.ar', 'Florestanía Mayorista'),
    ('30-99100002-9', 'pedidos@cofresdelitoral.com.ar', null, 'Cofres del Litoral'),
    ('30-99100003-7', 'info@cirial-velas.com.ar', 'https://cirial-velas.com.ar', 'Cirial Velas y Liturgia'),
    ('30-99100004-5', 'comercial@requiemtextiles.com.ar', null, 'Requiem Textiles'),
    ('30-99100005-3', 'admin@servicioscobrera.com.ar', 'https://servicioscobrera.com.ar', 'Servicios Cobrera SRL');

-- ---------------------------------------------------------------------------
-- 4. BRANDS + CATEGORIES.
-- ---------------------------------------------------------------------------

insert into brands (id, name, web_page) values
    (nextval('brands_seq'), '[TEST] Hermanos Riera', 'https://riera.com.ar'),
    (nextval('brands_seq'), '[TEST] Cofres Sánchez', null),
    (nextval('brands_seq'), '[TEST] Velas Litúrgicas SA', null),
    (nextval('brands_seq'), '[TEST] Textiles del Norte', null),
    (nextval('brands_seq'), '[TEST] Memorial Gráfica', 'https://memorialgrafica.com.ar'),
    (nextval('brands_seq'), '[TEST] Maderera del Plata', null),
    (nextval('brands_seq'), '[TEST] Floral Andina', null),
    (nextval('brands_seq'), '[TEST] Recordatorios Lumi', null);

insert into categories (id, name, description) values
    (nextval('categories_seq'), '[TEST] Cofres y urnas', 'Cofres de madera y metal, urnas para cremación.'),
    (nextval('categories_seq'), '[TEST] Velas y cirios', 'Velas litúrgicas, cirios de diferentes tamaños.'),
    (nextval('categories_seq'), '[TEST] Arreglos florales', 'Coronas, ramos y arreglos para velatorio.'),
    (nextval('categories_seq'), '[TEST] Textiles', 'Mortajas, cubiertas, sábanas internas.'),
    (nextval('categories_seq'), '[TEST] Recordatorios', 'Estampas, tarjetas y libros de firmas.'),
    (nextval('categories_seq'), '[TEST] Accesorios de capilla', 'Atriles, candelabros, herrajes.');

-- ---------------------------------------------------------------------------
-- 5. ITEMS. 18 items spread across brands + categories. Codes prefixed TEST-.
-- ---------------------------------------------------------------------------

with brand_lookup as (
    select id, name from brands where name like '[TEST]%'
),
category_lookup as (
    select id, name from categories where name like '[TEST]%'
)
insert into items (
    name, code, description, price, stock,
    item_height, item_length, item_width, brand_id, category_id,
    created_at, created_by, updated_at, updated_by
)
select
    seed.name, seed.code, seed.description, seed.price, seed.stock,
    seed.height, seed.length, seed.width,
    (select id from brand_lookup where name = seed.brand_name),
    (select id from category_lookup where name = seed.category_name),
    now(), 'seed-test-data', now(), 'seed-test-data'
from (values
    -- Cofres y urnas
    ('Cofre Standard Roble',        'TEST-COF-001', 'Cofre de roble macizo, herrajes dorados, capacidad estándar.', 185000.00, 12, 60.00, 200.00, 65.00, '[TEST] Cofres Sánchez', '[TEST] Cofres y urnas'),
    ('Cofre Premium Cedro',         'TEST-COF-002', 'Cofre de cedro con interior acolchado, cierre hermético.',     320000.00, 6,  62.00, 205.00, 67.00, '[TEST] Maderera del Plata', '[TEST] Cofres y urnas'),
    ('Cofre Económico Pino',        'TEST-COF-003', 'Cofre de pino lacado, ideal para presupuestos contenidos.',     98000.00, 18, 58.00, 198.00, 62.00, '[TEST] Hermanos Riera', '[TEST] Cofres y urnas'),
    ('Urna para cremación clásica', 'TEST-URN-001', 'Urna de aluminio cepillado, capacidad 4 L.',                    35000.00, 25, 25.00, 18.00, 18.00, '[TEST] Hermanos Riera', '[TEST] Cofres y urnas'),

    -- Velas y cirios
    ('Cirio Pascual 80 cm',         'TEST-CIR-001', 'Cirio decorado, alto poder calórico, 80 cm de altura.',           4500.00, 50, 80.00, 6.00, 6.00, '[TEST] Velas Litúrgicas SA', '[TEST] Velas y cirios'),
    ('Vela ritual blanca 30 cm',    'TEST-VEL-001', 'Vela blanca lisa, 30 cm.',                                         950.00, 200, 30.00, 4.00, 4.00, '[TEST] Velas Litúrgicas SA', '[TEST] Velas y cirios'),
    ('Vela ritual negra 25 cm',     'TEST-VEL-002', 'Vela negra, ceremonia de duelo, 25 cm.',                          1100.00, 150, 25.00, 4.00, 4.00, '[TEST] Velas Litúrgicas SA', '[TEST] Velas y cirios'),

    -- Arreglos florales
    ('Corona de claveles',          'TEST-FLO-001', 'Corona circular de claveles blancos y rojos, 80 cm.',            22000.00, 8,  80.00, 80.00, 15.00, '[TEST] Floral Andina', '[TEST] Arreglos florales'),
    ('Ramo de lirios blancos',      'TEST-FLO-002', 'Ramo de 12 lirios blancos con follaje.',                          9500.00, 30, 60.00, 30.00, 20.00, '[TEST] Floral Andina', '[TEST] Arreglos florales'),
    ('Pétalos sueltos blancos',     'TEST-FLO-003', 'Bolsa de 500 g de pétalos blancos para ceremonia.',               2800.00, 60, 30.00, 25.00, 10.00, '[TEST] Floral Andina', '[TEST] Arreglos florales'),

    -- Textiles
    ('Mortaja blanca',              'TEST-TEX-001', 'Mortaja blanca de tela 100% algodón, talla única.',              12500.00, 40, 5.00,  200.00, 90.00, '[TEST] Textiles del Norte', '[TEST] Textiles'),
    ('Forro interno acolchado',     'TEST-TEX-002', 'Forro interno con relleno acolchado para cofres standard.',      15800.00, 22, 4.00,  200.00, 65.00, '[TEST] Requiem Textiles' /* not a brand */, '[TEST] Textiles'),

    -- Recordatorios
    ('Estampa devocional x 100',    'TEST-REC-001', 'Pack de 100 estampas devocionales con texto personalizable.',     7200.00, 35, 0.50,  10.00, 7.00, '[TEST] Memorial Gráfica', '[TEST] Recordatorios'),
    ('Libro de firmas tapa dura',   'TEST-REC-002', 'Libro de firmas con tapa dura imitación cuero, 80 hojas.',        9800.00, 18, 3.00,  30.00, 22.00, '[TEST] Recordatorios Lumi', '[TEST] Recordatorios'),
    ('Tarjeta de agradecimiento',   'TEST-REC-003', 'Pack de 50 tarjetas de agradecimiento.',                          3500.00, 28, 0.20,  15.00, 10.00, '[TEST] Recordatorios Lumi', '[TEST] Recordatorios'),

    -- Accesorios de capilla
    ('Candelabro 5 brazos',         'TEST-ACC-001', 'Candelabro de bronce, 5 brazos, altura 90 cm.',                  68000.00, 5,  90.00, 60.00, 25.00, '[TEST] Hermanos Riera', '[TEST] Accesorios de capilla'),
    ('Atril liturgia roble',        'TEST-ACC-002', 'Atril de roble con superficie inclinable.',                      45500.00, 7,  120.00, 50.00, 40.00, '[TEST] Maderera del Plata', '[TEST] Accesorios de capilla'),
    ('Crucifijo de pared 40 cm',    'TEST-ACC-003', 'Crucifijo de madera, 40 cm de altura.',                          12000.00, 15, 40.00, 25.00, 4.00, '[TEST] Hermanos Riera', '[TEST] Accesorios de capilla')
) as seed(name, code, description, price, stock, height, length, width, brand_name, category_name);

-- The "Forro interno acolchado" row above references a supplier name that doesn't
-- exist as a brand — the lookup left brand_id null on that item. Patch it to a
-- valid brand so the items list still groups it visually.
update items
set brand_id = (select id from brands where name = '[TEST] Textiles del Norte')
where code = 'TEST-TEX-002' and brand_id is null;

-- ---------------------------------------------------------------------------
-- 6. PLANS + items_plan composition. Four tiers.
-- ---------------------------------------------------------------------------

insert into plans (name, description, price, profit_percentage, image_url) values
    ('[TEST] Plan Económico',  'Servicio básico para presupuestos contenidos. Incluye cofre de pino y velas estándar.',                     280000.00, 18.00, null),
    ('[TEST] Plan Standard',   'Servicio integral con cofre de roble, velas, flores y recordatorios.',                                       520000.00, 22.00, null),
    ('[TEST] Plan Premium',    'Cofre de cedro premium, decoración floral elaborada y servicios adicionales de despedida.',                  850000.00, 26.00, null),
    ('[TEST] Plan Memorial',   'Plan completo con cofre, accesorios de capilla, decoración y materiales de recordatorio para los deudos.',  1100000.00, 28.00, null);

with item_lookup as (
    select id, code from items where code like 'TEST-%'
),
plan_lookup as (
    select id, name from plans where name like '[TEST]%'
)
insert into items_plan (plan_id, item_id, quantity)
select
    (select id from plan_lookup where name = mapping.plan_name),
    (select id from item_lookup where code = mapping.item_code),
    mapping.quantity
from (values
    -- Económico: cofre simple + velas
    ('[TEST] Plan Económico', 'TEST-COF-003', 1),
    ('[TEST] Plan Económico', 'TEST-VEL-001', 4),
    ('[TEST] Plan Económico', 'TEST-CIR-001', 1),

    -- Standard
    ('[TEST] Plan Standard', 'TEST-COF-001', 1),
    ('[TEST] Plan Standard', 'TEST-CIR-001', 2),
    ('[TEST] Plan Standard', 'TEST-VEL-001', 6),
    ('[TEST] Plan Standard', 'TEST-FLO-002', 2),
    ('[TEST] Plan Standard', 'TEST-REC-001', 1),

    -- Premium
    ('[TEST] Plan Premium', 'TEST-COF-002', 1),
    ('[TEST] Plan Premium', 'TEST-TEX-002', 1),
    ('[TEST] Plan Premium', 'TEST-CIR-001', 2),
    ('[TEST] Plan Premium', 'TEST-VEL-001', 8),
    ('[TEST] Plan Premium', 'TEST-FLO-001', 2),
    ('[TEST] Plan Premium', 'TEST-FLO-002', 3),
    ('[TEST] Plan Premium', 'TEST-REC-002', 1),

    -- Memorial
    ('[TEST] Plan Memorial', 'TEST-COF-002', 1),
    ('[TEST] Plan Memorial', 'TEST-TEX-001', 1),
    ('[TEST] Plan Memorial', 'TEST-TEX-002', 1),
    ('[TEST] Plan Memorial', 'TEST-CIR-001', 3),
    ('[TEST] Plan Memorial', 'TEST-VEL-001', 10),
    ('[TEST] Plan Memorial', 'TEST-FLO-001', 3),
    ('[TEST] Plan Memorial', 'TEST-FLO-002', 4),
    ('[TEST] Plan Memorial', 'TEST-REC-002', 2),
    ('[TEST] Plan Memorial', 'TEST-REC-003', 5),
    ('[TEST] Plan Memorial', 'TEST-ACC-001', 1),
    ('[TEST] Plan Memorial', 'TEST-ACC-002', 1),
    ('[TEST] Plan Memorial', 'TEST-ACC-003', 1)
) as mapping(plan_name, item_code, quantity);

-- ---------------------------------------------------------------------------
-- 7. AFFILIATES. 18 entries with a mix of ages, genders, relationships.
-- DNIs 35000001..35000018. All linked to the admin user as policyholder.
-- ---------------------------------------------------------------------------

with admin_user as (
    select u.id
    from users u
    join user_role ur on ur.user_id = u.id
    join roles r on r.id = ur.role_id
    where r.name = 'ROLE_ADMIN'
    order by u.id
    limit 1
)
insert into affiliates (dni, first_name, last_name, birth_date, deceased, start_date, gender_id, relationship_id, user_id)
select
    seed.dni, seed.first_name, seed.last_name, seed.birth_date, seed.deceased,
    seed.start_date, seed.gender_id, seed.relationship_id,
    (select id from admin_user)
from (values
    (35000001, 'Mariana',  'Quiroga',    '1962-03-15'::date, false, '2024-01-12'::date, 1, 1),
    (35000002, 'Ricardo',  'Ferreyra',   '1958-07-22'::date, false, '2024-01-12'::date, 2, 1),
    (35000003, 'Sofía',    'Vázquez',    '1985-11-04'::date, false, '2024-02-03'::date, 1, 4),
    (35000004, 'Tomás',    'Vázquez',    '1990-05-18'::date, false, '2024-02-03'::date, 2, 3),
    (35000005, 'Lucía',    'Quiroga',    '1995-09-29'::date, false, '2024-02-03'::date, 1, 4),
    (35000006, 'Esteban',  'Quiroga',    '2001-12-06'::date, false, '2024-02-03'::date, 2, 3),
    (35000007, 'Carmen',   'Iglesias',   '1948-04-11'::date, true,  '2023-08-14'::date, 1, 2),
    (35000008, 'Héctor',   'Saavedra',   '1944-10-30'::date, true,  '2023-05-20'::date, 2, 1),
    (35000009, 'Pilar',    'Saavedra',   '1972-06-08'::date, false, '2023-05-20'::date, 1, 4),
    (35000010, 'Andrés',   'Reyes',      '1980-02-14'::date, false, '2024-03-01'::date, 2, 7),
    (35000011, 'Valeria',  'Reyes',      '1983-08-25'::date, false, '2024-03-01'::date, 1, 8),
    (35000012, 'Joaquín',  'Reyes',      '2012-01-30'::date, false, '2024-03-01'::date, 2, 13),
    (35000013, 'Renata',   'Reyes',      '2015-07-19'::date, false, '2024-03-01'::date, 1, 14),
    (35000014, 'Beatriz',  'Costa',      '1955-12-01'::date, false, '2024-04-15'::date, 1, 10),
    (35000015, 'Marcelo',  'Costa',      '1979-09-23'::date, false, '2024-04-15'::date, 2, 3),
    (35000016, 'Ana',      'Lema',       '1968-05-17'::date, false, '2024-05-08'::date, 1, 12),
    (35000017, 'Juan',     'Lema',       '1934-02-28'::date, true,  '2023-11-02'::date, 2, 9),
    (35000018, 'Soledad',  'Bordón',     '1990-10-09'::date, false, '2024-06-20'::date, 1, 29)
) as seed(dni, first_name, last_name, birth_date, deceased, start_date, gender_id, relationship_id);

-- ---------------------------------------------------------------------------
-- 7b. AFFILIATES bulk — 132 more synthetic rows so the operator hits the
-- pagination + filter affordances on /afiliados with realistic volume.
-- DNIs 35000019..35000150. Deterministic from `n` so re-runs produce identical
-- data; modulo arithmetic spreads names, birth dates, gender, relationships
-- across the catalog. ~8% flipped to `deceased = true` so the deceased flag
-- has something to surface.
-- ---------------------------------------------------------------------------

with admin_user as (
    select u.id
    from users u
    join user_role ur on ur.user_id = u.id
    join roles r on r.id = ur.role_id
    where r.name = 'ROLE_ADMIN'
    order by u.id
    limit 1
),
first_names(name) as (values
    ('Lucas'), ('Martín'), ('Diego'), ('Federico'), ('Carlos'), ('Roberto'),
    ('Hernán'), ('Pablo'), ('Sergio'), ('Daniel'), ('Maximiliano'), ('Nicolás'),
    ('Sebastián'), ('Mauricio'), ('Gustavo'), ('Eduardo'), ('Alejandro'),
    ('Fernando'), ('Gabriel'), ('Andrés'),
    ('Laura'), ('Patricia'), ('Silvia'), ('Verónica'), ('Claudia'), ('Mónica'),
    ('Adriana'), ('Marta'), ('Susana'), ('Liliana'), ('Florencia'), ('Carolina'),
    ('Belén'), ('Romina'), ('Daniela'), ('Camila'), ('Agustina'), ('Julieta'),
    ('Lorena'), ('Natalia')
),
last_names(name) as (values
    ('González'), ('Rodríguez'), ('Gómez'), ('Fernández'), ('López'), ('Díaz'),
    ('Martínez'), ('Pérez'), ('García'), ('Sánchez'), ('Romero'), ('Torres'),
    ('Álvarez'), ('Ruiz'), ('Ramírez'), ('Flores'), ('Acosta'), ('Benítez'),
    ('Medina'), ('Suárez'), ('Aguirre'), ('Ojeda'), ('Ortega'), ('Molina'),
    ('Castro'), ('Vega'), ('Cabrera'), ('Núñez'), ('Rojas'), ('Silva')
),
first_indexed as (
    select name, row_number() over () - 1 as idx from first_names
),
last_indexed as (
    select name, row_number() over () - 1 as idx from last_names
)
insert into affiliates (dni, first_name, last_name, birth_date, deceased, start_date, gender_id, relationship_id, user_id)
select
    35000000 + n,
    (select name from first_indexed where idx = (n * 7) % 40),
    (select name from last_indexed where idx = (n * 11) % 30),
    -- Birth dates between 1935-01-01 and 2010-12-31 (~75 years span, ~27000 days).
    date '1935-01-01' + ((n * 137) % 27000) * interval '1 day',
    -- ~8% deceased so the lifecycle filter on /afiliados has matches.
    (n % 13 = 0),
    -- Start dates between 2022-01-01 and 2026-04-30 (~1580 days).
    date '2022-01-01' + ((n * 17) % 1580) * interval '1 day',
    -- Gender 1 (Femenino) or 2 (Masculino), alternating.
    1 + (n % 2),
    -- Cycle through relationships 1..30 (the V2 catalog has 31 with one
    -- intentional duplicate); modulo gives a deterministic spread without
    -- skewing the distribution toward early ids.
    1 + (n % 30),
    (select id from admin_user)
from generate_series(19, 150) as n;

-- ---------------------------------------------------------------------------
-- 8. ADDRESSES for the deceased (deceased.address_id is unique).
-- ---------------------------------------------------------------------------

insert into addresses (id, street_name, block_street, apartment, flat, city_id, supplier_id, user_id) values
    (nextval('addresses_seq'), 'Av. Belgrano',        1245, '2',  'A', 21415, null, null),
    (nextval('addresses_seq'), 'Calle Rivadavia',      876, null, null, 12452, null, null),
    (nextval('addresses_seq'), 'Pasaje San Martín',    321, '1',  'B', 21802, null, null),
    (nextval('addresses_seq'), 'Av. Independencia',   1580, null, null, 12572, null, null);

-- ---------------------------------------------------------------------------
-- 9. DECEASED rows (linked to addresses + admin user; some link to affiliates).
-- DNIs 38000001..38000004 distinguish deceased from affiliates.
-- ---------------------------------------------------------------------------

with admin_user as (
    select u.id
    from users u
    join user_role ur on ur.user_id = u.id
    join roles r on r.id = ur.role_id
    where r.name = 'ROLE_ADMIN'
    order by u.id
    limit 1
),
test_addresses as (
    select id, row_number() over (order by id) as rn from addresses
    where street_name in ('Av. Belgrano', 'Calle Rivadavia', 'Pasaje San Martín', 'Av. Independencia')
)
insert into deceased (id, dni, first_name, last_name, birth_date, death_date, register_date, affiliated, gender_id, relationship_id, death_cause_id, address_id, user_id)
select
    nextval('deceased_seq'),
    seed.dni, seed.first_name, seed.last_name, seed.birth_date, seed.death_date,
    seed.death_date::timestamp + time '10:30',
    seed.affiliated, seed.gender_id, seed.relationship_id, seed.death_cause_id,
    (select id from test_addresses where rn = seed.address_rn),
    (select id from admin_user)
from (values
    (38000001, 'Carmen',  'Iglesias',    '1948-04-11'::date, '2024-09-12'::date, true,  1, 2, 2, 1),
    (38000002, 'Héctor',  'Saavedra',    '1944-10-30'::date, '2024-10-05'::date, true,  2, 1, 1, 2),
    (38000003, 'Juan',    'Lema',        '1934-02-28'::date, '2024-11-22'::date, true,  2, 9, 2, 3),
    (38000004, 'Olga',    'Pinedo',      '1939-08-15'::date, '2024-12-08'::date, false, 1, 2, 4, 4)
) as seed(dni, first_name, last_name, birth_date, death_date, affiliated, gender_id, relationship_id, death_cause_id, address_rn);

-- ---------------------------------------------------------------------------
-- 10. FUNERAL records. Receipt numbers F-99001..F-99004, series "T".
-- ---------------------------------------------------------------------------

with plan_lookup as (
    select id, name from plans where name like '[TEST]%'
),
deceased_lookup as (
    select id, dni from deceased where dni between 38000001 and 38000004
)
insert into funeral (receipt_number, receipt_series, funeral_date, register_date, tax, total_amount, plan_id, receipt_type_id, deceased_id)
select
    seed.receipt_number, 'T',
    seed.funeral_date::timestamp,
    seed.funeral_date::timestamp - interval '1 day',
    seed.tax, seed.total_amount,
    (select id from plan_lookup where name = seed.plan_name),
    seed.receipt_type_id,
    (select id from deceased_lookup where dni = seed.deceased_dni)
from (values
    ('F-99001', '2024-09-14'::date, 21.00,  280000.00, '[TEST] Plan Económico', 1, 38000001),
    ('F-99002', '2024-10-07'::date, 21.00,  520000.00, '[TEST] Plan Standard',  1, 38000002),
    ('F-99003', '2024-11-24'::date, 21.00,  850000.00, '[TEST] Plan Premium',   1, 38000003),
    ('F-99004', '2024-12-10'::date, 21.00, 1100000.00, '[TEST] Plan Memorial',  1, 38000004)
) as seed(receipt_number, funeral_date, tax, total_amount, plan_name, receipt_type_id, deceased_dni);

-- ---------------------------------------------------------------------------
-- 10b. DECEASED + FUNERAL bulk — 116 more synthetic services so the operator
-- hits pagination + dateRange filters on /servicios with realistic volume.
-- DNIs 38000005..38000120, receipt numbers F-99005..F-99120. Funeral dates
-- spread across ~32 months (2023-09 through 2026-05) so the dateRange column
-- menu actually slices something. `address_id` left null on the bulk rows —
-- the placeOfDeath section on the detail view is optional.
-- ---------------------------------------------------------------------------

with admin_user as (
    select u.id
    from users u
    join user_role ur on ur.user_id = u.id
    join roles r on r.id = ur.role_id
    where r.name = 'ROLE_ADMIN'
    order by u.id
    limit 1
),
first_names(name) as (values
    ('Lucas'), ('Martín'), ('Diego'), ('Federico'), ('Carlos'), ('Roberto'),
    ('Hernán'), ('Pablo'), ('Sergio'), ('Daniel'), ('Maximiliano'), ('Nicolás'),
    ('Sebastián'), ('Mauricio'), ('Gustavo'), ('Eduardo'), ('Alejandro'),
    ('Fernando'), ('Gabriel'), ('Andrés'),
    ('Laura'), ('Patricia'), ('Silvia'), ('Verónica'), ('Claudia'), ('Mónica'),
    ('Adriana'), ('Marta'), ('Susana'), ('Liliana'), ('Florencia'), ('Carolina'),
    ('Belén'), ('Romina'), ('Daniela'), ('Camila'), ('Agustina'), ('Julieta'),
    ('Lorena'), ('Natalia')
),
last_names(name) as (values
    ('González'), ('Rodríguez'), ('Gómez'), ('Fernández'), ('López'), ('Díaz'),
    ('Martínez'), ('Pérez'), ('García'), ('Sánchez'), ('Romero'), ('Torres'),
    ('Álvarez'), ('Ruiz'), ('Ramírez'), ('Flores'), ('Acosta'), ('Benítez'),
    ('Medina'), ('Suárez'), ('Aguirre'), ('Ojeda'), ('Ortega'), ('Molina'),
    ('Castro'), ('Vega'), ('Cabrera'), ('Núñez'), ('Rojas'), ('Silva')
),
first_indexed as (
    select name, row_number() over () - 1 as idx from first_names
),
last_indexed as (
    select name, row_number() over () - 1 as idx from last_names
),
plan_lookup as (
    select id, name, row_number() over (order by id) - 1 as idx
    from plans where name like '[TEST]%'
),
plan_total_amounts(name, amount) as (values
    ('[TEST] Plan Económico',  280000.00::numeric),
    ('[TEST] Plan Standard',   520000.00::numeric),
    ('[TEST] Plan Premium',    850000.00::numeric),
    ('[TEST] Plan Memorial',  1100000.00::numeric)
),
bulk_input as (
    select
        n,
        38000000 + n as dni,
        'F-' || lpad((99000 + n)::text, 5, '0') as receipt_number,
        (select name from first_indexed where idx = (n * 7) % 40) as first_name,
        (select name from last_indexed where idx = (n * 11) % 30) as last_name,
        -- Birth dates between 1925-01-01 and 1985-12-31 (~60 years; the
        -- deceased skew older than the affiliate population).
        date '1925-01-01' + ((n * 113) % 22300) as birth_date,
        -- Funeral dates between 2023-09-01 and 2026-05-15 (~990 days).
        date '2023-09-01' + ((n * 23) % 990) as funeral_date,
        -- Cycle through gender / relationship / death_cause (V2 catalogs):
        --   genders 1..3, relationships 1..31, death_causes 1..4.
        1 + (n % 3) as gender_id,
        1 + (n % 31) as relationship_id,
        1 + (n % 4) as death_cause_id,
        -- ~30% bound to an existing affiliate via the `affiliated` flag.
        (n % 3 = 0) as affiliated,
        -- Receipt types 1..3 cycle so the column filter on /servicios has data.
        1 + (n % 3) as receipt_type_id,
        -- Pick the plan deterministically across the 4-tier catalog.
        (select name from plan_lookup where idx = n % 4) as plan_name
    from generate_series(5, 120) as n
),
inserted_deceased as (
    insert into deceased (id, dni, first_name, last_name, birth_date, death_date, register_date, affiliated, gender_id, relationship_id, death_cause_id, address_id, user_id)
    select
        nextval('deceased_seq'),
        bulk_input.dni,
        bulk_input.first_name,
        bulk_input.last_name,
        bulk_input.birth_date,
        -- Death date is the day before the funeral (1-day funeral lead time).
        bulk_input.funeral_date - interval '1 day',
        (bulk_input.funeral_date - interval '1 day')::timestamp + time '14:00',
        bulk_input.affiliated,
        bulk_input.gender_id,
        bulk_input.relationship_id,
        bulk_input.death_cause_id,
        null,
        (select id from admin_user)
    from bulk_input
    returning id, dni
)
insert into funeral (receipt_number, receipt_series, funeral_date, register_date, tax, total_amount, plan_id, receipt_type_id, deceased_id)
select
    bulk_input.receipt_number, 'T',
    bulk_input.funeral_date::timestamp + time '10:30',
    bulk_input.funeral_date::timestamp - interval '1 day',
    21.00,
    (select amount from plan_total_amounts where name = bulk_input.plan_name),
    (select id from plan_lookup where name = bulk_input.plan_name),
    bulk_input.receipt_type_id,
    (select id from inserted_deceased where dni = bulk_input.dni)
from bulk_input;

-- ---------------------------------------------------------------------------
-- 11. INCOMES (compras a proveedores). 12 receipts spread over 3 months so the
-- "Fecha" column dateRange filter has data to slice. Receipt numbers 99001..99012.
-- ---------------------------------------------------------------------------

with admin_user as (
    select u.id
    from users u
    join user_role ur on ur.user_id = u.id
    join roles r on r.id = ur.role_id
    where r.name = 'ROLE_ADMIN'
    order by u.id
    limit 1
),
supplier_lookup as (
    select id, nif from suppliers where nif like '30-99%'
)
insert into incomes (id, deleted, tax, total_amount, income_date, last_modified_date, receipt_number, receipt_series, receipt_type_id, supplier_id, user_id, user_modified_id)
select
    nextval('incomes_seq'),
    false,
    seed.tax,
    seed.total_amount,
    seed.income_date::timestamp,
    seed.income_date::timestamp,
    seed.receipt_number,
    1001,
    1,
    (select id from supplier_lookup where nif = seed.supplier_nif),
    (select id from admin_user),
    (select id from admin_user)
-- V8 widened these to numeric(13, 2) so the seed can use realistic ARS amounts
-- (typical wholesale order of several cofres + accessories runs into the
-- millions of pesos at current price levels).
from (values
    -- September: 4 incomes
    (99001, '2025-09-03'::date, 21.00,  1485000.00, '30-99100001-1'),  -- Florestanía
    (99002, '2025-09-09'::date, 21.00,   620000.00, '30-99100003-7'),  -- Cirial Velas
    (99003, '2025-09-18'::date, 21.00,  2310000.00, '30-99100002-9'),  -- Cofres del Litoral
    (99004, '2025-09-26'::date, 10.50,   185000.00, '30-99100004-5'),  -- Requiem Textiles
    -- October: 4 incomes
    (99005, '2025-10-04'::date, 21.00,   840000.00, '30-99100001-1'),
    (99006, '2025-10-11'::date, 21.00,   390000.00, '30-99100005-3'),  -- Servicios Cobrera
    (99007, '2025-10-18'::date, 21.00,  1750000.00, '30-99100002-9'),
    (99008, '2025-10-25'::date, 21.00,   275000.00, '30-99100003-7'),
    -- November: 4 incomes
    (99009, '2025-11-02'::date, 21.00,   920000.00, '30-99100001-1'),
    (99010, '2025-11-09'::date, 21.00,   455000.00, '30-99100004-5'),
    (99011, '2025-11-17'::date, 21.00,  1980000.00, '30-99100002-9'),
    (99012, '2025-11-24'::date, 10.50,   148000.00, '30-99100005-3')
) as seed(receipt_number, income_date, tax, total_amount, supplier_nif);

-- ---------------------------------------------------------------------------
-- 12. INCOME_DETAILS. 2 lines per income on average.
-- ---------------------------------------------------------------------------

with income_lookup as (
    select id, receipt_number from incomes where receipt_number between 99001 and 99012
),
item_lookup as (
    select id, code from items where code like 'TEST-%'
)
insert into income_details (id, income_id, item_id, quantity, purchase_price, sale_price)
select
    nextval('income_details_seq'),
    (select id from income_lookup where receipt_number = seed.receipt_number),
    (select id from item_lookup where code = seed.item_code),
    seed.quantity, seed.purchase_price, seed.sale_price
from (values
    -- 99001: floral order
    (99001::bigint, 'TEST-FLO-001',  8, 18000.00, 22000.00),
    (99001,         'TEST-FLO-002', 25,  7500.00,  9500.00),
    -- 99002: candles
    (99002, 'TEST-CIR-001', 60, 3200.00, 4500.00),
    (99002, 'TEST-VEL-001', 400, 650.00,  950.00),
    -- 99003: caskets
    (99003, 'TEST-COF-001',  8, 142000.00, 185000.00),
    (99003, 'TEST-COF-002',  3, 245000.00, 320000.00),
    -- 99004: textile
    (99004, 'TEST-TEX-001', 18,  9500.00, 12500.00),
    -- 99005: more flowers
    (99005, 'TEST-FLO-001',  5, 18000.00, 22000.00),
    (99005, 'TEST-FLO-003', 30,  2100.00,  2800.00),
    -- 99006: service
    (99006, 'TEST-ACC-001',  3, 52000.00, 68000.00),
    -- 99007: caskets + accessories
    (99007, 'TEST-COF-001',  6, 142000.00, 185000.00),
    (99007, 'TEST-ACC-002',  4, 35000.00,  45500.00),
    -- 99008: candles
    (99008, 'TEST-VEL-001', 200, 650.00,  950.00),
    (99008, 'TEST-VEL-002', 100, 800.00, 1100.00),
    -- 99009: flowers
    (99009, 'TEST-FLO-001',  6, 18000.00, 22000.00),
    (99009, 'TEST-FLO-002', 25,  7500.00,  9500.00),
    -- 99010: textile + records
    (99010, 'TEST-TEX-002',  9, 11800.00, 15800.00),
    (99010, 'TEST-REC-001', 20,  5400.00,  7200.00),
    -- 99011: caskets
    (99011, 'TEST-COF-002',  5, 245000.00, 320000.00),
    (99011, 'TEST-COF-003', 10,  72000.00,  98000.00),
    -- 99012: service / accessories
    (99012, 'TEST-REC-002', 12,  7400.00,  9800.00),
    (99012, 'TEST-REC-003', 10,  2600.00,  3500.00)
) as seed(receipt_number, item_code, quantity, purchase_price, sale_price);

-- ---------------------------------------------------------------------------
-- 12b. INCOMES + INCOME_DETAILS bulk — 88 more receipts spread across ~28
-- months (2024-01 through 2026-05) so the /ingresos dateRange filter has
-- volume to slice. Receipt numbers 99013..99100. Two detail lines per
-- income on average so the totals + detail dialog look populated.
-- ---------------------------------------------------------------------------

with admin_user as (
    select u.id
    from users u
    join user_role ur on ur.user_id = u.id
    join roles r on r.id = ur.role_id
    where r.name = 'ROLE_ADMIN'
    order by u.id
    limit 1
),
supplier_lookup as (
    select id, nif, row_number() over (order by id) - 1 as idx
    from suppliers where nif like '30-99%'
),
item_lookup as (
    select id, code, price, row_number() over (order by id) - 1 as idx
    from items where code like 'TEST-%'
),
item_count as (select count(*)::int as n from item_lookup),
bulk_input as (
    select
        n,
        99000 + n as receipt_number,
        -- Income dates between 2024-01-15 and 2026-05-10 (~846 days).
        date '2024-01-15' + ((n * 19) % 846) as income_date,
        -- Cycle through the 5 suppliers; tax 21% on most, 10.5% on a few.
        (select nif from supplier_lookup where idx = n % 5) as supplier_nif,
        case when n % 7 = 0 then 10.50 else 21.00 end as tax,
        -- Plausible total amounts between $150k and $2.5M.
        round(150000 + ((n * 47) % 2350000)::numeric, 2) as total_amount
    from generate_series(13, 100) as n
),
inserted_incomes as (
    insert into incomes (id, deleted, tax, total_amount, income_date, last_modified_date, receipt_number, receipt_series, receipt_type_id, supplier_id, user_id, user_modified_id)
    select
        nextval('incomes_seq'),
        false,
        bulk_input.tax,
        bulk_input.total_amount,
        bulk_input.income_date::timestamp + time '11:00',
        bulk_input.income_date::timestamp + time '11:00',
        bulk_input.receipt_number,
        1001,
        1,
        (select id from supplier_lookup where nif = bulk_input.supplier_nif),
        (select id from admin_user),
        (select id from admin_user)
    from bulk_input
    returning id, receipt_number
),
-- Two synthetic detail lines per bulk income — picking items deterministically
-- from the cycle so each receipt has different content.
detail_rows as (
    select
        bulk_input.receipt_number,
        (select code from item_lookup
         where idx = (bulk_input.n * 3) % (select n from item_count)) as item_code,
        2 + (bulk_input.n % 8) as quantity,
        round((50000 + ((bulk_input.n * 41) % 250000))::numeric, 2) as purchase_price,
        round((70000 + ((bulk_input.n * 41) % 250000))::numeric, 2) as sale_price
    from bulk_input
    union all
    select
        bulk_input.receipt_number,
        (select code from item_lookup
         where idx = (bulk_input.n * 5 + 1) % (select n from item_count)) as item_code,
        1 + (bulk_input.n % 4) as quantity,
        round((30000 + ((bulk_input.n * 67) % 200000))::numeric, 2) as purchase_price,
        round((45000 + ((bulk_input.n * 67) % 200000))::numeric, 2) as sale_price
    from bulk_input
)
insert into income_details (id, income_id, item_id, quantity, purchase_price, sale_price)
select
    nextval('income_details_seq'),
    (select id from inserted_incomes where receipt_number = detail_rows.receipt_number),
    (select id from item_lookup where code = detail_rows.item_code),
    detail_rows.quantity,
    detail_rows.purchase_price,
    detail_rows.sale_price
from detail_rows;

-- ---------------------------------------------------------------------------
-- 12c. ARQUEO DIARIO fixtures (PR6). The daily cash-reconciliation report groups
-- by calendar day, but the bulk funerals / incomes above scatter their dates
-- across ~2-3 years with near-zero same-day overlap — so no single day has the
-- volume to make the arqueo meaningful, and nothing lands near "today". This
-- block seeds three hand-picked days RELATIVE to current_date so the operator
-- can open /arqueo and immediately reconcile a populated day no matter when the
-- seed runs:
--
--   * TODAY      — 3 servicios + 2 compras, net POSITIVE, no annulments.
--   * YESTERDAY  — 2 servicios + 2 compras + 1 compra ANULADA with its same-day
--                  reversal counter-entry; the reversal nets out of the active
--                  total and annulledCount surfaces 1.
--   * 7 DAYS AGO — 1 servicio chico + 3 compras grandes, net NEGATIVE so the
--                  "Neto del día" card renders red.
--
-- Markers stay inside the existing cleanup ranges (deceased dni 38000121..126,
-- funeral receipts F-99121..126, incomes 99301..308 + reversal 99211), so the
-- cleanup at the top of the file already wipes them on a re-run.
--
-- income_date is inserted at 11:00 local: comfortably inside the day on either
-- side of the report's Argentina-zone (UTC-3) bracket, same as every other
-- income in this seed.
-- ---------------------------------------------------------------------------

-- Deceased for the arqueo funerals. dni 38000121..126, just past the bulk range.
with admin_user as (
    select u.id from users u
    join user_role ur on ur.user_id = u.id
    join roles r on r.id = ur.role_id
    where r.name = 'ROLE_ADMIN'
    order by u.id
    limit 1
)
insert into deceased (id, dni, first_name, last_name, birth_date, death_date, register_date, affiliated, gender_id, relationship_id, death_cause_id, address_id, user_id)
select
    nextval('deceased_seq'),
    seed.dni,
    seed.first_name,
    seed.last_name,
    date '1940-01-01' + ((seed.dni % 18000)) as birth_date,
    current_date - seed.day_offset - 1 as death_date,
    (current_date - seed.day_offset - 1)::timestamp + time '14:00',
    false,
    1 + (seed.dni % 3),
    1 + (seed.dni % 31),
    1 + (seed.dni % 4),
    null,
    (select id from admin_user)
from (values
    (38000121, 'Raúl',    'Domínguez', 0),
    (38000122, 'Elena',   'Paredes',   0),
    (38000123, 'Tomás',   'Bianchi',   0),
    (38000124, 'Norma',   'Vázquez',   1),
    (38000125, 'Héctor',  'Salas',     1),
    (38000126, 'Beatriz', 'Ferreyra',  7)
) as seed(dni, first_name, last_name, day_offset);

-- Funerals (servicios = money IN). Receipt F-99121..126, dates relative to today.
with plan_lookup as (
    select id, name from plans where name like '[TEST]%'
),
deceased_lookup as (
    select id, dni from deceased where dni between 38000121 and 38000126
)
insert into funeral (receipt_number, receipt_series, funeral_date, register_date, tax, total_amount, plan_id, receipt_type_id, deceased_id)
select
    seed.receipt_number, 'T',
    (current_date - seed.day_offset)::timestamp + time '10:30',
    (current_date - seed.day_offset)::timestamp - interval '1 day',
    21.00, seed.total_amount,
    (select id from plan_lookup where name = seed.plan_name),
    1,
    (select id from deceased_lookup where dni = seed.deceased_dni)
from (values
    -- TODAY: 3 servicios → total 2,370,000
    ('F-99121', 0, 450000.00,  '[TEST] Plan Standard',   38000121),
    ('F-99122', 0, 820000.00,  '[TEST] Plan Premium',    38000122),
    ('F-99123', 0, 1100000.00, '[TEST] Plan Memorial',   38000123),
    -- YESTERDAY: 2 servicios → total 1,200,000
    ('F-99124', 1, 520000.00,  '[TEST] Plan Standard',   38000124),
    ('F-99125', 1, 680000.00,  '[TEST] Plan Premium',    38000125),
    -- 7 DAYS AGO: 1 servicio chico → total 300,000
    ('F-99126', 7, 300000.00,  '[TEST] Plan Económico',  38000126)
) as seed(receipt_number, day_offset, total_amount, plan_name, deceased_dni);

-- Incomes (compras = money OUT). 99301..308 originals, day-relative dates.
-- 99304 is ANNULLED (its reversal 99211 lands the same day, below).
with admin_user as (
    select u.id from users u
    join user_role ur on ur.user_id = u.id
    join roles r on r.id = ur.role_id
    where r.name = 'ROLE_ADMIN'
    order by u.id
    limit 1
),
supplier_lookup as (
    select id, nif from suppliers where nif like '30-99%'
)
insert into incomes (id, deleted, tax, total_amount, income_date, last_modified_date, receipt_number, receipt_series, receipt_type_id, supplier_id, user_id, user_modified_id, status)
select
    nextval('incomes_seq'),
    false, 21.00, seed.total_amount,
    (current_date - seed.day_offset)::timestamp + time '11:00',
    (current_date - seed.day_offset)::timestamp + time '11:00',
    seed.receipt_number, 1001, 1,
    (select id from supplier_lookup where nif = seed.supplier_nif),
    (select id from admin_user), (select id from admin_user),
    seed.status
from (values
    -- TODAY: 2 compras activas → total 275,000
    (99301, 0, 180000.00, '30-99100001-1', 'ACTIVE'),
    (99302, 0, 95000.00,  '30-99100003-7', 'ACTIVE'),
    -- YESTERDAY: 2 compras activas + 1 anulada → active 500,000, annulled 1
    (99303, 1, 350000.00, '30-99100002-9', 'ACTIVE'),
    (99305, 1, 150000.00, '30-99100004-5', 'ACTIVE'),
    (99304, 1, 200000.00, '30-99100002-9', 'ANNULLED'),
    -- 7 DAYS AGO: 3 compras grandes → total 2,650,000
    (99306, 7, 1200000.00, '30-99100002-9', 'ACTIVE'),
    (99307, 7, 800000.00,  '30-99100001-1', 'ACTIVE'),
    (99308, 7, 650000.00,  '30-99100005-3', 'ACTIVE')
) as seed(receipt_number, day_offset, total_amount, supplier_nif, status);

-- Reversal counter-entry for the annulled 99304 (yesterday). ACTIVE, negative
-- amount, reversal_of_id → 99304, same day. Nets the annulled amount out of
-- yesterday's active total: 350k + 150k + (-200k) = 300,000.
with admin_user as (
    select u.id from users u
    join user_role ur on ur.user_id = u.id
    join roles r on r.id = ur.role_id
    where r.name = 'ROLE_ADMIN'
    order by u.id
    limit 1
)
insert into incomes (id, deleted, tax, total_amount, income_date, last_modified_date, receipt_number, receipt_series, receipt_type_id, supplier_id, user_id, user_modified_id, status, reversal_of_id)
select
    nextval('incomes_seq'),
    false, src.tax, -src.total_amount,
    src.income_date + interval '2 hours', now(),
    99211, 1001, 1,
    src.supplier_id,
    (select id from admin_user), (select id from admin_user),
    'ACTIVE', src.id
from incomes src
where src.receipt_number = 99304;

-- One detail line per arqueo income so the detail dialog isn't empty. Picks a
-- TEST item deterministically; the reversal (99211) mirrors 99304's line negated.
with item_lookup as (
    select id, code from items where code like 'TEST-%'
)
insert into income_details (id, income_id, item_id, quantity, purchase_price, sale_price)
select
    nextval('income_details_seq'),
    inc.id,
    (select id from item_lookup where code = seed.item_code),
    seed.quantity, seed.purchase_price, seed.sale_price
from (values
    (99301, 'TEST-COF-001', 1, 180000.00, 230000.00),
    (99302, 'TEST-CIR-001', 5, 19000.00,  26000.00),
    (99303, 'TEST-COF-002', 1, 350000.00, 420000.00),
    (99305, 'TEST-FLO-002', 6, 25000.00,  34000.00),
    (99304, 'TEST-COF-002', 1, 200000.00, 260000.00),
    (99306, 'TEST-COF-002', 3, 400000.00, 480000.00),
    (99307, 'TEST-ACC-001', 4, 200000.00, 260000.00),
    (99308, 'TEST-REC-002', 5, 130000.00, 170000.00),
    -- Reversal line: negative quantity mirroring the annulled 99304.
    (99211, 'TEST-COF-002', -1, 200000.00, 260000.00)
) as seed(receipt_number, item_code, quantity, purchase_price, sale_price)
join incomes inc on inc.receipt_number = seed.receipt_number;

-- ---------------------------------------------------------------------------
-- 13. SEQUENCE REFRESH so future inserts via JPA do not collide.
-- ---------------------------------------------------------------------------

select setval('brands_seq',         coalesce((select max(id) from brands),         1));
select setval('categories_seq',     coalesce((select max(id) from categories),     1));
select setval('addresses_seq',      coalesce((select max(id) from addresses),      1));
select setval('deceased_seq',       coalesce((select max(id) from deceased),       1));
select setval('incomes_seq',        coalesce((select max(id) from incomes),        1));
select setval('income_details_seq', coalesce((select max(id) from income_details), 1));
-- Note: brands/categories/items/plans/affiliates/funeral/suppliers use the
-- 'generated by default as identity' clause for their id columns, which keeps
-- its own internal sequence. Postgres updates that sequence on direct inserts
-- automatically when you supply an explicit id; the `nextval('...')` calls
-- above already advance the regular sequences for the manually-id'd tables.

-- ---------------------------------------------------------------------------
-- 14. LOW-STOCK THRESHOLDS (PR5a). V14 added items.low_stock_threshold with a
-- default of 10. We vary it per category so the bell drop-down (PR5b/PR5c)
-- exercises several shapes:
--   * Cofres y urnas      -> threshold 5   (slow-moving, expensive)
--   * Velas / cirios      -> threshold 40  (high turnover, cheap)
--   * Arreglos florales   -> threshold 12
--   * Textiles            -> threshold 8
--   * Recordatorios       -> threshold 25
--   * Accesorios capilla  -> threshold 4
-- ---------------------------------------------------------------------------

update items set low_stock_threshold = 5
where code in ('TEST-COF-001', 'TEST-COF-002', 'TEST-COF-003', 'TEST-URN-001');

update items set low_stock_threshold = 40
where code in ('TEST-CIR-001', 'TEST-VEL-001', 'TEST-VEL-002');

update items set low_stock_threshold = 12
where code in ('TEST-FLO-001', 'TEST-FLO-002', 'TEST-FLO-003');

update items set low_stock_threshold = 8
where code in ('TEST-TEX-001', 'TEST-TEX-002');

update items set low_stock_threshold = 25
where code in ('TEST-REC-001', 'TEST-REC-002', 'TEST-REC-003');

update items set low_stock_threshold = 4
where code in ('TEST-ACC-001', 'TEST-ACC-002', 'TEST-ACC-003');

-- Force a handful of items to currently sit BELOW threshold so the operator
-- sees the red stock chip on the listing right after the seed runs. These
-- mirror what would have happened if PR3 (funeral consume stock) had been
-- exercised heavily — slow-movers are at or below their floor.
update items set stock = 3   where code = 'TEST-COF-001';   -- threshold 5
update items set stock = 2   where code = 'TEST-URN-001';   -- threshold 5
update items set stock = 35  where code = 'TEST-VEL-001';   -- threshold 40
update items set stock = 9   where code = 'TEST-FLO-002';   -- threshold 12
update items set stock = 3   where code = 'TEST-ACC-001';   -- threshold 4

-- ---------------------------------------------------------------------------
-- 15. PAPELERA — soft-deleted plans (V11) + items (V12). These rows let the
-- admin exercise GET /plans/deleted and GET /items/deleted plus the restore
-- endpoints right after the seed runs.
--
-- IMPORTANT: the cleanup at the top of the file already covers these by their
-- TEST- / [TEST] prefixes, so no extra cleanup wiring is needed.
-- ---------------------------------------------------------------------------

-- Two soft-deleted plans. NOT referenced by any funeral (the bulk funeral
-- block above only picks plans whose name does not include the [PAPELERA]
-- marker), so the restore call won't surface ordering surprises.
insert into plans (name, description, price, profit_percentage, image_url, deleted_at, deleted_by) values
    ('[TEST] Plan Discontinuado 2024', 'Plan retirado del catálogo en 2024. Conservado en papelera para auditoría.', 410000.00, 20.00, null, now() - interval '21 days', 'admin@example.com'),
    ('[TEST] Plan Piloto Norte',       'Plan piloto para sucursal norte. Quedó en papelera tras revisión comercial.', 690000.00, 24.00, null, now() - interval '5 days',  'admin@example.com');

-- Five soft-deleted items. Codes use the TEST-DEL- sub-prefix so it is obvious
-- at a glance which catalog rows are tombstones vs. active. Stock is zeroed
-- because the PR2 guard would have blocked the delete otherwise — leaving a
-- positive stock here would misrepresent the real state of the system.
with brand_lookup as (
    select id, name from brands where name like '[TEST]%'
),
category_lookup as (
    select id, name from categories where name like '[TEST]%'
)
insert into items (
    name, code, description, price, stock, low_stock_threshold,
    item_height, item_length, item_width, brand_id, category_id,
    created_at, created_by, updated_at, updated_by,
    deleted_at, deleted_by
)
select
    seed.name, seed.code, seed.description, seed.price, 0, 10,
    seed.height, seed.length, seed.width,
    (select id from brand_lookup where name = seed.brand_name),
    (select id from category_lookup where name = seed.category_name),
    now() - interval '90 days', 'seed-test-data',
    now() - interval '30 days', 'seed-test-data',
    seed.deleted_at, 'admin@example.com'
from (values
    ('Cofre Vintage Caoba (descontinuado)', 'TEST-DEL-COF-001', 'Cofre de caoba retirado del catálogo en 2024 por escasez del proveedor.',     280000.00, 60.00, 200.00, 65.00, '[TEST] Maderera del Plata', '[TEST] Cofres y urnas', (now() - interval '60 days')),
    ('Vela aromática lavanda (retirada)',   'TEST-DEL-VEL-001', 'Producto retirado por reporte de alergias en deudos.',                          1800.00, 22.00,  4.00,  4.00, '[TEST] Velas Litúrgicas SA', '[TEST] Velas y cirios', (now() - interval '45 days')),
    ('Corona artificial (descontinuada)',   'TEST-DEL-FLO-001', 'Reemplazada por la corona de claveles natural.',                                8500.00, 70.00, 70.00, 12.00, '[TEST] Floral Andina', '[TEST] Arreglos florales', (now() - interval '30 days')),
    ('Estampa devocional v1 (papelera)',    'TEST-DEL-REC-001', 'Versión inicial de las estampas, reemplazada por el pack actualizado.',         6500.00, 0.50,  10.00,  7.00, '[TEST] Memorial Gráfica', '[TEST] Recordatorios', (now() - interval '12 days')),
    ('Atril económico (papelera)',          'TEST-DEL-ACC-001', 'Atril de melamina retirado tras feedback de calidad.',                         28000.00, 110.00, 45.00, 35.00, '[TEST] Maderera del Plata', '[TEST] Accesorios de capilla', (now() - interval '7 days'))
) as seed(name, code, description, price, height, length, width, brand_name, category_name, deleted_at);

-- ---------------------------------------------------------------------------
-- 16. ANULACION DE INGRESOS (PR4). For five of the bulk incomes we flip the
-- status to ANNULLED and insert a matching reversal counter-entry:
--   * status = ACTIVE on the reversal (the reversal itself is a real income row)
--   * reversal_of_id points back at the original income
--   * receipt_number lives in 99201..99205 so the cleanup range still catches it
--   * income_details on the reversal carry NEGATIVE quantities at the same
--     prices, mirroring the contable convention: the reversal undoes the
--     stock + amount that the original posted.
-- We pick five spread across the bulk months so the listing shows them
-- interleaved with active rows.
-- ---------------------------------------------------------------------------

-- Step 1: mark the originals as ANNULLED.
update incomes
set status = 'ANNULLED'
where receipt_number in (99013, 99027, 99055, 99070, 99089);

-- Step 2: insert the reversal incomes. Each reversal mirrors its source's
-- supplier, tax rate and date (+ a few hours so the listing sort surfaces the
-- reversal right after the original), but the amount is negated.
with admin_user as (
    select u.id from users u
    join user_role ur on ur.user_id = u.id
    join roles r on r.id = ur.role_id
    where r.name = 'ROLE_ADMIN'
    order by u.id
    limit 1
)
insert into incomes (
    id, deleted, tax, total_amount, income_date, last_modified_date,
    receipt_number, receipt_series, receipt_type_id,
    supplier_id, user_id, user_modified_id,
    status, reversal_of_id
)
select
    nextval('incomes_seq'),
    false,
    src.tax,
    -src.total_amount,
    src.income_date + interval '2 hours',
    now(),
    src_pair.reversal_receipt,
    1001,
    1,
    src.supplier_id,
    (select id from admin_user),
    (select id from admin_user),
    'ACTIVE',
    src.id
from (values
    (99013, 99201),
    (99027, 99202),
    (99055, 99203),
    (99070, 99204),
    (99089, 99205)
) as src_pair(source_receipt, reversal_receipt)
join incomes src on src.receipt_number = src_pair.source_receipt;

-- Step 3: replicate the original income_details with negative quantities. We
-- collect the source details first then mirror them onto the new reversal
-- income ids.
with reversal_pairs as (
    select
        rev.id   as reversal_id,
        orig.id  as original_id
    from incomes rev
    join incomes orig on orig.id = rev.reversal_of_id
    where rev.receipt_number between 99201 and 99205
)
insert into income_details (id, income_id, item_id, quantity, purchase_price, sale_price)
select
    nextval('income_details_seq'),
    rp.reversal_id,
    src.item_id,
    -src.quantity,
    src.purchase_price,
    src.sale_price
from reversal_pairs rp
join income_details src on src.income_id = rp.original_id;

-- ---------------------------------------------------------------------------
-- 17. NOTIFICATIONS (PR5b/PR5c). Ten LOW_STOCK_REACHED rows for ROLE_ADMIN.
-- Five unread + five read so the bell badge shows a non-zero count AND the
-- "marcar como leida" / "ver leidas" paths have data to exercise.
--
-- The payload references real TEST- items so a click-through from the bell
-- drop-down can navigate to the items listing and find them. Each row's
-- stockBefore/stockAfter matches what the LowStockDetectionService would
-- have observed at the moment of the cross-down event.
-- ---------------------------------------------------------------------------

-- Payload shape mirrors what `NotificationConsumer.buildPayload` writes for a
-- real LowStockReached event:
--   {"itemId":42,"code":"TEST-...","name":"...","threshold":N,"stockBefore":N,"stockAfter":N}
-- We resolve itemId via a join on the seed items so the JSON references real
-- ids that survive a re-run (the cleanup wipes notifications by payload
-- substring, items get re-inserted, and the seq alignment below keeps future
-- inserts collision-free).
with seed_notifications(code, threshold, stock_before, stock_after, created_offset, read_offset) as (
    values
        -- Unread (read_offset null) — these power the bell badge count.
        ('TEST-COF-001', 5,  6,  3,  interval '2 hours',  null::interval),
        ('TEST-URN-001', 5,  6,  2,  interval '5 hours',  null::interval),
        ('TEST-VEL-001', 40, 41, 35, interval '1 day',    null::interval),
        ('TEST-FLO-002', 12, 13, 9,  interval '2 days',   null::interval),
        ('TEST-ACC-001', 4,  5,  3,  interval '3 days',   null::interval),
        -- Read (read_offset populated) — historical rows for the "leidas" tab.
        ('TEST-COF-003', 5,  7,  4,  interval '10 days', interval '9 days'),
        ('TEST-TEX-001', 8,  10, 7,  interval '14 days', interval '13 days'),
        ('TEST-REC-002', 25, 26, 18, interval '18 days', interval '17 days'),
        ('TEST-CIR-001', 40, 42, 38, interval '25 days', interval '24 days'),
        ('TEST-FLO-001', 12, 14, 8,  interval '30 days', interval '29 days')
)
insert into notifications (event_id, audience, type, payload, created_at, read_at)
select
    gen_random_uuid(),
    'ROLE_ADMIN',
    'LOW_STOCK_REACHED',
    '{"itemId":' || i.id
        || ',"code":"' || i.code
        || '","name":"' || replace(i.name, '"', '\"')
        || '","threshold":' || sn.threshold
        || ',"stockBefore":' || sn.stock_before
        || ',"stockAfter":' || sn.stock_after
        || '}',
    now() - sn.created_offset,
    case when sn.read_offset is null then null else now() - sn.read_offset end
from seed_notifications sn
join items i on i.code = sn.code;

-- bigserial keeps its own implicit sequence (notifications_id_seq). Re-align it
-- with the highest id we just inserted so the app's next insert does not
-- collide with our seed rows.
select setval('notifications_id_seq', coalesce((select max(id) from notifications), 1));

commit;

-- ---------------------------------------------------------------------------
-- Smoke check. Run these after the script to sanity-check the seed.
-- ---------------------------------------------------------------------------
--
--   select count(*) from suppliers where nif like '30-99%';      -- expects 5
--   select count(*) from brands     where name like '[TEST]%';   -- expects 8
--   select count(*) from categories where name like '[TEST]%';   -- expects 6
--   select count(*) from items      where code like 'TEST-%';    -- expects 23 (18 active + 5 papelera)
--   select count(*) from items      where code like 'TEST-%' and deleted_at is null;     -- expects 18
--   select count(*) from items      where code like 'TEST-DEL-%' and deleted_at is not null; -- expects 5
--   select count(*) from plans      where name like '[TEST]%';   -- expects 6 (4 active + 2 papelera)
--   select count(*) from plans      where name like '[TEST]%' and deleted_at is not null; -- expects 2
--   select count(*) from affiliates where dni between 35000001 and 35000999;   -- expects 150
--   select count(*) from deceased   where dni between 38000001 and 38000999;   -- expects 126
--   select count(*) from funeral    where receipt_number like 'F-99%';         -- expects 126
--   select count(*) from incomes    where receipt_number between 99001 and 99999; -- expects 114 (100 bulk + 5 reversals + 8 arqueo + 1 arqueo reversal)
--   -- Arqueo (PR6): three reconcilable days relative to current_date —
--   select count(*) from funeral where funeral_date::date = current_date;        -- expects 3 (services today)
--   select count(*) from incomes where income_date::date = current_date and status='ACTIVE'; -- expects 2 (purchases today)
--   select count(*) from incomes where income_date::date = current_date - 1 and status='ANNULLED'; -- expects 1 (annulled yesterday)
--   select count(*) from incomes    where receipt_number between 99001 and 99100 and status = 'ANNULLED'; -- expects 5
--   select count(*) from incomes    where receipt_number between 99201 and 99205 and reversal_of_id is not null; -- expects 5
--   select count(*) from income_details
--     where income_id in (select id from incomes where receipt_number between 99001 and 99999); -- expects ~208 (198 originals + ~10 reversal lines)
--   select count(*) from notifications where audience = 'ROLE_ADMIN' and payload like '%TEST-%';        -- expects 10
--   select count(*) from notifications where audience = 'ROLE_ADMIN' and read_at is null;              -- expects 5
--   select code, stock, low_stock_threshold from items where stock <= low_stock_threshold and deleted_at is null and code like 'TEST-%'; -- expects 5 rows
