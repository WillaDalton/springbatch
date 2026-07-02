package com.willadalton.springbatch.batch;

import com.willadalton.springbatch.domain.CsvPersonRecord;
import com.willadalton.springbatch.repository.PersonRecordRepository;
import com.willadalton.springbatch.service.CurrentFileState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(PersonRecordRepository.class)
class PersonSyncScenariosTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PersonRecordRepository repository;

    private ItemWriter<CsvPersonRecord> writer;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM PERSON_BATCH");
        writer = new BatchConfiguration().csvPersonWriter(repository, new CurrentFileState());
    }

    @Test
    void shouldCreateRowForNonExistingPerson() throws Exception {
        CsvPersonRecord record = new CsvPersonRecord("100", "DUPONT", "ALICE", "ENT001");

        writer.write(new Chunk<>(List.of(record)));

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM PERSON_BATCH", Integer.class);
        Date entryDate = jdbcTemplate.queryForObject("SELECT DATE_ENTREE FROM PERSON_BATCH WHERE PERSON_NUMBER = '100'", Date.class);
        Date exitDate = jdbcTemplate.queryForObject("SELECT DATE_SORTIE FROM PERSON_BATCH WHERE PERSON_NUMBER = '100'", Date.class);

        assertThat(count).isEqualTo(1);
        assertThat(entryDate).isEqualTo(Date.valueOf(LocalDate.now()));
        assertThat(exitDate).isNull();
    }

    @Test
    void shouldNotCreateDuplicateWhenSameActivePersonAlreadyExists() throws Exception {
        insertRow("101", "DUPONT", "ALICE", "ENT001", LocalDate.of(2024, 1, 10), null);

        writer.write(new Chunk<>(List.of(new CsvPersonRecord("101", "DUPONT", "ALICE", "ENT001"))));

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM PERSON_BATCH WHERE PERSON_NUMBER = '101' AND NOM = 'DUPONT' AND PRENOM = 'ALICE' AND CODE_ENTREPRISE = 'ENT001'",
                Integer.class
        );

        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldCreateNewRowWhenActivePersonExistsInDifferentCompany() throws Exception {
        insertRow("102", "MARTIN", "BOB", "ENT001", LocalDate.of(2024, 2, 1), null);

        writer.write(new Chunk<>(List.of(new CsvPersonRecord("102", "MARTIN", "BOB", "ENT002"))));

        Integer totalCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM PERSON_BATCH WHERE PERSON_NUMBER = '102'", Integer.class);
        Integer activeCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM PERSON_BATCH WHERE PERSON_NUMBER = '102' AND DATE_SORTIE IS NULL", Integer.class);

        assertThat(totalCount).isEqualTo(2);
        assertThat(activeCount).isEqualTo(2);
    }

    @Test
    void shouldHandleSamePersonTwiceInTwoCompaniesWhenOneActiveAlreadyExists() throws Exception {
        insertRow("103", "DURAND", "CHLOE", "ENT001", LocalDate.of(2024, 3, 1), null);

        writer.write(new Chunk<>(List.of(
                new CsvPersonRecord("103", "DURAND", "CHLOE", "ENT001"),
                new CsvPersonRecord("103", "DURAND", "CHLOE", "ENT002")
        )));

        Integer totalCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM PERSON_BATCH WHERE PERSON_NUMBER = '103'", Integer.class);
        Integer company1Count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM PERSON_BATCH WHERE PERSON_NUMBER = '103' AND CODE_ENTREPRISE = 'ENT001'", Integer.class);
        Integer company2Count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM PERSON_BATCH WHERE PERSON_NUMBER = '103' AND CODE_ENTREPRISE = 'ENT002'", Integer.class);

        assertThat(totalCount).isEqualTo(2);
        assertThat(company1Count).isEqualTo(1);
        assertThat(company2Count).isEqualTo(1);
    }

    @Test
    void shouldCreateActiveRowWhenOnlyHistoricalRowExistsInSameCompany() throws Exception {
        insertRow("104", "MOREAU", "DAVID", "ENT003", LocalDate.of(2023, 1, 1), LocalDate.of(2024, 1, 1));

        writer.write(new Chunk<>(List.of(new CsvPersonRecord("104", "MOREAU", "DAVID", "ENT003"))));

        Integer totalCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM PERSON_BATCH WHERE PERSON_NUMBER = '104'", Integer.class);
        Integer activeCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM PERSON_BATCH WHERE PERSON_NUMBER = '104' AND DATE_SORTIE IS NULL", Integer.class);

        assertThat(totalCount).isEqualTo(2);
        assertThat(activeCount).isEqualTo(1);
    }

    @Test
    void shouldCloseActiveRowWhenPersonMissingFromFile() {
        insertRow("105", "BERNARD", "EMMA", "ENT004", LocalDate.of(2024, 4, 1), null);

        repository.closeMissingActiveRows(Set.of(), LocalDate.now());

        Date exitDate = jdbcTemplate.queryForObject(
                "SELECT DATE_SORTIE FROM PERSON_BATCH WHERE PERSON_NUMBER = '105' AND CODE_ENTREPRISE = 'ENT004'",
                Date.class
        );

        assertThat(exitDate).isEqualTo(Date.valueOf(LocalDate.now()));
    }

    private void insertRow(String personNumber, String nom, String prenom, String companyCode, LocalDate entryDate, LocalDate exitDate) {
        jdbcTemplate.update(
                """
                INSERT INTO PERSON_BATCH (PERSON_NUMBER, NOM, PRENOM, CODE_ENTREPRISE, DATE_ENTREE, DATE_SORTIE)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                personNumber,
                nom,
                prenom,
                companyCode,
                Date.valueOf(entryDate),
                exitDate == null ? null : Date.valueOf(exitDate)
        );
    }
}
