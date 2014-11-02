#!/bin/bash

which keytool
if [ $? == 0 ]; then
	echo "java runtime found"
else 
	echo "java runtime not found"
	exit
fi

keytool -genkey -alias chatterServer -keyalg "RSA" -sigalg "SHA1withRSA" -keysize 2048 -validity 365
keytool -exportcert -alias chatterServer -file chatterCert.cer

