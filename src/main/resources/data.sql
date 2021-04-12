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





