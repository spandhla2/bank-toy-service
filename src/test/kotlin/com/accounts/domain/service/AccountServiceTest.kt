package com.accounts.domain.service

import com.accounts.Fixtures.account
import com.accounts.Fixtures.transaction
import com.accounts.Fixtures.transactionDTO
import com.accounts.domain.model.AccountType
import com.accounts.domain.model.AccountType.CHECKING
import com.accounts.domain.model.AccountType.PRIVATE_LOAN
import com.accounts.domain.model.AccountType.SAVINGS
import com.accounts.exceptions.AccountOperationException
import com.accounts.exceptions.BankAccountNotFoundException
import com.accounts.repository.AccountRepository
import com.accounts.repository.TransactionRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.math.BigDecimal.valueOf
import java.util.Optional

internal class AccountServiceTest {

    private val accountRepository: AccountRepository = mockk(relaxed = true)
    private val transactionRepository: TransactionRepository = mockk(relaxed = true)

    private val accountService = AccountService(accountRepository, transactionRepository)

    @BeforeEach
    fun setUp() {
    }

    @Test
    fun open() {
    }


    @ParameterizedTest
    @EnumSource(AccountType::class)
    fun `should deposit money in any account`(accountType: AccountType) {
        val transactionDTO = transactionDTO(amount = valueOf(120.00).setScale(2))
        val account = account(type = accountType, balance = valueOf(50.00))
        val expectedAccount = account.copy(balance = valueOf(170.00).setScale(2))
        every { accountRepository.findByIban(any()) } returns Optional.of(account)
        every { accountRepository.save(expectedAccount) } returns expectedAccount
        every { transactionRepository.save(transactionDTO.toTransaction()) } returns transactionDTO.toTransaction()

        accountService.deposit(transactionDTO)

        verify(exactly = 1) { accountRepository.save(expectedAccount) }
        verify(exactly = 1) { transactionRepository.save(transactionDTO.toTransaction()) }
    }

    @ParameterizedTest
    @EnumSource(value = AccountType::class, names = ["CHECKING", "SAVINGS"])
    fun `should withdraw money from checking or savings account`(accountType: AccountType) {
        val transactionDTO = transactionDTO(amount = valueOf(50.00).setScale(2))
        val account = account(type = accountType, balance = valueOf(100.00))
        val expectedAccount = account.copy(balance = valueOf(50.00).setScale(2))
        every { accountRepository.findByIban(any()) } returns Optional.of(account)
        every { accountRepository.save(expectedAccount) } returns expectedAccount
        every { transactionRepository.save(transactionDTO.toTransaction()) } returns transactionDTO.toTransaction()

        accountService.withdraw(transactionDTO)

        verify(exactly = 1) { accountRepository.save(expectedAccount) }
        verify(exactly = 1) { transactionRepository.save(transactionDTO.toTransaction()) }
    }

    @Test
    fun `should throw AccountOperationException when withdraw money from private loan account`() {
        val transactionDTO = transactionDTO(amount = valueOf(50.00))
        val account = account(type = PRIVATE_LOAN, balance = valueOf(100.00))
        every { accountRepository.findByIban(any()) } returns Optional.of(account)

        assertThrows<AccountOperationException> {
            accountService.withdraw(transactionDTO)
        }
    }

    @Test
    fun `should throw AccountOperationException when withdraw more money than available in account`() {
        val transactionDTO = transactionDTO(amount = valueOf(50.00))
        val account = account(type = SAVINGS, balance = valueOf(0.00))
        every { accountRepository.findByIban(any()) } returns Optional.of(account)

        assertThrows<AccountOperationException> {
            accountService.withdraw(transactionDTO)
        }
    }

    @Test
    fun `should get balance from account with provided iban`() {
        val account = account(iban = "iban1", balance = valueOf(100.00).setScale(2))
        every { accountRepository.findByIban(any()) } returns Optional.of(account)

        val balance = accountService.getBalance("iban1")

        assertThat(balance).isEqualTo(valueOf(100.00).setScale(2))
    }

    @Test
    fun `should throw BankAccountNotFoundException when missing iban is provided`() {
        every { accountRepository.findByIban(any()) } returns Optional.empty()

        assertThrows<BankAccountNotFoundException> {
            accountService.getBalance("randomIban")
        }
    }

