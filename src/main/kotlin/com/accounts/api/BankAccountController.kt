package com.accounts.api

import com.accounts.domain.model.AccountDTO
import com.accounts.domain.model.AccountType
import com.accounts.domain.model.TransactionDTO
import com.accounts.domain.model.TransactionType
import com.accounts.domain.service.IAccountService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
@RequestMapping("/api/v1/accounts")
@Tag(
    description = "Handles bank account operations",
    name = "Bank account operations"
)
class BankAccountController(
    private val accountService: IAccountService
) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @Operation(description = "Get accounts from IBAN")
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "Successfully retrieved accounts")])
    fun getAccounts(
        @Parameter(description = "account types", required = true, example = "SAVINGS,CHECKING,PRIVATE_LOAN")
        @RequestParam(required = true) accountTypes: List<AccountType>
    ): ResponseEntity<Collection<AccountDTO>> {
        return ResponseEntity.ok().body(accountService.getAccounts(accountTypes))
    }

    @GetMapping("/balance", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @Operation(description = "Get current account balance")
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "Successfully retrieved balance")])
    fun getBalance(
        @Parameter(description = "IBAN", required = true)
        @RequestParam(required = true) iban: String
    ): ResponseEntity<BigDecimal> {
        return ResponseEntity.ok().body(accountService.getBalance(iban))
    }

    @GetMapping("/transactions", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @Operation(description = "Get account transaction history")
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "Successfully retrieved account transaction history")])
    fun getAccountTransactionHistory(
        @Parameter(description = "IBAN", required = true)
        @RequestParam(required = true) iban: String
    ): ResponseEntity<Collection<TransactionDTO>> {
        return ResponseEntity.ok().body(accountService.getAccountTransactionHistory(iban))
    }

    @PatchMapping("/transaction", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(description = "Supports transaction operations i.e DEPOSIT, WITHDRAW or TRANSFER")
    @ApiResponses(value = [ApiResponse(responseCode = "204", description = "Successfully performed operation")])
    fun transfer(@RequestBody transactionDTO: TransactionDTO): ResponseEntity<Unit> {
        when (transactionDTO.type) {
            TransactionType.DEPOSIT -> accountService.deposit(transactionDTO)
            TransactionType.WITHDRAW -> accountService.withdraw(transactionDTO)
            TransactionType.TRANSFER -> accountService.transfer(transactionDTO)
        }
        return ResponseEntity.noContent().build()
    }
}
