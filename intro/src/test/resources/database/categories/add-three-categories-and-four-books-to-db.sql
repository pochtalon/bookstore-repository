INSERT INTO books (id, title, author, isbn, price, description, cover_image)
VALUES (1, 'Call of Cthulhu', 'Howard Lovecraft', '978-966-2355-82-6', 192.8, 'Book about Cthulhu', 'Cthulhu_cover');
INSERT INTO books (id, title, author, isbn, price, description, cover_image)
VALUES (2, 'The Black Cat', 'Edgar Poe', '978-0-8154-1038-6', 184.3, 'Scary black cat', 'Cat_cover');
INSERT INTO books (id, title, author, isbn, price, description, cover_image)
VALUES (3, 'The Name of the Rose', 'Umberto Eco', '978-0-15-144647-6', 198.0, 'Murderer is Jorge', 'Monastery_cover');
INSERT INTO books (id, title, author, isbn, price, description, cover_image)
VALUES (4, 'Guards! Guards!', 'Terry Pratchett', '0-575-04606-6', 198.9, 'Night life in Ankh-Morpork' , 'Samuel_Vimes_cover');

INSERT INTO categories (id, name, description) VALUES (1, 'Horror', 'Something scary');
INSERT INTO categories (id, name, description) VALUES (2, 'Detective', 'Something enigmatic');
INSERT INTO categories (id, name, description) VALUES (3, 'Fantasy', 'Something faibled');

INSERT INTO books_categories (book_id, category_id) VALUES (1, 1);
INSERT INTO books_categories (book_id, category_id) VALUES (1, 3);
INSERT INTO books_categories (book_id, category_id) VALUES (2, 1);
INSERT INTO books_categories (book_id, category_id) VALUES (2, 2);
INSERT INTO books_categories (book_id, category_id) VALUES (3, 2);
INSERT INTO books_categories (book_id, category_id) VALUES (4, 2);
INSERT INTO books_categories (book_id, category_id) VALUES (4, 3);
