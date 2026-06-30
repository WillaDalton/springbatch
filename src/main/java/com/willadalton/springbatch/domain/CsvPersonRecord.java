package com.willadalton.springbatch.domain;

public record CsvPersonRecord(String personNumber, String nom, String prenom, String companyCode) {

    public PersonKey toKey() {
        return new PersonKey(personNumber, nom, prenom, companyCode);
    }
}
