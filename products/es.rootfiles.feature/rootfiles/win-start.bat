@echo off
SET OSGI_CONSOLE=localhost:7234
SET ELS_HOMEDIR=%HOMEPATH%\elexis-server
mkdir %ELS_HOMEDIR%\logs
start elexis-server -console %OSGI_CONSOLE% > %ELS_HOMEDIR%\logs\console.log
echo connect to elexis-server osgi console: telnet %OSGI_CONSOLE%
