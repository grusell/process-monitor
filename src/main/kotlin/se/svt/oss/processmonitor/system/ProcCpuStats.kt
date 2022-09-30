package se.svt.oss.processmonitor.system

data class ProcCpuStats(
    val utime: Long,
    val stime: Long,
    val cutime: Long,
    val cstime: Long
)