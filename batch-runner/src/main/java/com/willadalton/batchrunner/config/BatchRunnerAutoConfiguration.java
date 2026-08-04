package com.willadalton.batchrunner.config;

import com.willadalton.batchrunner.BatchJobParametersConverter;
import com.willadalton.batchrunner.BatchParametersProvider;
import com.willadalton.batchrunner.BatchResultRecorder;
import com.willadalton.batchrunner.BatchRunner;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Auto-configuration that registers a {@link BatchRunner} bean when all required
 * collaborators ({@link JobLauncher}, at least one {@link Job}, a
 * {@link BatchParametersProvider}, a {@link BatchJobParametersConverter}, and a
 * {@link BatchResultRecorder}) are present in the application context.
 */
@Configuration
@ConditionalOnBean({
        JobLauncher.class,
        BatchParametersProvider.class,
        BatchJobParametersConverter.class,
        BatchResultRecorder.class
})
public class BatchRunnerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @SuppressWarnings({"unchecked", "rawtypes"})
    public BatchRunner batchRunner(
            JobLauncher jobLauncher,
            List<Job> jobs,
            BatchParametersProvider<?> parametersProvider,
            BatchJobParametersConverter<?> parametersConverter,
            BatchResultRecorder resultRecorder
    ) {
        return new BatchRunner(jobLauncher, jobs, (BatchParametersProvider) parametersProvider,
                (BatchJobParametersConverter) parametersConverter, resultRecorder);
    }
}
