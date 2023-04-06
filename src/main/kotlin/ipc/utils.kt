package ipc

import Config.FIRST_PROCESS_PORT
import Config.PRIORITY_PROCESS_PORT
import Config.SECOND_PROCESS_PORT
import Config.SOCKET_HOST
import Config.THIRD_PROCESS_PORT
import SharedSpace
import java.io.IOException
import java.io.PrintWriter
import java.net.Socket

/**
 * Simple check is the process priority by it port
 *
 * @param process
 */
fun Process.isPriorityProcess(): Boolean =
    this.name.split(" ")[1].toInt() == getNodeNumber(PRIORITY_PROCESS_PORT)

/**
 * Get a list of ports of neighboring blockchain nodes
 *
 * @param currentPort - current process port
 */
fun getPeerPorts(currentPort: Int): List<Int> =
    when (currentPort) {
        FIRST_PROCESS_PORT -> listOf(SECOND_PROCESS_PORT, THIRD_PROCESS_PORT)
        SECOND_PROCESS_PORT -> listOf(FIRST_PROCESS_PORT, THIRD_PROCESS_PORT)
        else -> listOf(FIRST_PROCESS_PORT, SECOND_PROCESS_PORT)
    }

/**
 * Get node number by port
 *
 * @param port - current process port
 */
fun getNodeNumber(port: Int) = port % 1000

/** Broadcast messages to other processes **/
fun Process.broadcastMessage(message: Message) {
    // Connect to the peer processes
    for (peerPort in getPeerPorts(this.port)) {
        try {
            // Get or create peer socket
            val peerSocket = this.peerSockets.getOrPut(peerPort) {
                Socket(SOCKET_HOST, peerPort)
            }
            // Send a message to the peer
            val output = PrintWriter(peerSocket.getOutputStream(), true)
            output.println(SharedSpace.gson.toJson(message))
        } catch (e: IOException) {
            println("${this.name} failed to connect to peer at port $peerPort")
        }
    }
}