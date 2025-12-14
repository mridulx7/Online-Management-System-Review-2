# How to Run the Online Event Management System

## Prerequisites
- Java JDK 8 or higher
- MySQL Database Server
- Apache Tomcat 9.0 or higher (or any servlet container)
- All JAR files in the `lib/` directory

## Project Structure
```
Online Event Management System/
├── src/
│   ├── com/
│   │   ├── servlet/          # Servlet controllers
│   │   └── eventmgmt/
│   │       ├── model/        # Data models
│   │       ├── dao/          # Data access layer
│   │       ├── util/         # Utility classes
│   │       └── thread/       # Threading components
│   ├── test/                 # Test files
│   └── webapp/
│       ├── views/            # JSP pages
│       └── WEB-INF/          # Web configuration
├── lib/                      # Dependencies
├── bin/                      # Compiled classes
└── target/                   # Build output
```

## Step 1: Database Setup

### 1.1 Create Database
```sql
CREATE DATABASE event_management;
USE event_management;
```

### 1.2 Create Tables
```sql
-- Users table
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'ORGANIZER', 'ATTENDEE') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Events table
CREATE TABLE events (
    id INT PRIMARY KEY AUTO_INCREMENT,
    organizer_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    date DATE NOT NULL,
    time TIME,
    venue VARCHAR(200) NOT NULL,
    capacity INT NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (organizer_id) REFERENCES users(id)
);

-- Tickets table
CREATE TABLE tickets (
    id INT PRIMARY KEY AUTO_INCREMENT,
    event_id INT NOT NULL,
    user_id INT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10,2),
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('ACTIVE', 'CANCELLED') DEFAULT 'ACTIVE',
    FOREIGN KEY (event_id) REFERENCES events(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Registrations table
CREATE TABLE registrations (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    event_id INT NOT NULL,
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (event_id) REFERENCES events(id)
);
```

### 1.3 Insert Sample Data
```sql
-- Insert admin user (password: admin123)
INSERT INTO users (name, email, password, role) 
VALUES ('Admin User', 'admin@example.com', 'admin123', 'ADMIN');

-- Insert organizer user (password: org123)
INSERT INTO users (name, email, password, role) 
VALUES ('Event Organizer', 'organizer@example.com', 'org123', 'ORGANIZER');

-- Insert attendee user (password: att123)
INSERT INTO users (name, email, password, role) 
VALUES ('John Attendee', 'attendee@example.com', 'att123', 'ATTENDEE');
```

### 1.4 Update Database Connection
Edit `src/com/eventmgmt/util/DBConnection.java`:
```java
private static final String URL = "jdbc:mysql://localhost:3306/event_management";
private static final String USER = "your_mysql_username";
private static final String PASSWORD = "your_mysql_password";
```

## Step 2: Compile the Project

### 2.1 Compile Source Files
```bash
javac -d bin -cp "lib/*" src/com/eventmgmt/util/*.java src/com/eventmgmt/model/*.java src/com/eventmgmt/dao/*.java src/com/servlet/*.java
```

### 2.2 Compile Test Files (Optional)
```bash
javac -d bin -cp "bin;lib/*" src/test/java/com/eventmgmt/util/*.java src/test/java/com/eventmgmt/servlet/*.java
```

## Step 3: Run Tests (Optional)

### 3.1 Run All Tests
```bash
java -cp "bin;lib/junit-platform-console-standalone-1.9.3.jar;lib/javax.servlet-api-4.0.1.jar;lib/jqwik-1.7.4.jar;lib/junit-jupiter-api-5.9.3.jar;lib/junit-jupiter-engine-5.9.3.jar;lib/mysql-connector-j-8.3.0.jar" org.junit.platform.console.ConsoleLauncher --select-package com.eventmgmt --details=summary
```

Expected output:
```
Test run finished after 3354 ms
[       505 tests found           ]
[       505 tests successful      ]
[         0 tests failed          ]
```

### 3.2 Run Specific Test Class
```bash
java -cp "bin;lib/*" org.junit.platform.console.ConsoleLauncher --select-class com.eventmgmt.servlet.ErrorHandlingPropertyTest
```

## Step 4: Deploy to Tomcat

