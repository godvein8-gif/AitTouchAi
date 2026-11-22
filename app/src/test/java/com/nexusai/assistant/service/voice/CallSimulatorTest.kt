package com.nexusai.assistant.service.voice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CallSimulatorTest {

    @Test
    fun everyCallStartsWithAiIntroducingItselfAsAi() {
        CallSimulator.scenarios.forEach { scenario ->
            val first = scenario.script.first()
            assertEquals(Speaker.Ai, first.speaker)
            assertTrue(first.text.contains("AI"))
            assertTrue(first.text.contains("NEXUS AI"))
        }
    }

    @Test
    fun aiNeverAsksForSecrets() {
        CallSimulator.scenarios
            .flatMap { it.script }
            .filter { it.speaker == Speaker.Ai }
            .forEach { line ->
                val text = line.text.lowercase()
                assertFalse(text.contains("otp"))
                assertFalse(text.contains("password"))
                assertFalse(text.contains("card number"))
            }
    }

    @Test
    fun everyScenarioHasSummaryAndLabelledAsDemo() {
        CallSimulator.scenarios.forEach { scenario ->
            assertTrue(scenario.summary.isNotBlank())
            assertTrue(scenario.callerLabel.startsWith("DEMO"))
        }
    }

    @Test
    fun scenarioIndexWrapsAround() {
        val n = CallSimulator.scenarios.size
        assertEquals(CallSimulator.scenario(0), CallSimulator.scenario(n))
    }
}
