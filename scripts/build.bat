@echo off
REM build.bat - Build script for Mirakas Java Application on Windows

REM Exit immediately if a command exits with a non-zero status
setlocal ENABLEEXTENSIONS ENABLEDELAYEDEXPANSION
set "ERROR_FLAG=0"

REM Directories
set "LIB_DIR=.\lib\java"
set "SRC_DIR=.\src"
set "BIN_DIR=.\bin"

REM JAR Files
set "DPJAR=dpuareu.jar"
set "CODEC_JAR=commons-codec-1.15.jar"
set "JSON_JAR=json-20230618.jar"

REM Native Libraries (if any)
set "NATIVE_LIB_DIR=.\lib\native\windows"

REM Classpath (use semicolon as separator on Windows)
set "CLASSPATH=%LIB_DIR%\%DPJAR%;%LIB_DIR%\%CODEC_JAR%;%LIB_DIR%\%JSON_JAR%"

REM Java Version
set "JAVA_VERSION=11"

REM Main Class
set "MAIN_CLASS=Main"

echo ==========================================
echo Building Mirakas Java Application
echo ==========================================

REM Create bin directory if it doesn't exist
if not exist "%BIN_DIR%" (
    echo Creating bin directory...
    mkdir "%BIN_DIR%"
    if ERRORLEVEL 1 (
        echo Failed to create bin directory.
        set "ERROR_FLAG=1"
    )
)

REM Compile Java source files
echo Compiling Java sources...
javac -source %JAVA_VERSION% -target %JAVA_VERSION% -classpath "%CLASSPATH%" -d "%BIN_DIR%" "%SRC_DIR%\*.java"
if ERRORLEVEL 1 (
    echo Compilation failed.
    set "ERROR_FLAG=1"
)

REM Check if compilation was successful before creating JAR
if "%ERROR_FLAG%"=="0" (
    echo Creating JAR file...
    jar -cvfm Mirakas.jar manifest.txt -C "%BIN_DIR%" .
    if ERRORLEVEL 1 (
        echo JAR creation failed.
        set "ERROR_FLAG=1"
    ) else (
        echo JAR created successfully: Mirakas.jar
    )
)

REM Final status
if "%ERROR_FLAG%"=="0" (
    echo Build completed successfully.
) else (
    echo Build encountered errors.
    exit /b 1
)

endlocal
