package dev.alphaserpentis.bots.zeroextheta.data.web3.onchain

import java.math.BigInteger

/**
 * Simplified object for a transfer. This is used for cache and stored in the database.
 * @see dev.alphaserpentis.bots.zeroextheta.data.api.alchemy.response.GetAssetTransfers
 */
data class SimpleTransfer(
    val category: String,
    val blockNumber: BigInteger,
    val chainId: Long, // long or biginteger
    val from: String, // address
    val to: String, // address
    val token: String, // address
    val value: String, // uint256
)
