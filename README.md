# ScriptedHardcore 1.2.0

Paper-Plugin für lokale Todesnachrichten und konfigurierbare Todessperren. **Keine LifeSteal-Mechanik, kein Herzklau und keine Wither-Events oder Wither-Sounds.** Die normalen Minecraft-Herzen bleiben unverändert.

## Voraussetzungen
- Paper 1.21.4 (API-Ziel; kompatible 1.21.x-Versionen können funktionieren)
- Java 21 JDK
- Gradle installiert oder Projekt in IntelliJ IDEA öffnen und Gradle importieren

## Bauen
Im Projektordner ausführen:

```bash
gradle build
```

Die JAR liegt danach in `build/libs/ScriptedHardcore-1.2.0.jar`.

## Installieren
1. Server stoppen und ein Backup erstellen.
2. JAR in den Serverordner `plugins/` kopieren.
3. Server starten.
4. `plugins/ScriptedHardcore/config.yml` konfigurieren.
5. Server neu starten.

## Verhalten
- Vanilla-Todesnachrichten werden unterdrückt und nur im `death-radius` an Spieler in derselben Welt gesendet.
- Es gibt keine Wither-Events, keine Wither-Sounds und keinen automatischen Wither-Spawn.
- Spieler behalten ihre normalen Minecraft-Herzen. Kein Herzverlust und kein Herzklau.
- Wenn `death-ban.enabled: true` ist, wird der Spieler nach seinem Tod temporär oder permanent gesperrt.

## Konfiguration
```yaml
death-radius: 80

death-ban:
  enabled: true
  mode: TEMPORARY # TEMPORARY oder PERMANENT
  temporary-ban-minutes: 60
  reason: "Du bist auf diesem Scripted-Hardcore-Server gestorben."
  kick-message: "&cDu bist gestorben und kannst vorerst nicht mehr beitreten."

local-death-message: "&7{player} &cist gestorben."
```

## Befehle
- `/hardcore help`
- `/hardcore radius <Bloecke>`
- `/hardcore deathban temporary`
- `/hardcore deathban permanent`
- `/hardcore deathban off`
- `/hardcore unban <Spieler>`

Admin-Berechtigung: `scriptedhardcore.admin` (standardmäßig OP).

## Wichtige Hinweise
- Mit `PERMANENT` kann ein Tod den Spieler dauerhaft sperren, bis ein Admin `/hardcore unban <Spieler>` ausführt. Erst auf einem Testserver ausprobieren.
- In dieser Version verwendet die Banliste Spielernamen. Für moderne Offline-/Proxy-Konfigurationen sollte die Ban-Logik ggf. auf UUIDs angepasst werden.
- Die JAR wurde in dieser Umgebung nicht mit Gradle kompiliert oder auf einem echten Paper-Server getestet.


## Automatischer Build auf GitHub
1. Erstelle auf GitHub ein neues Repository.
2. Lade den gesamten Inhalt dieses Projektordners hoch, einschließlich des versteckten Ordners `.github/workflows/build.yml`.
3. Öffne im Repository den Tab **Actions** und wähle **Build ScriptedHardcore JAR**.
4. Der Build startet bei einem Push automatisch. Du kannst ihn auch unter **Run workflow** manuell starten.
5. Öffne den abgeschlossenen Workflow-Lauf. Unter **Artifacts** lade `ScriptedHardcore-1.2.0` herunter und entpacke die ZIP-Datei darin. Die JAR darin ist die Datei für den Server.

Der Workflow baut mit Java 21 und Gradle 8.10 und speichert die JAR als GitHub Actions Artifact für 14 Tage.
