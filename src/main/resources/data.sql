-- Начальные данные (вставляются только если таблицы пустые)

INSERT INTO category (name, description, created_at, updated_at)
SELECT 'Художественная литература', 'Романы, повести, рассказы', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Художественная литература');

INSERT INTO category (name, description, created_at, updated_at)
SELECT 'Научная фантастика', 'Фантастика и футурология', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Научная фантастика');

INSERT INTO category (name, description, created_at, updated_at)
SELECT 'Детектив', 'Детективы и триллеры', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Детектив');

INSERT INTO stack (x, y, shelf_number, description, created_at, updated_at)
SELECT 10.0, 20.0, 'A-12', 'Первый ряд, центральный стеллаж', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM stack WHERE shelf_number = 'A-12');

INSERT INTO stack (x, y, shelf_number, description, created_at, updated_at)
SELECT 25.0, 30.0, 'B-05', 'У окна', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM stack WHERE shelf_number = 'B-05');

INSERT INTO stack (x, y, shelf_number, description, created_at, updated_at)
SELECT 5.0, 15.0, 'C-08', 'Входная зона', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM stack WHERE shelf_number = 'C-08');

INSERT INTO book (title, author, description, price, category_id, stack_id, created_at, updated_at)
SELECT 'Война и мир', 'Лев Толстой',
       'Эпический роман о русском обществе в эпоху Наполеоновских войн.',
       750.0,
       (SELECT id FROM category WHERE name = 'Художественная литература'),
       (SELECT id FROM stack WHERE shelf_number = 'A-12'),
       NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM book WHERE title = 'Война и мир');

INSERT INTO book (title, author, description, price, category_id, stack_id, created_at, updated_at)
SELECT 'Дюна', 'Фрэнк Герберт',
       'Научно-фантастический роман о пустынной планете Арракис.',
       650.0,
       (SELECT id FROM category WHERE name = 'Научная фантастика'),
       (SELECT id FROM stack WHERE shelf_number = 'B-05'),
       NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM book WHERE title = 'Дюна');

INSERT INTO book (title, author, description, price, category_id, stack_id, created_at, updated_at)
SELECT 'Убийство в Восточном экспрессе', 'Агата Кристи',
       'Классический детектив, действие происходит в поезде.',
       450.0,
       (SELECT id FROM category WHERE name = 'Детектив'),
       (SELECT id FROM stack WHERE shelf_number = 'C-08'),
       NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM book WHERE title = 'Убийство в Восточном экспрессе');
