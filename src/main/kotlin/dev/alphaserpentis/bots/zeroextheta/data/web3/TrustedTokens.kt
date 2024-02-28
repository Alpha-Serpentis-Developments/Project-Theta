package dev.alphaserpentis.bots.zeroextheta.data.web3

data class TrustedTokens(
    val chainToToken: Map<Long, Map<String, Token>> = mapOf()
) {
    data class Token(
        val name: String,
        val symbol: String,
        val decimals: Int,
        val protocolSlug: String?,
        val equivalent: List<String>?,
        val logoUrl: String?
    )
}
