@echo off
setlocal
set "APP_DIR=%~dp0"
"%APP_DIR%jre-minimal\bin\javaw.exe" -jar "%APP_DIR%bin\JapanStudySystem.jar"