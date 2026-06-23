package com.ivor.scale.domain

/**
 * Jewellery price breakdown.
 *
 *   karatPrice/g = price24/g × (karat / 24)
 *   goldValue    = karatPrice/g × weightGrams       (weightGrams = grams + mg/1000)
 *   labour       = goldValue × labour%
 *   gst          = (goldValue + labour) × gst%
 *   final        = goldValue + labour + gst
 */
object GoldCalculator {

    /** Common purities offered in the carat selector (highest first). */
    val KARATS = listOf(24, 23, 22, 21, 20, 18, 16, 14, 12, 10)

    fun result(
        price24Raw: String,
        karat: Int,
        gramsRaw: String,
        milligramsRaw: String,
        labourPctRaw: String,
        gstPctRaw: String,
    ): GoldResult {
        val price24 = Calculator.parse(price24Raw)
        val grams = Calculator.parse(gramsRaw) ?: 0.0
        val milligrams = Calculator.parse(milligramsRaw) ?: 0.0
        val weightGrams = grams + milligrams / 1000.0

        // Need a price and some weight before there is anything to show.
        if (price24 == null || price24 == 0.0 || weightGrams <= 0.0) return GoldResult.Empty

        val labourPct = Calculator.parse(labourPctRaw) ?: 0.0
        val gstPct = Calculator.parse(gstPctRaw) ?: 0.0

        val karatPricePerGram = price24 * (karat / 24.0)
        val goldValue = karatPricePerGram * weightGrams
        val labour = goldValue * labourPct / 100.0
        val gst = (goldValue + labour) * gstPct / 100.0
        val finalAmount = goldValue + labour + gst

        val weightLabel = formatWeightGrams(weightGrams)
        val rows = buildList {
            add(GoldRow("${karat}K Gold Price/gram", Calculator.formatRupees2(karatPricePerGram)))
            add(GoldRow("Gold (${weightLabel}g)", Calculator.formatRupees2(goldValue)))
            if (labour > 0.0) {
                add(GoldRow("Labour (${trimPct(labourPct)}%)", Calculator.formatRupees2(labour)))
            }
            if (gst > 0.0) {
                add(GoldRow("GST (${trimPct(gstPct)}%)", Calculator.formatRupees2(gst)))
            }
        }

        return GoldResult.Success(
            rows = rows,
            finalAmount = Calculator.formatRupees2(finalAmount),
        )
    }

    /** 5.005 -> "5.005", 5.0 -> "5" (drops needless trailing zeros). */
    private fun formatWeightGrams(g: Double): String {
        val rounded = Math.round(g * 1000.0) / 1000.0
        return if (rounded == rounded.toLong().toDouble()) {
            rounded.toLong().toString()
        } else {
            rounded.toString().trimEnd('0').trimEnd('.')
        }
    }

    private fun trimPct(p: Double): String =
        if (p == p.toLong().toDouble()) p.toLong().toString() else p.toString()
}

data class GoldRow(val label: String, val value: String)

sealed interface GoldResult {
    /** Not enough input yet. */
    data object Empty : GoldResult

    /** A computed breakdown: line items plus the final payable amount. */
    data class Success(val rows: List<GoldRow>, val finalAmount: String) : GoldResult
}
