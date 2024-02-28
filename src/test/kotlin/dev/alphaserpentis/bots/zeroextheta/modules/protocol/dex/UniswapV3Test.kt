package dev.alphaserpentis.bots.zeroextheta.modules.protocol.dex

import dev.alphaserpentis.bots.zeroextheta.exceptions.EthCallException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigInteger
import java.text.NumberFormat

class UniswapV3Test {
    private val USDC = "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48"
    private val WETH = "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2"
    private val TWO_POW_96 = BigInteger("79228162514264337593543950336")

    @Test
    fun mulDiv() {
        assertAll(
            {
                assertEquals(
                    BigInteger("179552932316770819750713311"),
                    UniswapV3.FullMath.mulDiv(
                        BigInteger("3678716017830320842661202398"),
                        BigInteger("3867014695495849883755113235"),
                        TWO_POW_96
                    )
                )
            },
            {
                assertEquals(
                    BigInteger("3088681662701196704709"),
                    UniswapV3.FullMath.mulDiv(
                        BigInteger("3239126564585100475527"),
                        BigInteger("179552932316770819750713311"),
                        BigInteger("188298677665529041093910837")
                    )
                )
            },
            {
                assertEquals(
                    BigInteger("0"),
                    UniswapV3.FullMath.mulDiv(
                        BigInteger("115792089237316195423570985008687907853200923833772036399315770955212473275087"),
                        BigInteger("0"),
                        BigInteger("340282366920938463463374607431768211456")
                    )
                )
            },
            {
                assertEquals(
                    BigInteger("461461225001115149"),
                    UniswapV3.FullMath.mulDiv(
                        BigInteger("15542048566638580201580"),
                        BigInteger("2352374899078517806514649"),
                        TWO_POW_96
                    )
                )
            },
            {
                assertEquals(
                    BigInteger("1423591319432976409447586144"),
                    UniswapV3.FullMath.mulDiv(
                        BigInteger("8927094545831003674704908909"),
                        BigInteger("12634404601730653380266596695"),
                        TWO_POW_96
                    )
                )
            },
            {
                assertEquals(
                    BigInteger("383995753785830744"),
                    UniswapV3.FullMath.mulDiv(
                        BigInteger("999999999999999924"),
                        BigInteger("1423591319432976409447586144"),
                        BigInteger("3707310055899649705561687786")
                    )
                )
            }
        )
    }

    @Test
    fun getSqrtRatioAtTick() {
        assertAll(
            {
                assertEquals(
                    BigInteger("5602223755577321903022134995689"),
                    UniswapV3.TickMath.getSqrtRatioAtTick(85176)
                )
            },
            {
                assertEquals(
                    BigInteger("5341283623238412454227108479223"),
                    UniswapV3.TickMath.getSqrtRatioAtTick(84222)
                )
            },
            {
                assertEquals(
                    BigInteger("5875617940067453351001625213169"),
                    UniswapV3.TickMath.getSqrtRatioAtTick(86129)
                )
            }
        )
    }

    /**
     * @see <a href="https://github.com/Uniswap/v3-periphery/issues/178">Precision Issue</a>
     */
    @Test
    fun getAmountsForLiquidity() {
        val adjustForPrecision = BigInteger.ONE

        assertAll(
            { // Based on #1 ID NFT
                assertEquals(
                    Pair(
                        BigInteger("999999999999999924") - adjustForPrecision,
                        BigInteger("12643817461972260") - adjustForPrecision
                    ),
                    UniswapV3.LiquidityAmounts.getAmountsForLiquidity(
                        BigInteger("8927094545831003674704908909"),
                        UniswapV3.TickMath.getSqrtRatioAtTick(-50580),
                        UniswapV3.TickMath.getSqrtRatioAtTick(-36720),
                        BigInteger("383995753785830744")
                    )
                )
            },
            { // Based on https://etherscan.io/tx/0x7cf8e96b3b0457e38b9ad97d9cd48921d5bf22a1a903b6c298d6c9a0df5115b3
                assertEquals(
                    Pair(
                        BigInteger("3239126564585100475527") - adjustForPrecision,
                        BigInteger("7004719061910004940") - adjustForPrecision
                    ),
                    UniswapV3.LiquidityAmounts.getAmountsForLiquidity(
                        BigInteger("3678716017830320842661202398"),
                        UniswapV3.TickMath.getSqrtRatioAtTick(-62400),
                        UniswapV3.TickMath.getSqrtRatioAtTick(-60400),
                        BigInteger("3088681662701196704709")
                    )
                )
            }
        )
    }

