package se.svt.oss.processmonitor.system

import se.svt.oss.processmonitor.exception.UnknownSystemClockTick
import java.util.concurrent.TimeUnit

fun runCommand(cmd: String, timeout: Long = 10L): String {
    val p = ProcessBuilder(cmd.split(" "))
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()
    if (p.waitFor(timeout, TimeUnit.SECONDS)) {
        return p.inputStream.bufferedReader().readText()
    }
    throw RuntimeException("Command '$cmd' did not finish within $timeout seconds")
}

fun getSystemClockTick(): Int {
    try {
        val output = runCommand("getconf CLK_TCK")
        return output.trim().toInt()
    } catch (e: Exception) {
        throw UnknownSystemClockTick(e)
    }
}