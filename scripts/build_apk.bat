@echo off
echo ========================================================
echo   Building Personal AI Life OS Android APK
echo ========================================================
cd /d "%~dp0..\android"

echo Running Gradle assembleDebug...
call gradlew.bat assembleDebug

if %ERRORLEVEL% equ 0 (
    echo.
    echo ========================================================
    echo   BUILD SUCCESSFUL!
    echo   Your APK is ready at:
    echo   android\app\build\outputs\apk\debug\app-debug.apk
    echo ========================================================
) else (
    echo.
    echo ========================================================
    echo   Notice: Local Android SDK build failed or SDK not configured.
    echo   To get the APK directly:
    echo   1. Push this project to your GitHub repository.
    echo   2. GitHub Actions will automatically compile the APK and give
    echo      you a 1-click downloadable ZIP containing app-debug.apk!
    echo   3. Or open the 'android' folder in Android Studio and click
    echo      Build -> Build Bundle(s) / APK(s) -> Build APK(s).
    echo ========================================================
)
pause
