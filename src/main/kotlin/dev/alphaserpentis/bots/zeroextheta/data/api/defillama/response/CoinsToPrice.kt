package dev.alphaserpentis.bots.zeroextheta.data.api.defillama.response

/**
 * Data class for coins.llama.fi/prices/current/{coin} endpoint
 *
 * @see DLEndpoints.getPrice
 */
data class CoinsToPrice(
    val coins: Map<String, Data> = mapOf()
) {
    data class Data(
        val decimals: Int?,
        val price: Double,
        val symbol: String,
        val timestamp: Long,
        val confidence: Double
    )
}
