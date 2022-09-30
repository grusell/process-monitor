package se.svt.oss.processmonitor

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import se.svt.oss.processmonitor.system.ProcCpuStats
import se.svt.oss.processmonitor.system.ProcessStatsReader
import se.svt.oss.processmonitor.system.getSystemClockTick
import java.time.Duration

internal class ProcessMonitorTest {

    private val processStatsReader = mockk<ProcessStatsReader>()

    private val systemClockTick = getSystemClockTick()

    @Test
    fun `Test add and remove process`() {
        val processMonitor = ProcessMonitor(processStatsReader)
        val pid = 1L
        every { processStatsReader.readProcessStats(any()) } returns ProcCpuStats(0,0,0,0)

        processMonitor.monitorProcess(pid, Duration.ofMillis(200))
        Thread.sleep(500)
        processMonitor.unmonitorProcess(pid)

        Assertions.assertThat(processMonitor.monitoredProcesses())
            .hasSize(0)
        verify(atLeast = 1) { processStatsReader.readProcessStats(pid) }
    }

    @Test
    fun `Test monitor multiple processes`() {
        val processMonitor = ProcessMonitor(processStatsReader)
        val pid1 = 1L
        val pid2 = 2L
        every { processStatsReader.readProcessStats(pid1) } returns ProcCpuStats(1000,0,0,0)
        every { processStatsReader.readProcessStats(pid2) } returns ProcCpuStats(2000,0,0,0)

        processMonitor.monitorProcess(pid1, Duration.ofMillis(200))
        processMonitor.monitorProcess(pid2, Duration.ofMillis(200))

        Thread.sleep(1000)

        val stats1 = processMonitor.unmonitorProcess(pid1)!!
        val stats2 = processMonitor.unmonitorProcess(pid2)!!

        assertThat(stats1.cpuTimeTotal).isEqualTo(1000L * 1000 / systemClockTick)
        assertThat(stats2.cpuTimeTotal).isEqualTo(1000L * 2000 / systemClockTick)
    }
}