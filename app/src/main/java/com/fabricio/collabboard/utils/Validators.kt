package com.fabricio.collabboard.utils

object Validators {

    fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return email.isNotBlank() && emailRegex.matches(email)
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    fun isValidProjectTitle(title: String): Boolean {
        return title.isNotBlank() && title.length >= 3
    }

    fun isValidTechStack(techStack: String): Boolean {
        return techStack.isNotBlank()
    }
}