package se.svt.oss.processmonitor


import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.concurrent.TimeUnit

internal class ProcessMonitorIntegrationTest {

    val processMonitor = ProcessMonitor()

    @Test
    fun `test monitor process`() {
        // This should give a cpu use of around 100%
        val p = ProcessBuilder("cat", "/dev/zero")
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .redirectError(ProcessBuilder.Redirect.DISCARD)
            .start()

        processMonitor.monitorProcess(p.pid(), Duration.ofMillis(100))
        Thread.sleep(2000)
        p.destroyForcibly()
        p.waitFor(5, TimeUnit.SECONDS)
        val stats = processMonitor.processStats(p.pid())!!

        // Running 100% cpu for two seconds should give a total
        // cpu-time of 2000 millis
        assertThat(stats.cpuTimeTotal)
            .isGreaterThan(1500)
            .isLessThan(2500)
    }
}