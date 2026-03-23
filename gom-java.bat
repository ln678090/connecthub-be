@echo off
setlocal EnableDelayedExpansion

cd /d "%~dp0"

set OUTPUT=java_all_code.txt
if exist "%OUTPUT%" del "%OUTPUT%"

echo ==================================================>> "%OUTPUT%"
echo JAVA PROJECT CODE DUMP >> "%OUTPUT%"
echo ROOT: %cd% >> "%OUTPUT%"
echo GENERATED: %date% %time% >> "%OUTPUT%"
echo ==================================================>> "%OUTPUT%"
echo.>> "%OUTPUT%"

for /r %%F in (*.java *.xml *.properties *.yml *.yaml *.gradle *.sql *.md) do (
    set "FULL=%%F"
    set "SKIP="

    echo !FULL! | findstr /i /c:"\.git\" >nul && set SKIP=1
    echo !FULL! | findstr /i /c:"\target\" >nul && set SKIP=1
    echo !FULL! | findstr /i /c:"\build\" >nul && set SKIP=1
    echo !FULL! | findstr /i /c:"\.idea\" >nul && set SKIP=1
    echo !FULL! | findstr /i /c:"\.settings\" >nul && set SKIP=1
    echo !FULL! | findstr /i /c:"\.mvn\" >nul && set SKIP=1
    echo !FULL! | findstr /i /c:"\out\" >nul && set SKIP=1

    if not defined SKIP (
        echo ==================================================>> "%OUTPUT%"
        echo FILE: %%F>> "%OUTPUT%"
        echo ==================================================>> "%OUTPUT%"
        type "%%F" >> "%OUTPUT%"
        echo.>> "%OUTPUT%"
        echo.>> "%OUTPUT%"
    )
)

echo Done. Output file: %OUTPUT%
pause