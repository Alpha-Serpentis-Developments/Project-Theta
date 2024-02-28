package dev.alphaserpentis.bots.zeroextheta.data.api.alchemy.request

data class TokenMetadataBody(
    val id: Int = 1,
    val jsonrpc: String = "2.0",
    val method: String = "alchemy_getTokenMetadata",
    val params: List<String>
)