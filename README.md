# SQL Database Manager with REST API and Flyway Integration

This project extends the functionality of the original [SQL Database Operations Manager](https://github.com/ruslanaprus/goit-academy-dev-hw07) by introducing several significant new features including database migrations using Flyway, implementation of CRUD operations for Workers, Clients, and Projects, and a flexible HTTP server to handle client requests for database communication. It is also enhanced with the use of the **Jackson** library for JSON serialization/deserialization.

## Table of Contents
1. [Key Features](#key-features)
   -  [Flyway Database Migrations](#1-flyway-database-migrations)
   - [Implementation of CRUD Operations](#2-implementation-of-crud-operations)
   - [Implementation of HTTP Server for Client-Server Communication](#3-implementation-of-http-server-for-client-server-communication)
   - [JSON Serialization/Deserialization with Jackson](#4-json-serializationdeserialization-with-jackson)
   - [Modular Architecture](#5-modular-architecture)
2. [File Structure and Key Classes](#file-structure-and-key-classes)
3. [Usage Instructions](#usage-instructions)

## Key Features

### 1. **Flyway Database Migrations**
- Two migration files (`V1__init_db.sql`, `V2__populate_db.sql`) have been added for database schema initialisation and data population.
- Migrations are managed using the **Flyway Gradle plugin**, allowing for easy version control and repeatable database changes.

### 2. **Implementation of CRUD Operations**
- **AbstractGenericService<T>**: An abstract class that provides a flexible structure for implementing CRUD operations across different entities (Workers, Clients, Projects). It defines the common behaviour for:
    - Creating (`create`)
    - Retrieving by ID (`getById`)
    - Listing all entries (`listAll`)
    - Updating entity names (`setName`)
    - Deleting by ID (`deleteById`)

- **BaseService<T>**: An interface that defines core service methods and enforces consistency across different service implementations. It is implemented by `AbstractGenericService`.

- **WorkerService, ClientService, ProjectService**: These classes extend `AbstractGenericService` and implement entity-specific CRUD operations for `Worker`, `Client`, and `Project` tables, respectively.

- The classes make use of **PreparedStatements** to execute SQL queries securely and efficiently, improving protection against SQL injection attacks.

### 3. **Implementation of HTTP Server for Client-Server Communication**
An HTTP server has been implemented to allow clients to communicate with the Java server and database via standard HTTP methods (`GET`, `POST`, `PUT`, `DELETE`). This server facilitates the interaction between external clients and the database, routing incoming requests to the appropriate service to perform the relevant CRUD operations.

#### **`HttpServerFactory`: A Factory for Server Initialization**

- **HttpServerFactory**: Responsible for creating and initializing the HTTP server. This class dynamically registers contexts (such as `/workers`, `/clients`, `/projects`) based on the available services, ensuring that each service is accessible via its own endpoint. The `HttpServerFactory` abstracts away the details of server setup, allowing developers to focus on service implementations.

The `HttpServerFactory` also ensures that the server can scale efficiently by managing the lifecycle of the HTTP server and its thread pool. It registers all service mappings with the server, enabling routing to the correct handler based on URL paths.

#### **`MyHttpServer` and Its Role**

- **`MyHttpServer`**: This class handles all incoming HTTP requests by mapping URLs to corresponding services based on the request context (e.g., `/workers`, `/clients`). It dispatches these requests to the relevant service (such as `WorkerService`, `ClientService`, or `ProjectService`) to perform the necessary CRUD operations.
  - **GET Requests**: Retrieves all entities (`listAll`) or a specific entity by ID (`getById`).
  - **POST Requests**: Creates a new entity by deserializing the JSON payload and calling the `create` method.
  - **PUT Requests**: Updates an existing entity (e.g., changing the name) using the `setName` method.
  - **DELETE Requests**: Deletes an entity by ID using the `deleteById` method.

The `MyHttpServer` class is service-agnostic, meaning it is flexible enough to handle any service that implements the `BaseService<T>` interface. This makes it adaptable to changes or additions of new services (e.g., new entities or database tables) without requiring modifications to the server logic. It uses **Jackson** and **JsonEntityMapper** to serialize and deserialize JSON data exchanged between the client and server.

#### **Integration with `AbstractGenericService<T>` and Child Classes**

The HTTP server is integrated directly with the service layer by using the following components:

- **`BaseService<T>`**: The core interface that defines CRUD methods (`create`, `getById`, `listAll`, `setName`, and `deleteById`). The HTTP server depends on this interface to interact with different services.
- **`AbstractGenericService<T>`**: The abstract class that implements `BaseService<T>` is extended by specific services (e.g., `WorkerService`, `ClientService`, `ProjectService`). The HTTP server calls methods of these services to handle database requests via HTTP operations.
    - For instance, a `GET` request to `/workers` maps to the `listAll` method in `WorkerService`.
    - A `POST` request with a new `Client` JSON object maps to the `create` method in `ClientService`.

The design of `MyHttpServer` and `HttpServerFactory` is service-agnostic. As long as a service implements `BaseService<T>`, it can be registered with the server without altering the HTTP handling logic. This provides flexibility for the system to be extended with new services in the future, making it highly modular and scalable. This design pattern separates concerns between the HTTP layer and the business logic, allowing for clean, maintainable code.

### 4. **JSON Serialization/Deserialization with Jackson**
- The **Jackson** library is used to handle JSON operations throughout the project.
    - **JsonFormatter**: This class simplifies the conversion of Java objects to JSON and vice versa, enabling seamless data exchange between the HTTP server and client.
    - **JsonEntityMapper**: An interface that defines the contract for converting entities to and from JSON format. It is implemented by specific mappers like `ClientJsonMapper`, `ProjectJsonMapper`, and `WorkerJsonMapper`.

### 5. **Modular Architecture**
- **Mappers**: Entity-specific mappers (e.g., `WorkerMapper`, `ClientMapper`, `ProjectMapper`) are used to map database records to entity objects and vice versa. This ensures that database interactions are clean and reusable.

- **MetricRegistry**: The project now incorporates metrics for better performance monitoring and logging.

## File Structure and Key Classes

### CRUD Services
- **`BaseService<T>`**: Interface defining core CRUD methods.
- **`AbstractGenericService<T>`**: Abstract class that implements the base logic for CRUD operations. Specific services extend this class.
- **`WorkerService`, `ClientService`, `ProjectService`**: Implementations of CRUD operations for Workers, Clients, and Projects.

### HTTP Server
- **`HttpServerFactory`**: Responsible for creating and starting the HTTP server.
- **`MyHttpServer`**: Handles HTTP requests and maps them to CRUD operations.
- **`JsonFormatter`**: Utility class for JSON handling.

### Mappers
- **`JsonEntityMapper<T>`**: Interface for serializing/deserializing entities to/from JSON.
- **`WorkerMapper`, `ClientMapper`, `ProjectMapper`**: Classes to map SQL result sets to entities.

### SQL Migrations
- **V1__init_db.sql**: Initializes the database schema.
- **V2__populate_db.sql**: Populates the database with seed data.

## Usage Instructions

1. **Configure Database Credentials**:

Ensure that your database credentials are properly configured in the environment variables or the properties file. This project supports dynamic configuration via environment variables.
    
- Edit `config.properties` with your database URL, username, and password for the database you plan to use. For example, for PostgresQL you can set environment variables `POSTGRES_DB_URL`, `POSTGRES_DB_USER`, `POSTGRES_DB_PASS` and use them in properties file like this:
```properties
postgres.db.url=${POSTGRES_DB_URL}
postgres.db.user=${POSTGRES_DB_USER}
postgres.db.password=${POSTGRES_DB_PASS}
```
- Alternatively, you can directly set the credentials in the config.properties file:
```properties
postgres.db.url=jdbc:postgresql://localhost:5432/mydatabase
postgres.db.user=mydbuser
postgres.db.password=mydbpassword
```
Make sure to replace the placeholders with actual values. Also, ensure your PostgreSQL instance is running and accessible.

2. **Flyway Migrations**:

- Before starting the server, you need to initialise the database schema and seed the database with initial data. Use Flyway for managing the database migrations.
- Run the following command to execute the migrations:
```shell
gradle flywayMigrate
```
This will apply the migration scripts (`V1__init_db.sql` and `V2__populate_db.sql`) to set up the required database tables and seed them with example data for `workers`, `clients`, and `projects`.

3. **HTTP Server**:
   - Start the HTTP server by running the following command in your terminal:
```shell
gradle run
```
- The server will start listening on the default port `9001`. You can customize the port by modifying the configuration in `HttpServerFactory`.
- Available RESTful endpoints for CRUD operations:
    - `/workers`
    - `/clients`
    - `/projects`
- The following HTTP methods are supported:
    - `GET` to retrieve data
    - `POST` to create new records
    - `PUT` to update existing records
    - `DELETE` to remove records

4. **Perform CRUD Operations via HTTP Requests:**:

You can test the API using cURL or any other REST client such as Postman. Below are some examples:

**Example 1: Create a new Worker (POST)**
   - Send a POST request to http://localhost:9001/workers with the following JSON payload:
```json
{
  "name" : "Kitten",
  "dateOfBirth" : "2024-08-20",
  "email" : "cutest@example.net",
  "level" : "TRAINEE",
  "salary" : 10000
}
```
 - Expected response:
```json
{
  "id": 1,
  "name": "Kitten",
  "dateOfBirth": "2024-08-20",
  "email": "cutest@example.net",
  "level": "TRAINEE",
  "salary": 10000
}
```

**Example 2: Fetch All Workers (GET)**
 - To retrieve a list of all workers, send a GET request to:
```shell
curl http://localhost:9001/workers
```
 - The response will be a JSON array of worker objects:

```json
[
    {
        "id": 1,
        "name": "Alice",
        "dateOfBirth": "2001-08-20",
        "email": "alice@example.com",
        "level": "SENIOR",
        "salary": 100000
    },
    {
        "id": 2,
        "name": "Bob",
        "dateOfBirth": "1995-10-11",
        "email": "bob@example.com",
        "level": "MIDDLE",
        "salary": 20000
    },
  ...
]
```

**Example 3: Update a Worker’s Name (PUT)**

- To update the name of an existing worker, send a PUT request to:
```shell
curl -X PUT -H "Content-Type: application/json" -d '{"name": "Eve Updated"}' http://localhost:9001/workers/1
```
- This request updates the worker with ID 1. The server will respond with the number of rows affected.

**Example 4: Delete a Worker (DELETE)**

- To delete a worker, send a DELETE request to:
```shell
curl -X DELETE http://localhost:9001/workers/1
```
- This request deletes the worker with ID 1. The server will respond with the number of rows affected.

5. **Handling Error Responses:**

- If an entity is not found (e.g., trying to update or delete an entity that doesn’t exist), the server will respond with a `404 Not Found` status and an appropriate error message.
- For unsupported HTTP methods, the server will respond with a `405 Method Not Allowed`.

6. **JSON Serialization/Deserialization:**

- All communication between the client and server is handled via JSON. The project uses the Jackson library to serialize Java objects into JSON and deserialize JSON into Java objects.
- Ensure that the JSON payload structure matches the entity model (e.g., fields like name, email, and dateOfBirth should match the corresponding class properties in Java).