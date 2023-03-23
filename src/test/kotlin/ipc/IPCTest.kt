package ipc

import Config.FIRST_PROCESS_PORT
import Config.SECOND_PROCESS_PORT
import Config.THIRD_PROCESS_PORT
import EventHandler.handleActualizeBlockBroadcast
import EventHandler.handleActualizeBlockReceived
import EventHandler.handleBlockReceived
import EventHandler.handleProduceBlock
import EventHandler.isActualizeBlockBroadcastEvent
import EventHandler.isActualizeBlockReceivedEvent
import EventHandler.isBlockReceivedEvent
import SharedSpace
import blockchain.Block
import blockchain.Node
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import utils.getProcessName
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class IPCTest {
    private lateinit var node1: Node
    private lateinit var node2: Node
    private lateinit var node3: Node
    private lateinit var process1: Process
    private lateinit var process2: Process
    private lateinit var process3: Process
    private val placeholderBlock = Block(123, "123", "123", "123", 123L, false)

    @BeforeEach
    fun setUp() {
        process1 = Process(getProcessName(FIRST_PROCESS_PORT), FIRST_PROCESS_PORT)
        process2 = Process(getProcessName(SECOND_PROCESS_PORT), SECOND_PROCESS_PORT)
        process3 = Process(getProcessName(THIRD_PROCESS_PORT), THIRD_PROCESS_PORT)
        process1.start()
        process2.start()
        process3.start()
        node1 = Node()
        node2 = Node()
        node3 = Node()
    }

    @AfterEach
    fun tearDown() {
        process1.stop()
        process2.stop()
        process3.stop()
        Thread.sleep(1000)
    }

    @Test
    fun jointOperationOfTwoNodesTest() {
        var totalBlocks1 = 0L
        var totalBlocks2 = 0L

        for (i in 0..999999) {
            val receivedBlock = SharedSpace.block?.copy()
            SharedSpace.block = null

            when {
                isBlockReceivedEvent(receivedBlock) -> handleBlockReceived(receivedBlock!!, node1)
                isActualizeBlockReceivedEvent(receivedBlock, process1) -> handleActualizeBlockReceived(
                    receivedBlock!!,
                    node1
                )

                isActualizeBlockBroadcastEvent(node1, process1) -> handleActualizeBlockBroadcast(node1, process1)
                else -> if (handleProduceBlock(node1, process1)) totalBlocks1++
            }

            when {
                isBlockReceivedEvent(receivedBlock) -> handleBlockReceived(receivedBlock!!, node2)
                isActualizeBlockReceivedEvent(receivedBlock, process2) -> handleActualizeBlockReceived(
                    receivedBlock!!,
                    node2
                )

                isActualizeBlockBroadcastEvent(node2, process2) -> handleActualizeBlockBroadcast(node2, process2)
                else -> if (handleProduceBlock(node2, process2)) totalBlocks2++
            }
        }

        assertEquals(node1.getLastBlock(), node2.getLastBlock())
        assertNotEquals(totalBlocks1, node1.getLastBlock().index)
        assertNotEquals(totalBlocks2, node2.getLastBlock().index)
    }

    @Test
    fun jointOperationOfThreeNodesTest() {
        var totalBlocks1 = 0L
        var totalBlocks2 = 0L
        var totalBlocks3 = 0L

        for (i in 0..999999) {
            val receivedBlock = SharedSpace.block?.copy()
            SharedSpace.block = null

            when {
                isBlockReceivedEvent(receivedBlock) -> handleBlockReceived(receivedBlock!!, node1)
                isActualizeBlockReceivedEvent(receivedBlock, process1) -> handleActualizeBlockReceived(
                    receivedBlock!!,
                    node1
                )

                isActualizeBlockBroadcastEvent(node1, process1) -> handleActualizeBlockBroadcast(node1, process1)
                else -> if (handleProduceBlock(node1, process1)) totalBlocks1++
            }

            when {
                isBlockReceivedEvent(receivedBlock) -> handleBlockReceived(receivedBlock!!, node2)
                isActualizeBlockReceivedEvent(receivedBlock, process2) -> handleActualizeBlockReceived(
                    receivedBlock!!,
                    node2
                )

                isActualizeBlockBroadcastEvent(node2, process2) -> handleActualizeBlockBroadcast(node2, process2)
                else -> if (handleProduceBlock(node2, process2)) totalBlocks2++
            }

            when {
                isBlockReceivedEvent(receivedBlock) -> handleBlockReceived(receivedBlock!!, node3)
                isActualizeBlockReceivedEvent(receivedBlock, process3) -> handleActualizeBlockReceived(
                    receivedBlock!!,
                    node3
                )

                isActualizeBlockBroadcastEvent(node3, process3) -> handleActualizeBlockBroadcast(node3, process3)
                else -> if (handleProduceBlock(node3, process3)) totalBlocks3++
            }
        }

        assertEquals(node1.getLastBlock(), node2.getLastBlock())
        assertEquals(node2.getLastBlock(), node3.getLastBlock())
        assertNotEquals(totalBlocks1, node1.getLastBlock().index)
        assertNotEquals(totalBlocks2, node2.getLastBlock().index)
        assertNotEquals(totalBlocks3, node3.getLastBlock().index)
    }

    @Test
    fun rejectNotValidBlockTest() {
        for (i in 0..999999) {
            val receivedBlock = SharedSpace.block?.copy()
            SharedSpace.block = null

            when {
                isBlockReceivedEvent(receivedBlock) -> handleBlockReceived(receivedBlock!!, node1)
                isActualizeBlockReceivedEvent(receivedBlock, process1) -> handleActualizeBlockReceived(
                    receivedBlock!!,
                    node1
                )

                isActualizeBlockBroadcastEvent(node1, process1) -> handleActualizeBlockBroadcast(node1, process1)
                else -> handleProduceBlock(node1, process1)
            }
        }
        val lastBlock1 = node1.getLastBlock()
        var producingBlock: Block? = null
        while (producingBlock == null) {
            producingBlock = node2.stepProduce()
        }
        process2.broadcastMessage(producingBlock)
        Thread.sleep(2000)

        val receivedBlock = SharedSpace.block?.copy()
        SharedSpace.block = null
        assertFalse(handleBlockReceived(receivedBlock!!, node1))
        assertEquals(lastBlock1, node1.getLastBlock())
    }

    @Test
    fun acceptValidBlockTest() {
        var producingBlock: Block? = null
        while (producingBlock == null) {
            producingBlock = node1.stepProduce()
        }
        producingBlock = null
        while (producingBlock == null) {
            producingBlock = node2.stepProduce()
        }
        producingBlock = null
        while (producingBlock == null) {
            producingBlock = node2.stepProduce()
        }
        process2.broadcastMessage(producingBlock)
        Thread.sleep(2000)

        val receivedBlock = SharedSpace.block?.copy()
        SharedSpace.block = null
        assertTrue(handleBlockReceived(receivedBlock!!, node1))
        assertEquals(producingBlock, node1.getLastBlock())
    }

    @Test
    fun acceptActualizeBlockTest() {
        var producingBlock: Block? = null
        while (producingBlock == null) {
            producingBlock = node1.stepProduce()
        }
        producingBlock = null
        while (producingBlock == null) {
            producingBlock = node1.stepProduce()
        }
        producingBlock = null
        while (producingBlock == null) {
            producingBlock = node1.stepProduce()
        }
        process1.broadcastMessage(producingBlock.copy(isActualize = true))
        Thread.sleep(2000)

        val receivedBlock = SharedSpace.block?.copy()
        SharedSpace.block = null
        assertTrue(handleActualizeBlockReceived(receivedBlock!!, node2))
        assertEquals(producingBlock.copy(isActualize = true), node2.getLastBlock())
    }
}