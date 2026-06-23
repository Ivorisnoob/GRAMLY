package com.ivor.scale.data

import android.content.Context
import com.ivor.scale.domain.WeightUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/** Which tab a saved calculation came from. */
enum class CalcMode { WEIGHT, RATE }

/**
 * One saved calculation.
 *
 * Weight mode: [pricePerKg] + [inputValue] (money) -> [resultText] (weight).
 * Rate mode:   [pricePerKg] + [inputValue] (weight, in [inputUnit]) -> [resultText] (money).
 */
data class Calculation(
    val id: Long,
    val mode: CalcMode,
    val pricePerKg: String,
    val inputValue: String,
    val inputUnit: WeightUnit,
    val resultText: String,
    val timestamp: Long,
)

/**
 * History persisted as a small JSON array in SharedPreferences. Using org.json
 * (bundled with Android) keeps the app dependency-free for storage.
 */
class HistoryRepository(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("scale_history", Context.MODE_PRIVATE)

    private val _items = MutableStateFlow(load())
    val items: StateFlow<List<Calculation>> = _items.asStateFlow()

    fun add(item: Calculation) {
        // Newest first, cap the list so storage stays tiny.
        val next = (listOf(item) + _items.value).take(MAX_ITEMS)
        _items.value = next
        persist(next)
    }

    fun delete(id: Long) {
        val next = _items.value.filterNot { it.id == id }
        _items.value = next
        persist(next)
    }

    fun clear() {
        _items.value = emptyList()
        persist(emptyList())
    }

    private fun persist(items: List<Calculation>) {
        val arr = JSONArray()
        items.forEach { c ->
            arr.put(
                JSONObject().apply {
                    put("id", c.id)
                    put("mode", c.mode.name)
                    put("price", c.pricePerKg)
                    put("input", c.inputValue)
                    put("unit", c.inputUnit.name)
                    put("result", c.resultText)
                    put("ts", c.timestamp)
                }
            )
        }
        prefs.edit().putString(KEY, arr.toString()).apply()
    }

    private fun load(): List<Calculation> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    add(
                        Calculation(
                            id = o.getLong("id"),
                            mode = CalcMode.valueOf(o.getString("mode")),
                            pricePerKg = o.getString("price"),
                            inputValue = o.getString("input"),
                            inputUnit = WeightUnit.valueOf(
                                o.optString("unit", WeightUnit.GRAM.name)
                            ),
                            resultText = o.getString("result"),
                            timestamp = o.getLong("ts"),
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    companion object {
        private const val KEY = "items"
        private const val MAX_ITEMS = 50
    }
}
