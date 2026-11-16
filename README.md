# Recruitment & Placement Management System

Java 17 + JDBC + MySQL 8 with a browser-based UI and Java HTTP API.

## 1. Database
1. Start your MySQL Server service.
2. In MySQL Workbench, open and execute `sql/database_schema.sql` (only if you have not already created the schema).
3. Execute `sql/sample_data.sql` and `sql/sample_candidates.sql` if the sample records have not already been loaded. Do not execute them repeatedly because they insert duplicate unique records.
4. The database name is `recruitment_placement`.

## 2. Set your MySQL password
Open `config.properties` in this folder and change `db.password=root` to the password of your MySQL `root` account. Keep username `root` unless you use another MySQL account. Do not share this file publicly.

## 3. Start the connected website
Install JDK 17+ and Apache Maven. Open a terminal in this project folder and run:

```bash
mvn clean compile
mvn exec:java
```

Keep the terminal open. Open `http://localhost:8080` in Chrome. The website is served by Java and uses the same MySQL database; do not open the HTML files directly with `file://`.

Windows users can also double-click `run-windows.bat` after configuring `config.properties`.

## Demo login
If you loaded the supplied `sql/sample_data.sql`, use:
- Email: `recruiter@demo.com`
- Password: `password`

The supplied sample SQL uses a placeholder password hash for this demonstration account. Replace it with a real authentication setup before production use.

## Notes
- UI pages load candidate, company, job, application, interview, placement and dashboard information through the Java API.
- Candidate, company, job, application and interview records can be created from their UI forms. Candidate deletion is available from the candidates list; database foreign-key constraints can prevent deleting records already in use.
- This is a local academic/demo application. It is not production-hardened; add real password hashing, authorization, CSRF protection, validation and audit controls before public deployment.
