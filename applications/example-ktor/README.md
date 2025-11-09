# KTML + Ktor Example Application

A task management demo application showcasing KTML with Ktor server framework.

## Running the Application

```bash
./gradlew :example-ktor:run
```

The server will start on http://localhost:8080

## Test Accounts

All test users share the same password: **`password123`**

| Email               | Name          | Role  | Admin |
|---------------------|---------------|-------|-------|
| test@test.com       | Alice Johnson | Admin | ✅     |
| bob@example.com     | Bob Smith     | User  |       |
| charlie@example.com | Charlie Brown | User  |       |
| diana@example.com   | Diana Prince  | User  |       |

### After login, you'll have access to:

- Task dashboard with filtering
- User profile management
- Admin panel (for admin users only)
- Task statistics and overdue tracking
