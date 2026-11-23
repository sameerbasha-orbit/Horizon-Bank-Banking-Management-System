# HORIZON BANK — Banking Management System

A full-stack Java application prototype for demonstrating core banking workflows.

## Technology
- Backend: Java 21, Spring Boot 3.4.4, Maven
- Frontend: React + Vite
- Database: MySQL 8
- API: REST

> Educational project only. This is not a real banking system and must not process real money or store real customer financial data.

## Project structure
- `Banking_Backend/` — Spring Boot REST API
- `Banking_Frontend/` — React UI
- `Database/` — SQL schema

## Prerequisites
- JDK 21
- Node.js 20+
- MySQL 8
- Git

## Run backend
1. Create a MySQL database using `Database/horizon_bank.sql`.
2. Set environment variables `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.
3. Open `Banking_Backend` and run `mvn spring-boot:run`.
4. API health check: `http://localhost:8080/api/health`

For local development, defaults are:
- DB URL: `jdbc:mysql://localhost:3306/horizon_bank`
- DB username: `root`
- DB password: set your own value in the environment; do not commit credentials.

## Run frontend
```bash
cd Banking_Frontend
npm install
npm run dev
```
Frontend: `http://localhost:5173`

## Current milestone
Initial project scaffold: API health endpoint, React dashboard shell, database schema foundation. Banking workflows will be implemented and tested in subsequent milestones.
