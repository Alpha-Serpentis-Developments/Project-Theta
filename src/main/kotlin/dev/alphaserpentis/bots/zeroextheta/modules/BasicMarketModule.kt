package dev.alphaserpentis.bots.zeroextheta.modules

/**
 * Module that is able to poll various sources
 *
 * Polls Uniswap v2/v3, Chainlink, and Defillama with a given weight of 30/30/25/15
 */
class BasicMarketModule<in T : Any, out R : Any> {
    data class MarketPoll(
        var uniswapV2: Double = 0.0,
        var uniswapV3: Double = 0.0,
        var chainlink: Double = 0.0,
        var defillama: Double = 0.0,
        var lastUpdated: Long = 0
    )

    private fun pollUniswapV2Market(chainId: Long, tokenAddress: String) {
    }

    private fun pollUniswapV3Market(chainId: Long, tokenAddress: String) {
    }

    private fun pollChainlink(chainId: Long, tokenAddress: String) {
    }

    private fun pollDefiLlama(chainId: Long, tokenAddress: String) {
    }
}
