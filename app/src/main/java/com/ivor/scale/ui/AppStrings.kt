package com.ivor.scale.ui

import androidx.compose.runtime.staticCompositionLocalOf

/** Supported in-app languages. Hinglish = romanized Hindi (the default). */
enum class Language(val displayName: String) {
    HINGLISH("Hinglish"),
    ENGLISH("English"),
}

/** All user-facing copy, swapped wholesale when the language changes. */
data class AppStrings(
    val homeRateWeightTitle: String,
    val homeRateWeightSubtitle: String,
    val homeSettingsTitle: String,
    val homeSettingsSubtitle: String,
    val rateWeightTitle: String,
    val rateWeightSubtitle: String,
    val tabRate: String,
    val tabWeight: String,
    val fieldPrice: String,
    val fieldAmount: String,
    val fieldKilo: String,
    val fieldGram: String,
    val fieldItemName: String,
    val resultWeight: String,
    val resultAmount: String,
    val hintWeight: String,
    val hintAmount: String,
    val errorPriceZero: String,
    val clear: String,
    val tipSharedPrice: String,
    val tapToCopy: String,
    val copiedPrefix: String,
    val back: String,
    val settingsTitle: String,
    val settingsSubtitle: String,
    val languageLabel: String,
    val openLabel: String,
    val add: String,
    val billHeader: String,
    val billTotal: String,
    val billClearAll: String,
)

private val HinglishStrings = AppStrings(
    homeRateWeightTitle = "Rate & Weight",
    homeRateWeightSubtitle = "Daam se vajan • vajan se daam",
    homeSettingsTitle = "Settings",
    homeSettingsSubtitle = "Bhasha aur app settings",
    rateWeightTitle = "Rate & Weight",
    rateWeightSubtitle = "Daam aur vajan ka turant hisaab",
    tabRate = "Rate",
    tabWeight = "Weight",
    fieldPrice = "Bhaav /kg",
    fieldAmount = "Rakam",
    fieldKilo = "Kilo",
    fieldGram = "Gram",
    fieldItemName = "Saman ka naam (optional)",
    resultWeight = "Result (vajan)",
    resultAmount = "Result (rakam)",
    hintWeight = "Bhaav aur rakam bharein",
    hintAmount = "Bhaav aur vajan bharein",
    errorPriceZero = "Bhaav 0 nahi ho sakta",
    clear = "Saaf karein",
    tipSharedPrice = "Tip: bhaav dono tab me ek saath rehta hai",
    tapToCopy = "Copy ke liye tap karein",
    copiedPrefix = "Copy ho gaya: ",
    back = "Wapas",
    settingsTitle = "Settings",
    settingsSubtitle = "App settings",
    languageLabel = "Bhasha",
    openLabel = "Kholein",
    add = "Jodein",
    billHeader = "List",
    billTotal = "Total",
    billClearAll = "Sab saaf",
)

private val EnglishStrings = AppStrings(
    homeRateWeightTitle = "Rate & Weight",
    homeRateWeightSubtitle = "Price → weight • weight → price",
    homeSettingsTitle = "Settings",
    homeSettingsSubtitle = "Language and app settings",
    rateWeightTitle = "Rate & Weight",
    rateWeightSubtitle = "Instant price & weight maths",
    tabRate = "Rate",
    tabWeight = "Weight",
    fieldPrice = "Price /kg",
    fieldAmount = "Amount",
    fieldKilo = "Kilos",
    fieldGram = "Grams",
    fieldItemName = "Item name (optional)",
    resultWeight = "Result (weight)",
    resultAmount = "Result (amount)",
    hintWeight = "Enter price and amount",
    hintAmount = "Enter price and weight",
    errorPriceZero = "Price can't be 0",
    clear = "Clear",
    tipSharedPrice = "Tip: price is shared across both tabs",
    tapToCopy = "Tap to copy",
    copiedPrefix = "Copied: ",
    back = "Back",
    settingsTitle = "Settings",
    settingsSubtitle = "App settings",
    languageLabel = "Language",
    openLabel = "Open",
    add = "Add",
    billHeader = "List",
    billTotal = "Total",
    billClearAll = "Clear all",
)

fun stringsFor(language: Language): AppStrings = when (language) {
    Language.HINGLISH -> HinglishStrings
    Language.ENGLISH -> EnglishStrings
}

/** Current language's strings, provided at the app root. */
val LocalStrings = staticCompositionLocalOf { HinglishStrings }
