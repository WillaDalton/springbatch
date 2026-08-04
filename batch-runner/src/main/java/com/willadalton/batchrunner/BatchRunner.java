package com.willadalton.batchrunner;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generic batch runner that launches a list of Spring Batch {@link Job}s for a named dataset.
 *
 * <p>Parameters are resolved via a {@link BatchParametersProvider} and converted to
 * Spring Batch {@link JobParameters} through a {@link BatchJobParametersConverter}.
 * After each execution the outcome is forwarded to a {@link BatchResultRecorder}.</p>
 */
public class BatchRunner {

    private final JobLauncher jobLauncher;
    private final List<Job> jobs;
    private final BatchParametersProvider<?> parametersProvider;
    private final BatchJobParametersConverter<?> parametersConverter;
    private final BatchResultRecorder resultRecorder;

    public <P> BatchRunner(
            JobLauncher jobLauncher,
            List<Job> jobs,
            BatchParametersProvider<P> parametersProvider,
            BatchJobParametersConverter<P> parametersConverter,
            BatchResultRecorder resultRecorder
    ) {
        this.jobLauncher = jobLauncher;
        this.jobs = List.copyOf(jobs);
        this.parametersProvider = parametersProvider;
        this.parametersConverter = parametersConverter;
        this.resultRecorder = resultRecorder;
    }

    /**
     * Runs all configured jobs for the given dataset name.
     *
     * @param datasetName logical name identifying the dataset / run configuration
     * @return aggregated result of all job executions
     */
    @SuppressWarnings("unchecked")
    public BatchRunResult run(String datasetName) {
        Object params = parametersProvider.getParameters(datasetName);
        JobParameters jobParameters = ((BatchJobParametersConverter<Object>) parametersConverter).toJobParameters(params);

        List<JobExecutionSummary> summaries = new ArrayList<>();
        for (Job job : jobs) {
            try {
                JobExecution execution = jobLauncher.run(job, jobParameters);
                String status = execution.getStatus().name();
                String exitCode = execution.getExitStatus().getExitCode();
                resultRecorder.record(execution.getId(), status);
                summaries.add(new JobExecutionSummary(execution.getId(), job.getName(), status, exitCode));
            } catch (Exception e) {
                summaries.add(new JobExecutionSummary(null, job.getName(), "FAILED", e.getMessage()));
            }
        }
        return new BatchRunResult(datasetName, summaries);
    }

    /**
     * Convenience factory that builds a {@link JobParameters} from a plain {@link Map}.
     * String values whose keys start with {@code "date:"} or {@code "long:"} or {@code "double:"}
     * are coerced accordingly; everything else is stored as a String parameter.
     */
    public static JobParameters toJobParameters(Map<String, Object> map) {
        JobParametersBuilder builder = new JobParametersBuilder();
        map.forEach((key, value) -> {
            if (value instanceof Long l) {
                builder.addLong(key, l);
            } else if (value instanceof Double d) {
                builder.addDouble(key, d);
            } else if (value instanceof java.util.Date date) {
                builder.addDate(key, date);
            } else {
                builder.addString(key, value == null ? null : value.toString());
            }
        });
        return builder.toJobParameters();
    }
}
