package com.willadalton.springbatch.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Service
public class JdbcExecBatchRecorder implements ExecBatchRecorder {

    private final JdbcTemplate jdbcTemplate;

    public JdbcExecBatchRecorder(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void record(Long executionNumber, String status) {
        jdbcTemplate.update(
                "INSERT INTO EXEC_BATCH (EXECUTION_NUMBER, EXECUTION_DATE, STATUS) VALUES (?, ?, ?)",
                executionNumber,
                Timestamp.valueOf(LocalDateTime.now()),
                status
        );
    }
}
