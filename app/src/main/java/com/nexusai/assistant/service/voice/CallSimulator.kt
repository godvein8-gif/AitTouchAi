package com.nexusai.assistant.service.voice

enum class Speaker { Ai, Caller }

data class TranscriptLine(val speaker: Speaker, val text: String)

data class CallScenario(
    val callerLabel: String,
    val script: List<TranscriptLine>,
    val summary: String,
    val callbackMessage: String?
)

/**
 * DEMO ONLY. These are fictional scripted calls. No real caller, no real audio,
 * no cloud provider is involved. Real calls arrive in Phase 4.
 */
object CallSimulator {

    private const val INTRO_HI =
        "Namaste! Main NEXUS AI hoon, is number ke owner ka AI assistant. " +
            "Ye baat ek AI se ho rahi hai. Bataiye, main kaise madad kar sakta hoon?"

    private const val INTRO_EN =
        "Hello! This is NEXUS AI, an AI assistant for the owner of this number. " +
            "How can I help you?"

    val scenarios: List<CallScenario> = listOf(
        CallScenario(
            callerLabel = "DEMO Caller: Courier",
            script = listOf(
                TranscriptLine(Speaker.Ai, INTRO_HI),
                TranscriptLine(Speaker.Caller, "Hello, main courier se bol raha hoon. Ek parcel aaya hai, kab deliver karun?"),
                TranscriptLine(Speaker.Ai, "Main owner ki taraf se koi time confirm nahi kar sakta, lekin aapka message pahuncha dunga. Aapka naam aur parcel ki detail bata sakte hain?"),
                TranscriptLine(Speaker.Caller, "Naam Ravi hai. Parcel kal subah tak dena hai."),
                TranscriptLine(Speaker.Ai, "Dhanyavaad Ravi ji. Maine message note kar liya hai, owner aapse sampark karenge. Aur kuch?"),
                TranscriptLine(Speaker.Caller, "Nahi, bas itna hi.")
            ),
            summary = "Courier ne parcel delivery ke baare mein poocha. AI ne koi time confirm nahi kiya aur message le liya.",
            callbackMessage = "Ravi (courier): parcel kal subah tak deliver karna hai. Please call back."
        ),
        CallScenario(
            callerLabel = "DEMO Caller: Meeting request",
            script = listOf(
                TranscriptLine(Speaker.Ai, INTRO_EN),
                TranscriptLine(Speaker.Caller, "Hi, I'd like to schedule a meeting with the owner tomorrow at 10."),
                TranscriptLine(Speaker.Ai, "I can't confirm any meeting on the owner's behalf, but I can take a message. May I have your name and what it is about?"),
                TranscriptLine(Speaker.Caller, "Priya from Acme Design. It's about the website project."),
                TranscriptLine(Speaker.Ai, "Thanks Priya. I've noted that and the owner will get back to you. Anything else?"),
                TranscriptLine(Speaker.Caller, "No, that's all. Thanks.")
            ),
            summary = "Caller asked to schedule a meeting. AI did not confirm any time and took a message.",
            callbackMessage = "Priya (Acme Design): wants a meeting tomorrow at 10 about the website project. Please call back."
        ),
        CallScenario(
            callerLabel = "DEMO Caller: Personal info request",
            script = listOf(
                TranscriptLine(Speaker.Ai, INTRO_EN),
                TranscriptLine(Speaker.Caller, "Can you tell me the owner's home address? I want to send a gift."),
                TranscriptLine(Speaker.Ai, "I'm sorry, I can't share any personal information. I can pass along a message and the owner can decide whether to reply."),
                TranscriptLine(Speaker.Caller, "Okay, tell them Sunil from the neighbourhood called."),
                TranscriptLine(Speaker.Ai, "Done. I've noted that Sunil called. Is there anything else?"),
                TranscriptLine(Speaker.Caller, "No thanks.")
            ),
            summary = "Caller asked for the owner's address. AI declined to share private information and took a message.",
            callbackMessage = "Sunil called and would like you to get in touch."
        )
    )

    fun scenario(index: Int): CallScenario = scenarios[index % scenarios.size]
}
