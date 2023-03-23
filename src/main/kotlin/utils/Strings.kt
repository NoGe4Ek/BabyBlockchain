package utils

import ipc.getNodeNumber

const val PROCESS_PREFIX = "Process"

fun getProcessName(port: Int): String = "$PROCESS_PREFIX ${getNodeNumber(port)}"