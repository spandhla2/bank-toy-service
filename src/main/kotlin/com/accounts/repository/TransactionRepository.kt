package com.accounts.repository

import com.accounts.domain.model.Transaction
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TransactionRepository : CrudRepository<Transaction, Long> {

    @Query(
        value = """
        SELECT * from transaction 
        WHERE from_iban = :iban OR to_iban = :iban
    """, nativeQuery = true
    )
    fun findByIban(@Param("iban") iban: String): List<Transaction>
}
