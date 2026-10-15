@echo off
setlocal EnableExtensions

echo ========================================
echo Android SDK / ADB PATH Setup
echo ========================================
echo.

set "ANDROID_SDK=%LOCALAPPDATA%\Android\Sdk"
set "PLATFORM_TOOLS=%ANDROID_SDK%\platform-tools"

rem --------------------------------------------------
rem Check Android SDK
rem --------------------------------------------------

if not exist "%ANDROID_SDK%" (
    echo [ERROR] Android SDK folder not found:
    echo %ANDROID_SDK%
    echo.
    pause
    exit /b 1
)

rem --------------------------------------------------
rem Check ADB
rem --------------------------------------------------

if not exist "%PLATFORM_TOOLS%\adb.exe" (
    echo [ERROR] adb.exe not found:
    echo %PLATFORM_TOOLS%\adb.exe
    echo.
    pause
    exit /b 1
)

echo [OK] Android SDK:
echo %ANDROID_SDK%
echo.

echo [OK] ADB:
echo %PLATFORM_TOOLS%\adb.exe
echo.

rem --------------------------------------------------
rem Set ANDROID_HOME
rem --------------------------------------------------

echo [INFO] Setting ANDROID_HOME...

reg add "HKCU\Environment" ^
    /v ANDROID_HOME ^
    /t REG_EXPAND_SZ ^
    /d "%ANDROID_SDK%" ^
    /f >nul

if errorlevel 1 (
    echo [ERROR] Failed to set ANDROID_HOME.
    pause
    exit /b 1
)

echo [OK] ANDROID_HOME

rem --------------------------------------------------
rem Set ANDROID_SDK_ROOT
rem --------------------------------------------------

echo [INFO] Setting ANDROID_SDK_ROOT...

reg add "HKCU\Environment" ^
    /v ANDROID_SDK_ROOT ^
    /t REG_EXPAND_SZ ^
    /d "%ANDROID_SDK%" ^
    /f >nul

if errorlevel 1 (
    echo [ERROR] Failed to set ANDROID_SDK_ROOT.
    pause
    exit /b 1
)

echo [OK] ANDROID_SDK_ROOT

rem --------------------------------------------------
rem Read current USER Path
rem --------------------------------------------------

echo [INFO] Checking User PATH...

set "USER_PATH="

for /f "tokens=2,*" %%A in ('reg query "HKCU\Environment" /v Path 2^>nul') do (
    set "USER_PATH=%%B"
)

rem --------------------------------------------------
rem Check whether platform-tools already exists
rem --------------------------------------------------

reg query "HKCU\Environment" /v Path 2>nul | find /I "%PLATFORM_TOOLS%" >nul

if not errorlevel 1 (
    echo [OK] platform-tools already exists in User PATH.
    goto :DONE
)

rem --------------------------------------------------
rem Add platform-tools
rem --------------------------------------------------

echo [INFO] Adding platform-tools to User PATH...

if defined USER_PATH (
    reg add "HKCU\Environment" ^
        /v Path ^
        /t REG_EXPAND_SZ ^
        /d "%USER_PATH%;%PLATFORM_TOOLS%" ^
        /f >nul
) else (
    reg add "HKCU\Environment" ^
        /v Path ^
        /t REG_EXPAND_SZ ^
        /d "%PLATFORM_TOOLS%" ^
        /f >nul
)

if errorlevel 1 (
    echo [ERROR] Failed to update User PATH.
    pause
    exit /b 1
)

echo [OK] platform-tools added to User PATH.

:DONE

echo.
echo ========================================
echo Setup Complete
echo ========================================
echo.
echo Close this terminal and open a NEW Command Prompt.
echo.
echo Test:
echo.
echo     adb version
echo     adb devices
echo.
pause