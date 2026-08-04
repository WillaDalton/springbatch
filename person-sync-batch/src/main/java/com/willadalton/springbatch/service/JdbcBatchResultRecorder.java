package com.willadalton.springbatch.service;

import com.willadalton.batchrunner.BatchResultRecorder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Service
public class JdbcBatchResultRecorder implements BatchResultRecorder {

    private final JdbcTemplate jdbcTemplate;

    public JdbcBatchResultRecorder(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void record(Long executionId, String status) {
        jdbcTemplate.update(
                "INSERT INTO EXEC_BATCH (EXECUTION_NUMBER, EXECUTION_DATE, STATUS) VALUES (?, ?, ?)",
                executionId,
                Timestamp.valueOf(LocalDateTime.now()),
                status
        );
    }
}
