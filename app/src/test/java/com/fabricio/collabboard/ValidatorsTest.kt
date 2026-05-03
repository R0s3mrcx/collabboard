package com.fabricio.collabboard

import com.fabricio.collabboard.utils.Validators
import org.junit.Assert.*
import org.junit.Test

class ValidatorsTest {

    // --- Email ---
    @Test fun validEmail_returnsTrue() = assertTrue(Validators.isValidEmail("test@university.edu"))
    @Test fun emptyEmail_returnsFalse() = assertFalse(Validators.isValidEmail(""))
    @Test fun invalidEmail_returnsFalse() = assertFalse(Validators.isValidEmail("notanemail"))

    // --- Password ---
    @Test fun validPassword_returnsTrue() = assertTrue(Validators.isValidPassword("abc123"))
    @Test fun shortPassword_returnsFalse() = assertFalse(Validators.isValidPassword("abc"))

    // --- Project title ---
    @Test fun validTitle_returnsTrue() = assertTrue(Validators.isValidProjectTitle("App"))
    @Test fun emptyTitle_returnsFalse() = assertFalse(Validators.isValidProjectTitle(""))
    @Test fun tooShortTitle_returnsFalse() = assertFalse(Validators.isValidProjectTitle("ab"))

    // --- Tech stack ---
    @Test fun validTechStack_returnsTrue() = assertTrue(Validators.isValidTechStack("Kotlin, Firebase"))
    @Test fun emptyTechStack_returnsFalse() = assertFalse(Validators.isValidTechStack(""))

    // --- Lab 2 new tests: Description ---
    @Test fun validDescription_returnsTrue() = assertTrue(Validators.isValidDescription("A cool project"))
    @Test fun emptyDescription_returnsFalse() = assertFalse(Validators.isValidDescription(""))
    @Test fun blankDescription_returnsFalse() = assertFalse(Validators.isValidDescription("   "))
}
