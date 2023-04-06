package utils.models

import ipc.Process
import kotlin.test.assertTrue

class StateCheckableProcess(name: String, port: Int) : Process(name, port) {
    fun isTearDownCorrectly() {
        assertTrue(clientSockets.all { it.isClosed } && serverSocket.isClosed)
        assertTrue(clientThreads.all { it.isAlive.not() } && serverThread?.isAlive?.not() ?: true)
    }

    fun isSetupCorrectly() {
        assertTrue(clientSockets.all { it.isConnected } && !serverSocket.isClosed)
        assertTrue(clientThreads.all { it.isAlive } && serverThread?.isAlive ?: true)
    }
}