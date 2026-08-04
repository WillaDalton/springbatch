package com.willadalton.springbatch.repository;

import com.willadalton.springbatch.domain.CsvPersonRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;

@Repository
public class PersonRecordRepository {

    private final JdbcTemplate jdbcTemplate;

    public PersonRecordRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void clearStaging() {
        jdbcTemplate.update("DELETE FROM PERSON_BATCH_STAGE");
    }

    public void insertIntoStaging(CsvPersonRecord record) {
        jdbcTemplate.update(
                """
                INSERT INTO PERSON_BATCH_STAGE (PERSON_NUMBER, NOM, PRENOM, CODE_ENTREPRISE)
                VALUES (?, ?, ?, ?)
                """,
                record.personNumber(),
                record.nom(),
                record.prenom(),
                record.companyCode()
        );
    }

    public void insertMissingActiveFromStaging(LocalDate entryDate) {
        jdbcTemplate.update(
                """
                INSERT INTO PERSON_BATCH (PERSON_NUMBER, NOM, PRENOM, CODE_ENTREPRISE, DATE_ENTREE, DATE_SORTIE)
                SELECT DISTINCT s.PERSON_NUMBER, s.NOM, s.PRENOM, s.CODE_ENTREPRISE, ?, NULL
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
                """,
                Date.valueOf(entryDate)
        );
    }

    public void closeMissingActiveRowsFromStaging(LocalDate exitDate) {
        jdbcTemplate.update(
                """
                UPDATE PERSON_BATCH p
                SET DATE_SORTIE = ?
                WHERE p.DATE_SORTIE IS NULL
                  AND NOT EXISTS (
                    SELECT 1
                    FROM PERSON_BATCH_STAGE s
                    WHERE s.PERSON_NUMBER = p.PERSON_NUMBER
                      AND s.NOM = p.NOM
                      AND s.PRENOM = p.PRENOM
                      AND s.CODE_ENTREPRISE = p.CODE_ENTREPRISE
                  )
                """,
                Date.valueOf(exitDate)
        );
    }
}
