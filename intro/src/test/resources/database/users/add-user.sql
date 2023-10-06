INSERT INTO users (id, email, password, first_name, last_name, shipping_address)
VALUES (1, 'handsome_bob@mail.com', '$2a$10$pVfjvP8petI6Q.locgLd4ua.JSf.evDRNT0FaeaWtW0eesn9Qk37W', 'Tom', 'Hardy', 'London');
INSERT INTO users_roles (user_id, role_id) VALUES (1, 1);
