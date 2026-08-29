@echo off
setlocal
set GRADLE_VERSION=9.2.1
set ROOT=%~dp0
set BOOT=%ROOT%.gradle-bootstrap
set ZIP=%BOOT%\gradle-%GRADLE_VERSION%-bin.zip
set HOME=%BOOT%\gradle-%GRADLE_VERSION%

if not exist "%HOME%\bin\gradle.bat" (
  if not exist "%BOOT%" mkdir "%BOOT%"
  echo Downloading Gradle %GRADLE_VERSION%...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -UseBasicParsing 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%ZIP%'"
  if errorlevel 1 exit /b 1
  echo Extracting Gradle...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Force '%ZIP%' '%BOOT%'"
  if errorlevel 1 exit /b 1
)

call "%HOME%\bin\gradle.bat" %*
endlocal
