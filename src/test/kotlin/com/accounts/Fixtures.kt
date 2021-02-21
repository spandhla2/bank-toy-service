package com.accounts

import com.accounts.domain.model.Account
import com.accounts.domain.model.AccountDTO
import com.accounts.domain.model.AccountType
import com.accounts.domain.model.Transaction
import com.accounts.domain.model.TransactionDTO
import com.accounts.domain.model.TransactionType
import java.math.BigDecimal

object Fixtures {

    fun account(
        iban: String = "DE89370400440532013000",
        type: AccountType = AccountType.CHECKING,
        balance: BigDecimal = BigDecimal.valueOf(100.00),
        routingNumber: Int = 12345,
        referenceAccountIban: String? = null,
        customerId: String = "c1"
    ): Account {
        return Account(
            iban = iban,
            type = type,
            balance = balance,
            routingNumber = routingNumber,
            referenceAccountIban = referenceAccountIban,
            customerId = customerId
        )
    }

    fun accountDTO(
        iban: String = "DE89370400440532013000",
        type: AccountType = AccountType.CHECKING,
        balance: BigDecimal = BigDecimal.valueOf(100.00),
        routingNumber: Int = 12345,
        referenceAccountIban: String? = null,
        customerId: String = "c1"
    ): AccountDTO {
        return AccountDTO(
            iban = iban,
            type = type,
            balance = balance,
            routingNumber = routingNumber,
            referenceAccountIban = referenceAccountIban,
            customerId = customerId
        )
    }

    fun transaction(
        id: Long = 1,
        fromIban: String = "DE89370400440532013000",
        toIban: String = "DE75512108001245126199",
        type: TransactionType = TransactionType.TRANSFER,
        amount: BigDecimal = BigDecimal.valueOf(100.00)
    ): Transaction {
        return Transaction(
            id = id,
            fromIban = fromIban,
            toIban = toIban,
            type = type,
            amount = amount
        )
    }

    fun transactionDTO(
        fromIban: String = "DE89370400440532013000",
        toIban: String = "DE75512108001245126199",
        type: TransactionType = TransactionType.TRANSFER,
        amount: BigDecimal = BigDecimal.valueOf(100.00)
    ): TransactionDTO {
        return TransactionDTO(
            fromIban = fromIban,
            toIban = toIban,
            type = type,
            amount = amount
        )
    }

}
