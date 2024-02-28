package dev.alphaserpentis.bots.zeroextheta.handlers.ethereum

import com.google.gson.Gson
import dev.alphaserpentis.bots.zeroextheta.data.web3.TokenInfo
import dev.alphaserpentis.bots.zeroextheta.data.web3.TrustedTokens
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger
import java.nio.file.Path

class TokenHandler(
    pathToTrustedTokens: Path
) {
    val trustedTokens: TrustedTokens =
        Gson().fromJson(pathToTrustedTokens.toFile().readText(), TrustedTokens::class.java)
    val currentTokenInfo: MutableMap<Long, MutableMap<String, TokenInfo>> = mutableMapOf()

    /**
     * Looks up a token by address, name, or symbol.
     *
     * This will return the first token found that matches the given parameters.
     * @param address The address of the token.
     * @param name The name of the token.
     * @param symbol The symbol of the token.
     * @param chainId The chain ID of the token.
     * @return A pair of the chain ID and the token. If no token is found, null is returned.
     * @throws IllegalArgumentException If none of address, name, or symbol are provided.
     */
    fun lookupTrustedToken(
        address: String? = null,
        name: String? = null,
        symbol: String? = null,
        chainId: Long?
    ): Pair<Long, Pair<String, TrustedTokens.Token>>? {
        if (address == null && name == null && symbol == null) {
            throw IllegalArgumentException("At least one of address, name, or symbol must be provided.")
        }

        return if (chainId != null) {
            var innerAddr: String = address ?: ""
            val outerMapping = trustedTokens.chainToToken[chainId]!!
            val trustedToken: TrustedTokens.Token? = outerMapping[innerAddr]

            if (trustedToken == null) {
                outerMapping.forEach { (address, token) ->
                    if (token.name.equals(name, true) || token.symbol.equals(symbol, true)) {
                        innerAddr = address
                        return Pair(chainId, Pair(innerAddr, token))
                    }
                }

                return null
            } else {
                return Pair(chainId, Pair(innerAddr, trustedToken))
            }
        } else { // If a chain ID wasn't supplied
            trustedTokens.chainToToken
                .forEach { (chainId, mappings) ->
                    if (address != null) { // Check if the address is null
                        mappings[address]?.let { return Pair(chainId, Pair(address, it)) }
                    } else { // Start going through addresses
                        mappings.forEach { (address, token) ->
                            if (token.name.equals(name, true) || token.symbol.equals(symbol, true)) {
                                return Pair(chainId, Pair(address, token))
                            }
                        }
                    }
                }

            null
        }
    }

    fun lookupTokenByAddress(address: String, chainId: Long = 1): TokenInfo? {
        return currentTokenInfo[chainId]?.get(address)
    }

    /**
     * Looks up a token by its symbol. This will return a list of pairs of the chain ID and the address of the token.
     *
     * @param symbol The symbol of the token.
     * @return A list of pairs of the chain ID and the address of the token. If no token is found, an empty list is returned.
     */
    fun lookupSymbolToAddress(symbol: String): List<Pair<Long, String>> {
        val results: MutableList<Pair<Long, String>> = mutableListOf()

        trustedTokens.chainToToken
            .forEach { (chainId, mappings) ->
                mappings.forEach { (address, token) ->
                    if (token.symbol.equals(symbol, true)) {
                        results.add(Pair(chainId, address))
                    }
                }
            }

        return results
    }

    fun getDecimals(address: String, chainId: Long): BigInteger {
        return currentTokenInfo[chainId]?.get(address)?.decimals?.toBigInteger() ?: RPCHandler.executeCall(
            to = address,
            function = Function(
                "decimals",
                emptyList(),
                listOf(object : TypeReference<Uint256>() {})
            )
        )?.get(0)?.value as BigInteger
    }
}
