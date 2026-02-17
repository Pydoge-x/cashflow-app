# Cashflow App Backend

This is the Spring Boot backend service for the Personal/Family Financial Report Application.

## Prerequisites

- **Java 17+**
- **Maven** (Apache Maven 3.8+)
- **MySQL Database**

## Configuration

1.  Ensure MySQL is running on `localhost:3306`.
2.  Create a database named `cashflow_db`.
    ```sql
    CREATE DATABASE cashflow_db;
    ```
3.  Update `src/main/resources/application.properties` if your database credentials differ from:
    - Username: `root`
    - Password: `root`

## Build and Run

Since there is no Maven Wrapper included, you need to have Maven installed on your system.

1.  Navigate to the `backend` directory:

    ```sh
    cd backend
    ```

2.  Build the project:

    ```sh
    mvn clean install
    ```

3.  Run the application:
    ```sh
    mvn spring-boot:run
    ```

The server will start on `http://localhost:8080`.

## API Documentation

- **Auth**: `/api/auth/register`, `/api/auth/login`
- **User**: `/api/user/profile`
- **Reports**: `/api/reports`
- **Balance Sheet**: `/api/reports/{id}/balance-sheet`
- **Income/Expense**: `/api/reports/{id}/income-expense`
- **Cash Flow**: `/api/reports/{id}/cashflow`

## Notes

- The application uses `update` strategy for Hibernate DDL, so tables will be created automatically on first run.
- JWT Secret is hardcoded for development convenience in `application.properties`.
