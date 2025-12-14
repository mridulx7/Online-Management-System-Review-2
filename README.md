# Online Event Management System

## 📌 Project Overview
The **Online Event Management System** is a Java-based web application designed to manage events efficiently.  
It allows **Admins**, **Event Organizers**, and **Attendees** to interact through dedicated dashboards, enabling event creation, ticket management, registrations, and system monitoring.

The project is built using **Core Java**, **JDBC**, **Servlets**, and **JSP**, following proper layered architecture and OOP principles.

---

## 🧩 User Roles
- **Admin**: Manages users, approves events, and monitors system activities.
- **Event Organizer**: Creates and manages events, tickets, and communicates with attendees.
- **Attendee**: Registers for events, purchases tickets, and receives updates.

---

## 🛠️ Technologies Used
- **Java (Core & Advanced)**
- **Servlets & JSP**
- **JDBC**
- **MySQL**
- **Apache Tomcat 9**
- **Eclipse IDE**
- **HTML**

- Online Event Management System
├── src
│ └── com.eventmgmt
│ ├── dao (Database operations)
│ ├── model (Entity classes)
│ ├── util (DB connection & utilities)
│ ├── thread (Multithreading features)
│ └── servlet (Servlet controllers)
├── webapp
│ ├── views (JSP pages)
│ └── WEB-INF
│ └── web.xml
├── README.md


---

## ⚙️ Core Features
- User authentication & role-based dashboards
- Event creation and approval workflow
- Ticket management and booking
- Database-driven operations using DAO pattern
- Input validation and exception handling
- Multithreading for background tasks (notifications, booking)

---

## 🔌 Database Connectivity
- JDBC is used to connect the application with MySQL.
- DAO classes handle all database operations.
- Connection management is implemented using a utility class.

---

## 🚀 How to Run the Project
1. Install **Java JDK 11+**
2. Install **Apache Tomcat 9**
3. Import the project into **Eclipse (Enterprise Edition)**
4. Add **MySQL Connector JAR** to the build path
5. Configure database credentials in `DBConnection.java`
6. Run the project using **Run on Server (Tomcat)**
7. Open browser:


---

## 📄 Documentation
- Code is modular, well-structured, and commented.
- Follows OOP principles such as inheritance, polymorphism, interfaces, and exception handling.
- Clear separation between UI, business logic, and database layers.

---

## ✨ Future Enhancements
- Payment gateway integration
- Email/SMS notifications
- Advanced analytics dashboard
- Improved UI with modern frameworks

---



## 🙏 Acknowledgment
This project was developed as part of an academic evaluation to demonstrate Java programming, database integration, and web application development skills.


---

## 🗂️ Project Structure
