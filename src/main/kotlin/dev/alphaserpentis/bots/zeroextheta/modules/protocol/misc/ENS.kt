package dev.alphaserpentis.bots.zeroextheta.modules.protocol.misc

import dev.alphaserpentis.bots.zeroextheta.data.web3.protocol.ENSData
import dev.alphaserpentis.bots.zeroextheta.handlers.ethereum.RPCHandler
import dev.alphaserpentis.bots.zeroextheta.modules.protocol.ProtocolInfo
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.ens.EnsResolver
import java.util.Optional

object ENS : ProtocolInfo<String, ENSData> {

    private val baseRegistrar: Address = Address("0x57f1887a8bf19b14fc0df6fd9b2acc9af147ea85")
    private val controller: Address = Address("0x253553366Da8546fC250F225fe3d25d0C782303b")
    private val getNameExpires: Function = Function(
        "nameExpires",
        emptyList(),
        listOf(object : TypeReference<Uint256>() {})
    )

    override fun getProtocolName() = "Ethereum Name Service"

    override fun getProtocolSlug() = "ens"

    /**
     * Returns the resolved address of the ENS name if it exists, or if the input is an address, returns the ENS name
     * if it exists
     *
     * @param input The ENS name or address to resolve
     * @return The resolved data if it exists
     */
    override fun getData(input: String?): Optional<ENSData> {
        if (input == null) throw IllegalArgumentException("Input cannot be null")

        val web3j = RPCHandler.getWeb3jInstance()
        val ensResolver = EnsResolver(web3j)

        // If the input ends with .eth, resolve it backwards
        if (input.endsWith(".eth")) {
        } else {

        }

        return Optional.empty()
    }
}