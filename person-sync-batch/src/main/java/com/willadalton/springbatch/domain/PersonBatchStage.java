package com.willadalton.springbatch.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "PERSON_BATCH_STAGE")
public class PersonBatchStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "PERSON_NUMBER", nullable = false, length = 64)
    private String personNumber;

    @Column(name = "NOM", nullable = false, length = 128)
    private String nom;

    @Column(name = "PRENOM", nullable = false, length = 128)
    private String prenom;

    @Column(name = "CODE_ENTREPRISE", nullable = false, length = 6)
    private String codeEntreprise;

    protected PersonBatchStage() {
    }

    public PersonBatchStage(String personNumber, String nom, String prenom, String codeEntreprise) {
        this.personNumber = personNumber;
        this.nom = nom;
        this.prenom = prenom;
        this.codeEntreprise = codeEntreprise;
    }

    public Long getId() {
        return id;
    }

    public String getPersonNumber() {
        return personNumber;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getCodeEntreprise() {
        return codeEntreprise;
    }
}
