package com.accounts.api

import com.accounts.Fixtures.account
import com.accounts.Fixtures.transaction
import com.accounts.Fixtures.transactionDTO
import com.accounts.IntegrationTestParent
import com.accounts.domain.model.AccountDTO
import com.accounts.domain.model.AccountType.CHECKING
import com.accounts.domain.model.AccountType.PRIVATE_LOAN
import com.accounts.domain.model.AccountType.SAVINGS
import com.accounts.domain.model.TransactionDTO
import com.accounts.domain.model.TransactionType.DEPOSIT
import com.accounts.domain.model.TransactionType.TRANSFER
import com.accounts.domain.model.TransactionType.WITHDRAW
import com.accounts.repository.AccountRepository
import com.accounts.repository.TransactionRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.builder.RequestSpecBuilder
import io.restassured.specification.RequestSpecification
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.hamcrest.Matchers.equalTo
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import java.math.BigDecimal

internal class BankAccountControllerIT : IntegrationTestParent() {

    @LocalServerPort
    private val port: Int = 0

    @Autowired
    private lateinit var accountRepository: AccountRepository

    @Autowired
    private lateinit var transactionRepository: TransactionRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var requestSpec: RequestSpecification

    companion object {
        private val TRANSACTION_AMOUNT_50 = BigDecimal.valueOf(50.00)
    }

