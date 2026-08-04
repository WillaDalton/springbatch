package com.willadalton.batchrunner;

/**
 * Strategy for recording the outcome of a batch job execution.
 */
public interface BatchResultRecorder {

    /**
     * Records the result of a job execution.
     *
     * @param executionId the Spring Batch execution identifier
     * @param status      the batch status string (e.g. COMPLETED, FAILED)
     */
    void record(Long executionId, String status);
}
