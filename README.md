# Витрина интернет-магазина

Веб-приложение, в котором можно просматривать товары, складывать их в корзину и оформлять заказы.
Покупка эмулируется: оплаты и доставки нет. Пользователей тоже нет, поэтому корзина одна на всё приложение.

## Технологии

- Java 21, Maven
- Spring Boot 4, Spring Web MVC, встроенный Tomcat
- Spring Data JPA, Hibernate ORM, H2
- Thymeleaf, Bootstrap
- JUnit 5, Spring TestContext Framework, Spring Boot Test, Mockito
- Docker

## Возможности

| Страница | Адрес | Что на ней есть |
| --- | --- | --- |
| Витрина | `/`, `/items` | Плитка товаров, поиск по названию и описанию, сортировка, пагинация, изменение количества в корзине |
| Товар | `/items/{id}` | Карточка товара и изменение количества в корзине |
| Корзина | `/cart/items` | Товары, их количество, общая сумма, кнопка покупки |
| Заказы | `/orders` | Все оформленные заказы |
| Заказ | `/orders/{id}` | Состав и сумма заказа |
| Загрузка товаров | `/items/import` | Импорт товаров из CSV-файла |

Изображения отдаёт эндпоинт `GET /images/{name}`. Покупку выполняет `POST /buy`.

## Запуск

### В среде разработки

1. Откройте папку проекта в IntelliJ IDEA как Maven-проект.
2. Выберите JDK 21.
3. Запустите класс `ru.yandex.practicum.mymarket.MyMarketAppApplication`.
4. Откройте <http://localhost:8080>.

### Локально

Нужна JDK 21. Maven устанавливать не обязательно, его скачает `mvnw`.

```bash
./mvnw clean package
java -jar target/my-market-app-0.0.1-SNAPSHOT.jar
```

В Windows вместо `./mvnw` используйте `mvnw.cmd`.

### В Docker

```bash
docker build -t my-market-app .
docker run --rm -p 8080:8080 -v my-market-data:/app/data my-market-app
```

Приложение будет доступно на <http://localhost:8080>. Данные хранятся в томе `my-market-data`.

## Данные

Приложение использует файловую базу H2. При локальном запуске она лежит в папке `data/` рядом с местом запуска.
Схема описана в `src/main/resources/db/schema.sql`, начальные товары в `src/main/resources/db/seed.sql`.
Начальные товары добавляются, только если таблица товаров пуста.

| Таблица | Назначение |
| --- | --- |
| `items` | Товары витрины |
| `cart_items` | Позиции корзины, не больше одной на товар |
| `orders` | Оформленные заказы |
| `order_items` | Позиции заказа с названием и ценой на момент покупки |

## Загрузка товаров из CSV

Файл загружается на странице `/items/import`. Формат: UTF-8, разделитель `;`, первая строка с заголовком.

```csv
title;description;imgPath;price
Гантели;Набор разборных гантелей по 10 кг;;4900
```

Если `imgPath` пуст, показывается картинка по умолчанию. При ошибке в любой строке не загружается весь файл.
Пример лежит в `samples/items.csv`.

## Тесты

```bash
./mvnw test
```

| Вид | Инструмент | Базовый класс |
| --- | --- | --- |
| Юнит-тесты сервисов | JUnit 5, Mockito | нет |
| Веб-слой и шаблоны | `@WebMvcTest`, MockMvc | `AbstractWebMvcTest` |
| Слой данных | `@DataJpaTest` | `AbstractDataJpaTest` |
| Сквозные сценарии | `@SpringBootTest`, MockMvc | `AbstractIntegrationTest` |

Конфигурация каждого вида тестов задана один раз в базовом классе, а `@DirtiesContext` не используется.
Поэтому на весь прогон поднимаются три контекста Spring, и они берутся из кеша.

## Структура

Код разделён по модулям, а внутри модулей по слоям (`controller`, `service`, `repository`, `model`, `dto`, `exception`).

```text
ru.yandex.practicum.mymarket
├── item      товары, импорт, изображения
├── cart      корзина
├── order     заказы
└── purchase  покупка
```