    @Test
    fun getPool() {
        assertAll(
            {
                assertEquals(
                    "0x88e6a0c2ddd26feeb64f039a2c41296fcb3f5640", // USDC/WETH 0.05% Pool
                    UniswapV3.getPool(
                        USDC,
                        WETH,
                        UniswapV3.FeeTiers.ZERO_DOT_ZERO_FIVE // 0.05%
                    )
                )
            },
            {
                assertEquals(
                    "0x8ad599c3a0ff1de082011efddc58f1908eb6e6d8", // USDC/WETH 0.3% Pool
                    UniswapV3.getPool(
                        USDC,
                        WETH,
                        UniswapV3.FeeTiers.ZERO_DOT_THREE // 0.3%
                    )
                )
            },
            {
                assertEquals(
                    "0x7bea39867e4169dbe237d55c8242a8f2fcdcc387", // USDC/WETH 1% Pool
                    UniswapV3.getPool(
                        USDC,
                        WETH,
                        UniswapV3.FeeTiers.ONE // 1%
                    )
                )
            }
        )
    }

    @Test
    fun getAllPools() {
        assertAll(
            {
                assertArrayEquals(
                    listOf(
                        "0x88e6a0c2ddd26feeb64f039a2c41296fcb3f5640",
                        "0x8ad599c3a0ff1de082011efddc58f1908eb6e6d8",
                        "0x7bea39867e4169dbe237d55c8242a8f2fcdcc387",
                        "0xe0554a476a092703abdb3ef35c80e0d76d32939f"
                    ).sorted().toTypedArray(),
                    UniswapV3.getAllPools(
                        USDC,
                        WETH
                    ).sorted().toTypedArray()
                )
            }
        )
    }

    @Test
    fun convertSqrtPriceX96ToPrice() {
        val nf = NumberFormat.getInstance().apply {
            minimumFractionDigits = 18
            maximumFractionDigits = 18
        }

        assertAll(
            {
                assertEquals(
                    "0.000649004842701370",
                    nf.format(
                        UniswapV3.convertSqrtPriceX96ToPrice(
                            BigInteger("2018382873588440326581633304624437"),
                            6,
                            18
                        )
                    )
                )
            },
            {
                assertEquals(
                    "0.000356535342935731",
                    nf.format(
                        UniswapV3.convertSqrtPriceX96ToPrice(
                            BigInteger("1495997533376497003867814207368339"),
                            6,
                            18
                        )
                    )
                )
            },
            {
                assertEquals(
                    "0.006985565528280091",
                    nf.format(
                        UniswapV3.convertSqrtPriceX96ToPrice(
                            BigInteger("6621865711430905658115982293"),
                            18,
                            18
                        )
                    )
                )
            },
            {
                assertEquals(
                    "2,800.039926091067387396",
                    nf.format(
                        UniswapV3.convertSqrtPriceX96ToPrice(
                            BigInteger("4192390186900335788306822"),
                            18,
                            6
                        )
                    )
                )
            }
        )
    }

    @Test
    fun convertPriceToSqrtPriceX96() {
        assertAll(
            {
                assertEquals(
                    BigInteger("2018382873588440326581633304624437"),
                    UniswapV3.convertPriceToSqrtPriceX96(
                        UniswapV3.convertSqrtPriceX96ToPrice(
                            BigInteger("2018382873588440326581633304624437"),
                            6,
                            18
                        ),
                        6,
                        18
                    )
                )
            },
            {
                assertEquals(
                    BigInteger("1495997533376497003867814207368339"),
                    UniswapV3.convertPriceToSqrtPriceX96(
                        UniswapV3.convertSqrtPriceX96ToPrice(
                            BigInteger("1495997533376497003867814207368339"),
                            6,
                            18
                        ),
                        6,
                        18
                    )
                )
            },
            {
                assertEquals(
                    BigInteger("6621865711430905658115982293"),
                    UniswapV3.convertPriceToSqrtPriceX96(
                        UniswapV3.convertSqrtPriceX96ToPrice(
                            BigInteger("6621865711430905658115982293"),
                            18,
                            18
                        ),
                        18,
                        18
                    )
                )
            },
            {
                assertEquals(
                    BigInteger("4192390186900335788306822"),
                    UniswapV3.convertPriceToSqrtPriceX96(
                        UniswapV3.convertSqrtPriceX96ToPrice(
                            BigInteger("4192390186900335788306822"),
                            18,
                            6
                        ),
                        18,
                        6
                    )
                )
            }
        )
    }

    @Test
    fun getQuoteExactInputSingle() {
        assertDoesNotThrow {
            UniswapV3.getQuoteExactInputSingle(
                USDC,
                WETH,
                UniswapV3.FeeTiers.ZERO_DOT_THREE,
                BigInteger("100000000"), // 100 USDC
                BigInteger.ZERO
            )
        }
        assertThrows(EthCallException::class.java) {
            UniswapV3.getQuoteExactInputSingle(
                USDC,
                WETH,
                UniswapV3.FeeTiers.ZERO_DOT_THREE,
                BigInteger("1000000000000000"), // 1BN USDC
                UniswapV3.getSlot0(
                    "0x8ad599c3a0ff1de082011efddc58f1908eb6e6d8"
                ).sqrtPriceX96
            )
        }
    }
}