### 4.1 Create WAR File Structure
```
event-management.war
├── WEB-INF/
│   ├── web.xml
│   ├── classes/
│   │   └── com/
│   │       ├── servlet/
│   │       └── eventmgmt/
│   └── lib/
│       ├── mysql-connector-j-8.3.0.jar
│       └── javax.servlet-api-4.0.1.jar
└── views/
    ├── login.jsp
    ├── adminDashboard.jsp
    ├── organizerDashboard.jsp
    ├── attendeeDashboard.jsp
    ├── createEvent.jsp
    └── manageTickets.jsp
```

### 4.2 Build WAR File
```bash
# Create WAR directory structure
mkdir -p target/event-management/WEB-INF/classes
mkdir -p target/event-management/WEB-INF/lib
mkdir -p target/event-management/views

# Copy compiled classes
xcopy /E /I bin\com target\event-management\WEB-INF\classes\com

# Copy libraries
copy lib\mysql-connector-j-8.3.0.jar target\event-management\WEB-INF\lib\
copy lib\javax.servlet-api-4.0.1.jar target\event-management\WEB-INF\lib\

# Copy web.xml
copy src\webapp\WEB-INF\web.xml target\event-management\WEB-INF\

# Copy JSP files
xcopy /E /I src\webapp\views target\event-management\views

# Create WAR file
cd target\event-management
jar -cvf ..\event-management.war *
cd ..\..
```

### 4.3 Deploy to Tomcat
1. Copy `target/event-management.war` to Tomcat's `webapps/` directory
2. Start Tomcat server
3. Access the application at: `http://localhost:8080/event-management/`

## Step 5: Access the Application

### 5.1 Login Page
```
URL: http://localhost:8080/event-management/login
```

### 5.2 Test Credentials
- **Admin:**
  - Email: admin@example.com
  - Password: admin123
  - Access: User management, event approval

- **Organizer:**
  - Email: organizer@example.com
  - Password: org123
  - Access: Create/manage events

- **Attendee:**
  - Email: attendee@example.com
  - Password: att123
  - Access: Browse events, book tickets

## Step 6: Servlet Endpoints

### Authentication
- `GET /login` - Display login form
- `POST /login` - Process login
- `GET /login?action=logout` - Logout

### Admin Operations
- `GET /admin` - Get all users
- `GET /admin?userId=1` - Get specific user
- `POST /admin` - Create new user
- `PUT /admin` - Update user
- `DELETE /admin` - Delete user

### Organizer Operations
- `GET /organizer` - Get organizer's events
- `POST /organizer` - Create new event
- `PUT /organizer` - Update event
- `DELETE /organizer` - Delete event

### Event Operations
- `GET /events` - Get all events
- `GET /events?action=details&eventId=1` - Get event details
- `GET /events?action=search&query=...` - Search events
- `POST /events?action=approve&eventId=1` - Approve event (admin)
- `POST /events?action=reject&eventId=1` - Reject event (admin)

### Ticket Operations
- `GET /tickets` - Get user's tickets
- `POST /tickets` - Book ticket
- `DELETE /tickets?ticketId=1` - Cancel ticket

### Attendee Operations
- `GET /attendee?action=profile` - Get profile
- `GET /attendee?action=events` - Get registered events
- `POST /attendee` - Update profile

## Troubleshooting

### Database Connection Issues
- Verify MySQL is running
- Check database credentials in DBConnection.java
- Ensure MySQL JDBC driver is in lib/ directory

### Compilation Errors
- Verify Java version (JDK 8+)
- Check all JAR files are in lib/ directory
- Ensure classpath includes all dependencies

### Tomcat Deployment Issues
- Check Tomcat logs in `logs/catalina.out`
- Verify WAR file structure
- Ensure web.xml is properly configured

### Session Issues
- Clear browser cookies
- Check session timeout settings
- Verify session validation logic

## Testing Checklist

✅ Database connection successful  
✅ All 505 tests passing  
✅ No compilation errors  
✅ Servlets respond to HTTP requests  
✅ Session management working  
✅ Input validation working  
✅ Error handling working  
✅ JSP pages rendering correctly  

## Support

For issues or questions:
1. Check the REVIEW_2_COMPLIANCE.md document
2. Review the design document at .kiro/specs/servlet-implementation/design.md
3. Check the requirements at .kiro/specs/servlet-implementation/requirements.md
4. Review test results for specific functionality

---

**Project Status:** Ready for Review 2 ✅
