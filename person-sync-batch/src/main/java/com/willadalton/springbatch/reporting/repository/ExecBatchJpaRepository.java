package com.willadalton.springbatch.reporting.repository;

import com.willadalton.springbatch.reporting.domain.ExecBatch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExecBatchJpaRepository extends JpaRepository<ExecBatch, Long> {
}
