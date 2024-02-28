package dev.alphaserpentis.bots.zeroextheta.handlers.ethereum

import dev.alphaserpentis.bots.zeroextheta.data.web3.Networks
import dev.alphaserpentis.bots.zeroextheta.exceptions.EthCallException
import io.github.cdimascio.dotenv.Dotenv
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.DefaultBlockParameterNumber
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.core.methods.response.EthCall
import org.web3j.protocol.http.HttpService

object RPCHandler {
    /**
     * Primary RPC URL to be used
     */
    private const val REST_ALCHEMY_URL = "g.alchemy.com/v2/"
    /**
     * Fallback RPC URL to be used
     */
    private const val REST_LLAMANODE_URL = "llamarpc.com"
    private const val ZERO_ADDRESS = "0x0000000000000000000000000000000000000000"
    private val llamaNodeWeb3jInstances = mutableMapOf<Long, Web3j>()
    private val alchemyWeb3jInstances = mutableMapOf<Long, Web3j>()

    init {
        Networks.entries.forEach {
            alchemyWeb3jInstances[it.chainId] = Web3j.build(
                HttpService("https://${it.alchemy}.$REST_ALCHEMY_URL${Dotenv.load()["ALCHEMY_API_KEY"]}")
            )
            llamaNodeWeb3jInstances[it.chainId] = Web3j.build(
                HttpService("https://${it.llamanode}.$REST_LLAMANODE_URL")
            )
        }
    }

    /**
     * Returns a working Web3j instance for the given chain ID
     *
     * @param chainId The chain ID to get the Web3j instance for
     * @return A working Web3j instance
     */
    fun getWeb3jInstance(chainId: Long = 1): Web3j {
        val alchemy = alchemyWeb3jInstances[chainId]!!

        return try {
            alchemy.ethBlockNumber().send()
            alchemy
        } catch (e: Exception) {
            val llamanode = llamaNodeWeb3jInstances[chainId]
            llamanode!!.ethBlockNumber().send()
            llamanode
        }
    }

    fun executeCall(
        block: Long? = null,
        from: String = ZERO_ADDRESS,
        to: String,
        chainId: Long = 1,
        function: Function
    ): List<Type<*>>? {
        val transaction = Transaction.createEthCallTransaction(
            from,
            to,
            FunctionEncoder.encode(function)
        )

        return FunctionReturnDecoder.decode(
            tryAndExecuteCall(block, transaction, chainId),
            function.outputParameters
        )
    }

    private fun tryAndExecuteCall(
        block: Long? = null,
        tx: Transaction,
        chainId: Long = 1
    ): String {
        val blockParam: DefaultBlockParameter =
            if (block == null) DefaultBlockParameterName.LATEST else DefaultBlockParameterNumber(block)

        return try {
            val call: EthCall = alchemyWeb3jInstances[chainId]!!
                .ethCall(tx, blockParam)
                .send()

            if (call.hasError()) {
                throw EthCallException(call.error!!)
            } else {
                call.result
            }
        } catch (e: Exception) {
            val call: EthCall = llamaNodeWeb3jInstances[chainId]!!
                .ethCall(tx, blockParam)
                .send()

            if (call.hasError()) {
                throw EthCallException(call.error!!)
            } else {
                call.result
            }
        }
    }
}
