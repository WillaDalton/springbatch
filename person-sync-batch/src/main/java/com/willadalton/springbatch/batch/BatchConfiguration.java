package com.willadalton.springbatch.batch;

import com.willadalton.springbatch.domain.CsvPersonRecord;
import com.willadalton.springbatch.repository.PersonRecordRepository;
import com.willadalton.batchrunner.BatchResultRecorder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;

@Configuration
public class BatchConfiguration {

    @Bean
    public FlatFileItemReader<CsvPersonRecord> csvPersonReader(
            @Value("${batch.input-file}") Resource inputFile
    ) {
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("personNumber", "nom", "prenom", "companyCode");

        DefaultLineMapper<CsvPersonRecord> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSet -> new CsvPersonRecord(
                fieldSet.readString("personNumber"),
                fieldSet.readString("nom"),
                fieldSet.readString("prenom"),
                fieldSet.readString("companyCode")
        ));

        return new FlatFileItemReaderBuilder<CsvPersonRecord>()
                .name("csvPersonReader")
                .resource(inputFile)
                .linesToSkip(1)
                .lineMapper(lineMapper)
                .build();
    }

    @Bean
    public ItemProcessor<CsvPersonRecord, CsvPersonRecord> csvPersonProcessor() {
        return item -> {
            String personNumber = normalize(item.personNumber());
            String nom = normalize(item.nom());
            String prenom = normalize(item.prenom());
            String companyCode = normalize(item.companyCode());

            if (companyCode == null || companyCode.length() != 6) {
                int receivedLength = companyCode == null ? 0 : companyCode.length();
                throw new IllegalArgumentException("Le code entreprise doit contenir exactement 6 caractères, longueur reçue: " + receivedLength);
            }

            return new CsvPersonRecord(personNumber, nom, prenom, companyCode);
        };
    }

    @Bean
    public ItemWriter<CsvPersonRecord> stagingWriter(PersonRecordRepository repository) {
        return items -> {
            for (CsvPersonRecord item : items) {
                repository.insertIntoStaging(item);
            }
        };
    }

    @Bean
    public Step stageImportStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FlatFileItemReader<CsvPersonRecord> csvPersonReader,
            ItemProcessor<CsvPersonRecord, CsvPersonRecord> csvPersonProcessor,
            ItemWriter<CsvPersonRecord> stagingWriter
    ) {
        return new StepBuilder("stageImportStep", jobRepository)
                .<CsvPersonRecord, CsvPersonRecord>chunk(100, transactionManager)
                .reader(csvPersonReader)
                .processor(csvPersonProcessor)
                .writer(stagingWriter)
                .build();
    }

    @Bean
    public Step processStagingStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            PersonRecordRepository repository
    ) {
        return new StepBuilder("processStagingStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    LocalDate today = LocalDate.now();
                    repository.insertMissingActiveFromStaging(today);
                    repository.closeMissingActiveRowsFromStaging(today);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Job personSyncJob(
            JobRepository jobRepository,
            Step stageImportStep,
            Step processStagingStep,
            PersonRecordRepository repository,
            BatchResultRecorder resultRecorder
    ) {
        return new JobBuilder("personSyncJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(new JobExecutionListener() {
                    @Override
                    public void beforeJob(JobExecution jobExecution) {
                        repository.clearStaging();
                    }

                    @Override
                    public void afterJob(JobExecution jobExecution) {
                        resultRecorder.record(jobExecution.getId(), jobExecution.getStatus().name());
                    }
                })
                .start(stageImportStep)
                .next(processStagingStep)
                .build();
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
