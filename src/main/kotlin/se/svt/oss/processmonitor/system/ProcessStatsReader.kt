package se.svt.oss.processmonitor.system

import se.svt.oss.processmonitor.exception.NoSuchProcess
import java.io.File
import java.io.FileNotFoundException


class ProcessStatsReader {
    fun readProcessStats(pid: Long): ProcCpuStats {
        try {
            val statsString = File("/proc/$pid/stat").readText(Charsets.UTF_8)
            // See `man proc` for mor info about the content of /proc/PID/stat
            return parseStats(statsString)
        } catch (fileNotFound: FileNotFoundException) {
            throw NoSuchProcess(pid)
        }
    }

    internal fun parseStats(statsString: String) =
        statsString.split(" ").let { stats->
            ProcCpuStats(
                utime = stats[13].toLong(),
                stime = stats[14].toLong(),
                cutime = stats[15].toLong(),
                cstime = stats[16].toLong()
            )
        }
}