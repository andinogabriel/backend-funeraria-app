INSERT INTO brands (id, name, web_page)
VALUES (1, 'Marca primer nivel', 'www.marcaprimernivel.com');

INSERT INTO categories (id, name, description)
VALUES (1, 'Coronas', 'Categoria de todas las coronas para sepelios');

INSERT INTO items (id, name, description, code, price, stock, category_id, brand_id)
VALUES (1, 'Corona simple', 'Corona de plastico', '67ad6c26-f586-4cb2-9d5e-3fbcc3e2e8eb', 3000, 8, 1, 1),
       (2, 'Corona de flores', 'Corona de de flores de plastico', 'fa4c62f0-fc40-4f3b-b333-2a1c8ed35292', 5000, 5, 1,
        1),
       (3, 'Corona sin precio', 'Corona de plastico', '01c5fc8a-9ab2-4c85-ac15-6b66b92b266c', null, 8, 1, 1);

INSERT INTO plans (id, name, description, profit_percentage, price)
VALUES (1, 'Plan Simple', 'Plan mas economico', 10, 21000),
       (2, 'Plan nivel medio', 'Plan con mas variedad de prestaciones', 15, 35000);

INSERT INTO items_plan (quantity, item_id, plan_id)
VALUES (1, 1, 1),
       (2, 2, 2),
       (1, 1, 2);

INSERT INTO roles (id, name)
VALUES (1, 'ROLE_ADMIN'),
       (2, 'ROLE_USER');

INSERT INTO users (id, active, email, enabled, encrypted_password, first_name, last_name, start_date)
VALUES (12345, true, 'email_test@gmail.com', true, '$2a$10$/.VBx8PROOR/pzjMubIYO.aveWvkUWBrb2JpVavhDRgsOd6YzlnpO',
        'Juan',
        'Perez', '2023-09-26'),
       (123456, false, 'email_registered@gmail.com', false,
        '$2a$10$/.VBx8PROOR/pzjMubIYO.aveWvkUWBrb2JpVavhDRgsOd6YzlnpO',
        'Pedro',
        'Acosta', '2024-02-26');

INSERT INTO user_role (user_id, role_id)
VALUES (12345, 1),
       (123456, 2),
       (12345, 2);

INSERT INTO suppliers (id, email, name, nif, web_page)
VALUES (1, 'proveedor@gmail.com', 'Proveedorazo', 'NIF123ASD', null);

INSERT INTO genders (id, name)
VALUES (1, 'Femenino'),
       (2, 'Masculino'),
       (3, 'Otro');

INSERT INTO relationships (id, name)
VALUES (1, 'Padre'),
       (2, 'Madre'),
       (3, 'Hijo'),
       (4, 'Hija'),
       (5, 'Nieto'),
       (6, 'Nieta'),
       (7, 'Hermano'),
       (8, 'Hermana'),
       (9, 'Abuelo'),
       (10, 'Abuela'),
       (11, 'Tío'),
       (12, 'Tía'),
       (13, 'Sobrino'),
       (14, 'Sobrina'),
       (15, 'Primo'),
       (16, 'Prima'),
       (17, 'Yerno'),
       (18, 'Nuera'),
       (19, 'Suegro'),
       (20, 'Suegra'),
       (21, 'Cuñado'),
       (22, 'Cuñada'),
       (23, 'Padrastro'),
       (24, 'Madrastra'),
       (25, 'Madrastra'),
       (26, 'Hermanastro'),
       (27, 'Hermanastra'),
       (28, 'Amigo'),
       (29, 'Amiga'),
       (30, 'Bisnieto'),
       (31, 'Bisnieta');

INSERT INTO receipt_types (id, name)
VALUES (1, 'Recibo de caja de ingreso'),
       (2, 'Recibo de caja de egreso'),
       (3, 'Recibo de depósito en cuenta corriente');

INSERT INTO death_causes (id, name)
VALUES (1, 'Muerte súbita'),
       (2, 'Muerte clínica'),
       (3, 'Suicidio'),
       (4, 'Accidente de transito');

INSERT INTO provinces (id, name, code31662)
VALUES (1, 'Ciudad Autónoma de Buenos Aires (CABA)', 'AR-C'),
       (2, 'Buenos Aires', 'AR-B'),
       (3, 'Catamarca', 'AR-K'),
       (4, 'Córdoba', 'AR-X'),
       (5, 'Corrientes', 'AR-W'),
       (6, 'Entre Ríos', 'AR-E'),
       (7, 'Jujuy', 'AR-Y'),
       (8, 'Mendoza', 'AR-M'),
       (9, 'La Rioja', 'AR-F'),
       (10, 'Salta', 'AR-A'),
       (11, 'San Juan', 'AR-J'),
       (12, 'San Luis', 'AR-D'),
       (13, 'Santa Fe', 'AR-S'),
       (14, 'Santiago del Estero', 'AR-G'),
       (15, 'Tucumán', 'AR-T'),
       (16, 'Chaco', 'AR-H'),
       (17, 'Chubut', 'AR-U'),
       (18, 'Formosa', 'AR-P'),
       (19, 'Misiones', 'AR-N'),
       (20, 'Neuquén', 'AR-Q'),
       (21, 'La Pampa', 'AR-L'),
       (22, 'Río Negro', 'AR-R'),
       (23, 'Santa Cruz', 'AR-Z'),
       (24, 'Tierra del Fuego', 'AR-V');

