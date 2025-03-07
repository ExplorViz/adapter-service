package net.explorviz.adapter.service.converter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DefaultAttributeValueTest {

    @Test
    fun testDefaultTokenValue() {
        assertEquals(DefaultAttributeValues.DEFAULT_LANDSCAPE_TOKEN, "mytokenvalue")
    }

    @Test
    fun testDefaultTokenSecret() {
        assertEquals(DefaultAttributeValues.DEFAULT_LANDSCAPE_SECRET, "mytokensecret")
    }

    @Test
    fun testDefaultAppName() {
        assertEquals(DefaultAttributeValues.DEFAULT_APP_NAME, "UNKNOWN-APPLICATION")
    }
}
