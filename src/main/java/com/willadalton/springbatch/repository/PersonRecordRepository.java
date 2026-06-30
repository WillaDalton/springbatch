package com.willadalton.springbatch.repository;

import com.willadalton.springbatch.domain.ActivePersonRow;
import com.willadalton.springbatch.domain.CsvPersonRecord;
import com.willadalton.springbatch.domain.PersonKey;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Repository
public class PersonRecordRepository {

    private final JdbcTemplate jdbcTemplate;

    public PersonRecordRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean existsActive(PersonKey key) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM PERSON_BATCH
                WHERE PERSON_NUMBER = ?
                  AND NOM = ?
                  AND PRENOM = ?
                  AND CODE_ENTREPRISE = ?
                  AND DATE_SORTIE IS NULL
                """,
                Integer.class,
                key.personNumber(), key.nom(), key.prenom(), key.companyCode()
        );
        return count != null && count > 0;
    }

    public void insert(CsvPersonRecord record, LocalDate entryDate) {
        jdbcTemplate.update(
                """
                INSERT INTO PERSON_BATCH (PERSON_NUMBER, NOM, PRENOM, CODE_ENTREPRISE, DATE_ENTREE, DATE_SORTIE)
                VALUES (?, ?, ?, ?, ?, NULL)
                """,
                record.personNumber(),
                record.nom(),
                record.prenom(),
                record.companyCode(),
                Date.valueOf(entryDate)
        );
    }

    public List<ActivePersonRow> findActiveRows() {
        return jdbcTemplate.query(
                """
                SELECT ID, PERSON_NUMBER, NOM, PRENOM, CODE_ENTREPRISE
                FROM PERSON_BATCH
                WHERE DATE_SORTIE IS NULL
                """,
                (rs, rowNum) -> new ActivePersonRow(
                        rs.getLong("ID"),
                        new PersonKey(
                                rs.getString("PERSON_NUMBER"),
                                rs.getString("NOM"),
                                rs.getString("PRENOM"),
                                rs.getString("CODE_ENTREPRISE")
                        )
                )
        );
    }

    public void closeMissingActiveRows(Set<PersonKey> keysInFile, LocalDate exitDate) {
        List<Long> idsToClose = new ArrayList<>();
        for (ActivePersonRow row : findActiveRows()) {
            if (!keysInFile.contains(row.key())) {
                idsToClose.add(row.id());
            }
        }

        if (!idsToClose.isEmpty()) {
            jdbcTemplate.batchUpdate(
                    "UPDATE PERSON_BATCH SET DATE_SORTIE = ? WHERE ID = ?",
                    idsToClose,
                    idsToClose.size(),
                    (ps, id) -> {
                        ps.setDate(1, Date.valueOf(exitDate));
                        ps.setLong(2, id);
                    }
            );
        }
    }
}
