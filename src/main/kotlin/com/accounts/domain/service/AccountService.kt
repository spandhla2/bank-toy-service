package com.accounts.domain.service

import com.accounts.domain.model.Account
import com.accounts.domain.model.AccountDTO
import com.accounts.domain.model.AccountType
import com.accounts.domain.model.AccountType.PRIVATE_LOAN
import com.accounts.domain.model.AccountType.SAVINGS
import com.accounts.domain.model.Transaction
import com.accounts.domain.model.TransactionDTO
import com.accounts.exceptions.AccountOperationException
import com.accounts.exceptions.BankAccountNotFoundException
import com.accounts.repository.AccountRepository
import com.accounts.repository.TransactionRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import javax.transaction.Transactional

/**
 * Account service which handles account operations
 */
@Service
class AccountService(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) : IAccountService {

    companion object {
        private val logger = KotlinLogging.logger {}
        private const val OPERATION_NOT_PERMITTED = "Operation not permitted."
    }

    override fun open(accountDTO: AccountDTO): AccountDTO {
        TODO("Not yet implemented")
    }

    @Transactional
    override fun deposit(transactionDTO: TransactionDTO) {
        logger.info("Performing deposit operation $transactionDTO")
        val transaction = transactionDTO.toTransaction()

        val account = transaction.addToAccount()

        accountRepository.save(account)
        transactionRepository.save(transaction)
    }

    @Transactional
    override fun withdraw(transactionDTO: TransactionDTO) {
        logger.info("Performing withdraw operation $transactionDTO")
        val transaction = transactionDTO.toTransaction()

        val account = transaction.subtractFromAccount()

        validateWithdrawOperation(account)

        accountRepository.save(account)
        transactionRepository.save(transaction)
    }

    override fun getBalance(iban: String): BigDecimal =
        iban.findAccountByIban().balance.setScale(2, RoundingMode.HALF_EVEN)

    @Transactional
    override fun transfer(transactionDTO: TransactionDTO) {
        logger.info("Performing transfer operation $transactionDTO")
        val transaction = transactionDTO.toTransaction()

        val accountFrom = transaction.subtractFromAccount()
        val accountTo = transaction.addToAccount()

        validateTransferOperation(accountFrom, accountTo)

        accountRepository.save(accountFrom)
        accountRepository.save(accountTo)
        transactionRepository.save(transaction)
    }

    override fun getAccounts(accountTypes: List<AccountType>): List<AccountDTO> {
        return accountRepository.findByTypeIn(accountTypes).map { it.toAccountDTO() }
    }

    override fun getAccountTransactionHistory(iban: String): List<TransactionDTO> {
        iban.findAccountByIban()
        return transactionRepository
            .findByIban(iban)
            .map { transaction -> transaction.toTransactionDTO() }
    }

    private fun Transaction.subtractFromAccount(): Account {
        val account = this.fromIban.findAccountByIban()
        if (account.balance < this.amount) {
            throw AccountOperationException("Operation failed. Insufficient funds.")
        }
        account.balance = account.balance.subtract(this.amount)
        return account
    }

    private fun Transaction.addToAccount(): Account {
        val account = this.toIban.findAccountByIban()
        account.balance = account.balance.add(this.amount)
        return account
    }

    private fun String.findAccountByIban(): Account {
        return accountRepository.findByIban(this)
            .orElseThrow { throw BankAccountNotFoundException(this) }
    }

    private fun validateTransferOperation(accountFrom: Account, accountTo: Account) {
        if(accountFrom.type == SAVINGS) {
            if(accountTo.iban != accountFrom.referenceAccountIban) {
                throw AccountOperationException(OPERATION_NOT_PERMITTED)
            }
        }
        if(accountFrom.type == PRIVATE_LOAN) {
            throw AccountOperationException(OPERATION_NOT_PERMITTED)
        }
    }

    private fun validateWithdrawOperation(account: Account) {
        if(account.type == PRIVATE_LOAN) {
            throw AccountOperationException(OPERATION_NOT_PERMITTED)
        }
    }

}
