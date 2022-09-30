package se.svt.oss.processmonitor.system


import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import se.svt.oss.processmonitor.exception.NoSuchProcess

internal class ProcessStatsReaderTest {
    private val processStatsReader = ProcessStatsReader()

    @Test
    fun `Process does not exist, throws NoSuchProcess` () {
        Assertions.assertThatThrownBy {
            processStatsReader.readProcessStats(Long.MAX_VALUE)
        }.isInstanceOf(NoSuchProcess::class.java)
    }

    @Test
    fun `Process does exists, returns stats` () {
        val stats = processStatsReader.readProcessStats(ProcessHandle.current().pid())
        assertThat(stats.utime)
            .isGreaterThan(0)
    }

    @Test
    fun `Test parse stats` () {
        val statsString = this::class.java.getResource("/statsFile.txt")!!.readText()
        val stats = processStatsReader.parseStats(statsString)

        assertThat(stats.utime).isEqualTo(2413195)
        assertThat(stats.stime).isEqualTo(229846)
        assertThat(stats.cutime).isEqualTo(755308)
        assertThat(stats.cstime).isEqualTo(38305)
    }
}