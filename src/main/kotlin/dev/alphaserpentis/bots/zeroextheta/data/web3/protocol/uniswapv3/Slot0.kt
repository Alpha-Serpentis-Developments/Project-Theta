package dev.alphaserpentis.bots.zeroextheta.data.web3.protocol.uniswapv3

import dev.alphaserpentis.bots.zeroextheta.modules.protocol.dex.UniswapV3.getSlot0
import java.math.BigInteger

/**
 * @see getSlot0
 */
data class Slot0(
    val sqrtPriceX96: BigInteger, // uint160
    val tick: Int, // int24
    val observationIndex: Int, // uint16
    val observationCardinality: Int, // uint16
    val observationCardinalityNext: Int, // uint16
    val feeProtocol: Int, // uint8
    val unlocked: Boolean // bool
)
