package com.willadalton.springbatch.repository;

import com.willadalton.springbatch.domain.PersonBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface PersonBatchJpaRepository extends JpaRepository<PersonBatch, Long> {

    @Modifying
    @Query(value = """
            INSERT INTO PERSON_BATCH (PERSON_NUMBER, NOM, PRENOM, CODE_ENTREPRISE, DATE_ENTREE, DATE_SORTIE)
            SELECT DISTINCT s.PERSON_NUMBER, s.NOM, s.PRENOM, s.CODE_ENTREPRISE, :entryDate, NULL
            FROM PERSON_BATCH_STAGE s
            WHERE NOT EXISTS (
                SELECT 1
                FROM PERSON_BATCH p
                WHERE p.PERSON_NUMBER = s.PERSON_NUMBER
                  AND p.NOM = s.NOM
                  AND p.PRENOM = s.PRENOM
                  AND p.CODE_ENTREPRISE = s.CODE_ENTREPRISE
                  AND p.DATE_SORTIE IS NULL
            )
            """, nativeQuery = true)
    void insertMissingActiveFromStaging(@Param("entryDate") LocalDate entryDate);

    @Modifying
    @Query(value = """
            UPDATE PERSON_BATCH p
            SET DATE_SORTIE = :exitDate
            WHERE p.DATE_SORTIE IS NULL
              AND NOT EXISTS (
                SELECT 1
                FROM PERSON_BATCH_STAGE s
                WHERE s.PERSON_NUMBER = p.PERSON_NUMBER
                  AND s.NOM = p.NOM
                  AND s.PRENOM = p.PRENOM
                  AND s.CODE_ENTREPRISE = p.CODE_ENTREPRISE
              )
            """, nativeQuery = true)
    void closeMissingActiveRowsFromStaging(@Param("exitDate") LocalDate exitDate);
}
