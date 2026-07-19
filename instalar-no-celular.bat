@echo off
setlocal

cd /d "%~dp0"

echo.
echo [Viso] Instalacao rapida no celular
echo.

where adb >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    set "ADB=adb"
) else if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" (
    set "ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
) else (
    echo Nao encontrei o adb.
    echo Instale o Android SDK Platform Tools ou abra o projeto pelo Android Studio uma vez.
    echo.
    pause
    exit /b 1
)

echo Procurando aparelho conectado...
"%ADB%" start-server >nul
"%ADB%" devices

for /f "skip=1 tokens=1,2" %%A in ('"%ADB%" devices') do (
    if "%%B"=="device" (
        set "ANDROID_SERIAL=%%A"
        goto :found_device
    )
)

echo.
echo Nenhum celular pronto para instalacao.
echo Confira se o cabo USB esta conectado e se a depuracao USB foi autorizada no telefone.
echo.
pause
exit /b 1

:found_device
echo.
echo Instalando no aparelho %ANDROID_SERIAL%...
call "%~dp0gradlew.bat" :app:installDebug --console=plain
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo A instalacao falhou.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo Abrindo o Viso...
"%ADB%" shell monkey -p com.viso 1 >nul 2>nul

echo.
echo Pronto.
pause
