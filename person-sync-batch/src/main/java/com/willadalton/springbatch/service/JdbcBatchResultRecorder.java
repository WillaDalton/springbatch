package com.willadalton.springbatch.service;

import com.willadalton.batchrunner.BatchResultRecorder;
import com.willadalton.springbatch.reporting.domain.ExecBatch;
import com.willadalton.springbatch.reporting.repository.ExecBatchJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class JdbcBatchResultRecorder implements BatchResultRecorder {

    private final ExecBatchJpaRepository execBatchJpaRepository;

    public JdbcBatchResultRecorder(ExecBatchJpaRepository execBatchJpaRepository) {
        this.execBatchJpaRepository = execBatchJpaRepository;
    }

    @Override
    @Transactional("reportingTransactionManager")
    public void record(Long executionId, String status) {
        execBatchJpaRepository.save(new ExecBatch(executionId, LocalDateTime.now(), status));
    }
}

