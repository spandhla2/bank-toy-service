package com.accounts.exceptions


class UserNotFoundException(userId: String) : RuntimeException() {
    override val message: String = "User with id $userId not found."
}

class BankAccountNotFoundException(iban: String) : RuntimeException() {
    override val message: String = "Bank account with iban $iban not found."
}

class AccountOperationException(message: String) : RuntimeException() {
    override val message: String = "$message"
}
