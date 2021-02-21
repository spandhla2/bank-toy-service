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
import java.time.ZonedDateTime
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
@EntityListeners(AuditingEntityListener::class)
data class Transaction(
    @Id
    @GeneratedValue
    val id: Long? = -1,
    val fromIban: String,
    val toIban: String,
    val amount: BigDecimal,
    val type: TransactionType,
    @CreatedDate
    var createdAt: LocalDateTime? = null
) : Serializable {

    fun toTransactionDTO(): TransactionDTO =
        with(this) {
            TransactionDTO(
                fromIban = fromIban,
                toIban = toIban,
                amount = amount.setScale(2, RoundingMode.HALF_EVEN),
                type = type,
                createdAt = createdAt
            )
        }
}

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class TransactionDTO(
    val fromIban: String,
    val toIban: String,
    val amount: BigDecimal,
    val type: TransactionType,
    var createdAt: LocalDateTime? = null
) : Serializable {

    fun toTransaction(): Transaction =
        with(this) {
            Transaction(
                fromIban = fromIban,
                toIban = toIban,
                amount = amount,
                type = type,
                createdAt = createdAt
            )
        }
}

enum class TransactionType(val type: String) {
    DEPOSIT("deposit"),
    WITHDRAW("withdraw"),
    TRANSFER("transfer")
}
