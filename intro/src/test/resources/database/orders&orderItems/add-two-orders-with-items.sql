INSERT INTO orders (id, user_id, status, total, order_date, shipping_address)
VALUES (1, 1, 'COMPLETED', 561.8, '2020-08-07 19:34:20', 'Kyiv');
INSERT INTO orders (id, user_id, status, total, order_date, shipping_address)
VALUES (2, 1, 'COMPLETED', 1345.5, '2021-06-07 19:34:20', 'Vinnytsia');

INSERT INTO order_items (id, order_id, book_id, quantity, price) VALUES (1, 1, 1, 1, 192.8);
INSERT INTO order_items (id, order_id, book_id, quantity, price) VALUES (2, 1, 2, 2, 369.0);
INSERT INTO order_items (id, order_id, book_id, quantity, price) VALUES (3, 2, 2, 3, 553.5);
INSERT INTO order_items (id, order_id, book_id, quantity, price) VALUES (4, 2, 3, 4, 792.0);
