package integration

import Config.GENESIS
import blockchain.Block
import blockchain.Node
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import utils.generateBlock
import utils.getRandomLong

internal class GenesisTest : Node() {
    private lateinit var node: Node

    @BeforeEach
    fun setUp() {
        node = Node()
    }

    @Test
    fun genesisVariationTest() {
        for (i in 0..9) {
            fuzzGenesis()
            assertEquals(node.getLastBlock(), GENESIS)
            for (j in 0..2) {
                val block = node.generateBlock()
                assertEquals(node.getLastBlock(), block)
            }
        }
    }

    private fun fuzzGenesis() {
        GENESIS = Block(
            index = getRandomIndex(),
            prevHash = getRandomHash(),
            hash = getRandomHash(),
            data = getData(),
            nonce = getRandomNonce(),
            isActualize = false
        )
        node = Node()
    }

    private fun getRandomIndex(): Long = getRandomLong()
    private fun getRandomNonce(): Long = getRandomLong()
    private fun getRandomHash(): String = getHash(
        index = getRandomIndex(),
        prevHash = getData(),
        data = getData(),
        nonce = getRandomNonce()
    )
}