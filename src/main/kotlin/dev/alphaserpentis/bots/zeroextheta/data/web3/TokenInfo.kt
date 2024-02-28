package dev.alphaserpentis.bots.zeroextheta.data.web3

import dev.alphaserpentis.bots.zeroextheta.handlers.ethereum.TokenHandler
import dev.alphaserpentis.bots.zeroextheta.launcher.Launcher
import dev.alphaserpentis.bots.zeroextheta.handlers.ethereum.DefiLlamaHandler
import java.math.BigInteger
import java.text.NumberFormat
import kotlin.math.pow

data class TokenInfo(
    val name: String,
    val symbol: String,
    val address: String,
    val chainId: Long?,
    val decimals: Int,
    var totalSupply: BigInteger,
    val trusted: Boolean = false,
    var averagedPrice: Double = 0.0,
    var lastUpdated: Long = 0
) {
    private companion object {
        private val tokenHandler: TokenHandler by lazy { Launcher.tokenHandler }
        private val nf: NumberFormat = NumberFormat.getInstance().apply { maximumFractionDigits = 2 }
    }

    fun updatePrice() {
        val dlInput: String = Networks.ETHEREUM.name.lowercase() + ":$address"
        // Temporary
        averagedPrice = DefiLlamaHandler.getPrice(dlInput).coins[dlInput]?.price ?: 0.0
        lastUpdated = System.currentTimeMillis() / 1000
    }

    fun getFormattedTotalSupply(): String = nf.format(totalSupply.toDouble() / 10.0.pow(18))

    /**
     * If the token is trusted, returns the token info from the trusted tokens file.
     */
    fun getTrustedTokenInfo(): TrustedTokens.Token? {
        if (trusted) {
            val (_, token) = tokenHandler.lookupTrustedToken(address = address, chainId = chainId) ?: return null
            return token.second
        } else {
            return null
        }
    }
}