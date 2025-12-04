MERGE INTO mpa (id, name) KEY(id) VALUES (1, 'G');
MERGE INTO mpa (id, name) KEY(id) VALUES (2, 'PG');
MERGE INTO mpa (id, name) KEY(id) VALUES (3, 'PG-13');
MERGE INTO mpa (id, name) KEY(id) VALUES (4, 'R');
MERGE INTO mpa (id, name) KEY(id) VALUES (5, 'NC-17');

MERGE INTO genre (id, name) KEY(id) VALUES (1, 'Комедия');
MERGE INTO genre (id, name) KEY(id) VALUES (2, 'Драма');
MERGE INTO genre (id, name) KEY(id) VALUES (3, 'Мультфильм');
MERGE INTO genre (id, name) KEY(id) VALUES (4, 'Триллер');
MERGE INTO genre (id, name) KEY(id) VALUES (5, 'Документальный');
MERGE INTO genre (id, name) KEY(id) VALUES (6, 'Боевик');
