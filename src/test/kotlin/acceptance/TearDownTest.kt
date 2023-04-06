package acceptance

import Config.FIRST_PROCESS_PORT
import Config.SECOND_PROCESS_PORT
import Config.THIRD_PROCESS_PORT
import SharedSpace
import blockchain.Node
import getBlock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import utils.getProcessName
import utils.handleEvent
import utils.models.StateCheckableProcess

internal class TearDownTest {
    private lateinit var node1: Node
    private lateinit var node2: Node
    private lateinit var node3: Node
    private lateinit var process1: StateCheckableProcess
    private lateinit var process2: StateCheckableProcess
    private lateinit var process3: StateCheckableProcess

    @BeforeEach
    fun setUp() {
        process1 = StateCheckableProcess(getProcessName(FIRST_PROCESS_PORT), FIRST_PROCESS_PORT)
        process2 = StateCheckableProcess(getProcessName(SECOND_PROCESS_PORT), SECOND_PROCESS_PORT)
        process3 = StateCheckableProcess(getProcessName(THIRD_PROCESS_PORT), THIRD_PROCESS_PORT)
        process1.start()
        process2.start()
        process3.start()
        node1 = Node()
        node2 = Node()
        node3 = Node()
    }

    private fun tearDown() {
        process1.stop()
        process2.stop()
        process3.stop()
        Thread.sleep(1000)
    }

    @Test
    fun correctTearDownTest() {
        for (i in 0..999999) {
            val receivedBlock = SharedSpace.getBlock()

            process1.handleEvent(receivedBlock, node1)
            process2.handleEvent(receivedBlock, node2)
            process3.handleEvent(receivedBlock, node3)
        }
        process1.isSetupCorrectly()
        process2.isSetupCorrectly()
        process3.isSetupCorrectly()

        tearDown()
        process1.isTearDownCorrectly()
        process2.isTearDownCorrectly()
        process3.isTearDownCorrectly()
    }
}