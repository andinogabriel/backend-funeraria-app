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
--   * Affiliates have DNIs in the 35000001 .. 35000020 range.
--   * Suppliers have NIFs starting with "30-99" (the 99 prefix is rare in
--     real Argentine CUIT issuance).
--   * Incomes have receipt numbers in the 99001 .. 99099 range.
--   * Funerals have receipt numbers F-99001 .. F-99099 with series "T".
--   * Items have codes prefixed "TEST-".
--
-- Run it
-- ------
--   docker exec -i backend-funeraria-postgres psql -U postgres -d funerariadb \
--     < docs/local/seed-test-data.sql
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
--   *  5 suppliers
--   *  8 brands
--   *  6 categories
--   * 18 items (each linked to a brand + category)
--   *  4 plans + items_plan rows for each
--   * 18 affiliates (mix of alive / deceased)
--   *  4 deceased rows with addresses (linked to funerals)
--   *  4 funerals (different receipt types + plans)
--   * 12 incomes (mix of suppliers + dates spanning ~3 months for date-range tests)
--   * 24 income_details (2 per income avg)
--
-- After running, the activity-feed + dashboard KPIs will NOT immediately reflect
-- this data: those panels project events from the outbox, and direct SQL inserts
-- do not fire the use-case path. To populate the activity feed, perform any
-- update / delete through the UI (eg. edit an affiliate) — the use case will
-- emit through the outbox and the relay will project it within ~5 s.
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

-- income_details → incomes
delete from income_details
where income_id in (
    select id from incomes where receipt_number between 99001 and 99099
);
delete from incomes where receipt_number between 99001 and 99099;

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
    where dni between 38000001 and 38000099;

    select coalesce(array_agg(address_id), array[]::bigint[]) into test_address_ids
    from deceased
    where dni between 38000001 and 38000099 and address_id is not null;

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
delete from affiliates where dni between 35000001 and 35000020;

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

commit;

-- ---------------------------------------------------------------------------
-- Smoke check. Run these after the script to sanity-check the seed.
-- ---------------------------------------------------------------------------
--
--   select count(*) from suppliers where nif like '30-99%';      -- expects 5
--   select count(*) from brands     where name like '[TEST]%';   -- expects 8
--   select count(*) from categories where name like '[TEST]%';   -- expects 6
--   select count(*) from items      where code like 'TEST-%';    -- expects 18
--   select count(*) from plans      where name like '[TEST]%';   -- expects 4
--   select count(*) from affiliates where dni between 35000001 and 35000020;  -- expects 18
--   select count(*) from deceased   where dni between 38000001 and 38000099;  -- expects 4
--   select count(*) from funeral    where receipt_number like 'F-9900%';      -- expects 4
--   select count(*) from incomes    where receipt_number between 99001 and 99099; -- expects 12
--   select count(*) from income_details
--     where income_id in (select id from incomes where receipt_number between 99001 and 99099); -- expects 22
