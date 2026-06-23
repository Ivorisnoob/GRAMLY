package com.ivor.scale.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ivor.scale.data.CalcMode
import com.ivor.scale.data.Calculation
import com.ivor.scale.data.HistoryRepository
import com.ivor.scale.domain.CalcResult
import com.ivor.scale.domain.Calculator
import com.ivor.scale.domain.GoldCalculator
import com.ivor.scale.domain.GoldResult
import com.ivor.scale.domain.WeightUnit

/**
 * Holds all calculator state. `pricePerKg` is deliberately shared across both
 * tabs — entering the rate once carries over, which is how a real shopkeeper
 * works (one product, two questions).
 */
class ScaleViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = HistoryRepository(app)
    val history = repo.items

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
    fun clearAll() { pricePerKg = ""; amount = ""; rateKilos = ""; rateGrams = "" }

    // ---- Gold calculator -------------------------------------------------

    var goldPrice24 by mutableStateOf("")
        private set
    var goldKarat by mutableStateOf(22)
        private set
    var goldGrams by mutableStateOf("")
        private set
    var goldMilligrams by mutableStateOf("")
        private set
    var goldLabourPct by mutableStateOf("")
        private set
    var goldGstPct by mutableStateOf("")
        private set

    fun onGoldPriceChange(v: String) { goldPrice24 = sanitize(v) }
    fun onGoldKaratChange(k: Int) { goldKarat = k }
    fun onGoldGramsChange(v: String) { goldGrams = sanitize(v) }
    fun onGoldMilligramsChange(v: String) { goldMilligrams = sanitize(v) }
    fun onGoldLabourChange(v: String) { goldLabourPct = sanitize(v) }
    fun onGoldGstChange(v: String) { goldGstPct = sanitize(v) }

    val goldResult: GoldResult
        get() = GoldCalculator.result(
            price24Raw = goldPrice24,
            karat = goldKarat,
            gramsRaw = goldGrams,
            milligramsRaw = goldMilligrams,
            labourPctRaw = goldLabourPct,
            gstPctRaw = goldGstPct,
        )

    fun clearGold() {
        goldPrice24 = ""
        goldGrams = ""
        goldMilligrams = ""
        goldLabourPct = ""
        goldGstPct = ""
        goldKarat = 22
    }

    /** Save the current Weight-tab calculation if it produced a result. */
    fun saveWeight() {
        val res = weightResult as? CalcResult.Success ?: return
        repo.add(
            Calculation(
                id = System.currentTimeMillis(),
                mode = CalcMode.WEIGHT,
                pricePerKg = pricePerKg,
                inputValue = amount,
                inputUnit = WeightUnit.KILOGRAM, // not used for weight mode
                resultText = res.display,
                timestamp = System.currentTimeMillis(),
            )
        )
    }

    /** Save the current Rate-tab calculation if it produced a result. */
    fun saveRate() {
        val res = rateResult as? CalcResult.Success ?: return
        repo.add(
            Calculation(
                id = System.currentTimeMillis(),
                mode = CalcMode.RATE,
                pricePerKg = pricePerKg,
                inputValue = "${rateKilos.ifBlank { "0" }}kg ${rateGrams.ifBlank { "0" }}g",
                inputUnit = WeightUnit.KILOGRAM,
                resultText = res.display,
                timestamp = System.currentTimeMillis(),
            )
        )
    }

    fun deleteHistory(id: Long) = repo.delete(id)
    fun clearHistory() = repo.clear()

    /**
     * Re-populate the inputs from a saved item so the user can tweak it.
     * Returns the tab index the caller should switch to (0 = Weight, 1 = Rate).
     */
    fun refill(item: Calculation): Int {
        pricePerKg = item.pricePerKg
        return when (item.mode) {
            CalcMode.WEIGHT -> {
                amount = item.inputValue
                0
            }
            CalcMode.RATE -> {
                rateKilos = item.inputValue.substringBefore("kg").trim()
                rateGrams = item.inputValue.substringAfter("kg").removeSuffix("g").trim()
                1
            }
        }
    }

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
}
