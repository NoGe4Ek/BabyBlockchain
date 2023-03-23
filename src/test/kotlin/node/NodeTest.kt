package node

import Config.GENESIS
import blockchain.Block
import blockchain.Node
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class NodeTest {
    private lateinit var node: Node
    private val placeholderBlock = Block(123, "123", "123", "123", 123L, false)

    @BeforeEach
    fun setUp() {
        node = Node()
    }

    @AfterEach
    fun tearDown() {

    }

    @Test
    fun getLastBlock() {
        assertEquals(node.getLastBlock(), GENESIS)
        node.updateLastBlock(placeholderBlock)
        assertEquals(node.getLastBlock(), placeholderBlock)
        var producingBlock: Block? = null
        while (producingBlock == null) {
            producingBlock = node.stepProduce()
        }
        node.updateLastBlock(producingBlock)
        assertEquals(node.getLastBlock(), producingBlock)
    }

    @Test
    fun stepProduce() {
        node.stepProduce()
        val block1 = node.producingBlock?.copy()
        node.stepProduce()
        val block2 = node.producingBlock?.copy()

        assertNotNull(block1)
        assertNotNull(block2)
        assertEquals(block1.index, block2.index)
        assertEquals(block1.prevHash, block2.prevHash)
        assertEquals(block1.data, block2.data)
        assertEquals(block1.nonce, block2.nonce - 1)
        assertNotEquals(block1.hash, block2.hash)

        val previousBlock = node.getLastBlock()
        var producingBlock: Block? = null
        while (producingBlock == null) {
            producingBlock = node.stepProduce()
        }
        assertEquals(previousBlock.index, producingBlock.index - 1)
        assertEquals(previousBlock.hash, producingBlock.prevHash)
    }

    @Test
    fun isBlockValid() {
        var producingBlock: Block? = null
        while (producingBlock == null) {
            producingBlock = node.stepProduce()
        }
        node.updateLastBlock(GENESIS)
        assertTrue(node.isBlockValid(producingBlock))
        assertFalse(node.isBlockValid(placeholderBlock))
    }

    @Test
    fun updateLastBlock() {
        assertEquals(GENESIS, node.getLastBlock())
        node.updateLastBlock(placeholderBlock)
        assertEquals(node.getLastBlock(), placeholderBlock)
    }
}