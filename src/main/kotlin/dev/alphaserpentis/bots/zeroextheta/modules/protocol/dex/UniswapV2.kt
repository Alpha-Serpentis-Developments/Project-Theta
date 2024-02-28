package dev.alphaserpentis.bots.zeroextheta.modules.protocol.dex

import dev.alphaserpentis.bots.zeroextheta.modules.protocol.ProtocolInfo

object UniswapV2 : ProtocolInfo<Any, Any> {
    data class UniswapV2Data(
        val pairData: PairData? = null,
        val lpData: LPData? = null
    ) {
        data class PairData(
            val token0: String,
            val token1: String
        )

        data class LPData(
            val token0: String,
            val token1: String,
            var amount0: Double,
            var amount1: Double
        )
    }

    override fun getProtocolName() = "Uniswap v2"

    override fun getProtocolSlug() = "uniswap-v2"
}