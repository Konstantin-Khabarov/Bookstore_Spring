// Очистка старых данных (опционально)
db.categories.deleteMany({});
db.stacks.deleteMany({});
db.books.deleteMany({});

// Вставка категорий
var categories = [
    { _id: ObjectId("100000000000000000000001"), name: "Художественная литература", description: "Романы, повести, рассказы" },
    { _id: ObjectId("100000000000000000000002"), name: "Научная фантастика", description: "Фантастика и футурология" },
    { _id: ObjectId("100000000000000000000003"), name: "Детектив", description: "Детективы и триллеры" }
];
db.categories.insertMany(categories);

// Вставка стеллажей (координаты x, y)
var stacks = [
    { _id: ObjectId("200000000000000000000001"), location: [10, 20], shelfNumber: "A-12", description: "Первый ряд, центральный стеллаж" },
    { _id: ObjectId("200000000000000000000002"), location: [25, 30], shelfNumber: "B-05", description: "У окна" },
    { _id: ObjectId("200000000000000000000003"), location: [5, 15], shelfNumber: "C-08", description: "Входная зона" }
];
db.stacks.insertMany(stacks);

// Вставка книг
var books = [
    {
        _id: ObjectId("300000000000000000000001"),
        title: "Война и мир",
        author: "Лев Толстой",
        description: "Эпический роман о русском обществе в эпоху Наполеоновских войн.",
        price: 750,
        categoryId: ObjectId("100000000000000000000001"),
        stackId: ObjectId("200000000000000000000001"),
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        _id: ObjectId("300000000000000000000002"),
        title: "Дюна",
        author: "Фрэнк Герберт",
        description: "Научно-фантастический роман о пустынной планете Арракис.",
        price: 650,
        categoryId: ObjectId("100000000000000000000002"),
        stackId: ObjectId("200000000000000000000002"),
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        _id: ObjectId("300000000000000000000003"),
        title: "Убийство в Восточном экспрессе",
        author: "Агата Кристи",
        description: "Классический детектив, действие происходит в поезде.",
        price: 450,
        categoryId: ObjectId("100000000000000000000003"),
        stackId: ObjectId("200000000000000000000003"),
        createdAt: new Date(),
        updatedAt: new Date()
    }
];
db.books.insertMany(books);

print("Initial data inserted.");