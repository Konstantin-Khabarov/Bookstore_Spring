-- GIN-индекс для полнотекстового поиска по книгам (title + description)
CREATE INDEX IF NOT EXISTS idx_book_fts
    ON book USING GIN (to_tsvector('simple', coalesce(title, '') || ' ' || coalesce(description, '')));

-- Индекс для выборки по полю category_id (используется в п.7)
CREATE INDEX IF NOT EXISTS idx_book_category_id ON book (category_id);

-- Индекс для сортировки по названию (используется в п.7)
CREATE INDEX IF NOT EXISTS idx_book_title ON book (title);

-- Инициализация version для строк, созданных до добавления @Version (п.11)
UPDATE book SET version = 0 WHERE version IS NULL;
