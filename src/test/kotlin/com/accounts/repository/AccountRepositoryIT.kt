package com.accounts.repository

import com.accounts.Fixtures.account
import com.accounts.IntegrationTestParent
import com.accounts.domain.model.AccountType.CHECKING
import com.accounts.domain.model.AccountType.PRIVATE_LOAN
import com.accounts.domain.model.AccountType.SAVINGS
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.springframework.beans.factory.annotation.Autowired

internal class AccountRepositoryIT : IntegrationTestParent() {

    @Autowired
    private lateinit var accountRepository: AccountRepository

    @BeforeEach
    @AfterEach
    fun setUp() {
        accountRepository.deleteAll()
    }

    @Test
    fun `should find account by iban`() {
        val iban = "DE89370400440532013000"
        val savedAccount = account(iban = iban)
        accountRepository.save(savedAccount)

        val account = accountRepository.findByIban(iban)

        assertThat(account).isPresent
        assertThat(account.get().iban).isEqualTo(iban)
    }

    @Test
    fun `should return optional empty when iban not found`() {
        val savedAccount = account()
        accountRepository.save(savedAccount)

        val account = accountRepository.findByIban("randomIban")

        assertThat(account).isNotPresent
    }

    @Test
    fun `should filter accounts by account type`() {
        accountRepository.save(account(type = CHECKING))
        accountRepository.save(account(type = SAVINGS))
        accountRepository.save(account(type = PRIVATE_LOAN))

        val accounts = accountRepository.findByTypeIn(listOf(CHECKING, SAVINGS))

        assertThat(accounts).hasSize(2)
    }

    @Test
    fun `should return empty list of accounts when no account type is found`() {
        accountRepository.save(account(type = CHECKING))

        val accounts = accountRepository.findByTypeIn(emptyList())

        assertThat(accounts).hasSize(0)
    }
}
