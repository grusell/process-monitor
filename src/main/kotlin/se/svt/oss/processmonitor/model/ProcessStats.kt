package se.svt.oss.processmonitor.model

data class ProcessStats(
    /** Total cpu time in milliseconds */
    val cpuTimeTotal: Long,
    /** Average cpu usage since last sample in millicpus */
    val cpuCurrent: Int,
    val timestamp: Long,
)