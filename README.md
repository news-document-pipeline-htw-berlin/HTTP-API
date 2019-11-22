# Backend

Momentan ist nur dir Route /api/articles/by-id erreichbar

## Use-Cases aus Protokoll 3
+ nach Begriffen suchen (Schlagwortsuche)
→ auch mit Synonymen des Suchbegriffs
+ Default: z.B. die letzten 10 aktuellen
+ Anzahl an Begriffen pro Text
+ Filterung nach Metadaten (Datum [letzte Woche/Monat/...], Website/Quelle, AutorIn), verwandten Begriffen, eventuell Filterung mit Blacklist (welche Begriffe sollen nicht enthalten sein)
+ Filterung nach Kategorie (vorgegebene, z.B. Sport/Politik/Kultur und dynamische, z.B. Brexit, Demonstrationen Hongkong)
+ Suche
+ Analytics (z.B. wie oft wurde welcher Begriff im Verlauf der Zeit gesucht)
+ Schlagwortcluster
+ zeitlicher Verlauf
+ Duplikate erkennen/filtern
+ grafische Darstellung (z.B. für Kategorien/Tags/...)
+ Sortierung (nach Datum, Relevanz, Quelle)
+ eventuell Artikel exportieren (→ Druckerbutton)


## Routen (erster Entwurf)

| Method | Route                               | Parameters   |
| :----: |:------------------------------------| :-----------:|
| `GET`    | `/api/articles/by-id`               |  `id`        |
| `GET`    | `/api/articles/search`              |  `q` (search term),  `department`, `date`, `source` |
| `GET`    | `/api/articles/trending`            | `department` |
