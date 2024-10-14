package com.pav_analytics.session

data class User(
    private var email: String,
    private var password: String,
    val uid: String = ""
) {

    fun setEmail(email: String) {
        this.email = email
    }

    fun setPassword(password: String) {
        this.password = password
    }

    fun getEmail(): String {
        return this.email
    }

    fun getPassword(): String {
        return this.password
    }
}