package se.svt.oss.processmonitor

import mu.KotlinLogging
import se.svt.oss.processmonitor.exception.NoSuchProcess
import se.svt.oss.processmonitor.model.ProcessStats
import se.svt.oss.processmonitor.system.ProcessStatsReader
import se.svt.oss.processmonitor.system.getSystemClockTick
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit


class ProcessMonitor internal constructor (private val processStatsReader: ProcessStatsReader) {

    constructor(): this(ProcessStatsReader())

    private val log = KotlinLogging.logger {}

    private val processStats = ConcurrentHashMap<Long, MonitoredProcess>()
    private val executor = ScheduledThreadPoolExecutor(1)
    private val systemClockTick: Int = getSystemClockTick()


    fun monitorProcess(pid: Long, monitoringInterval: Duration = Duration.ofSeconds(1)) {
        log.debug { "Start monitor process $pid" }
        processStats[pid] = MonitoredProcess(pid, monitoringInterval, stats(pid))
        executor.schedule({collectStats(pid)}, 1, TimeUnit.MILLISECONDS);
    }

    fun unmonitorProcess(pid: Long) =
        processStats.remove(pid)?.processStats

    fun processStats(pid: Long) =
        processStats[pid]?.processStats

    fun monitoredProcesses() =
        processStats.values.toList()

    private fun cpuMillis(pid: Long, includeChildren: Boolean = true): Long {
        val stats = processStatsReader.readProcessStats(pid)
        val total = stats.utime + stats.stime +
                if (includeChildren) stats.cutime + stats.cstime
                else 0
        return total * 1000 / systemClockTick
    }

    private fun stats(pid: Long, previousStats: ProcessStats? = null): ProcessStats {
        val cpuMillis = cpuMillis(pid)
        val time = System.currentTimeMillis()
        val cpuCurrent =
            if (previousStats == null) 0
            else (cpuMillis - previousStats.cpuTimeTotal) * 1_000 / (time - previousStats.timestamp)
        return ProcessStats(cpuMillis, cpuCurrent.toInt(), time)
    }

    private fun collectStats(pid: Long) {
        log.debug { "Collecting stats for process $pid" }
        try {
            val monitoredProcess = processStats.compute(pid) { _, v ->
                v?.copy(processStats = stats(v.pid, v.processStats))
            }
            if (monitoredProcess != null) {
                log.debug { "Updates process stats: $monitoredProcess" }
                executor.schedule(
                    { collectStats(pid) },
                    monitoredProcess.monitoringInterval.toMillis(),
                    TimeUnit.MILLISECONDS
                );
            }
        } catch (nsp: NoSuchProcess) {
            log.debug { "Process with pid ${nsp.pid} does not exist, monitoring will be stopped" }
            // Process exited, stop monitoring this process
        } catch (exception: Exception) {
            log.warn(exception) { "Failed to read cpu stats for process with pid ${pid}, monitoring will be stopped" }
            processStats.compute(pid) { _, v ->
                v?.copy(readStatsError = exception.toString())
            }
        }
    }

    data class MonitoredProcess(
        val pid: Long,
        val monitoringInterval: Duration,
        val processStats: ProcessStats,
        val readStatsError: String? = null)
}

