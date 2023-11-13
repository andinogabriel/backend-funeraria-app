/*
Migracion por defecto, se llama automaticamente cuando se inicia la applicacion
Hay que especificar este archivo en el application.properties con spring.datasource.initialization-mode=always
el ON CONFLICT (id) DO NOTHING; sirve para ver si ya existen estos datos en la DB no va a ejecutar el INSERT
*/


INSERT INTO genders (id, name) VALUES
	(1, 'Femenino'),
	(2, 'Masculino'),
	(3, 'Otro')
	ON CONFLICT (id) DO NOTHING;

INSERT INTO roles (id, name) VALUES
   (1, 'ROLE_ADMIN'),
   (2, 'ROLE_USER')
    ON CONFLICT (id) DO NOTHING;

INSERT INTO relationships (id, name) VALUES
	(1, 'Padre'),
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
	(31, 'Bisnieta')
	ON CONFLICT (id) DO NOTHING;

INSERT INTO receipt_types (id, name) VALUES
	(1, 'Recibo de caja de ingreso'),
	(2, 'Recibo de caja de egreso'),
	(3, 'Recibo de depósito en cuenta corriente')
	ON CONFLICT (id) DO NOTHING;

INSERT INTO death_causes (id, name) VALUES
     (1, 'Muerte súbita'),
     (2, 'Muerte clínica'),
     (3, 'Suicidio'),
     (4, 'Accidente de transito')
ON CONFLICT (id) DO NOTHING;

INSERT INTO provinces (id, name, code31662) VALUES
	(1,'Ciudad Autónoma de Buenos Aires (CABA)','AR-C'),
	(2,'Buenos Aires','AR-B'),
	(3,'Catamarca','AR-K'),
	(4,'Córdoba','AR-X'),
	(5,'Corrientes','AR-W'),
	(6,'Entre Ríos','AR-E'),
	(7,'Jujuy','AR-Y'),
	(8,'Mendoza','AR-M'),
	(9,'La Rioja','AR-F'),
	(10,'Salta','AR-A'),
	(11,'San Juan','AR-J'),
	(12,'San Luis','AR-D'),
	(13,'Santa Fe','AR-S'),
	(14,'Santiago del Estero','AR-G'),
	(15,'Tucumán','AR-T'),
	(16,'Chaco','AR-H'),
	(17,'Chubut','AR-U'),
	(18,'Formosa','AR-P'),
	(19,'Misiones','AR-N'),
	(20,'Neuquén','AR-Q'),
	(21,'La Pampa','AR-L'),
	(22,'Río Negro','AR-R'),
	(23,'Santa Cruz','AR-Z'),
	(24,'Tierra del Fuego','AR-V')
	ON CONFLICT (id) DO NOTHING;
    
INSERT INTO cities (id, province_id, name, zip_code) VALUES
	(7871,16,'PRESIDENCIA ROQUE SAENZ PEÑA','3700'),
    (6372,5,'CORRIENTES','3400'),
    (5095,18,'FORMOSA','3600'),
    (8217,10,'SALTA','4400'),
    (10465,14,'SANTIAGO DEL ESTERO','4200'),
    (10855,12,'ENTRE RIOS','5711'),
    (11143,12,'EL ROSARIO','5750'),
    (12452,13,'SANTA FE','3000'),
    (21415,4,'CORDOBA','5000'),
    (21802,24,'CABA','9420'),
    (8019,16,'LA MATANZA','3531'),
    (12572,2,'QUILMES','1878'),
    (12565,2,'TIGRE','1648'),
    (8022,16,'QUITILIPI','3530'),
    (7770,16,'MACHAGAI','3534'),
    (8027,16,'RESISTENCIA','3500'),
    (7926,16,'NAPENAY','3706'),
    (15273,8,'EL COLORADO','5595'),
    (3096,8,'MENDOZA','5500'),
    (8870,15,'SAN MIGUEL DE TUCUMAN','4000'),
    (9738,7,'SAN SALVADOR DE JUJUY','4600'),
    (17199,10,'MISIONES','4554'),
    (19441,7,'EL COLORADO','4618'),
    (7853,16,'CHARATA','3730'),
    (8800,15,'PASO DE LA PATRIA','4187'),
    (7876,16,'CHOROTIS','3733'),
    (12484,2,'EL PALOMAR','1684'),
    (12588,2,'FLORENCIO VARELA','1888'),
    (12485,2,'HAEDO','1706'),
	(12486,2,'MORON','1708'),
	(12480,2,'HURLINGHAM','1686')
    ON CONFLICT (id) DO NOTHING;
