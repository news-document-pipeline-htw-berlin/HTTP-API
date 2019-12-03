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

## Konfiguration

Konfigurationen für den Webserver befinden sich in `src/main/resources/application.conf`.

## Deployment

Zum erstellen einer `.jar` Datei den Befehl `sbt clean assembly` ausführen. Die generierte Datei befindet sich unter `target/scala<SCALA_VERSION>/inews-backend-assembly-<VERSION>.jar`.

Zum Deployment müssen die `.jar` Datei sowie `inewsbackend.service` auf den Server kopiert werden. Die `.service` Datei muss unter `/lib/systemd/system/` abgelegt werden. Der entsprechende Pfad für die `.jar` steht in der `.service` Datei.

Der Server wird dann mit `systemctl start inewsbackend.service` gestartet. [Mehr Infos](https://wiki.archlinux.org/index.php/systemd) 