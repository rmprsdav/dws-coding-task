# Account Transaction Management Service

This project is a simple bank account management system built using Java and Spring Boot. It allows users to create accounts, retrieve account details, and transfer money between accounts with proper concurrency control and exception handling.

#Features

- Create a new account
- Retrieve account details
- Transfer money between accounts
- Global exception handling
- Thread-safe operations

## Technologies Used

- Java
- Spring Boot
- Gradle
- JUnit 5

## Getting Started

### Prerequisites

- Java 17 or higher
- Gradle 7.0 or higher

### Installation

1. Clone the repository:
    ```sh
    git clone https://github.com/rmprsdav/dws-coding-task.git
    cd dws-coding-task
    ```

2. Build the project:
    ```sh
    ./gradlew build
    ```

3. Run the application:
    ```sh
    ./gradlew bootRun
    ```

### Running Tests

To run the tests, use the following command:
	```sh
	./gradlew test
	```

### API Endpoints

Create Account
URL: /v1/accounts
Method: POST

Request Body:
{
    "accountId": "Id-123",
    "balance": 1000
}

Response:
{
    "requestId": "29afca70-c809-42f1-bf38-b20b7e0d97fb",
    "message": "Account created successfully"
}

Get Account
URL: /v1/accounts/{accountId}
Method: GET

Response:
{
    "accountId": "Id-123",
    "balance": 1000
}

Transfer Money
URL: /v1/accounts/transfer
Method: POST

Request Body:
{
    "accountFromId": "Id-123",
    "accountToId": "Id-456",
    "amount": "100"
}

Response:
{
    "requestId": "29afca70-c809-42f1-bf38-b20b7e0d97av",
    "message": "Transferring 100 from account Id-123 to account Id-456"
}
