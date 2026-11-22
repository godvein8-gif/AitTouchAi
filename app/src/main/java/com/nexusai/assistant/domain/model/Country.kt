package com.nexusai.assistant.domain.model

data class Country(val name: String, val code: String)

val SupportedCountries = listOf(
    Country("India", "+91"),
    Country("United States / Canada", "+1"),
    Country("United Kingdom", "+44"),
    Country("UAE", "+971"),
    Country("Singapore", "+65"),
    Country("Australia", "+61")
)
