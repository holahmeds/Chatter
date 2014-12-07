@echo off

FOR /F "skip=2 tokens=3" %%A IN ('REG QUERY "HKLM\Software\JavaSoft\Java Runtime Environment" /v CurrentVersion') DO set CurrentVersion=%%A

IF NOT "%CurrentVersion%" == "" (
FOR /F "skip=2 tokens=2*" %%A IN ('REG QUERY "HKLM\Software\JavaSoft\Java Runtime Environment\%CurrentVersion%" /v JavaHome') DO set JAVAHOME=%%B
echo java runtime found
)

if "%JAVAHOME%" == "" (
echo java runtime not found
pause
exit
)

"%JAVAHOME%\bin\keytool.exe" -genkey -alias chatterServer -keyalg "RSA" -sigalg "SHA1withRSA" -keysize 2048 -validity 365 -keystore serverKeystore
"%JAVAHOME%\bin\keytool.exe" -exportcert -alias chatterServer -file chatterCert.cer -keystore serverKeystore

pause