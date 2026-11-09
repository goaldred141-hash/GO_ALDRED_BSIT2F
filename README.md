# Public Market Price Monitoring System (PF201 - IT2F)

Console app: Java 17+, Maven, SQLite (sqlite-jdbc).

## Run in IntelliJ IDEA
1. *File > Open* -> select this folder's `pom.xml` -> "Open as Project".
2. Click the Maven **Reload** icon (downloads `sqlite-jdbc`).
3. Open `src/main/java/com/market/Main.java` and press the green Run arrow.
   (Or in the Terminal tab: `mvn compile exec:java`)

The database file is `students.db` in this folder (it is also created automatically if missing).
Open that same file in DB Browser for SQLite / SQLite Studio.

## Default login
`admin` / `admin123` (approved administrator). New registrations are PENDING until the admin approves them.

## Quick demo flow (generates all 4 mandatory audit events)
1. Register an Officer (menu 2) -> login as `admin` -> *3. Approve User Account*.
2. Login as the Officer -> add commodity, register vendor, *Encode Daily Price*.
3. Login as `admin` -> *4. Update User* (optional), *5. Validate Price Entries*, *6. View System Audit Logs*.
