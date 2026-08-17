package com.willadalton.springbatch.repository;

import com.willadalton.springbatch.domain.CsvPersonRecord;
import com.willadalton.springbatch.domain.PersonBatchStage;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Repository
public class PersonRecordRepository {

    private final PersonBatchJpaRepository personBatchJpaRepository;
    private final PersonBatchStageJpaRepository personBatchStageJpaRepository;

    public PersonRecordRepository(PersonBatchJpaRepository personBatchJpaRepository,
                                  PersonBatchStageJpaRepository personBatchStageJpaRepository) {
        this.personBatchJpaRepository = personBatchJpaRepository;
        this.personBatchStageJpaRepository = personBatchStageJpaRepository;
    }

    @Transactional
    public void clearStaging() {
        personBatchStageJpaRepository.deleteAllInBatch();
    }

    @Transactional
    public void insertIntoStaging(CsvPersonRecord record) {
        personBatchStageJpaRepository.save(new PersonBatchStage(
                record.personNumber(),
                record.nom(),
                record.prenom(),
                record.companyCode()
        ));
    }

    @Transactional
    public void insertMissingActiveFromStaging(LocalDate entryDate) {
        personBatchJpaRepository.insertMissingActiveFromStaging(entryDate);
    }

    @Transactional
    public void closeMissingActiveRowsFromStaging(LocalDate exitDate) {
        personBatchJpaRepository.closeMissingActiveRowsFromStaging(exitDate);
    }
}

