# Database Setup Guide

## Issue
The error "JDBC exception executing SQL ...e does not exist" indicates that database tables are missing.

## Solution

### Option 1: Restart Application (Recommended)
1. **Stop** your Spring Boot application
2. **Restart** it - Hibernate will automatically create missing tables with `spring.jpa.hibernate.ddl-auto=update`

### Option 2: Force Table Creation
If restarting doesn't work, temporarily change in `application.properties`:
```properties
spring.jpa.hibernate.ddl-auto=create-drop
```
**WARNING**: This will DELETE all existing data!

Then restart the application. After tables are created, change back to:
```properties
spring.jpa.hibernate.ddl-auto=update
```

### Option 3: Manual Table Creation
If you need to preserve data, you can manually create the missing tables in PostgreSQL:

```sql
-- Create tags table
CREATE TABLE IF NOT EXISTS tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT true
);

-- Create issues table (if missing)
CREATE TABLE IF NOT EXISTS issues (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255),
    description TEXT,
    category VARCHAR(255),
    status VARCHAR(50),
    location_id UUID REFERENCES locations(id),
    photo_url VARCHAR(255),
    date_reported TIMESTAMP,
    date_resolved TIMESTAMP,
    reported_by UUID REFERENCES users(id)
);

-- Create notifications table (if missing)
CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message TEXT,
    channel VARCHAR(50),
    sent_at TIMESTAMP,
    delivered BOOLEAN,
    recipient_id UUID NOT NULL REFERENCES users(id),
    issue_id UUID REFERENCES issues(id)
);

-- Create issue_tags join table (if missing)
CREATE TABLE IF NOT EXISTS issue_tags (
    issue_id UUID REFERENCES issues(id) ON DELETE CASCADE,
    tag_id UUID REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (issue_id, tag_id)
);
```

## Current Configuration
- Database: PostgreSQL
- Connection: `jdbc:postgresql://localhost:5432/community_alert`
- DDL Mode: `update` (creates/updates tables without dropping data)

## Verification
After restarting, check that these tables exist:
- `tags`
- `issues`
- `notifications`
- `issue_tags` (join table)

You can verify in PostgreSQL:
```sql
\dt
```

