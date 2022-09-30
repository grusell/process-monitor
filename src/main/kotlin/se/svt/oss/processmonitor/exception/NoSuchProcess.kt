package se.svt.oss.processmonitor.exception


class NoSuchProcess(val pid: Long):
    RuntimeException("Process with pid $pid does not exist")