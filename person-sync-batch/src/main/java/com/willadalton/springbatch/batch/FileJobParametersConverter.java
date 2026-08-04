package com.willadalton.springbatch.batch;

import com.willadalton.batchrunner.BatchJobParametersConverter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.stereotype.Component;

/**
 * Converts {@link FileParameters} into Spring Batch {@link JobParameters}.
 */
@Component
public class FileJobParametersConverter implements BatchJobParametersConverter<FileParameters> {

    @Override
    public JobParameters toJobParameters(FileParameters parameters) {
        return new JobParametersBuilder()
                .addString("input-file", parameters.inputFile().getDescription())
                .toJobParameters();
    }
}
