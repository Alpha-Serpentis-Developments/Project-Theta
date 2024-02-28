package dev.alphaserpentis.bots.zeroextheta.handlers.ethereum

import dev.alphaserpentis.bots.zeroextheta.data.api.alchemy.response.GetAssetTransfers
import dev.alphaserpentis.bots.zeroextheta.data.bot.ThetaUserData
import dev.alphaserpentis.bots.zeroextheta.data.web3.onchain.SimpleTransfer
import dev.alphaserpentis.bots.zeroextheta.handlers.bot.ThetaDataHandler
import dev.alphaserpentis.bots.zeroextheta.modules.protocol.dex.UniswapV3
import dev.alphaserpentis.coffeecore.data.entity.EntityData
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.math.abs

/**
 * Handler for getting positions and balances for a given user and address
 */
class PortfolioHandler<T : EntityData>(
    private val tokenHandler: TokenHandler,
    private val dataHandler: ThetaDataHandler<T>,
) {
    private val zero = BigInteger.ZERO

    /**
     * Returns a list of addresses that the user is tracking
     * @param userId The user ID to get the addresses for
     * @return A list of addresses that the user is tracking (potentially empty)
     */
    fun getAddresses(userId: Long): List<String> {
        val userData: ThetaUserData = dataHandler.getEntityData("user", userId) as ThetaUserData

        return userData.addressesToTrack
    }

    /**
     * Gets all the tokens found in the given address
     * @param address The address to get the tokens for
     * @param block The block to get the tokens at (default: latest block)
     */
    fun getTokensInAddress(address: String, chainId: Long? = 1, block: Long? = null): List<String> {
        TODO()
    }

    /**
     * Returns all transfers (incoming and outgoing) for the given address
     * @param address The address to get the transfers for
     * @param chainIds The chain IDs to get the transfers for
     * @param category The category of the transfers to get (default: all)
     * @param fromBlock The block to start getting transfers from (default: genesis block)
     * @param toBlock The block to stop getting transfers at (default: latest block)
     * @param useCache Whether to use the cache for the transfers (default: true)
     * @param userId The user ID of who's requesting the transfers (default: -1 )
     * @return A list of all transfers for the given address (potentially empty) sorted by block number
     */
    fun getAllTransfers(
        address: String,
        chainIds: List<Long>,
        category: List<String> = listOf("external", "erc20", "erc721", "erc1155", "specialnft"),
        fromBlock: String = "0x0",
        toBlock: String = "latest",
        useCache: Boolean = true,
        userId: Long = -1,
    ): List<SimpleTransfer> {
        val workingList: MutableList<SimpleTransfer> = mutableListOf()
        var hasPageKey = true // default true to start the loop
        var incomingPageKey: String? = null
        var outgoingPageKey: String? = null

        for (chainId in chainIds) {
            val activeToBlock = if (toBlock == "latest") {
                convertBigIntegerToHex(RPCHandler.getWeb3jInstance(chainId).ethBlockNumber().send().blockNumber)
            } else { toBlock }

            while (hasPageKey) {
                // TODO: Add another variable to see if the address is exempted from the limit
                if (workingList.size > 69420)
                    throw IllegalStateException("Too many transfers to handle! Limit is 69,420 transfers (nice)")

                val incoming = AlchemyHandler.getAllTransfers(
                    to = address,
                    chainId = chainId,
                    category = category,
                    fromBlock = fromBlock,
                    toBlock = activeToBlock,
                    pageKey = incomingPageKey
                )
                val outgoing = AlchemyHandler.getAllTransfers(
                    from = address,
                    chainId = chainId,
                    category = category,
                    fromBlock = fromBlock,
                    toBlock = activeToBlock,
                    pageKey = outgoingPageKey
                )
                val convertedIncoming = convertAlchemyTransfers(chainId, incoming.result.transfers)
                val convertedOutgoing = convertAlchemyTransfers(chainId, outgoing.result.transfers)

                workingList.addAll(convertedIncoming)
                workingList.addAll(convertedOutgoing)

                incomingPageKey = incoming.result.pageKey
                outgoingPageKey = outgoing.result.pageKey

                hasPageKey = incoming.result.pageKey != null || outgoing.result.pageKey != null
            }
        }

        return workingList.sortedBy { it.blockNumber }
    }

    /**
     * Converts the Alchemy transfers to a list of simplified transfers
     * @param chainId The chain ID of the transfers
     * @param transfers The transfers to convert
     * @param cacheResults Whether to cache the results (default: true)
     * @return A list of simplified transfers
     */
    fun convertAlchemyTransfers(
        chainId: Long,
        transfers: List<GetAssetTransfers.Result.Transfer>,
        cacheResults: Boolean = true
    ): List<SimpleTransfer> {
        val convertedList = transfers
            .filter {
                it.rawContract.value != null && it.rawContract.value.substring(2).toBigInteger(16) > BigInteger.ZERO
            }
            .map {
                SimpleTransfer(
                    it.category,
                    it.blockNum.substring(2).toBigInteger(16),
                    chainId = chainId,
                    it.from,
                    it.to,
                    it.rawContract.address ?: "0x0000000000000000000000000000000000000000",
                    it.rawContract.value?.substring(2) ?: "0"
                )
            }

        if (cacheResults) {

        }

        return convertedList
    }

    /**
     * Calculates the PNL of all the ERC20 tokens in the given address, provided the price can be discovered. This uses
     * LIFO rules to calculate the PNL.
     */
    fun calculatePnl(
        address: String,
        transfers: List<SimpleTransfer>
    ): List<Pair<String, Double>> {
        if (transfers.isEmpty()) {
            return emptyList()
        } else {
            val workingList = transfers.filter { it.category == "erc20" }
            val addressToMappedTokens: MutableMap<String, MutableMap<BigInteger, BigInteger>> = mutableMapOf()
            val pnlMap: MutableMap<String, Double> = transfers.associate { it.token to 0.0 }.toMutableMap()

            for (transfer in workingList) {
                val blockToToken = addressToMappedTokens.getOrPut(transfer.token) { mutableMapOf() }

                if (transfer.from.equals(address, true)) { // Tokens are LEAVING the address
                    if (blockToToken.isEmpty()) continue

                    var lowestBlock = blockToToken.keys.minOf { it }
                    var value = blockToToken[lowestBlock]!! - transfer.value.toBigInteger(16)

                    // If the value is negative, this block needs to be removed and subtract the
                    // next lowest block's value. Repeat as necessary
                    while (value < zero) {
                        blockToToken.remove(lowestBlock)

                        if (blockToToken.isEmpty()) break // Account is completely empty

                        lowestBlock = blockToToken.keys.minOf { it }
                        value = blockToToken[lowestBlock]!! - value.abs()
                    }

                    if (value == zero) {
                        blockToToken.remove(lowestBlock)
                    } else {
                        blockToToken[lowestBlock] = value
                    }
                } else { // Tokens are ENTERING the address
                    blockToToken[transfer.blockNumber] = transfer.value.toBigInteger(16)
                }
            }

            for (tokenEntry in addressToMappedTokens) {
                // Calculate the cost basis
                for (block in tokenEntry.value.keys) {
                    val token = tokenEntry.key
                    val priceAtBlock = getPriceOfToken(tokenToTrade = token, chainId = 1, block = block.toLong())
                    val costBasis = (tokenEntry.value[block]!!.toBigDecimal() * priceAtBlock.toBigDecimal()) / BigDecimal.TEN.pow(18)

                    pnlMap[token] = ((pnlMap[token]!!.toBigDecimal() + costBasis)).toDouble()
                }

                // Calculate the value now
                val priceNow = getPriceOfToken(tokenToTrade = tokenEntry.key, chainId = 1)
                val totalAmount = tokenEntry.value.values.sumOf { it.toBigDecimal() } / BigDecimal.TEN.pow(18)
                val diff = (totalAmount * priceNow.toBigDecimal()) - pnlMap[tokenEntry.key]!!.toBigDecimal()

                pnlMap[tokenEntry.key] = (diff).toDouble()

                println(pnlMap[tokenEntry.key])
            }

            return pnlMap.toList()
        }
    }

    /**
     * Gets the price of a token in terms of another token
     * @param tokenToTrade The token to get the price of
     * @param tokenToTradeAgainst The token to trade against (default: USDC)
     * @param chainId The chain ID to get the price on
     * @param block The block to get the price at (default: latest block)'
     * @return The price of the token in terms of the other token
     */
    fun getPriceOfToken(
        tokenToTrade: String,
        tokenToTradeAgainst: String = "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48",
        chainId: Long,
        block: Long? = null
    ): Double {
        val pools = UniswapV3.getAllPools(tokenToTrade, tokenToTradeAgainst, block)
        val token0Token1 = findToken0Token1(tokenToTrade, tokenToTradeAgainst)

        if (pools.isEmpty()) {
            return 0.0
        }

        val slot0OfPools = pools.map { UniswapV3.getSlot0(it, block = block) }
        val liquidityOfPools = pools.map { UniswapV3.getLiquidity(it, block = block) }
        val prices = slot0OfPools.map {
            UniswapV3.convertSqrtPriceX96ToPrice(
                it.sqrtPriceX96,
                tokenHandler.getDecimals(token0Token1.first, chainId).toInt(),
                tokenHandler.getDecimals(token0Token1.second, chainId).toInt()
            ).toDouble()
        }.toMutableList()
        val filteredPools = filterBadPools(pools, liquidityOfPools, prices)
        val filteredPrices = filteredPools.map {
            UniswapV3.convertSqrtPriceX96ToPrice(
                UniswapV3.getSlot0(it, block = block).sqrtPriceX96,
                tokenHandler.getDecimals(token0Token1.first, chainId).toInt(),
                tokenHandler.getDecimals(token0Token1.second, chainId).toInt()
            ).toDouble()
        }

        return filteredPrices.average()

//        val usdc = "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48"
//        val usdcWethPool03 = "0x8ad599c3a0ff1de082011efddc58f1908eb6e6d8"
//        var pools = when (tokenToTrade) {
//            "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2" -> listOf(usdcWethPool03)
//            "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48" -> listOf(usdcWethPool03)
//            else -> UniswapV3.getAllPools(tokenToTrade, weth, block)
//        }
//        var token0Token1 = findToken0Token1(tokenToTrade, weth)
//        var isInETHTerms = true
//
//        if (pools.isEmpty()) {
//            pools = UniswapV3.getAllPools(tokenToTrade, usdc, block)
//
//            if (pools.isEmpty()) return 0.0
//
//            token0Token1 = findToken0Token1(tokenToTrade, usdc)
//            isInETHTerms = false
//        }
//        val slot0OfPools = pools.map { UniswapV3.getSlot0(it) }
//        val prices = slot0OfPools.map {
//            UniswapV3.convertSqrtPriceX96ToPrice(
//                it.sqrtPriceX96,
//                tokenHandler.getDecimals(token0Token1.first, chainId).toInt(),
//                tokenHandler.getDecimals(token0Token1.second, chainId).toInt()
//            ).toDouble()
//        }.toMutableList()
//
//        return prices.average()
    }

    fun addAddress(address: String, userId: Long) {
        val userData: ThetaUserData = dataHandler.getEntityData("user", userId) as ThetaUserData

        userData.addressesToTrack.add(address)

        dataHandler.updateEntityData()
    }

    fun removeAddress(address: String, userId: Long) {
        val userData: ThetaUserData = dataHandler.getEntityData("user", userId) as ThetaUserData

        userData.addressesToTrack.remove(address)

        dataHandler.updateEntityData()
    }

    private fun convertIntToHex(int: Int): String {
        return "0x${int.toString(16)}"
    }

    private fun convertBigIntegerToHex(bigInt: BigInteger): String {
        return "0x${bigInt.toString(16)}"
    }

    /**
     * Uniswap handles token0 and token1 in alphabetical order between the two addresses. This function will return
     * the two tokens in alphabetical order.
     * @param tokenA The first token
     * @param tokenB The second token
     * @return A pair of the two tokens in alphabetical order
     */
    private fun findToken0Token1(tokenA: String, tokenB: String): Pair<String, String> {
        val sorted = listOf(tokenA, tokenB).sortedBy { it }
        return Pair(sorted[0], sorted[1])
    }

    /**
     * Filters out bad pools based on liquidity and price deviation
     * @param pools The pools to filter
     * @param liquidity The liquidity of the pools
     * @param prices The prices of the pools
     * @param knownPrice The known RELIABLE price of the token, if any (default: null)
     * @return A list of pools that are considered "good"
     */
    private fun filterBadPools(
        pools: List<String>,
        liquidity: List<BigInteger>,
        prices: List<Double>,
        knownPrice: Double? = null
    ): List<String> {
        return if (pools.size <= 1)
            pools
        else if (pools.size == 2) { // If there are only two pools, obtain the one with the highest liquidity
            if (liquidity[0] > liquidity[1]) {
                listOf(pools[0])
            } else {
                listOf(pools[1])
            }
        } else {
            // Filter by liquidity threshold
            val liquidityThreshold = BigInteger("1000") // Example threshold
            val filteredByLiquidity = pools.indices.filter { liquidity[it] > liquidityThreshold }

            // Calculate median price of filtered pools
            val medianPrice = filteredByLiquidity.map { prices[it] }.sorted()[filteredByLiquidity.size / 2]

            // Define acceptable price deviation (e.g., 10%)
            val priceDeviationThreshold = 0.1

            // Filter by price deviation
            val filteredPools = filteredByLiquidity.filter {
                val priceDeviation = abs(prices[it] - medianPrice) / medianPrice
                priceDeviation <= priceDeviationThreshold
            }.map { pools[it] }

            return filteredPools
        }
    }
}