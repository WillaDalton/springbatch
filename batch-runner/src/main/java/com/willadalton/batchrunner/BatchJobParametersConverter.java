package com.willadalton.batchrunner;

import org.springframework.batch.core.JobParameters;

/**
 * Converts a domain-specific parameters object into Spring Batch {@link JobParameters}.
 *
 * @param <P> the type of the parameters object
 */
public interface BatchJobParametersConverter<P> {

    JobParameters toJobParameters(P parameters);
}
