# 💸 Expense Splitter (Splitwise Clone)

A full-stack web application that helps you **split expenses among friends and groups**, similar to [Splitwise](https://splitwise.com).  
Built with **Spring Boot (Java)** for the backend and **React + Vite + Material UI** for the frontend.

---

## 🚀 Features

- 👥 **User Authentication & Authorization** (JWT-based login & registration)
- 📂 **Group Management** (create, join, and manage groups)
- 💵 **Expense Tracking** (add expenses, split costs among group members)
- 📊 **Balance Calculation** (see who owes whom and how much)
- ✅ **Settlements** (record payments to settle debts)
- 🔒 **Secure API** with Spring Security & CORS config
- 🗄️ **Database Migrations** using Flyway

---

## 🛠️ Tech Stack

### Backend
- Java 17+
- Spring Boot
- Spring Security (JWT Auth)
- Flyway (DB migrations)
- Maven

### Frontend
- React 19 (with Hooks & Context API)
- Vite (fast bundler)
- Material UI
- Axios
- React Router

### Database
- SQL-based (Flyway migrations included)

---

## 📂 Project Structure

```
expense-splitter-splitwise-dupe-master/
├── expense-splitter-backend/   # Spring Boot backend
│   ├── src/main/java/...       # Controllers, Services, Entities
│   ├── src/main/resources/     # Config & Flyway migrations
│   └── pom.xml
├── frontend/                   # React frontend
│   ├── src/                    # Components, Pages, Context
│   ├── public/                 # Static assets
│   └── package.json
└── README.md
```

---

## ⚡ Getting Started

### Prerequisites
- Java 17+
- Node.js 18+
- Maven
- SQL Database (e.g., MySQL, PostgreSQL, H2)

### Backend Setup
```bash
cd expense-splitter-backend
# Configure application.yml with DB credentials
mvn spring-boot:run
```

### Frontend Setup
```bash
cd frontend
npm install
npm run dev
```

Frontend will run at: **http://localhost:5173**  
Backend API will run at: **http://localhost:8080**

---

## 🧪 Running Tests

Backend:
```bash
cd expense-splitter-backend
mvn test
```
