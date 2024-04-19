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