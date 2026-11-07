# Public Market Price Monitoring System - Deliverable 4 - Entry point, DatabaseInitializer, FK pragma, Authentication

Roles (from the approved proposal): ADMIN (Market Administrator), OFFICER (Price Monitoring Officer), CONSUMER (Public Viewer).

## Run (IntelliJ IDEA + Maven)
- File > Open > select this folder's pom.xml > Open as Project (Java 17+). Let Maven download dependencies.
- Run `com.pmpms.Main` (right-click > Run). Database file `market.db` is created in the project root.
- Or from a terminal: `mvn compile exec:java`
- Default admin (auto-created): username `admin`, password `admin123`

## Demo / screenshots
1. Run Main. Choose 2 (Register), create an account with any role -> status PENDING.
2. Screenshot 1: terminal showing '[DB] Database initialized' + the registration SUCCESS message.
3. Screenshot 2: DB Browser for SQLite -> market.db -> users table showing the new row with PENDING.
