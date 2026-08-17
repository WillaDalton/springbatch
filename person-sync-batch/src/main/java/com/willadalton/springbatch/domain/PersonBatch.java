package com.willadalton.springbatch.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "PERSON_BATCH")
public class PersonBatch {

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

    @Column(name = "DATE_ENTREE", nullable = false)
    private LocalDate dateEntree;

    @Column(name = "DATE_SORTIE")
    private LocalDate dateSortie;

    protected PersonBatch() {
    }

    public PersonBatch(String personNumber, String nom, String prenom, String codeEntreprise, LocalDate dateEntree, LocalDate dateSortie) {
        this.personNumber = personNumber;
        this.nom = nom;
        this.prenom = prenom;
        this.codeEntreprise = codeEntreprise;
        this.dateEntree = dateEntree;
        this.dateSortie = dateSortie;
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

    public LocalDate getDateEntree() {
        return dateEntree;
    }

    public LocalDate getDateSortie() {
        return dateSortie;
    }

    public void setDateSortie(LocalDate dateSortie) {
        this.dateSortie = dateSortie;
    }
}
