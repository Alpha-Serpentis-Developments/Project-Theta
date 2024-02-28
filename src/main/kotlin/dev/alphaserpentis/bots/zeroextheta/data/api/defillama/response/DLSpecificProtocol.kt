package dev.alphaserpentis.bots.zeroextheta.data.api.defillama.response

import com.google.gson.annotations.SerializedName

/**
 * Data class for the /protocol/{protocol} endpoint of DeFiLlama.
 *
 * This is not the same data class used for /protocols.
 *
 * @see DLGeneralProtocol
 * @see DLEndpoints
 */
data class DLSpecificProtocol(
    val name: String,
    val address: String?,
    val symbol: String?,
    val url: String?,
    val description: String = "No description",
    val logo: String?,
    @SerializedName("gecko_id")
    val geckoId: String?,
    @SerializedName("cmc_id")
    val cmcId: String?,
    val category: String = "Unknown",
    val chains: List<String> = listOf(),
    val twitter: String?,
    val currentChainTvls: Map<String, Double> = mapOf(),
    val otherProtocols: List<String>?
)
