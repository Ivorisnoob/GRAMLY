package com.ivor.scale.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.ivor.scale.domain.CalcResult
import com.ivor.scale.domain.Calculator

/**
 * Holds calculator state plus the app language. `pricePerKg` is deliberately
 * shared across both tabs — entering the rate once carries over, which is how a
 * real shopkeeper works (one product, two questions).
 */
class ScaleViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("scale_settings", Context.MODE_PRIVATE)

    // Shared across tabs.
    var pricePerKg by mutableStateOf("")
        private set

    // Weight tab.
    var amount by mutableStateOf("")
        private set

    // Rate tab — weight entered as whole kilos + grams (no unit conversion needed).
    var rateKilos by mutableStateOf("")
        private set
    var rateGrams by mutableStateOf("")
        private set

    // App language (persisted across launches).
    var language by mutableStateOf(loadLanguage())
        private set

    fun onPriceChange(v: String) { pricePerKg = sanitize(v) }
    fun onAmountChange(v: String) { amount = sanitize(v) }
    fun onRateKilosChange(v: String) { rateKilos = sanitize(v) }
    fun onRateGramsChange(v: String) { rateGrams = sanitize(v) }

    val weightResult: CalcResult
        get() = Calculator.weightResult(pricePerKg, amount)

    val rateResult: CalcResult
        get() = Calculator.rateResult(pricePerKg, rateKilos, rateGrams)

    fun clearWeightTab() { amount = "" }
    fun clearRateTab() { rateKilos = ""; rateGrams = "" }

    fun changeLanguage(lang: Language) {
        language = lang
        prefs.edit().putString(KEY_LANGUAGE, lang.name).apply()
    }

    private fun loadLanguage(): Language = runCatching {
        Language.valueOf(prefs.getString(KEY_LANGUAGE, Language.HINGLISH.name)!!)
    }.getOrDefault(Language.HINGLISH)

    /** Keep only digits and a single decimal point; trim absurd lengths. */
    private fun sanitize(input: String): String {
        val filtered = buildString {
            var dotSeen = false
            for (ch in input) {
                when {
                    ch.isDigit() -> append(ch)
                    (ch == '.' || ch == ',') && !dotSeen -> { append('.'); dotSeen = true }
                }
            }
        }
        return filtered.take(12)
    }

    companion object {
        private const val KEY_LANGUAGE = "language"
    }
}
