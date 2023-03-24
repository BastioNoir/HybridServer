package es.uvigo.esei.dai.hybridserver;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class ConfigurationContentHandler implements ContentHandler {
	Configuration configuration;
	List<ServerConfiguration> servers;

	//Variables para poder acceder a los valores con characters
	boolean http_accessed;
	boolean ws_accessed;
	boolean nc_accessed;
	boolean us_accessed;
	boolean pass_accessed;
	boolean url_accessed;

	public Configuration getConfiguration() {
		return this.configuration;
	}
	
	public ConfigurationContentHandler(){
		http_accessed= false;
		ws_accessed  = false;
		nc_accessed  = false;
		us_accessed  = false;
		pass_accessed= false;
		url_accessed = false;
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		//  Auto-generated method stub

	}

	@Override
	public void startDocument() throws SAXException {
		this.configuration=new Configuration();
		this.servers= new ArrayList<ServerConfiguration>();
	}

	@Override
	public void endDocument() throws SAXException {
		//  Auto-generated method stub

	}

	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		//  Auto-generated method stub

	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		//  Auto-generated method stub

	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		if("connections".equals(localName)) {
			//System.out.println("CONNECTIONS ACCESSED");
		}else if("http".equals(localName)) {
			//System.out.println("HTTP ACCESSED");
			http_accessed=true;
		}else if("webservice".equals(localName)) {
			//System.out.println("WEBSERVICE ACCESSED");
			ws_accessed=true;
		}else if("numClients".equals(localName)) {
			//System.out.println("NUMCLIENTS ACCESSED");
			nc_accessed=true;
		}else if("database".equals(localName)) {
			//System.out.println("DATABASE ACCESSED");
		}else if("user".equals(localName)) {
			//System.out.println("USER ACCESSED");
			us_accessed=true;
		}else if("password".equals(localName)) {
			//System.out.println("PASSWORD ACCESSED");
			pass_accessed=true;
		}else if("url".equals(localName)) {
			//System.out.println("URL ACCESSED");
			url_accessed=true;
		}else if("servers".equals(localName)) {
			//System.out.println("SERVERS ACCESSED");
			url_accessed=true;
		}else if("server".equals(localName)) {
			//Accede a cada uno de los servers
			//System.out.println("SERVER ACCESSED");
			//System.out.println("ADDED SERVER WITH PARAMS: "+atts.getValue("name")+" "+atts.getValue("wsdl")+" "+atts.getValue("namespace")+" "+atts.getValue("service")+" "+atts.getValue("httpAddress"));
			servers.add(new ServerConfiguration(atts.getValue("name"),atts.getValue("wsdl"),atts.getValue("namespace"),atts.getValue("service"),atts.getValue("httpAddress")));
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		//Actualizamos la lista de servidores
		this.configuration.setServers(servers);
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		String textContent = new String(ch, start, length);

		if(http_accessed) {
			//System.out.println("http: " + textContent);
			this.configuration.setHttpPort(Integer.parseInt(textContent));
			http_accessed=false;
		}else if(ws_accessed) {
			//System.out.println("webservice: " + textContent);
			this.configuration.setWebServiceURL(textContent);
			ws_accessed=false;
		}else if(nc_accessed) {
			//System.out.println("numClients: " + textContent);
			this.configuration.setNumClients(Integer.parseInt(textContent));
			nc_accessed=false;
		}else if(us_accessed) {
			//System.out.println("user: " + textContent);
			this.configuration.setDbUser(textContent);
			us_accessed=false;
		}else if(pass_accessed) {
			//System.out.println("password: " + textContent);
			this.configuration.setDbPassword(textContent);
			pass_accessed=false;
		}else if(url_accessed) {
			//System.out.println("url: " + textContent);
			this.configuration.setDbURL(textContent);
			url_accessed=false;
		}
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		//  Auto-generated method stub
	}

	@Override
	public void processingInstruction(String target, String data) throws SAXException {
		//  Auto-generated method stub
	}

	@Override
	public void skippedEntity(String name) throws SAXException {
		//  Auto-generated method stub
	}

}
