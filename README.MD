# Test de spring batch

Projet Spring Batch Maven qui lit un fichier CSV (`personNumber,nom,prenom,companyCode`) et synchronise les lignes dans la table `PERSON_BATCH`.

## Règles métier implémentées

- Si une ligne du fichier n'existe pas en base (ou existe seulement avec `DATE_SORTIE` renseignée), insertion d'une nouvelle ligne avec:
  - `DATE_ENTREE = date du jour`
  - `DATE_SORTIE = NULL`
- Si une ligne existe déjà avec `DATE_SORTIE = NULL`, aucune action.
- Si une ligne active (`DATE_SORTIE = NULL`) n'est plus dans le fichier, mise à jour avec `DATE_SORTIE = date du jour`.

Le batch enregistre également le résultat d'exécution dans la table `EXEC_BATCH` (`EXECUTION_NUMBER`, `EXECUTION_DATE`, `STATUS`).

## Lancer le build

```bash
mvn clean package
```

## Lancer le batch

```bash
mvn spring-boot:run
```

Le fichier d'entrée par défaut est `src/main/resources/input/personnes.csv`.
