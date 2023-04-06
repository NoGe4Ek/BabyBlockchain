package unit

import Config.GENESIS
import blockchain.Node
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import utils.*
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class NodeTest {
    private lateinit var node: Node

    @BeforeEach
    fun setUp() {
        node = Node()
    }

    @Test
    fun getLastBlock() {
        assertEquals(node.getLastBlock(), GENESIS)
        node.updateLastBlock(PLACEHOLDER_BLOCK)
        assertEquals(node.getLastBlock(), PLACEHOLDER_BLOCK)
        val block = node.generateBlock()
        node.updateLastBlock(block)
        assertEquals(node.getLastBlock(), block)
    }

    @Test
    fun stepProduce() {
        isProducingStepsValid((0..2).let { node.makeStepWithoutGenerating() })

        val previousBlock = node.getLastBlock()
        val block = node.generateBlock()
        isChainValid(previousBlock, block)
    }

    @Test
    fun isBlockValid() {
        for (i in 0..9) {
            val previousBlock = node.getLastBlock()
            val block = node.generateBlock()
            node.updateLastBlock(previousBlock)
            assertTrue(node.isBlockValid(block))
            assertFalse(node.isBlockValid(PLACEHOLDER_BLOCK))
        }
    }

    @Test
    fun updateLastBlock() {
        assertEquals(GENESIS, node.getLastBlock())
        node.updateLastBlock(PLACEHOLDER_BLOCK)
        assertEquals(node.getLastBlock(), PLACEHOLDER_BLOCK)
    }
}