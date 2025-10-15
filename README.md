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

