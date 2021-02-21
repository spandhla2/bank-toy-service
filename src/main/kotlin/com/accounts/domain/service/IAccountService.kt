package com.accounts.domain.service

import com.accounts.domain.model.AccountDTO
import com.accounts.domain.model.AccountType
import com.accounts.domain.model.TransactionDTO
import java.math.BigDecimal

/**
 * Account service which handles account operations
 */
interface IAccountService {

    /**
     * Open Account
     * @param account
     * @return account the opened account
     */
    fun open(accountDTO: AccountDTO): AccountDTO

    /**
     * Deposit into account
     * @param transaction transaction details
     */
    fun deposit(transactionDTO: TransactionDTO)

    /**
     * Withdraw from account
     * @param transfer transfer details
     */
    fun withdraw(transactionDTO: TransactionDTO)

    /**
     * Get current account balance
     * @param iban IBAN
     * @return current balance
     */
    fun getBalance(iban: String): BigDecimal

    /**
     * Transfer across accounts
     * @param transaction transaction details
     */
    fun transfer(transactionDTO: TransactionDTO)

    /**
     * Get accounts
     * @param accountTypes list of account types to filter with
     * @return list of accounts associated with the IBAN
     */
    fun getAccounts(accountTypes: List<AccountType>): List<AccountDTO>

    /**
     * Get account transaction history
     * @param iban IBAN
     * @return list of account transactions
     */
    fun getAccountTransactionHistory(iban: String): List<TransactionDTO>
}
