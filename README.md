# process-monitor

A kotlin/java library for monitoring cpu-utilisation of processes

Cpu-usage for monitored processes are read from `/proc/PID/stat`
(see `man proc` for more info). Returned cpu-time includes the sum
of the time in user mode, time in kernel mode, as well as time in user
and kernel mode for child-processes.

Process monitor is not involved in starting, stopping, or waiting for
processes. If a process dies, monitoring will be stopped. Process stats
will be stored until `unmonitorProcess(pid)` is called for the process.

Stats are read for each monitored process with a configurable period.

Note that the final
cpu stats will be somewhat inaccurate because we only sample cpu usage
with the sampling frequency and cannot read the cpu stats after
the process exited. So cpu usage between last sample and process exit will
not be included.

# Usage

    val processMonitor = ProcessMonitor()
    processMonitor.monitorProcess(pid, Duration.ofSeconds(1)) // Update stats every second

    // Do something else...

    // Get stats
    val stats = processMonitor.processStats(pid)
    println("Total used cpu time of my process: ${stats.cpuTimeTotal}ms")
    println("Current cpu-usage of my process: ${stats.cpuCurrent}mcpu")

    // Stop monitoring process
    processMonitor.unmonitorProcess(pid)

