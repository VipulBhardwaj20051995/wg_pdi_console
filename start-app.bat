@echo off
echo Starting WovenGold PDI Application
echo ---------------------------------

:: Stop any running Java processes
echo Stopping existing Java processes...
taskkill /F /IM java.exe > nul 2>&1

:: Build the application
echo Building the application...
call mvnw.cmd clean package -DskipTests

:: Create uploads directory
if not exist uploads mkdir uploads

:: Run the application
echo Starting the application on port 9090...
echo API testing instructions are in api-tests.md
echo ---------------------------------
echo Open a new command prompt to test the API while this window is running
echo Press Ctrl+C to stop the application when done

java -jar target\wovengold-pdi-0.0.1-SNAPSHOT.jar --server.port=9090 