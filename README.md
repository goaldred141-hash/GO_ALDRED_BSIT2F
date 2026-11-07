# Public Market Price Monitoring System - Deliverable 5 - UserDao enhancements + Admin approval

Roles (from the approved proposal): ADMIN (Market Administrator), OFFICER (Price Monitoring Officer), CONSUMER (Public Viewer).

## Run (IntelliJ IDEA + Maven)
- File > Open > select this folder's pom.xml > Open as Project (Java 17+). Let Maven download dependencies.
- Run `com.pmpms.Main` (right-click > Run). Database file `market.db` is created in the project root.
- Or from a terminal: `mvn compile exec:java`
- Default admin (auto-created): username `admin`, password `admin123`

## Demo / screenshots
1. Register a user (option 2), then log in as admin / admin123.
2. Admin menu: 2 (View Pending) then 3 (Approve) -> enter the user id.
3. Screenshot 1: pending table, approval, success message. Screenshot 2: users table, status now APPROVED.
