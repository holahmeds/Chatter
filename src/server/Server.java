package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Server {
	static final int SERVER_LISTEN_PORT = 11234;
	private static SimpleDateFormat formatter;
	private static ClientListener listener;
	private static Connection dbCon;

	public static void main(String[] args) {
		formatter = new SimpleDateFormat("[MM/dd/yyyy h:mm:ss a]");
		BufferedReader gimi = new BufferedReader(new InputStreamReader(System.in));
		
		System.setProperty("javax.net.ssl.keyStore", "chatterServerKeyStore.jks");
		System.setProperty("javax.net.ssl.keyStorePassword", "somethingortheother");
		
//		try {
//			dbCon = DriverManager.getConnection("jdbc:derby:serverDB;create=true");
//			checkDatabases();
//		} catch (SQLException e) {
//			log(e.toString());
//			return;
//		}
		
		listener = new ClientListener();
		new Thread(listener).start();
		
		String s;
		try {
			while((s=gimi.readLine()) != null) {
				if(s.equals("stop"))
					break;
			}
		} catch (IOException e) {
		} finally {
			listener.close();
//			try {
//				DriverManager.getConnection("jdbc:derby:serverDB;shutdown=true");
//			} catch (SQLException e) {
//				log(e.getMessage());
//			}
		}
	}

	public static void log(String s) {
		System.out.println(formatter.format(new Date()) + s);
	}
	
	private static void checkDatabases() throws SQLException {
		ResultSet databases = dbCon.getMetaData().getCatalogs();
		System.out.println(databases.getFetchSize());
		
		do {
			System.out.println(databases.getString(1));
		} while (databases.next());
		
		databases.close();
	}
}
