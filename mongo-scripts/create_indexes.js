// Индекс для категорий (уникальное имя)
db.categories.createIndex({ name: 1 }, { unique: true });

// 2d индекс для stacks (для геопоиска)
db.stacks.createIndex({ location: "2d" });

// Полнотекстовый индекс для books
db.books.createIndex({ title: "text", description: "text" });

// Обычные индексы для связей
db.books.createIndex({ categoryId: 1 });
db.books.createIndex({ stackId: 1 });

print("Indexes created.");