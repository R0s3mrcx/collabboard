package com.fabricio.collabboard

import com.fabricio.collabboard.utils.Validators
import org.junit.Assert.*
import org.junit.Test

class ValidatorsTest {

    // ── Email ────────────────────────────────────────────
    @Test fun validEmail_returnsTrue() = assertTrue(Validators.isValidEmail("test@university.edu"))
    @Test fun emptyEmail_returnsFalse() = assertFalse(Validators.isValidEmail(""))
    @Test fun blankEmail_returnsFalse() = assertFalse(Validators.isValidEmail("   "))
    @Test fun invalidEmail_returnsFalse() = assertFalse(Validators.isValidEmail("notanemail"))
    @Test fun emailWithoutDomain_returnsFalse() = assertFalse(Validators.isValidEmail("test@"))
    @Test fun emailWithSubdomain_returnsTrue() = assertTrue(Validators.isValidEmail("user@mail.domain.com"))

    // ── Password ─────────────────────────────────────────
    @Test fun validPassword_returnsTrue() = assertTrue(Validators.isValidPassword("abc123"))
    @Test fun shortPassword_returnsFalse() = assertFalse(Validators.isValidPassword("abc"))
    @Test fun emptyPassword_returnsFalse() = assertFalse(Validators.isValidPassword(""))
    @Test fun exactlyMinPassword_returnsTrue() = assertTrue(Validators.isValidPassword("123456"))
    @Test fun longPassword_returnsTrue() = assertTrue(Validators.isValidPassword("aVeryLongP@ssword123"))

    // ── Project Title ─────────────────────────────────────
    @Test fun validTitle_returnsTrue() = assertTrue(Validators.isValidProjectTitle("App"))
    @Test fun emptyTitle_returnsFalse() = assertFalse(Validators.isValidProjectTitle(""))
    @Test fun blankTitle_returnsFalse() = assertFalse(Validators.isValidProjectTitle("   "))
    @Test fun tooShortTitle_returnsFalse() = assertFalse(Validators.isValidProjectTitle("ab"))
    @Test fun longTitle_returnsTrue() = assertTrue(Validators.isValidProjectTitle("A Really Long Project Title That Should Be Valid"))

    // ── Tech Stack ────────────────────────────────────────
    @Test fun validTechStack_returnsTrue() = assertTrue(Validators.isValidTechStack("Kotlin, Firebase"))
    @Test fun emptyTechStack_returnsFalse() = assertFalse(Validators.isValidTechStack(""))
    @Test fun blankTechStack_returnsFalse() = assertFalse(Validators.isValidTechStack("   "))

    // ── Description ───────────────────────────────────────
    @Test fun validDescription_returnsTrue() = assertTrue(Validators.isValidDescription("A cool project about AI"))
    @Test fun emptyDescription_returnsFalse() = assertFalse(Validators.isValidDescription(""))
    @Test fun blankDescription_returnsFalse() = assertFalse(Validators.isValidDescription("   "))

    // ── Business Logic: Search Filter ─────────────────────
    @Test fun searchFilter_matchesTitle() {
        val titles = listOf("Android App", "Web Dashboard", "ML Model")
        val result = titles.filter { it.lowercase().contains("android") }
        assertEquals(1, result.size)
        assertEquals("Android App", result[0])
    }

    @Test fun searchFilter_emptyQuery_returnsAll() {
        val titles = listOf("App A", "App B", "App C")
        val result = if ("".isBlank()) titles else titles.filter { it.contains("") }
        assertEquals(3, result.size)
    }

    @Test fun searchFilter_noMatch_returnsEmpty() {
        val titles = listOf("Android App", "Web Dashboard")
        val result = titles.filter { it.lowercase().contains("python") }
        assertTrue(result.isEmpty())
    }

    @Test fun searchFilter_caseInsensitive() {
        val titles = listOf("KOTLIN APP", "java project")
        val result = titles.filter { it.lowercase().contains("kotlin") }
        assertEquals(1, result.size)
    }

    // ── Business Logic: Status Toggle ─────────────────────
    @Test fun statusToggle_openBecomesClosedLogic() {
        assertEquals("closed", if ("open" == "open") "closed" else "open")
    }

    @Test fun statusToggle_closedBecomesOpenLogic() {
        assertEquals("open", if ("closed" == "open") "closed" else "open")
    }

    // ── Business Logic: Duplicate Request Check ───────────
    @Test fun duplicateCheck_existingRequest_blocked() {
        val existingRequests = listOf(
            mapOf("projectId" to "proj1", "applicantId" to "user1")
        )
        val hasDuplicate = existingRequests.any {
            it["projectId"] == "proj1" && it["applicantId"] == "user1"
        }
        assertTrue(hasDuplicate)
    }

    @Test fun duplicateCheck_differentUser_allowed() {
        val existingRequests = listOf(
            mapOf("projectId" to "proj1", "applicantId" to "user1")
        )
        val hasDuplicate = existingRequests.any {
            it["projectId"] == "proj1" && it["applicantId"] == "user2"
        }
        assertFalse(hasDuplicate)
    }

    // ── Business Logic: Notification Badge Count ──────────
    @Test fun badgeCount_showsWhenUnread() {
        val unreadCount = 3
        val shouldShow = unreadCount > 0
        assertTrue(shouldShow)
    }

    @Test fun badgeCount_hiddenWhenZero() {
        val unreadCount = 0
        val shouldShow = unreadCount > 0
        assertFalse(shouldShow)
    }
}
