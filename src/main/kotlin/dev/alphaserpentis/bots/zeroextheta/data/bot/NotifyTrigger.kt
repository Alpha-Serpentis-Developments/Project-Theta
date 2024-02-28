package dev.alphaserpentis.bots.zeroextheta.data.bot

import com.google.gson.annotations.SerializedName

data class NotifyTrigger(
    val conditions: MutableList<Condition> = mutableListOf()
) {
    enum class NumericalOperators {
        @SerializedName("0") EQUALS,
        @SerializedName("1") GREATER_THAN,
        @SerializedName("2") LESS_THAN,
        @SerializedName("3") GREATER_THAN_OR_EQUAL_TO,
        @SerializedName("4") LESS_THAN_OR_EQUAL_TO
    }

    data class Condition(
        val name: String,
        val description: String = "No description",
        var value: String,
        var numOp: NumericalOperators
    )
}
