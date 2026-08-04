package com.willadalton.batchrunner;

import java.util.List;

/**
 * The aggregated result of launching all configured jobs for one dataset name.
 */
public record BatchRunResult(
        String datasetName,
        List<JobExecutionSummary> jobResults
) {
}
