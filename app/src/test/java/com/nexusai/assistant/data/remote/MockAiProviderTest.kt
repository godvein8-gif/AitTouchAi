package com.nexusai.assistant.data.remote

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class MockAiProviderTest {

    private val ai = MockAiProvider()

    @Test
    fun providerIsFlaggedAsMock() {
        assertTrue(ai.isMock)
    }

    @Test
    fun failCommandThrowsSoRetryCanBeTested() = runTest {
        var thrown = false
        try {
            ai.reply(emptyList(), "/fail")
        } catch (e: IOException) {
            thrown = true
        }
        assertTrue(thrown)
    }

    @Test
    fun hinglishInputGetsHinglishReply() = runTest {
        val reply = ai.reply(emptyList(), "mujhe madad chahiye")
        assertTrue(reply.contains("Namaste"))
    }

    @Test
    fun englishInputGetsEnglishReplyMarkedDemo() = runTest {
        val reply = ai.reply(emptyList(), "What is the weather like")
        assertTrue(reply.contains("DEMO"))
        assertTrue(!reply.contains("Namaste"))
    }
}
