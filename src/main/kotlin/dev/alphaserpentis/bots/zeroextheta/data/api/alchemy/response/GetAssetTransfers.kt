package dev.alphaserpentis.bots.zeroextheta.data.api.alchemy.response

/**
 * @see <a href="https://docs.alchemy.com/reference/alchemy-getassettransfers">Alchemy - getAssetTransfers</a>
 */
data class GetAssetTransfers(
    val id: Int,
    val jsonrpc: String,
    val result: Result
) {
    data class Result(
        val pageKey: String?,
        val transfers: List<Transfer>
    ) {
        data class Transfer(
            val category: String,
            val blockNum: String,
            val from: String,
            val to: String,
            val value: Double?,
            val erc1155Metadata: List<ERC1155Metadata>?,
            val tokenId: String,
            val asset: String?,
            val uniqueId: String,
            val hash: String,
            val rawContract: RawContract,
        ) {
            data class ERC1155Metadata(
                val tokenId: String,
                val value: String
            )

            data class RawContract(
                val value: String?,
                val address: String?,
                val decimal: String?
            )
        }
    }
}
