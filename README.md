# bank-toy-service

Serves the basic bank account operations such as deposit, transfer and withdraw money via a REST API.


### Architecture
...

### API

The API is best viewed in Swagger API (http://localgost:8080/swagger-ui.html) and Swagger API Docs (http://localgost:8080/v3/api-docs/).

A Transaction payload example :    
{  
  "fromIban": "string", //for WITHDRAW & TRANSFER transactions  
  "toIban": "string",  //for DEPOSIT & TRANSFER transactions  
  "amount": 20.00,  
  "type": "DEPOSIT", // WITHDRAW, DEPOSIT or TRANSFER  
}

### Technologies

- Spring boot
- Kotlin
- OpenAPI

### Key decisions 
- OpenAPI:- for the service's API documentation.
- Not to expose account or transaction Ids in API, since IBAN is used for reference.

### Running Application
- On application start up there are 4 dummy accounts loaded in DB with initial balance of 100.00 using `ApplicationReadyEvent` event listener in `InitialDataConfig.kt`:  
 i) Customer Id 1 - `DE89370400440532013000: CHECKING`, `DE75512108001245126199: SAVINGS`, `DE56500105177124582257: PRIVATE_LOAN`  
 ii) Customer Id 2 - `DE07500105176735774838: CHECKING`


### Possible improvements
- Add more validation for DTO fields like `amount >= 0` etc.  
- User validation:- introduce validation if the user with customerId exists before performing a transaction. 
Most probably through a cached DB or downstream call to a service with customer data.
- User <-> Account relationship:- introduce a one to many relationship on customerId were a user can have multiple accounts.
- Database:- For this service used a in-memory db h2, but for real life use-case, a relational database like 
Postgres would be ideal.
- Use UUIDs for Account or Transaction Ids.
- Functionality to open account

