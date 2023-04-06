package integration

import Config.FIRST_PROCESS_PORT
import Config.SECOND_PROCESS_PORT
import Config.THIRD_PROCESS_PORT
import EventHandler.handleActualizeBlockReceived
import EventHandler.handleBlockReceived
import SharedSpace
import blockchain.Node
import getBlock
import ipc.Process
import ipc.broadcastMessage
import ipc.isPriorityProcess
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import utils.*
import utils.models.ProcessResult
import utils.models.isActualizeBlocksWasBroadcasted
import utils.models.isActualizeBlocksWasReceived
import utils.models.merge
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
    private val testSteps = (0..999999)

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
        process3.stop()
        var processResult1: ProcessResult? = null
        var processResult2: ProcessResult? = null

        for (i in testSteps) {
            val receivedBlock = SharedSpace.getBlock()

            processResult1 = processResult1?.merge(process1.handleEvent(receivedBlock, node1))
                ?: process1.handleEvent(receivedBlock, node1)
            processResult2 = processResult2?.merge(process2.handleEvent(receivedBlock, node2))
                ?: process1.handleEvent(receivedBlock, node1)
        }

        assertTrue(process1.isPriorityProcess())
        processResult1!!.isActualizeBlocksWasBroadcasted()
        processResult2!!.isActualizeBlocksWasReceived()
        assertTrue(processResult1.totalReceivedBlocks > 0)
        assertTrue(processResult2.totalReceivedBlocks > 0)
        assertNotEquals(processResult1.totalProducedBlocks, node1.getLastBlock().index)
        assertNotEquals(processResult2.totalProducedBlocks, node2.getLastBlock().index)

        Benchmark.makePerformanceAssessment(testSteps.last + 1, 2, node1.getLastBlock().index.toInt())
    }

    @Test
    fun jointOperationOfThreeNodesTest() {
        var processResult1: ProcessResult? = null
        var processResult2: ProcessResult? = null
        var processResult3: ProcessResult? = null

        for (i in testSteps) {
            val receivedBlock = SharedSpace.getBlock()

            processResult1 = processResult1?.merge(process1.handleEvent(receivedBlock, node1))
                ?: process1.handleEvent(receivedBlock, node1)
            processResult2 = processResult2?.merge(process2.handleEvent(receivedBlock, node2))
                ?: process1.handleEvent(receivedBlock, node1)
            processResult3 = processResult3?.merge(process3.handleEvent(receivedBlock, node3))
                ?: process3.handleEvent(receivedBlock, node3)
        }

        assertTrue(process1.isPriorityProcess())
        processResult1!!.isActualizeBlocksWasBroadcasted()
        processResult2!!.isActualizeBlocksWasReceived()
        processResult3!!.isActualizeBlocksWasReceived()
        assertTrue(processResult1.totalReceivedBlocks > 0)
        assertTrue(processResult2.totalReceivedBlocks > 0)
        assertTrue(processResult3.totalReceivedBlocks > 0)
        assertNotEquals(processResult1.totalProducedBlocks, node1.getLastBlock().index)
        assertNotEquals(processResult2.totalProducedBlocks, node2.getLastBlock().index)
        assertNotEquals(processResult3.totalProducedBlocks, node3.getLastBlock().index)

        Benchmark.makePerformanceAssessment(testSteps.last + 1, 3, node1.getLastBlock().index.toInt())
    }

    @Test
    fun rejectNotValidBlockTest() {
        var processResult1: ProcessResult? = null

        for (i in testSteps) {
            val receivedBlock = SharedSpace.getBlock()

            processResult1 = processResult1?.merge(process1.handleEvent(receivedBlock, node1))
                ?: process1.handleEvent(receivedBlock, node1)
        }
        val block = node1.getLastBlock()
        val blockToReject = node2.generateBlock()
        process2.broadcastMessage(blockToReject)
        Thread.sleep(2000)

        val receivedBlock = SharedSpace.getBlock()
        assertFalse(handleBlockReceived(receivedBlock!!, node1))
        assertEquals(block, node1.getLastBlock())
    }

    @Test
    fun acceptValidBlockTest() {
        node1.generateBlock()
        node2.generateBlock()
        val blockToAccept = node2.generateBlock()

        process2.broadcastMessage(blockToAccept)
        Thread.sleep(2000)

        val receivedBlock = SharedSpace.getBlock()
        assertTrue(handleBlockReceived(receivedBlock!!, node1))
        assertEquals(blockToAccept, node1.getLastBlock())
    }

    @Test
    fun acceptActualizeBlockTest() {
        node1.generateBlock()
        node1.generateBlock()
        val blockToActualize = node1.generateBlock()

        process1.broadcastMessage(blockToActualize.copy(isActualize = true))
        Thread.sleep(2000)

        val receivedBlock = SharedSpace.getBlock()
        assertTrue(handleActualizeBlockReceived(receivedBlock!!, node2))
        assertEquals(blockToActualize.copy(isActualize = true), node2.getLastBlock())
    }
}