package com.nexusai.assistant.ui.util

fun maskedPhone(phone: String): String =
    if (phone.length > 6) phone.take(3) + "*****" + phone.takeLast(2) else phone
