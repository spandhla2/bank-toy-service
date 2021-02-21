package com.accounts.domain.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
@EntityListeners(AuditingEntityListener::class)
data class Account(
    @Id
    @GeneratedValue
    val id: Long? = -1,
    val iban: String,
    val type: AccountType,
    var balance: BigDecimal,
    val routingNumber: Int,
    val referenceAccountIban: String?,
    val customerId: String,
    @CreatedDate
    var createdAt: LocalDateTime? = null,
    @LastModifiedDate
    var updatedAt: LocalDateTime? = null
) : Serializable {

    fun toAccountDTO(): AccountDTO =
        with(this) {
            AccountDTO(
                iban = iban,
                type = type,
                balance = balance.setScale(2, RoundingMode.HALF_EVEN),
                routingNumber = routingNumber,
                referenceAccountIban = referenceAccountIban,
                customerId = customerId,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        }
}

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class AccountDTO(
    val iban: String,
    val type: AccountType,
    val balance: BigDecimal,
    val routingNumber: Int,
    val referenceAccountIban: String?,
    val customerId: String,
    var createdAt: LocalDateTime? = null,
    var updatedAt: LocalDateTime? = null
) : Serializable {

    fun toAccount(): Account =
        with(this) {
            Account(
                iban = iban,
                type = type,
                balance = balance,
                routingNumber = routingNumber,
                referenceAccountIban = referenceAccountIban,
                customerId = customerId,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        }
}

enum class AccountType(val type: String, val operations: List<Operation>) {
    CHECKING("checking", listOf(Operation.DEPOSIT, Operation.WITHDRAW, Operation.TRANSFER)),
    SAVINGS("savings", listOf(Operation.DEPOSIT, Operation.WITHDRAW, Operation.REFERENCE_TRANSFER)),
    PRIVATE_LOAN("private-loan", listOf(Operation.DEPOSIT))
}

enum class Operation {
    DEPOSIT,
    WITHDRAW,
    TRANSFER,
    REFERENCE_TRANSFER
}