INSERT INTO cities (id, province_id, name, zip_code)
VALUES (7871, 16, 'PRESIDENCIA ROQUE SAENZ PEÑA', '3700'),
       (6372, 5, 'CORRIENTES', '3400'),
       (5095, 18, 'FORMOSA', '3600'),
       (8217, 10, 'SALTA', '4400'),
       (10465, 14, 'SANTIAGO DEL ESTERO', '4200'),
       (10855, 12, 'ENTRE RIOS', '5711'),
       (11143, 12, 'EL ROSARIO', '5750'),
       (12452, 13, 'SANTA FE', '3000'),
       (21415, 4, 'CORDOBA', '5000'),
       (21802, 24, 'CABA', '9420'),
       (8019, 16, 'LA MATANZA', '3531'),
       (12572, 2, 'QUILMES', '1878'),
       (12565, 2, 'TIGRE', '1648'),
       (8022, 16, 'QUITILIPI', '3530'),
       (7770, 16, 'MACHAGAI', '3534'),
       (8027, 16, 'RESISTENCIA', '3500'),
       (7926, 16, 'NAPENAY', '3706'),
       (15273, 8, 'EL COLORADO', '5595'),
       (3096, 8, 'MENDOZA', '5500'),
       (8870, 15, 'SAN MIGUEL DE TUCUMAN', '4000'),
       (9738, 7, 'SAN SALVADOR DE JUJUY', '4600'),
       (17199, 10, 'MISIONES', '4554'),
       (19441, 7, 'EL COLORADO', '4618'),
       (7853, 16, 'CHARATA', '3730'),
       (8800, 15, 'PASO DE LA PATRIA', '4187'),
       (7876, 16, 'CHOROTIS', '3733'),
       (12484, 2, 'EL PALOMAR', '1684'),
       (12588, 2, 'FLORENCIO VARELA', '1888'),
       (12485, 2, 'HAEDO', '1706'),
       (12486, 2, 'MORON', '1708'),
       (12480, 2, 'HURLINGHAM', '1686');

INSERT INTO affiliates (id, birth_date, deceased, dni, first_name, last_name, start_date, gender_id, relationship_id,
                        user_id)
VALUES (98, '1950-10-31', null, 11236549, 'Juan', 'Acosta', '2024-09-10', 2, 9, 12345);

INSERT INTO incomes (id, deleted, receipt_number, receipt_series, tax, total_amount,
                     user_id, user_modified_id, receipt_type_id, supplier_id)
VALUES (555555, false, 20231108223102347, 1, 21.00, 12100.00, 12345,
        12345, 1, 1);

INSERT INTO income_details (id, purchase_price, quantity, sale_price, income_id, item_id)
VALUES (1, 1500.00, 2, 3000.00, 555555, 1);

INSERT INTO mobile_numbers (id, mobile_number, supplier_id, user_id)
VALUES (7532, 3644123456, null, 12345);

INSERT INTO addresses (id, apartment, block_street, flat, street_name, city_id, supplier_id, user_id)
VALUES (45, null, 500, null, 'Belgrano', 7871, null, 12345),
       (59, null, 500, null, 'Belgrano', 7871, null, null);

INSERT INTO deceased (id, affiliated, birth_date, death_date, dni, first_name, last_name, register_date, death_cause_id,
                      relationship_id, user_id, gender_id, address_id)
VALUES (38, true, '1965-07-15', '2024-11-10', 22156961, 'Marta', 'Perez', '2023-11-13 16:26:26.818638', 2, 10, 12345, 1,
        45),
       (72, false, '1960-03-28', '2024-12-10', 17621970, 'Maria', 'Perez', '2023-08-25 16:26:26.818638', 2, 10, 12345,
        1,
        59);

INSERT INTO funeral (id, funeral_date, receipt_number, receipt_series, register_date, tax, total_amount, deceased_id,
                     plan_id, receipt_type_id)
VALUES (120, '2024-11-12 03:00:00', '2024290420241A', '1', '2023-05-12 18:59:36.345607', 21.00, 130680.00, 72, 2,
        2),
       (45, '2024-11-11 03:00:00', '123465sad465', '465asd4as', '2023-11-12 18:59:36.345607', 21.00, 130680.00, 38, 2,
        2);

INSERT INTO confirmation_tokens (id, expiry_date, token, user_id)
VALUES (654, '2024-12-12 03:00:00', '972d8f36-7051-4867-a314-0e175a3b1065', 12345),
       (672, '2024-03-12 03:00:00', '5cb86e45-aabd-4479-a328-a4e22b753bff', 123456),
       (696, CURRENT_TIMESTAMP + INTERVAL '3' HOUR, '66155026-24ed-4696-9396-76120b9457ef', 123456);

INSERT INTO user_devices (id, device_id, device_type, is_refresh_active, user_id)
VALUES (4831, 'd520c7a8-421b-4563-b955-f5abc56b97ec', 'windows-10-desktop-Chrome-v117.0.0.0', true, 12345);

INSERT INTO refresh_tokens (id, expiry_date, refresh_count, token, user_device_id)
VALUES (7532, CURRENT_TIMESTAMP + INTERVAL '3' HOUR, 0,
        'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhbm9ueW1vdXNVc2VyIiwiYXV0aG9yaXRpZXMiOiJST0xFX0FOT05ZTU9VUyIsImlhdCI6MTcxNTEyNzgyMywiZXhwIjoxNzE1MjE0MjIzfQ.wVIX5T-Wz5tUpnkf30ufe1pzTQExuGCZmU3EucO50TBIMWYGfA3ZNrRX3la2TiVIaPTp6kdlhjbJzfShVz--6A',
        4831);
