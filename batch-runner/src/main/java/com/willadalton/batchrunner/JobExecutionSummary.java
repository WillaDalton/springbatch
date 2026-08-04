package com.willadalton.batchrunner;

/**
 * Summary of a single job execution produced by {@link BatchRunner}.
 */
public record JobExecutionSummary(
        Long executionId,
        String jobName,
        String status,
        String exitCode
) {
}
