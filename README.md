# E-Commerce App

A full-stack e-commerce application built with Spring Boot and React. Features user authentication, product browsing, shopping cart, and order checkout.

## Tech Stack

**Backend:** Java 21, Spring Boot 4, PostgreSQL, raw JDBC, Lombok
**Frontend:** React 19, Vite 8, JavaScript (JSX), plain CSS
**Auth:** Custom JWT (HS256) with BCrypt password hashing

## Features

- User registration and login with JWT authentication
- Product catalog with image support
- Shopping cart with quantity management
- Order checkout with stock tracking
- Admin product management (CRUD + image upload)
- Responsive design

## Project Structure

```
backend/          Spring Boot REST API
frontend/         React single-page application
```

## Getting Started

### Prerequisites

- Java 21
- Node.js
- PostgreSQL

### Database Setup

Create a PostgreSQL database called `ecommerce` and run the schema:

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DOUBLE PRECISION NOT NULL,
    stock INTEGER NOT NULL,
    image_data BYTEA,
    image_type VARCHAR(100)
);

CREATE TABLE cart_items (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    quantity INTEGER NOT NULL
);

CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    total_amount DOUBLE PRECISION NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    quantity INTEGER NOT NULL,
    price DOUBLE PRECISION NOT NULL
);
```

### Backend

```bash
cd backend
```

Copy `application.properties.example` to `application.properties` and fill in your database credentials and a JWT secret:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ecommerce
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver
app.jwt.secret=your_secret_key
app.jwt.expiration-ms=86400000
```

Then run:

```bash
./mvnw spring-boot:run
```

The API starts on `http://localhost:8080`.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

The app starts on `http://localhost:5173`.

To point the frontend at a different backend URL, set the `VITE_API_URL` environment variable before building.

## API Endpoints

| Method | Endpoint | Access |
|--------|----------|--------|
| POST | `/api/auth/register` | Public |
| POST | `/api/auth/login` | Public |
| GET | `/api/products` | Public |
| GET | `/api/products/{id}` | Public |
| POST | `/api/products` | Admin |
| PUT | `/api/products/{id}` | Admin |
| POST | `/api/products/{id}/image` | Admin |
| GET | `/api/products/{id}/image` | Public |
| POST | `/api/cart/add` | User |
| GET | `/api/cart/{userId}` | User |
| PUT | `/api/cart/update` | User |
| DELETE | `/api/cart/remove/{cartItemId}` | User |
| POST | `/api/orders/checkout/{userId}` | User |
| GET | `/api/orders/user/{userId}` | User |

## Deployment

- **Frontend:** Netlify — build with `VITE_API_URL=<backend-url> npm run build` and deploy the `dist/` folder
- **Backend:** Render Web Service using the Dockerfile in `backend/`
- **Database:** Render PostgreSQL (or any hosted PostgreSQL)
