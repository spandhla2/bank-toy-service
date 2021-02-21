package com.accounts.repository

import com.accounts.Fixtures.transaction
import com.accounts.IntegrationTestParent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class TransactionRepositoryIT : IntegrationTestParent() {

    @Autowired
    private lateinit var transactionRepository: TransactionRepository

    @BeforeEach
    @AfterEach
    fun setUp() {
        transactionRepository.deleteAll()
    }

    @Test
    fun `should save and retrieve a transaction`() {
        val transaction = transaction()

        transactionRepository.save(transaction)

        val lol = transactionRepository.findAll()

        assertThat(transactionRepository.findAll()).hasSize(1)
    }

    @Test
    fun `should find by iban using from or to iban`() {
        transactionRepository.save(transaction(fromIban = "DE89370400440532013000", toIban = "DE75512108001245126199"))
        transactionRepository.save(transaction(fromIban = "DE75512108001245126199", toIban = "DE89370400440532013000"))
        transactionRepository.save(transaction(fromIban = "iban1", toIban = "iban2"))

        val transactions = transactionRepository.findByIban("DE89370400440532013000")

        assertThat(transactions).hasSize(2)
    }

    @Test
    fun `should return empty list of transactions when iban not found`() {
        transactionRepository.save(transaction(fromIban = "DE89370400440532013000", toIban = "DE75512108001245126199"))

        val transactions = transactionRepository.findByIban("randomIban")

        assertThat(transactions).hasSize(0)
    }
}
