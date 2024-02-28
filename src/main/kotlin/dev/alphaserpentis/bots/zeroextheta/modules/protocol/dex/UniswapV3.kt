package dev.alphaserpentis.bots.zeroextheta.modules.protocol.dex

import dev.alphaserpentis.bots.zeroextheta.data.web3.Networks
import dev.alphaserpentis.bots.zeroextheta.data.web3.protocol.uniswapv3.Slot0
import dev.alphaserpentis.bots.zeroextheta.handlers.ethereum.RPCHandler
import dev.alphaserpentis.bots.zeroextheta.modules.protocol.ProtocolInfo
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Bool
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Int24
import org.web3j.abi.datatypes.generated.Uint128
import org.web3j.abi.datatypes.generated.Uint16
import org.web3j.abi.datatypes.generated.Uint160
import org.web3j.abi.datatypes.generated.Uint24
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.abi.datatypes.generated.Uint8
import org.web3j.abi.datatypes.generated.Uint96
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.sqrt

object UniswapV3 : ProtocolInfo<Any, Any> {
    private val ZERO = BigInteger.ZERO
    private val ONE = BigInteger.ONE
    private val TWO_POWER_96 = BigInteger.TWO.pow(96).toBigDecimal()
    private val UINT256_MAX = BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16)

    /**
     * Address of the Quoter contract. Valid for mainnet, Arbitrum, Optimism, and Polygon
     *
     * @see <a href="https://etherscan.io/address/0xb27308f9F90D607463bb33eA1BeBb41C27CE5AB6">Etherscan - Uniswap V3 Quoter</a>
     */
    val quoter: String = "0xb27308f9F90D607463bb33eA1BeBb41C27CE5AB6"
    /**
     * Address of the UniswapV3Factory contract. Valid for mainnet, Arbitrum, Optimism, and Polygon
     *
     * @see <a href="https://etherscan.io/address/0x1F98431c8aD98523631AE4a59f267346ea31F984">Etherscan - Uniswap V3 Factory</a>
     */
    val factory: String = "0x1F98431c8aD98523631AE4a59f267346ea31F984"
    /**
     * Address of the NFTPositionManager contract. Valid for mainnet, Arbitrum, Optimism, and Polygon
     *
     * @see <a href="https://etherscan.io/address/0xC36442b4a4522E871399CD717aBDD847Ab11FE88">Etherscan - NonfungiblePositionManager</a>
     */
    val nftPositionManager: String = "0xC36442b4a4522E871399CD717aBDD847Ab11FE88"

    /**
     * @see getPositionsDataFromNft
     */
    data class PositionsResponse(
        val nonce: BigInteger, // uint96
        val operator: String, // address
        val token0: String, // address
        val token1: String, // address
        val fee: Int, // uint24
        val tickLower: Int, // int24
        val tickUpper: Int, // int24
        val liquidity: BigInteger, // uint128
        val feeGrowthInside0LastX128: BigInteger, // uint256 TODO: Determine if needed
        val feeGrowthInside1LastX128: BigInteger, // uint256 TODO: Determine if needed
        val tokensOwed0: BigInteger, // uint128
        val tokensOwed1: BigInteger // uint128
    )

    enum class FeeTiers(val fee: Long) {
        ZERO_DOT_ZERO_ONE(100),
        ZERO_DOT_ZERO_FIVE(500),
        ZERO_DOT_THREE(3000),
        ONE(10000);
    }

    /**
     * A Kotlin-equivalent of the TickMath contract
     * @see <a href="https://github.com/Uniswap/v3-core/blob/main/contracts/libraries/TickMath.sol">TickMath.sol</a>
     */
    object TickMath {
        private val MAX_TICK = 887272

        /**
         * Calculates the sqrtRatioX96 for a given tick
         * @param tick The tick to calculate the sqrtRatioX96 for
         * @return The sqrtRatioX96
         */
        fun getSqrtRatioAtTick(tick: Int): BigInteger {
            val absTick = if (tick < 0) -tick else tick
            if (absTick > MAX_TICK) throw IllegalArgumentException("Tick provided is too high (max is $MAX_TICK)")
            var ratio = if (absTick and 0x1 != 0) {
                BigInteger("fffcb933bd6fad37aa2d162d1a594001", 16)
            } else {
                BigInteger("100000000000000000000000000000000", 16)
            }

            if (absTick and 0x2 != 0) ratio = (ratio * BigInteger("fff97272373d413259a46990580e213a", 16)) shr 128
            if (absTick and 0x4 != 0) ratio = (ratio * BigInteger("fff2e50f5f656932ef12357cf3c7fdcc", 16)) shr 128
            if (absTick and 0x8 != 0) ratio = (ratio * BigInteger("ffe5caca7e10e4e61c3624eaa0941cd0", 16)) shr 128
            if (absTick and 0x10 != 0) ratio = (ratio * BigInteger("ffcb9843d60f6159c9db58835c926644", 16)) shr 128
            if (absTick and 0x20 != 0) ratio = (ratio * BigInteger("ff973b41fa98c081472e6896dfb254c0", 16)) shr 128
            if (absTick and 0x40 != 0) ratio = (ratio * BigInteger("ff2ea16466c96a3843ec78b326b52861", 16)) shr 128
            if (absTick and 0x80 != 0) ratio = (ratio * BigInteger("fe5dee046a99a2a811c461f1969c3053", 16)) shr 128
            if (absTick and 0x100 != 0) ratio = (ratio * BigInteger("fcbe86c7900a88aedcffc83b479aa3a4", 16)) shr 128
            if (absTick and 0x200 != 0) ratio = (ratio * BigInteger("f987a7253ac413176f2b074cf7815e54", 16)) shr 128
            if (absTick and 0x400 != 0) ratio = (ratio * BigInteger("f3392b0822b70005940c7a398e4b70f3", 16)) shr 128
            if (absTick and 0x800 != 0) ratio = (ratio * BigInteger("e7159475a2c29b7443b29c7fa6e889d9", 16)) shr 128
            if (absTick and 0x1000 != 0) ratio = (ratio * BigInteger("d097f3bdfd2022b8845ad8f792aa5825", 16)) shr 128
            if (absTick and 0x2000 != 0) ratio = (ratio * BigInteger("a9f746462d870fdf8a65dc1f90e061e5", 16)) shr 128
            if (absTick and 0x4000 != 0) ratio = (ratio * BigInteger("70d869a156d2a1b890bb3df62baf32f7", 16)) shr 128
            if (absTick and 0x8000 != 0) ratio = (ratio * BigInteger("31be135f97d08fd981231505542fcfa6", 16)) shr 128
            if (absTick and 0x10000 != 0) ratio = (ratio * BigInteger("9aa508b5b7a84e1c677de54f3e99bc9", 16)) shr 128
            if (absTick and 0x20000 != 0) ratio = (ratio * BigInteger("5d6af8dedb81196699c329225ee604", 16)) shr 128
            if (absTick and 0x40000 != 0) ratio = (ratio * BigInteger("2216e584f5fa1ea926041bedfe98", 16)) shr 128
            if (absTick and 0x80000 != 0) ratio = (ratio * BigInteger("48a170391f7dc42444e8fa2", 16)) shr 128

            if (tick > 0) ratio = UINT256_MAX / ratio

            return (ratio shr 32) + (if (ratio.mod(ONE shl 32) == ZERO) ZERO else ONE)
        }
    }

    object FullMath {
        /**
         * Calculates floor(x * y / denominator)
         * @param x Multiplicand
         * @param y Multiplier
         * @param denominator Divisor
         * @return The result of the operation
         * @see <a href="https://github.com/Uniswap/v3-core/blob/main/contracts/libraries/FullMath.sol">FullMath.sol</a>
         * @see <a href="https://xn--2-umb.com/21/muldiv">Remco Bloeman - mulDiv</a>
         */
        fun mulDiv(
            x: BigInteger,
            y: BigInteger,
            denominator: BigInteger
        ): BigInteger {
            return if (denominator == BigInteger.ZERO) {
                throw ArithmeticException("division by zero")
            } else {
                val result = x * y / denominator

                if (result > UINT256_MAX) {
                    throw ArithmeticException("multiplication overflow (256-bit limit hit)")
                }

                result
            }
        }
    }

    /**
     * A Kotlin-equivalent of the LiquidityAmounts contract
     *
     * Functions called are all done locally, but are heavy operations
     * @see <a href="https://github.com/Uniswap/v3-periphery/blob/main/contracts/libraries/LiquidityAmounts.sol">LiquidityAmounts.sol</a>
     * @see <a href="https://docs.uniswap.org/contracts/v3/reference/periphery/libraries/LiquidityAmounts">LiquidityAmounts</a>
     */
    object LiquidityAmounts {
        fun getAmount0ForLiquidity(
            sqrtRatioAX96: BigInteger, // uint160
            sqrtRatioBX96: BigInteger, // uint160
            liquidity: BigInteger // uint128
        ): BigInteger {
            if (sqrtRatioAX96 > sqrtRatioBX96) {
                return getAmount0ForLiquidity(sqrtRatioBX96, sqrtRatioAX96, liquidity)
            }

            return FullMath.mulDiv(
                liquidity shl 96,
                sqrtRatioBX96 - sqrtRatioAX96,
                sqrtRatioBX96
            ) / sqrtRatioAX96
        }

        fun getAmount1ForLiquidity(
            sqrtRatioAX96: BigInteger, // uint160
            sqrtRatioBX96: BigInteger, // uint160
            liquidity: BigInteger // uint128
        ): BigInteger {
            if (sqrtRatioAX96 > sqrtRatioBX96) {
                return getAmount1ForLiquidity(sqrtRatioBX96, sqrtRatioAX96, liquidity)
            }

            return FullMath.mulDiv(
                liquidity,
                sqrtRatioBX96 - sqrtRatioAX96,
                BigInteger.ONE shl 96
            )
        }

        /**
         * Computes the token0 and token1 amounts for a given liquidity amount, current pool price, and price of the
         * tick boundaries. THIS ROUNDS DOWN
         * @param sqrtRatioX96 The current sqrtRatioX96 of the pool
         * @param sqrtRatioAX96 The sqrtRatioX96 of the first tick boundary
         * @param sqrtRatioBX96 The sqrtRatioX96 of the second tick boundary
         * @param liquidity The liquidity amount
         * @return A pair of token0 and token1 amounts
         * @see <a href="https://github.com/Uniswap/v3-periphery/issues/178">Uniswap V3 Periphery - Issue 178</a>
         */
        fun getAmountsForLiquidity(
            sqrtRatioX96: BigInteger, // uint160
            sqrtRatioAX96: BigInteger, // uint160
            sqrtRatioBX96: BigInteger, // uint160
            liquidity: BigInteger // uint128
        ): Pair<BigInteger, BigInteger> {
            if (sqrtRatioAX96 > sqrtRatioBX96) {
                return getAmountsForLiquidity(sqrtRatioX96, sqrtRatioBX96, sqrtRatioAX96, liquidity)
            }

            if (sqrtRatioX96 <= sqrtRatioAX96) {
                val amount0 = getAmount0ForLiquidity(sqrtRatioAX96, sqrtRatioBX96, liquidity)
                return Pair(amount0, BigInteger.ZERO)
            } else if (sqrtRatioX96 < sqrtRatioBX96) {
                val amount0 = getAmount0ForLiquidity(sqrtRatioX96, sqrtRatioBX96, liquidity)
                val amount1 = getAmount1ForLiquidity(sqrtRatioAX96, sqrtRatioX96, liquidity)
                return Pair(amount0, amount1)
            } else {
                val amount1 = getAmount1ForLiquidity(sqrtRatioAX96, sqrtRatioBX96, liquidity)
                return Pair(BigInteger.ZERO, amount1)
            }
        }
    }

    override fun getProtocolName() = "Uniswap v3"

    override fun getProtocolSlug() = "uniswap-v3"

    override fun getSupportedNetworks() = listOf(
        Networks.ETHEREUM,
        Networks.ARBITRUM,
        Networks.OPTIMISM,
        Networks.POLYGON
    )

    /**
     * Given the sqrtPriceX96 of a pool, this returns the price of token0 in terms of token1
     * @param sqrtPriceX96 The sqrtPriceX96 of the pool
     * @param token0Decimals The number of decimals of token0
     * @param token1Decimals The number of decimals of tokenA
     * @return The price of token0 in terms of token1
     * @see <a href="https://blog.uniswap.org/uniswap-v3-math-primer/">Uniswap V3 Math Primer</a>
     * @see convertPriceToSqrtPriceX96
     */
    fun convertSqrtPriceX96ToPrice(
        sqrtPriceX96: BigInteger,
        token0Decimals: Int,
        token1Decimals: Int
    ): BigDecimal {
        val ten = BigDecimal("10").setScale(maxOf(token0Decimals, token1Decimals))
        val price: BigDecimal = sqrtPriceX96.toBigDecimal().divide(TWO_POWER_96).pow(2)
        val adjPrice = price.divide(ten.pow(token1Decimals).divide(ten.pow(token0Decimals)))

        return adjPrice
    }

    /**
     * Reverse of convertSqrtPriceX96ToPrice
     * @param price The price of token0 in terms of token1
     * @param token0Decimals The number of decimals of token0
     * @param token1Decimals The number of decimals of tokenA
     * @return The sqrtPriceX96 of the pool
     * @see <a href="https://blog.uniswap.org/uniswap-v3-math-primer/">Uniswap V3 Math Primer</a>
     * @see convertSqrtPriceX96ToPrice
     */
    fun convertPriceToSqrtPriceX96(
        price: BigDecimal,
        token0Decimals: Int,
        token1Decimals: Int
    ): BigInteger {
        val mathContext = MathContext(256, RoundingMode.HALF_UP)
        val ten = BigDecimal("10").setScale(maxOf(token0Decimals, token1Decimals))
        val bigDecimalDecimals = (ten.pow(token1Decimals)).div(ten.pow(token0Decimals))
        val sqrtPrice = price.times(bigDecimalDecimals).sqrt(mathContext)

        return sqrtPrice.times(TWO_POWER_96).toBigInteger()
    }

    fun convertSqrtPriceX96ToTick(
        sqrtPriceX96: BigInteger
    ): Long {
        val sqrtPrice = sqrtPriceX96.toBigDecimal().divide(TWO_POWER_96).pow(2)
        val tick = sqrt(sqrtPrice.toDouble()).times(10_000).toLong()

        return tick
    }

    /**
     * Returns the pool address for a given pair of tokens and a fee
     *
     * @param tokenA The address of the first token
     * @param tokenB The address of the second token
     * @param fee The fee tier of the pool
     * @return The address of the pool if it exists, or the zero address if it does not
     * @throws IOException If the call fails
     * @see getAllPools
     * @see <a href="https://docs.uniswap.org/contracts/v3/reference/core/interfaces/IUniswapV3Factory#getpool">IUniswapV3Factory - getPool</a>
     */
    fun getPool(
        tokenA: String,
        tokenB: String,
        fee: FeeTiers
    ): String {
        val function = Function(
            "getPool",
            listOf(
                Address(tokenA),
                Address(tokenB),
                Uint24(fee.fee)
            ),
            listOf(
                object : TypeReference<Address>() {}

            )
        )

        return RPCHandler.executeCall(
            to = factory,
            function = function
        )?.get(0)?.value as String
    }

    /**
     * Returns all the pools for a given pair of tokens
     * @param tokenA The address of the first token
     * @param tokenB The address of the second token
     * @param block The block number to use for the call (default: null)
     * @return A list of pool addresses. The list may be empty if no pools exist for the given pair
     * @see getPool
     */
    fun getAllPools(
        tokenA: String,
        tokenB: String,
        block: Long? = null
    ): List<String> {
        val list = mutableListOf<String>()

        for (fee in FeeTiers.entries) {
            val function = Function(
                "getPool",
                listOf(
                    Address(tokenA),
                    Address(tokenB),
                    Uint24(fee.fee)
                ),
                listOf(
                    object : TypeReference<Address>() {}
                )
            )
            val pool = RPCHandler.executeCall(
                block = block,
                to = factory,
                function = function
            )?.get(0)?.value as String

            if (pool != "0x0000000000000000000000000000000000000000") {
                list.add(pool)
            }
        }

        return list
    }

    fun getPositionsDataFromNft(
        tokenId: BigInteger,
        block: Long? = null
    ): PositionsResponse {
        val function = Function(
            "positions",
            listOf(
                Uint256(tokenId)
            ),
            listOf(
                object : TypeReference<Uint96>() {},
                object : TypeReference<Address>() {},
                object : TypeReference<Address>() {},
                object : TypeReference<Address>() {},
                object : TypeReference<Uint24>() {},
                object : TypeReference<Int24>() {},
                object : TypeReference<Int24>() {},
                object : TypeReference<Uint128>() {},
                object : TypeReference<Uint256>() {},
                object : TypeReference<Uint256>() {},
                object : TypeReference<Uint128>() {},
                object : TypeReference<Uint128>() {}
            )
        )
        val result = RPCHandler.executeCall(
            to = nftPositionManager,
            function = function,
            block = block
        )

        return PositionsResponse(
            result?.get(0)?.value as BigInteger,
            result[1].value as String,
            result[2].value as String,
            result[3].value as String,
            result[4].value.toString().toInt(),
            result[5].value.toString().toInt(),
            result[6].value.toString().toInt(),
            result[7].value as BigInteger,
            result[8].value as BigInteger,
            result[9].value as BigInteger,
            result[10].value as BigInteger,
            result[11].value as BigInteger
        )
    }

    /**
     * Returns data from the slot0 method of the pool
     * @param pool The address of the pool
     * @param block The block number to use for the call (default: null)
     * @return The data from the slot0 method
     */
    fun getSlot0(pool: String, chainId: Long = 1, block: Long? = null): Slot0 {
        val functionToCall = Function(
            "slot0",
            emptyList(),
            listOf(
                object : TypeReference<Uint160>() {},
                object : TypeReference<Int24>() {},
                object : TypeReference<Uint16>() {},
                object : TypeReference<Uint16>() {},
                object : TypeReference<Uint16>() {},
                object : TypeReference<Uint8>() {},
                object : TypeReference<Bool>() {}
            )
        )
        val result = RPCHandler.executeCall(
            block = block,
            to = pool,
            chainId = chainId,
            function = functionToCall
        )
        return Slot0(
            result?.get(0)?.value as BigInteger,
            result[1].value.toString().toInt(),
            result[2].value.toString().toInt(),
            result[3].value.toString().toInt(),
            result[4].value.toString().toInt(),
            result[5].value.toString().toInt(),
            result[6].value as Boolean
        )
    }

    fun getLiquidity(
        pool: String,
        chainId: Long = 1,
        block: Long? = null
    ): BigInteger {
        val function = Function(
            "liquidity",
            emptyList(),
            listOf(
                object : TypeReference<Uint128>() {}
            )
        )

        return RPCHandler.executeCall(
            to = pool,
            chainId = chainId,
            function = function,
            block = block
        )?.get(0)?.value as BigInteger
    }

    /**
     * Returns the amount out received for a given exact input, but for a swap of a single pool
     * @param tokenIn The address of the token to swap from
     * @param tokenOut The address of the token to swap to
     * @param fee The fee tier of the pool
     * @param amountIn The amount of tokenIn to swap
     * @param sqrtPriceLimitX96 The sqrtPriceLimitX96 to use for the swap
     * @return The amount of tokenOut that will be received
     * @see <a href="https://docs.uniswap.org/contracts/v3/reference/periphery/lens/Quoter#quoteexactinputsingle">Quoter - quoteExactInputSingle</a>
     */
    fun getQuoteExactInputSingle(
        tokenIn: String,
        tokenOut: String,
        fee: FeeTiers,
        amountIn: BigInteger,
        sqrtPriceLimitX96: BigInteger,
        block: Long? = null
    ): BigInteger {
        val function = Function(
            "quoteExactInputSingle",
            listOf(
                Address(tokenIn),
                Address(tokenOut),
                Uint24(fee.fee),
                Uint256(amountIn),
                Uint160(sqrtPriceLimitX96)
            ),
            listOf(
                object : TypeReference<Uint256>() {}
            )
        )

        return RPCHandler.executeCall(
            to = quoter,
            function = function,
            block = block
        )?.get(0)?.value as BigInteger
    }

    /**
     * Returns the amount in required for a given exact output, but for a swap of a single pool
     * @param tokenIn The address of the token to swap from
     * @param tokenOut The address of the token to swap to
     * @param fee The fee tier of the pool
     * @param amountOut The amount of tokenOut to receive
     * @param sqrtPriceLimitX96 The sqrtPriceLimitX96 to use for the swap
     * @return The amount of tokenIn that will be required
     * @see <a href="https://docs.uniswap.org/contracts/v3/reference/periphery/lens/Quoter#quoteexactoutputsingle">Quoter - quoteExactOutputSingle</a>
     */
    fun getQuoteExactOutputSingle(
        tokenIn: String,
        tokenOut: String,
        fee: FeeTiers,
        amountOut: BigInteger,
        sqrtPriceLimitX96: BigInteger,
        block: Long? = null
    ): BigInteger {
        val function = Function(
            "quoteExactOutputSingle",
            listOf(
                Address(tokenIn),
                Address(tokenOut),
                Uint24(fee.fee),
                Uint256(amountOut),
                Uint160(sqrtPriceLimitX96)
            ),
            listOf(
                object : TypeReference<Uint256>() {}
            )
        )

        return RPCHandler.executeCall(
            to = quoter,
            function = function,
            block = block
        )?.get(0)?.value as BigInteger
    }
}