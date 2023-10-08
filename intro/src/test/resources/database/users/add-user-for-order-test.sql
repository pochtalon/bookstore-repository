INSERT INTO users (id, email, password, first_name, last_name, shipping_address)
VALUES (5, 'some_user@mail.com', '$2a$10$pVfjvP8petI6Q.locgLd4ua.JSf.evDRNT0FaeaWtW0eesn9Qk37W', 'Some', 'User', 'Nowhere');
INSERT INTO users_roles (user_id, role_id) VALUES (5, 1);
