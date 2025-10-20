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

## Testing: SQL injection test cases (safe, local)

This project includes an integration test that verifies common SQL-injection-style inputs against the tariff history endpoint and (optionally) authenticated endpoints. The tests are configured to run against an isolated in-memory H2 database and are wrapped in transactions with automatic rollback to avoid permanent changes.

Quick steps to run the tests safely:

1. Run only the SQL-injection test class (uses H2 via `test` profile):

```bash
mvn -Dspring.profiles.active=test -Dtest=TariffHistorySqlInjectionTests test
```

2. Run the entire test suite under the test profile (recommended for CI):

```bash
mvn -Dspring.profiles.active=test test
```

Notes on authenticated tests

- The file `src/test/java/com/example/tariffkey/controller/TariffHistorySqlInjectionTests.java` includes additional authenticated tests. These are intentionally commented out by default.
- To enable them safely:
  1. Make sure you run tests with the `test` profile (see commands above). This uses an in-memory H2 DB and Flyway migrations so the schema matches production, but the data is ephemeral.
  2. Uncomment the authenticated test block in the test file.
  3. The tests rely on a test helper `TestAuthHelper` which registers and logs in a test user via `/auth/register` and `/auth/login`. Because the `test` profile uses H2, this registration happens only in the in-memory DB.
  4. Tests are annotated with `@Transactional` and `@Rollback` — all DB changes are rolled back at the end of each test method.

Best practices and extra safety

- Always run tests with the `test` profile to avoid touching your development/production DB.
- Consider running tests in CI where the environment is ephemeral (e.g., GitHub Actions) to further isolate side effects.
- If you prefer extra insurance, you can temporarily disable Flyway migrations in the test profile and instead use a dedicated test schema; or add explicit row-count assertions in tests to confirm no destructive changes occurred.

Troubleshooting

- If tests fail with authentication errors, check `AuthController` and `TestAuthHelper` to ensure the register/login endpoints are available and the test user creation logic matches your validation rules (e.g., password complexity).
- If tests fail with DB errors, verify `src/test/resources/application-test.properties` exists and sets `spring.datasource.url=jdbc:h2:mem:testdb`.


