package com.willadalton.springbatch.service;

public interface ExecBatchRecorder {

    void record(Long executionNumber, String status);
}
