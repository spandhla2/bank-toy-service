package com.accounts.repository

import com.accounts.domain.model.Account
import com.accounts.domain.model.AccountType
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface AccountRepository: CrudRepository<Account, String> {

    fun findByIban(iban: String): Optional<Account>

    fun findByTypeIn(accountTypes: List<AccountType>): List<Account>
}
