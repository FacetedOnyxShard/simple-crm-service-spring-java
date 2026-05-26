# CRM-система (Back-End)

Упрощённая CRM-система для управления информацией о продавцах и их транзакциях с функциями аналитики.  
Реализована на **Java 25** с использованием **Spring Boot 4.0.6**, **PostgreSQL** и **Gradle**.  
Для миграций баз данных используется **Liquibase**.

## Функциональность

- Управление продавцами (CRUD): создание, просмотр, обновление, удаление.
- Управление транзакциями: создание, просмотр списка, просмотр одной.
- Получение всех транзакций конкретного продавца.
- Аналитика:
  - **Самый продуктивный продавец** за день, месяц, квартал или год.
  - **Список продавцов**, чья сумма транзакций за период меньше заданного порога.
  - **Лучший период продавца** – интервал (день, неделя или месяц) с максимальным количеством транзакций.
- Централизованная обработка ошибок с осмысленными HTTP-кодами и сообщениями.
- Валидация входных данных.
- Покрытие кода тестами (юнит-тесты + интеграционные тесты API).

## Требования

- **JDK 25**
- **Gradle 9+** (в проекте используется wrapper)
- **Docker** и **Docker Compose** (для запуска PostgreSQL)
- Порт `8001` (можно изменить через переменную окружения `APPLICATION_PORT`)

## Сборка и запуск

### 1. Клонирование репозитория

```bash
git clone <repository-url>
cd crm
```

### 2. Настройка окружения

Создайте файл `.env` в корне проекта со следующими переменными:

```env
POSTGRES_ROOT_USER=postgres
POSTGRES_APP_USER=crm_user
POSTGRES_APP_PASSWORD=crm_pass
POSTGRES_APP_DB=crm_db
APPLICATION_PORT=8001
```

### 3. Запуск приложения
Запуск:

```bash
docker-compose up -d
```

Приложение стартует на порту `8001` (если не переопределено).  
Swagger UI будет доступен по адресу: [http://localhost:8001/swagger-ui.html](http://localhost:8001/swagger-ui.html).

## Структура проекта

```
src/
├── main/
│   ├── java/ru/shift/crm/
│   │   ├── CrmApplication.java
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── exception/
│   │   ├── repository/
│   │   └── service/
│   └── resources/
│       ├── application.yaml
│       └── db/changelog/
└── test/
    ├── java/ru/shift/crm/
    └── resources/
```

## База данных

Используется **PostgreSQL** (продакшен) и **H2 in-memory** для тестов.  
Миграции управляются Liquibase, changesets находятся в `src/main/resources/db/changelog/`.

## Тестирование

Для запуска всех тестов:

```bash
./gradlew test
```

Тесты включают:

- **Юнит-тесты** сервисов (SellerService, TransactionService, AnalyticsService) с Mockito.
- **Интеграционные тесты** REST API с использованием MockMvc и H2.

Покрытие превышает 50% кодовой базы.

## API Endpoints

Базовый URL: `http://localhost:8001`

### Продавцы (Sellers)

| Метод   | URL                       | Описание                                                                             |
|---------|---------------------------|--------------------------------------------------------------------------------------|
| `GET`   | `/api/sellers`            | Получить список всех продавцов                                                       |
| `GET`   | `/api/sellers/{id}`       | Получить продавца по ID                                                              |
| `POST`  | `/api/sellers`            | Создать нового продавца                                                              |
| `PUT`   | `/api/sellers/{id}`       | Обновить информацию о продавце                                                       |
| `DELETE`| `/api/sellers/{id}`       | Удалить продавца (soft delete)                                                       |
| `GET`   | `/api/sellers/{id}/transactions` | Получить все транзакции продавца                                                     |
| `GET`   | `/api/sellers/{id}/best-period` | Лучший период продавца (параметр `periodSize`: DAY, WEEK, MONTH; по умолчанию MONTH) |

### Транзакции (Transactions)

| Метод  | URL                      | Описание                         |
|--------|--------------------------|----------------------------------|
| `GET`  | `/api/transactions`      | Список всех транзакций           |
| `GET`  | `/api/transactions/{id}` | Получить транзакцию по ID        |
| `POST` | `/api/transactions`      | Создать новую транзакцию         |

### Аналитика (Analytics)

| Метод | URL                         | Параметры                                                                                   | Описание                                       |
|-------|-----------------------------|---------------------------------------------------------------------------------------------|------------------------------------------------|
| `GET` | `/api/analytics/top-seller`  | `period` (day, month, quarter, year), `date` (ISO-дата, например `2025-01-15`)              | Самый продуктивный продавец за указанный период |
| `GET` | `/api/analytics/sellers-below` | `startDate` (ISO), `endDate` (ISO), `threshold` (положительное число)                      | Продавцы с суммой транзакций меньше порога      |

## Примеры запросов

### Создание продавца

```bash
curl -X POST http://localhost:8001/api/sellers \
  -H "Content-Type: application/json" \
  -d '{"name": "Иван Петров", "contactInfo": "ivan@example.com"}'
```

### Создание транзакции

```bash
curl -X POST http://localhost:8001/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "sellerId": 1,
    "amount": 250.00,
    "paymentType": "CARD",
    "transactionDate": "2025-01-15T14:30:00"
  }'
```

### Получить самого продуктивного продавца за месяц

```bash
curl "http://localhost:8001/api/analytics/top-seller?period=month&date=2025-01-01"
```

### Получить список продавцов с суммой транзакций меньше 1000 за январь 2025

```bash
curl "http://localhost:8001/api/analytics/sellers-below?startDate=2025-01-01&endDate=2025-01-31&threshold=1000"
```

## Обработка ошибок

Приложение возвращает единообразную структуру ошибки:

```json
{
  "status": 404,
  "error": "Ресурс не найден",
  "message": "Продавец с id 99 не найден"
}
```

Возможные HTTP-статусы:  
- `400 Bad Request` - ошибки валидации или некорректные параметры.  
- `404 Not Found` - ресурс не найден.  
- `500 Internal Server Error` - внутренние ошибки.

## Зависимости (ключевые)

- **Spring Boot** (web, data-jpa, validation, liquibase)
- **PostgreSQL** драйвер
- **H2** (для тестирования)
- **Lombok**
- **Jackson** (с модулем JSR310)
- **SpringDoc OpenAPI** (Swagger UI)
- **JUnit 5**, **Mockito**, **AssertJ** (тестирование)

Полный список см. в `build.gradle.kts`.

## Документация API

Swagger UI доступен при запущенном приложении:  
[http://localhost:8001/swagger-ui.html](http://localhost:8001/swagger-ui.html)
