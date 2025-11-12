Cloud Deployments:
Frontend: https://frontend-production-a446.up.railway.app
Backend: https://cs203g6-production-170f.up.railway.app

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
1. Run the new Flyway migration (e.g. `./mvnw flyway:migrate`) to create/upgrade the WITS tables.
2. Install the Python dependencies: `pip install psycopg[binary] xlrd`.
3. Make sure the metadata CSVs exist under `lookups/` (the repo includes `wits_country_metadata.csv` and `wits_hs_product_metadata.csv`, both derived from the official WITS Excel downloads). If you regenerate them elsewhere, point the importer at the new paths via `--country-metadata` / `--hs-metadata`.
4. Execute the importer from the project root, pointing it at the directory that contains all the WITS `.zip` files (the script streams them, no manual unzip needed). The lookup CSVs will be written to `./lookups` by default (override with `--lookup-dir` if desired):
   ```
   python scripts/import_wits_bulk.py \
     --input-dir "2983760_49725EA1-0/AVEPref" \
     --db-host localhost --db-port 5433 \
     --db-name tariffs --db-user dev --db-password devpass \
     --lookup-dir lookups
   ```
   Every successful ZIP import is recorded in the `wits_import_audit` table, so rerunning the command automatically resumes from the first unfinished archive. Pass `--force` if you need to reprocess every file, or tweak `--max-file-retries` (default `3`) to control how many times the script reconnects when the database drops during a batch.
5. After a successful import you can regenerate just the lookup CSVs (without reprocessing the archives) via:
   ```
   python scripts/import_wits_bulk.py --lookup-only --lookup-dir lookups
   ```
   This pulls distinct codes directly from the `wits_tariffs` table, ensuring the frontend/backend use the exact reporter/partner/product/nomenclature values present in the dataset.
