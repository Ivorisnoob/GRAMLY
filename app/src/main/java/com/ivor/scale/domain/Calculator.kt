package com.ivor.scale.domain

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToLong

/**
 * Pure calculation + formatting logic for Scale. No Android dependencies, so it
 * is trivially unit-testable and reused by both tabs.
 *
 * Two inverse operations share one input (price per kg):
 *   Weight tab: weightKg = amount / pricePerKg
 *   Rate  tab: amount   = pricePerKg * weightKg
 */
object Calculator {

    /** Parse a user-typed number; blank / malformed / negative -> null. */
    fun parse(raw: String): Double? {
        val v = raw.trim().replace(",", "").toDoubleOrNull() ?: return null
        return if (v < 0 || v.isNaN() || v.isInfinite()) null else v
    }

    // ---- Weight tab: money -> weight -------------------------------------

    fun weightResult(pricePerKgRaw: String, amountRaw: String): CalcResult {
        val price = parse(pricePerKgRaw)
        val amount = parse(amountRaw)
        if (price == null && amount == null) return CalcResult.Empty
        if (price == null || amount == null) return CalcResult.Empty
        if (price == 0.0) return CalcResult.Error("Bhaav 0 nahi ho sakta")
        val weightKg = amount / price
        return CalcResult.Success(
            display = formatWeight(weightKg),
            plain = formatWeightPlain(weightKg),
        )
    }

    // ---- Rate tab: weight (kg + g) -> money ------------------------------

    fun rateResult(pricePerKgRaw: String, kilosRaw: String, gramsRaw: String): CalcResult {
        val price = parse(pricePerKgRaw) ?: return CalcResult.Empty
        // Need at least one of the weight fields before there's an answer.
        if (kilosRaw.isBlank() && gramsRaw.isBlank()) return CalcResult.Empty
        val kilos = parse(kilosRaw) ?: 0.0
        val grams = parse(gramsRaw) ?: 0.0
        val weightKg = kilos + grams / 1000.0
        if (weightKg <= 0.0) return CalcResult.Empty
        val amount = price * weightKg
        return CalcResult.Success(
            display = formatRupees(amount),
            plain = formatRupeesPlain(amount),
        )
    }

    // ---- Formatting ------------------------------------------------------

    private val rupeeFormat: NumberFormat =
        NumberFormat.getNumberInstance(Locale.forLanguageTag("en-IN")).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 0
        }

    private val rupeeFormat2: NumberFormat =
        NumberFormat.getNumberInstance(Locale.forLanguageTag("en-IN")).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 2
        }

    /** Always two decimals — used for money breakdowns (₹50.84, ₹254.40). */
    fun formatRupees2(amount: Double): String = "₹" + rupeeFormat2.format(amount)

    /** 0.5 -> "500 g", 1.25 -> "1 kg 250 g", 2.0 -> "2 kg". */
    fun formatWeight(kg: Double): String {
        val totalGrams = (kg * 1000.0).roundToLong()
        if (totalGrams <= 0L) return "0 g"
        val kgPart = totalGrams / 1000L
        val gPart = totalGrams % 1000L
        return when {
            kgPart == 0L -> "$gPart g"
            gPart == 0L -> "$kgPart kg"
            else -> "$kgPart kg $gPart g"
        }
    }

    private fun formatWeightPlain(kg: Double): String {
        val totalGrams = (kg * 1000.0).roundToLong()
        return "$totalGrams g"
    }

    /** 20.0 -> "₹20", 20.5 -> "₹20.5", 1234.56 -> "₹1,234.56" (Indian grouping). */
    fun formatRupees(amount: Double): String = "₹" + rupeeFormat.format(amount)

    private fun formatRupeesPlain(amount: Double): String {
        val rounded = (amount * 100.0).roundToLong() / 100.0
        return if (rounded == rounded.toLong().toDouble()) {
            rounded.toLong().toString()
        } else {
            rounded.toString()
        }
    }
}

enum class WeightUnit(val label: String) { GRAM("g"), KILOGRAM("kg") }

/** Outcome of a live calculation. */
sealed interface CalcResult {
    /** Not enough input yet — show a friendly hint, never "NaN". */
    data object Empty : CalcResult

    /** Invalid input (e.g. price 0) — show [message]. */
    data class Error(val message: String) : CalcResult

    /** A computed answer. [display] is formatted for the hero card; [plain] for clipboard. */
    data class Success(val display: String, val plain: String) : CalcResult
}
