package dev.alphaserpentis.bots.zeroextheta.data.api.alchemy.request

import dev.alphaserpentis.bots.zeroextheta.data.api.alchemy.AlchemyEndpoints

/**
 * @see <a href="https://docs.alchemy.com/reference/alchemy-getassettransfers">Alchemy - getAssetTransfers</a>
 * @see [AlchemyEndpoints.getAssetTransfers]
 */
data class AssetTransfersBody(
    val id: Int = 1,
    val jsonrpc: String = "2.0",
    val method: String = "alchemy_getAssetTransfers",
    val params: List<Parameter>
) {
    data class Parameter(
        val fromBlock: String = "0x0",
        val toBlock: String = "latest",
        val fromAddress: String? = null,
        val toAddress: String? = null,
        val contractAddresses: List<String>? = null,
        val category: List<String> = listOf("external", "erc20", "erc721", "erc1155", "specialnft"),
        val pageKey: String? = null
    )
}
