To test locally:

**REQUIREMENTS**:
1. .env file in frontend set to VITE_API_BASE_URL=http://localhost:8080
2. application.properties file profile set to `dev`
3. Docker Application up, database is on

**SETTING UP**:
1. open up docker desktop and run: docker compose up -d in terminal / open DB in the application itself
2. set up 'npm run dev' in frontend folder
3. ./mvnw spring-boot:run in project root folder
-------------------------------------------------------------------------------------------------------------------------
To test remotely:
1. .env file in frontend set to VITE_API_BASE_URL=https://cs203g6-production.up.railway.app
2. Ensure that someone is hosting the server

**Setting up**:
1. npm run dev in frontend folder

**TROUBLESHOOTING**:
1. Check if docker database is open and running
2. Ensure that backend is running, just because it says BUILD SUCCESSFUL does not mean its running
3. Ensure .env file is set properly
4. try compose down -v and compose up -d if issue with docker
5. try npm install in frontend folder if missing dependencies
6. If failing to build the backend, try ./mvnw clean package first (NOT ./mvnw clean)

Flyway Error:
- If backend has issue with the database due to Flyway, might need to repair, run:
**mvn -Dflyway.url=jdbc:postgresql://localhost:5433/tariffs -Dflyway.user=dev -Dflyway.password=devpass flyway:repair**

-------------------------------------------------------------------------------------------------------------------------
### Bulk WITS tariff data
1. Run the new Flyway migration (e.g. `./mvnw flyway:migrate`) to create the `wits_tariffs` table.
2. Install the Python dependency once: `pip install psycopg[binary]`.
3. Execute the importer from the project root, pointing it at the directory that contains all the WITS `.zip` files (the script streams them, no manual unzip needed). The lookup CSVs will be written to `./lookups` by default (override with `--lookup-dir` if desired):
   ```
   python scripts/import_wits_bulk.py \
     --input-dir "2983760_49725EA1-0/AVEPref" \
     --db-host localhost --db-port 5433 \
     --db-name tariffs --db-user dev --db-password devpass \
     --lookup-dir lookups
   ```
4. After a successful import you can regenerate just the lookup CSVs (without reprocessing the archives) via:
   ```
   python scripts/import_wits_bulk.py --lookup-only --lookup-dir lookups
   ```
   This pulls distinct codes directly from the `wits_tariffs` table, ensuring the frontend/backend use the exact reporter/partner/product/nomenclature values present in the dataset.
