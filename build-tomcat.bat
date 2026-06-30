@echo off
setlocal

set "PROJECT_DIR=%~dp0"
set "WEB_DIR=%PROJECT_DIR%WebContent"
set "CLASS_DIR=%WEB_DIR%\WEB-INF\classes"
set "WAR_FILE=%PROJECT_DIR%JAVA-COMPILER-PROJECT.war"

if not defined JAVA_HOME (
    echo JAVA_HOME is not set. Please set JAVA_HOME to a JDK, not a JRE.
    exit /b 1
)

set "SERVLET_API=C:\Program Files\Apache Software Foundation\Tomcat 9.0\lib\servlet-api.jar"
if not exist "%SERVLET_API%" (
    set "SERVLET_API=C:\Program Files\Java\jdk1.8.0_131\lib\missioncontrol\plugins\javax.servlet_3.0.0.v201112011016.jar"
)

if not exist "%SERVLET_API%" (
    echo Could not find servlet-api.jar.
    echo Install Apache Tomcat 9 or update SERVLET_API in this script.
    exit /b 1
)

if not exist "%CLASS_DIR%" mkdir "%CLASS_DIR%"

"%JAVA_HOME%\bin\javac" -encoding UTF-8 -cp "%SERVLET_API%" -d "%CLASS_DIR%" "%PROJECT_DIR%src\Compile.java" "%PROJECT_DIR%src\Run.java"
if errorlevel 1 exit /b 1

if exist "%WAR_FILE%" del "%WAR_FILE%"
pushd "%WEB_DIR%"
"%JAVA_HOME%\bin\jar" -cvf "%WAR_FILE%" *
popd

echo Built %WAR_FILE%
endlocal
