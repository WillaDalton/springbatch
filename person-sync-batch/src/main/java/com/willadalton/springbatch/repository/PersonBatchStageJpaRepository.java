package com.willadalton.springbatch.repository;

import com.willadalton.springbatch.domain.PersonBatchStage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonBatchStageJpaRepository extends JpaRepository<PersonBatchStage, Long> {
}