    @Test
    fun `should transfer money from checking or savings account`() {
        val transactionDTO = transactionDTO(fromIban = "iban1", toIban = "iban2", amount = valueOf(50.00).setScale(2))
        val accountFrom = account(iban = "iban1", type = CHECKING, balance = valueOf(100.00))
        val accountTo = account(type = CHECKING, balance = valueOf(100.00))
        val expectedAccountFrom = accountFrom.copy(balance = valueOf(50.00).setScale(2))
        val expectedAccountTo = accountTo.copy(balance = valueOf(150.00).setScale(2))
        every { accountRepository.findByIban("iban1") } returns Optional.of(accountFrom)
        every { accountRepository.findByIban("iban2") } returns Optional.of(accountTo)
        every { accountRepository.save(expectedAccountFrom) } returns expectedAccountFrom
        every { accountRepository.save(expectedAccountTo) } returns expectedAccountTo
        every { transactionRepository.save(transactionDTO.toTransaction()) } returns transactionDTO.toTransaction()

        accountService.transfer(transactionDTO)

        verify(exactly = 1) { accountRepository.save(expectedAccountFrom) }
        verify(exactly = 1) { accountRepository.save(expectedAccountTo) }
        verify(exactly = 1) { transactionRepository.save(transactionDTO.toTransaction()) }
    }

    @Test
    fun `should transfer money from savings account to reference account`() {
        val transactionDTO = transactionDTO(fromIban = "iban1", toIban = "iban2", amount = valueOf(50.00).setScale(2))
        val accountFrom = account(iban = "iban1", referenceAccountIban = "iban2", type = SAVINGS, balance = valueOf(100.00))
        val accountTo = account(iban = "iban2", type = CHECKING, balance = valueOf(100.00))
        val expectedAccountFrom = accountFrom.copy(balance = valueOf(50.00).setScale(2))
        val expectedAccountTo = accountTo.copy(balance = valueOf(150.00).setScale(2))
        every { accountRepository.findByIban("iban1") } returns Optional.of(accountFrom)
        every { accountRepository.findByIban("iban2") } returns Optional.of(accountTo)
        every { accountRepository.save(expectedAccountFrom) } returns expectedAccountFrom
        every { accountRepository.save(expectedAccountTo) } returns expectedAccountTo
        every { transactionRepository.save(transactionDTO.toTransaction()) } returns transactionDTO.toTransaction()

        accountService.transfer(transactionDTO)

        verify(exactly = 1) { accountRepository.save(expectedAccountFrom) }
        verify(exactly = 1) { accountRepository.save(expectedAccountTo) }
        verify(exactly = 1) { transactionRepository.save(transactionDTO.toTransaction()) }
    }

    @Test
    fun `should throw AccountOperationException when transfer money from savings to another account which is not the reference account`() {
        val transactionDTO = transactionDTO(fromIban = "iban1", toIban = "iban2", amount = valueOf(50.00))
        val accountFrom = account(iban = "iban1", referenceAccountIban = "iban3", type = SAVINGS, balance = valueOf(100.00))
        val accountTo = account(iban = "iban2", type = CHECKING, balance = valueOf(100.00))
        every { accountRepository.findByIban("iban1") } returns Optional.of(accountFrom)
        every { accountRepository.findByIban("iban2") } returns Optional.of(accountTo)

        assertThrows<AccountOperationException> {
            accountService.transfer(transactionDTO)
        }
    }

    @Test
    fun `should throw AccountOperationException when transfer money from private loan account`() {
        val transactionDTO = transactionDTO(fromIban = "iban1", toIban = "iban2", amount = valueOf(50.00))
        val accountFrom = account(iban = "iban1", type = PRIVATE_LOAN, balance = valueOf(100.00))
        val accountTo = account(iban = "iban2", type = CHECKING, balance = valueOf(100.00))
        every { accountRepository.findByIban("iban1") } returns Optional.of(accountFrom)
        every { accountRepository.findByIban("iban2") } returns Optional.of(accountTo)

        assertThrows<AccountOperationException> {
            accountService.transfer(transactionDTO)
        }
    }

    @Test
    fun `should get accounts using account type filter`() {
        every { accountRepository.findByTypeIn(any()) } returns listOf(account())

        val accounts = accountService.getAccounts(listOf(CHECKING, SAVINGS))

        assertThat(accounts).hasSize(1)
        verify(exactly = 1) { accountRepository.findByTypeIn(listOf(CHECKING, SAVINGS)) }
    }

    @Test
    fun `should get account transaction history when valid iban provided`() {
        every { accountRepository.findByIban(any()) } returns Optional.of(account())
        every { transactionRepository.findByIban(any()) } returns listOf(transaction())

        val transactions = accountService.getAccountTransactionHistory("iban")

        assertThat(transactions).hasSize(1)
        verify(exactly = 1) { accountRepository.findByIban("iban") }
        verify(exactly = 1) { transactionRepository.findByIban("iban") }
    }

    @Test
    fun `should throw BankAccountNotFoundException on get account transaction history when missing iban is provided`() {
        every { accountRepository.findByIban(any()) } returns Optional.empty()

        assertThrows<BankAccountNotFoundException> {
            accountService.getAccountTransactionHistory("iban")
        }
    }
}
