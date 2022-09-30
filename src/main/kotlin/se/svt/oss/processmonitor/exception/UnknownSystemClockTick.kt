package se.svt.oss.processmonitor.exception


class UnknownSystemClockTick(cause: Throwable): RuntimeException("Failed to read system clocktick", cause)