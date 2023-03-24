/**
 *  HybridServer
 *  Copyright (C) 2022 Miguel Reboiro-Jato
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.uvigo.esei.dai.hybridserver;

import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.xml.ws.Endpoint;

public class HybridServer {
	private Thread serverThread;
	private boolean stop;
	private ExecutorService threadPool;

	private int httpPort=8888;
	private int numClients;
	private String webServiceURL;
	
	private List<ServerConfiguration> servers= null;
	
	private String dbUser;
	private String dbPassword;
	private String dbURL;
	
	//Database
	private HTMLDAO HTMLdao;
	private XMLDAO XMLdao;
	private XSDDAO XSDdao;
	private XSLTDAO XSLTdao;
	
	private Endpoint webService=null;
	    
	public HybridServer() {
		System.out.println("DEFAULT CONFIG");
		numClients=50; //50 por defecto

		dbURL= "jdbc:mysql://localhost:3306/hstestdb";
		dbUser= "hsdb";
		dbPassword="hsdbpass";
		
		HTMLdao= new HTMLDAO(dbURL, dbUser, dbPassword, httpPort);
		XMLdao= new XMLDAO(dbURL, dbUser, dbPassword, httpPort);
		XSDdao= new XSDDAO(dbURL, dbUser, dbPassword, httpPort);
		XSLTdao= new XSLTDAO(dbURL, dbUser, dbPassword, httpPort);
	}

	public HybridServer(Configuration config) {
		System.out.println("CONFIGURATION HYBRIDSERVER");
		
		try {
			this.httpPort=config.getHttpPort();
			this.numClients=config.getNumClients();
			this.webServiceURL=config.getWebServiceURL();
		
			this.dbUser=config.getDbUser();
			this.dbPassword=config.getDbPassword();
			this.dbURL=config.getDbURL();
		
			this.servers= config.getServers();  
		
			System.out.println("Charged Config: "+httpPort+" "+numClients+" "+webServiceURL+" "+dbUser+" "+dbPassword+" "+dbURL+"\n");
			
			HTMLdao= new HTMLDAO(dbURL, dbUser, dbPassword, httpPort);
			XMLdao= new XMLDAO(dbURL, dbUser, dbPassword, httpPort);
			XSDdao= new XSDDAO(dbURL, dbUser, dbPassword, httpPort);
			XSLTdao= new XSLTDAO(dbURL, dbUser, dbPassword, httpPort);

		}catch(Exception e) {
			System.out.println("Problem charging configuration, retry");
		}
	}

	public HybridServer(Properties properties) {

		System.out.println("PROPERTIES: CONFIG");
		httpPort= Integer.parseInt(properties.getProperty("port"));
		numClients= Integer.parseInt(properties.getProperty("numClients"));
		dbURL= properties.getProperty("db.url");
		dbUser=properties.getProperty("db.user");
		dbPassword=properties.getProperty("db.password");
		
		HTMLdao= new HTMLDAO(dbURL, dbUser, dbPassword, httpPort);
		XMLdao= new XMLDAO(dbURL, dbUser, dbPassword, httpPort);
		XSDdao= new XSDDAO(dbURL, dbUser, dbPassword, httpPort);
		XSLTdao= new XSLTDAO(dbURL, dbUser, dbPassword, httpPort);
	}

	public void start() {
		
		//Si hay servicio lo publicamos
		if(webServiceURL!=null){
			System.out.println(webServiceURL);
			webService= Endpoint.publish(webServiceURL,new HybridServerServiceImpl(HTMLdao, XMLdao, XSDdao, XSLTdao));
			System.out.println("WebService was published");
		}

		this.serverThread = new Thread() {
			@Override
			public void run() {
				try (final ServerSocket serverSocket = new ServerSocket(httpPort)) {			
					threadPool = Executors.newFixedThreadPool(numClients);
					System.out.println("ThreadPool initialized");
					while (true) {
							Socket socket = serverSocket.accept();

							if (stop) break;
							threadPool.execute(new ServiceThread(socket, HTMLdao, XMLdao, XSDdao, XSLTdao, servers)); // Pasamos el socket, los dao y los servidores
					}
				} catch (IOException e) {
					System.out.println("Server socket could not be created");
				}
			}
		};

		this.stop = false;
		this.serverThread.start();
	}

	public void stop() {
		this.stop = true;

		try (Socket socket = new Socket("localhost", httpPort)) {
			// Esta conexión se hace, simplemente, para "despertar" el hilo servidor
			this.serverThread.join();

			//Finalizamos servicio en caso de que exista
			if(webService != null) {
				webService.stop();
			}
			
		 	//Finalizacion pool
		 	threadPool.shutdownNow();
		 	threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		}catch (IOException e) {
			throw new RuntimeException(e);
		}catch (InterruptedException e) {
			e.getStackTrace();
		}

		this.serverThread = null;
	}

	public int getPort() {
		return httpPort;
	}
}
