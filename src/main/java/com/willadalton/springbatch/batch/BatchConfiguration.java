package com.willadalton.springbatch.batch;

import com.willadalton.springbatch.domain.CsvPersonRecord;
import com.willadalton.springbatch.domain.PersonKey;
import com.willadalton.springbatch.repository.PersonRecordRepository;
import com.willadalton.springbatch.service.CurrentFileState;
import com.willadalton.springbatch.service.ExecBatchRecorder;
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
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
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

        BeanWrapperFieldSetMapper<CsvPersonRecord> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(CsvPersonRecord.class);

        DefaultLineMapper<CsvPersonRecord> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

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
                throw new IllegalArgumentException("Le code entreprise doit contenir exactement 6 caracteres");
            }

            return new CsvPersonRecord(personNumber, nom, prenom, companyCode);
        };
    }

    @Bean
    public ItemWriter<CsvPersonRecord> csvPersonWriter(
            PersonRecordRepository repository,
            CurrentFileState currentFileState
    ) {
        return items -> {
            LocalDate today = LocalDate.now();
            for (CsvPersonRecord item : items) {
                PersonKey key = item.toKey();
                currentFileState.add(key);
                if (!repository.existsActive(key)) {
                    repository.insert(item, today);
                }
            }
        };
    }

    @Bean
    public Step importStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FlatFileItemReader<CsvPersonRecord> csvPersonReader,
            ItemProcessor<CsvPersonRecord, CsvPersonRecord> csvPersonProcessor,
            ItemWriter<CsvPersonRecord> csvPersonWriter
    ) {
        return new StepBuilder("importStep", jobRepository)
                .<CsvPersonRecord, CsvPersonRecord>chunk(100, transactionManager)
                .reader(csvPersonReader)
                .processor(csvPersonProcessor)
                .writer(csvPersonWriter)
                .build();
    }

    @Bean
    public Step closeMissingStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            PersonRecordRepository repository,
            CurrentFileState currentFileState
    ) {
        return new StepBuilder("closeMissingStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    repository.closeMissingActiveRows(currentFileState.snapshot(), LocalDate.now());
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Job personSyncJob(
            JobRepository jobRepository,
            Step importStep,
            Step closeMissingStep,
            CurrentFileState currentFileState,
            ExecBatchRecorder execBatchRecorder
    ) {
        return new JobBuilder("personSyncJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(new JobExecutionListener() {
                    @Override
                    public void beforeJob(JobExecution jobExecution) {
                        currentFileState.clear();
                    }

                    @Override
                    public void afterJob(JobExecution jobExecution) {
                        execBatchRecorder.record(jobExecution.getId(), jobExecution.getStatus().name());
                    }
                })
                .start(importStep)
                .next(closeMissingStep)
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
