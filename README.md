# THS-Enhanced (COIT20258 A3)

This repository contains the **Telehealth System (THS-Enhanced)** JavaFX client and multi‑threaded Java server backed by MySQL.
It includes two creative features ready to demo:
- **Vital Trends Chart** (JavaFX LineChart) backed by live DB data
- **PDF Health Report** (PDFBox 3) with embedded chart and recent vitals table

> Built and tested with **JDK 23**, **Maven 3.9+**, **MySQL 8** and **JavaFX 23.0.1**.

---

## 1) Prerequisites

- **JDK 23** (set `JAVA_HOME` accordingly)
- **Apache Maven 3.9+**
- **MySQL 8.x** server and a database user (default in this guide: `ths/ths123`)
- macOS/Windows/Linux supported

> If you use NetBeans 23: open the client and server projects and run the provided run configs.

---

## 2) Database Setup (once)

1. Start MySQL and create a database user (change password as needed):
   ```sql
   CREATE DATABASE IF NOT EXISTS ths CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER IF NOT EXISTS 'ths'@'%' IDENTIFIED BY 'ths123';
   GRANT ALL PRIVILEGES ON ths.* TO 'ths'@'%';
   FLUSH PRIVILEGES;
   ```

2. Load schema and optional demo data:
   ```bash
   mysql -u ths -pths123 ths < sql/schema.sql
   mysql -u ths -pths123 ths < sql/seed.sql   # optional
   ```

> The server also ensures (creates) tables at startup, but running the SQL is recommended for marking.

---

## 3) Configure DB connection

The server reads DB settings from environment variables (recommended) **or** uses fallbacks:

- `THS_DB_URL` (default: `jdbc:mysql://localhost:3306/ths?useSSL=false&allowPublicKeyRetrieval=true`)
- `THS_DB_USER` (default: `ths`)
- `THS_DB_PASS` (default: `ths123`)

Example (macOS/Linux):
```bash
export THS_DB_URL='jdbc:mysql://localhost:3306/ths?useSSL=false&allowPublicKeyRetrieval=true'
export THS_DB_USER='ths'
export THS_DB_PASS='ths123'
```

---

## 4) Build (both projects)

From the repository root:
```bash
mvn -q -DskipTests clean install
```

---

## 5) Run (Terminal / Maven)

Open **two terminals**.

### A) Start the server
```bash
cd ths-server
mvn -q -DskipTests exec:java -Dexec.mainClass=com.mycompany.ths.server.THSServer
```
- The server listens on TCP **5555** and connects to the MySQL DB above.

### B) Start the JavaFX client
```bash
cd client
mvn -q javafx:run
```

Login with demo accounts (if you loaded `seed.sql`):
- Patient: `rayhan / p@ss`
- Doctor : `dr.ahmed / d@ss`

---

## 6) Run (NetBeans IDE)

1. **Open** the root folder; NetBeans will detect `client` and `ths-server` Maven projects.
2. Right‑click **ths-server** → **Run** (or use a NetBeans Action that runs `exec:java`).
3. Right‑click **client** → **Run** (uses `javafx-maven-plugin` to launch).
4. If NetBeans asks for JavaFX runtime, ensure **JDK 23** is selected for the project and the JavaFX Maven deps are present (they are).

Screenshots of these steps are in **docs/install_guide.pdf**.

---

## 7) Features to demonstrate

- **Appointments**: create/book (past times rejected), list by role.
- **Prescriptions**: patient requests refill; doctor updates status.
- **Referrals**: doctor creates & updates (datetime parsing `yyyy-MM-ddTHH:mm`).
- **Notes**: add/list by patient/doctor.
- **Vitals**: add/list; **Show Trends** to render the chart; **Export PDF** to save a report to `~/Downloads/THS_<user>_HealthReport.pdf`.

---

## 8) Troubleshooting

- **Port 5555 in use**: stop other instances or change the port in `THSServer` and client `Remote` utility.
- **MySQL auth plugin**: ensure user `ths` uses `mysql_native_password` or compatible default.
- **Fonts warning from PDFBox**: benign; PDF still generated.
- **FXML LoadException (Insets/AnchorPane)**: use the provided `vitals.fxml` (already fixed) and keep JavaFX 23.0.1.

---

## 9) License & Acknowledgements

- JavaFX by OpenJFX; PDF generation by Apache PDFBox.
- For unit marking only; not for production use.
