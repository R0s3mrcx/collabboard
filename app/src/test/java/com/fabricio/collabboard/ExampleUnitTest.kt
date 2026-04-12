package com.fabricio.collabboard

import org.junit.Assert.*
import org.junit.Test

class ValidatorsTest {

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return email.isNotBlank() && emailRegex.matches(email)
    }

    private fun isValidPassword(password: String) = password.length >= 6

    private fun isValidProjectTitle(title: String) =
        title.isNotBlank() && title.length >= 3

    private fun isValidTechStack(techStack: String) = techStack.isNotBlank()

    @Test fun validEmail_returnsTrue() =
        assertTrue(isValidEmail("test@university.edu"))

    @Test fun emptyEmail_returnsFalse() =
        assertFalse(isValidEmail(""))

    @Test fun invalidEmail_returnsFalse() =
        assertFalse(isValidEmail("notanemail"))

    @Test fun validPassword_returnsTrue() =
        assertTrue(isValidPassword("abc123"))

    @Test fun shortPassword_returnsFalse() =
        assertFalse(isValidPassword("abc"))

    @Test fun validTitle_returnsTrue() =
        assertTrue(isValidProjectTitle("App"))

    @Test fun emptyTitle_returnsFalse() =
        assertFalse(isValidProjectTitle(""))

    @Test fun validTechStack_returnsTrue() =
        assertTrue(isValidTechStack("Kotlin, Firebase"))

    @Test fun emptyTechStack_returnsFalse() =
        assertFalse(isValidTechStack(""))
}