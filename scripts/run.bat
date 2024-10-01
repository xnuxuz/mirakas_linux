@echo off
REM run.bat - Run script for Mirakas Java Application on Windows

REM Exit immediately if a command exits with a non-zero status
setlocal ENABLEEXTENSIONS ENABLEDELAYEDEXPANSION
set "ERROR_FLAG=0"

REM Directories
set "LIB_DIR=.\lib\java"
set "NATIVE_LIB_DIR=.\lib\native\windows"

REM Classpath (use semicolon as separator on Windows)
set "CLASSPATH=Mirakas.jar;%LIB_DIR%\*"

REM Main Class
set "MAIN_CLASS=Main"

echo ==========================================
echo Running Mirakas Java Application
echo ==========================================

REM Set PATH to include native libraries
set "PATH=%NATIVE_LIB_DIR%;%PATH%"

REM Run the Java application
echo Executing Java application...
%JAVA_HOME%\bin\java.exe -cp "%CLASSPATH%" %MAIN_CLASS%
if ERRORLEVEL 1 (
    echo Java application encountered an error.
    set "ERROR_FLAG=1"
)

REM Final status
if "%ERROR_FLAG%"=="0" (
    echo Application ran successfully.
) else (
    echo Application encountered errors.
    exit /b 1
)

endlocal
