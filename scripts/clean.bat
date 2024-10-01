@echo off
REM clean.bat - Clean script for Mirakas Java Application on Windows

REM Exit immediately if a command exits with a non-zero status
setlocal ENABLEEXTENSIONS ENABLEDELAYEDEXPANSION
set "ERROR_FLAG=0"

REM Directories
set "BIN_DIR=.\bin"
set "FINGERPRINTS_DIR=.\fingerprints"

echo ==========================================
echo Cleaning Mirakas Java Application Build Artifacts
echo ==========================================

REM Remove bin directory
if exist "%BIN_DIR%" (
    echo Removing bin directory...
    rmdir /s /q "%BIN_DIR%"
    if ERRORLEVEL 1 (
        echo Failed to remove bin directory.
        set "ERROR_FLAG=1"
    )
)

REM Remove JAR file
if exist "Mirakas.jar" (
    echo Removing Mirakas.jar...
    del /f /q "Mirakas.jar"
    if ERRORLEVEL 1 (
        echo Failed to remove Mirakas.jar.
        set "ERROR_FLAG=1"
    )
)

REM Remove fingerprints directory
if exist "%FINGERPRINTS_DIR%" (
    echo Removing fingerprints directory...
    rmdir /s /q "%FINGERPRINTS_DIR%"
    if ERRORLEVEL 1 (
        echo Failed to remove fingerprints directory.
        set "ERROR_FLAG=1"
    )
)

REM Final status
if "%ERROR_FLAG%"=="0" (
    echo Clean completed successfully.
) else (
    echo Clean encountered errors.
    exit /b 1
)

endlocal
