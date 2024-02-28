package dev.alphaserpentis.bots.zeroextheta.data.api.alchemy.response

data class GetTokenMetadata(
    val id: Int,
    val jsonrpc: String,
    val result: Result
) {
    data class Result(
        val name: String?,
        val symbol: String?,
        val decimals: Int?,
        val logo: String?
    )
}
