package com.willadalton.springbatch.reporting.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "EXEC_BATCH")
public class ExecBatch {

    @Id
    @Column(name = "EXECUTION_NUMBER")
    private Long executionNumber;

    @Column(name = "EXECUTION_DATE", nullable = false)
    private LocalDateTime executionDate;

    @Column(name = "STATUS", nullable = false, length = 32)
    private String status;

    protected ExecBatch() {
    }

    public ExecBatch(Long executionNumber, LocalDateTime executionDate, String status) {
        this.executionNumber = executionNumber;
        this.executionDate = executionDate;
        this.status = status;
    }

    public Long getExecutionNumber() {
        return executionNumber;
    }

    public LocalDateTime getExecutionDate() {
        return executionDate;
    }

    public String getStatus() {
        return status;
    }
}
