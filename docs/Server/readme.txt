Before first start-
	Execute 'create certificate.bat'.
	This creates a 'chatterCert.cer' file which needs to be imported by
	the client before they can connect to the server.

To start the server execute 'start srver.bat'

Server Commands-

	useradd {username} {password}
		create a new user with the specified username and password.
		(The user should change this password through the client for
		security purposes.)
	
	useradd {username} {password}
		Change the password of user. This should be used only to 'reset'
		the password if the user forgot it. (Once again the user should
		change the password through the client afterwords.)
	
	stop
		Stop the server
