package dev.alphaserpentis.bots.zeroextheta.data.api.defillama.response

import com.google.gson.annotations.SerializedName

/**
 * Data class for the /protocols endpoint of DeFiLlama.
 *
 * This is not the same data class used for /protocol/{protocol}.
 *
 * @see DLSpecificProtocol
 * @see DLEndpoints
 */
data class DLGeneralProtocol(
    val name: String,
    val address: String?,
    val symbol: String?,
    val url: String?,
    val description: String = "N/A",
    val chain: String = "N/A",
    val audits: Int = 0,
    @SerializedName("audit_note")
    val auditNote: String = "N/A",
    @SerializedName("gecko_id")
    val geckoId: String?,
    @SerializedName("cmc_id")
    val cmcId: String?,
    val category: String = "Unknown",
    val chains: List<String> = listOf(),
    val slug: String,
    val tvl: Double = 0.0,
    val chainTvls: Map<String, Double> = mapOf(),
    @SerializedName("change_1h")
    val change1h: Double?,
    @SerializedName("change_1d")
    val change1d: Double?,
    @SerializedName("change_7d")
    val change7d: Double?,
    val mcap: Double?,
)