    @BeforeEach
    fun setUp() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
        requestSpec = RequestSpecBuilder()
            .addHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .setPort(port)
            .setBaseUri("http://localhost")
            .build()
    }

    @BeforeEach
    @AfterEach
    fun `tear down db`() {
        accountRepository.deleteAll()
        transactionRepository.deleteAll()
    }

    @Test
    fun `should return accounts with status 200 ok when filtered by account types`() {
        accountRepository.save(account(type = SAVINGS))
        accountRepository.save(account(type = CHECKING))
        accountRepository.save(account(type = PRIVATE_LOAN))

        val result = given()
            .spec(requestSpec)
            .param("accountTypes", "CHECKING,SAVINGS")
            .`when`()
            .get("/api/v1/accounts")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract().response()
        val accounts = objectMapper.readValue<List<AccountDTO>>(result.body().asString())

        assertThat(accounts).hasSize(2)
        assertThat(accounts.map { it.type }).containsExactlyInAnyOrder(CHECKING, SAVINGS)
    }

    @Test
    fun `should return 400 bad request on get accounts when invalid account type is provided`() {
        given()
            .spec(requestSpec)
            .param("accountTypes", "INVALID-ACCOUNT-TYPE")
            .`when`()
            .get("/api/v1/accounts")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
    }

    @Test
    fun `should return current balance and 200 ok when valid iban provided`() {
        accountRepository.save(account(iban = "iban1", balance = BigDecimal.valueOf(100.00)))

        val result = given()
            .spec(requestSpec)
            .param("iban", "iban1")
            .`when`()
            .get("/api/v1/accounts/balance")
            .then()
            .statusCode(HttpStatus.OK.value())
            .body(equalTo("100.00"))
    }

    @Test
    fun `should return status 404 not found on get current balance when missing iban is provided`() {
        given()
            .spec(requestSpec)
            .param("iban", "iban1")
            .`when`()
            .get("/api/v1/accounts/balance")
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value())
    }

    @Test
    fun `should return account transaction history with status 200 ok when provided valid iban`() {
        accountRepository.save(account(iban = "iban1", type = SAVINGS))
        transactionRepository.save(transaction(fromIban = "iban1", toIban = "iban2"))
        transactionRepository.save(transaction(fromIban = "iban2", toIban = "iban1"))
        transactionRepository.save(transaction(fromIban = "iban3", toIban = "iban4"))

        val result = given()
            .spec(requestSpec)
            .param("iban", "iban1")
            .`when`()
            .get("/api/v1/accounts/transactions")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract().response()
        val transactions = objectMapper.readValue<List<TransactionDTO>>(result.body().asString())

        assertThat(transactions).hasSize(2)
    }

    @Test
    fun `should return status 404 not found on get account transaction history when provided missing iban`() {
        given()
            .spec(requestSpec)
            .param("iban", "iban1")
            .`when`()
            .get("/api/v1/accounts/transactions")
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value())
    }

    @Test
    fun `should return status 204 no content when transfer is completed successfully`() {
        accountRepository.save(account(iban = "iban1", type = CHECKING))
        accountRepository.save(account(iban = "iban2", type = CHECKING))
        val transactionDTO = objectMapper.writeValueAsString(transactionDTO(type = TRANSFER, fromIban = "iban1", toIban = "iban2", amount = TRANSACTION_AMOUNT_50))

        given()
            .spec(requestSpec)
            .body(transactionDTO)
            .`when`()
            .patch("/api/v1/accounts/transaction")
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value())
    }

    @Test
    fun `should return status 400 bad when transfer money from private loan account`() {
        accountRepository.save(account(iban = "iban1", type = PRIVATE_LOAN))
        accountRepository.save(account(iban = "iban2", type = CHECKING))
        val transactionDTO = objectMapper.writeValueAsString(transactionDTO(type = WITHDRAW, fromIban = "iban1", toIban = "iban2", amount = TRANSACTION_AMOUNT_50))

        given()
            .spec(requestSpec)
            .body(transactionDTO)
            .`when`()
            .patch("/api/v1/accounts/transaction")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
    }

    @Test
    fun `should return status 204 no content when deposit is completed successfully`() {
        accountRepository.save(account(iban = "iban1", type = CHECKING))
        val transactionDTO = objectMapper.writeValueAsString(transactionDTO(type = DEPOSIT, fromIban = "", toIban = "iban1", amount = TRANSACTION_AMOUNT_50))

        given()
            .spec(requestSpec)
            .body(transactionDTO)
            .`when`()
            .patch("/api/v1/accounts/transaction")
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value())
    }

    @Test
    fun `should return status 404 not found on deposit when iban provided is missing`() {
        val transactionDTO = objectMapper.writeValueAsString(transactionDTO(type = DEPOSIT, fromIban = "", toIban = "missing-iban", amount = TRANSACTION_AMOUNT_50))

        given()
            .spec(requestSpec)
            .body(transactionDTO)
            .`when`()
            .patch("/api/v1/accounts/transaction")
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value())
    }

    @Test
    fun `should return status 204 no content when withdraw is completed successfully`() {
        accountRepository.save(account(iban = "iban1", type = CHECKING))
        val transactionDTO = objectMapper.writeValueAsString(transactionDTO(type = WITHDRAW, fromIban = "iban1", toIban = "", amount = TRANSACTION_AMOUNT_50))

        given()
            .spec(requestSpec)
            .body(transactionDTO)
            .`when`()
            .patch("/api/v1/accounts/transaction")
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value())
    }

    @Test
    fun `should return status 400 bad when withdraw money from private loan account`() {
        accountRepository.save(account(iban = "iban1", type = PRIVATE_LOAN))
        val transactionDTO = objectMapper.writeValueAsString(transactionDTO(type = WITHDRAW, fromIban = "iban1", toIban = "", amount = TRANSACTION_AMOUNT_50))

        given()
            .spec(requestSpec)
            .body(transactionDTO)
            .`when`()
            .patch("/api/v1/accounts/transaction")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
    }

    @Test
    fun `should return status 400 bad when withdraw more money than available in account`() {
        accountRepository.save(account(iban = "iban1", type = CHECKING, balance = BigDecimal.valueOf(0.00)))
        val transactionDTO = objectMapper.writeValueAsString(transactionDTO(type = WITHDRAW, fromIban = "iban1", toIban = "", amount = TRANSACTION_AMOUNT_50))

        given()
            .spec(requestSpec)
            .body(transactionDTO)
            .`when`()
            .patch("/api/v1/accounts/transaction")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
    }

    @Test
    fun `should return status 404 not found on withdraw when iban provided is missing`() {
        val transactionDTO = objectMapper.writeValueAsString(transactionDTO(type = WITHDRAW, fromIban = "missing-iban", toIban = "", amount = TRANSACTION_AMOUNT_50))

        given()
            .spec(requestSpec)
            .body(transactionDTO)
            .`when`()
            .patch("/api/v1/accounts/transaction")
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value())
    }

    @Test
    fun `should return status 204 no content when transfer from savings account to reference account is completed successfully`() {
        accountRepository.save(account(iban = "iban1", type = SAVINGS, referenceAccountIban = "iban2"))
        accountRepository.save(account(iban = "iban2", type = CHECKING))
        val transactionDTO = objectMapper.writeValueAsString(transactionDTO(fromIban = "iban1", toIban = "iban2", amount = TRANSACTION_AMOUNT_50))

        given()
            .spec(requestSpec)
            .body(transactionDTO)
            .`when`()
            .patch("/api/v1/accounts/transaction")
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value())
    }

    @Test
    fun `should return status 400 bad when transfer money from savings to another account which is not the reference account`() {
        accountRepository.save(account(iban = "iban1", type = SAVINGS, referenceAccountIban = "iban3"))
        accountRepository.save(account(iban = "iban2", type = CHECKING))
        val transactionDTO = objectMapper.writeValueAsString(transactionDTO(type = TRANSFER, fromIban = "iban1", toIban = "iban2", amount = TRANSACTION_AMOUNT_50))

        given()
            .spec(requestSpec)
            .body(transactionDTO)
            .`when`()
            .patch("/api/v1/accounts/transaction")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
    }
}
