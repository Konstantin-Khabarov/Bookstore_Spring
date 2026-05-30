-- GIN-индекс для полнотекстового поиска по книгам (title + description)
CREATE INDEX IF NOT EXISTS idx_book_fts
    ON book USING GIN (to_tsvector('simple', coalesce(title, '') || ' ' || coalesce(description, '')));
