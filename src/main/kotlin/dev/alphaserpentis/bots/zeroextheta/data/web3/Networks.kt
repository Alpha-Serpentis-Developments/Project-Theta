package dev.alphaserpentis.bots.zeroextheta.data.web3

enum class Networks(
    val alchemy: String,
    val llamanode: String,
    val chainId: Long,
) {
    ETHEREUM("eth-mainnet", "eth", 1),
    ARBITRUM("arb-mainnet", "arbitrum", 42161),
    OPTIMISM("opt-mainnet", "optimism", 10),
    POLYGON("polygon-mainnet", "polygon", 137),
}
