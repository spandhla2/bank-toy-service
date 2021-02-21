package com.accounts.config

import com.accounts.domain.model.Account
import com.accounts.domain.model.AccountType.CHECKING
import com.accounts.domain.model.AccountType.PRIVATE_LOAN
import com.accounts.domain.model.AccountType.SAVINGS
import com.accounts.repository.AccountRepository
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class InitialDataConfig(
    private val accountRepository: AccountRepository
) {

    @EventListener
    fun loadInitialData(event: ApplicationReadyEvent) {
        val account = Account(
            iban = "DE89370400440532013000",
            type = CHECKING,
            balance = BigDecimal.valueOf(100.00),
            routingNumber = 12345,
            referenceAccountIban = null,
            customerId = "1"
        )

        val accounts = listOf(
            account,
            account.copy(iban = "DE75512108001245126199", type = SAVINGS, referenceAccountIban = "DE89370400440532013000"),
            account.copy(iban = "DE56500105177124582257", type = PRIVATE_LOAN),
            account.copy(iban = "DE07500105176735774838", type = CHECKING, customerId = "2")
        )
        accountRepository.saveAll(accounts)
    }
}
