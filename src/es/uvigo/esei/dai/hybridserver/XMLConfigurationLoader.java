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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class XMLConfigurationLoader {
	public static Configuration load(File xmlFile) throws Exception { 
		System.out.println("XMLCONFIGURATOR");
		Configuration configuration=null;
		try {
			//Tenemos que hacerlo así porque no funciona con el ../configuration.xsd interno del xml sino podría hacerse una validacion interna
			configuration = parseAndValidateCONFIGWithExternalXSD(xmlFile,"./configuration.xsd",new ConfigurationContentHandler());
			System.out.println("Loaded XML Configuration ");
		}catch(Exception e) {
			e.printStackTrace();
			throw new Exception();
		}
		return configuration;
	}
	
	// Procesado y validación con un XSD interno de un XML string con SAX
	public static Configuration parseAndValidateCONFIGWithInternalXSD(File xmlFile, ConfigurationContentHandler handler) throws ParserConfigurationException, SAXException, IOException{
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		parserFactory.setValidating(true);
		parserFactory.setNamespaceAware(true);
		// Se añade el manejador de errores y se activa la validación
		// por schema
		SAXParser parser = parserFactory.newSAXParser();
		parser.setProperty(
		"http://java.sun.com/xml/jaxp/properties/schemaLanguage",
		XMLConstants.W3C_XML_SCHEMA_NS_URI
		);
		
		XMLReader xmlReader = parser.getXMLReader();
		xmlReader.setContentHandler(handler);
		xmlReader.setErrorHandler(new SimpleErrorHandler());
		
		// Parsing
		try (FileReader fileReader = new FileReader(xmlFile)) {
			xmlReader.parse(new InputSource(fileReader));
		}
		
		return handler.getConfiguration();
	}
		
	// Procesado y validación con un XSD externo de un XML string con SAX
		public static Configuration parseAndValidateCONFIGWithExternalXSD(File xmlFile, String schemaPath, ConfigurationContentHandler handler) throws ParserConfigurationException, SAXException, IOException{
			handler= new ConfigurationContentHandler();
			// Construcción del schema
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = schemaFactory.newSchema(new File(schemaPath));
		
			// Construcción del parser del documento.
			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
			parserFactory.setValidating(false);
			parserFactory.setNamespaceAware(true);
			parserFactory.setSchema(schema);
		
			// Se añade el manejador de errores
			SAXParser parser = parserFactory.newSAXParser();
			XMLReader xmlReader = parser.getXMLReader();
			xmlReader.setContentHandler(handler);
			xmlReader.setErrorHandler(new SimpleErrorHandler());
		
			// Parsing
			try (FileReader fileReader = new FileReader(xmlFile)) {
				xmlReader.parse(new InputSource(fileReader));
			}
			
			return handler.getConfiguration();
		}
		
	/** Procesado y validación con un XSD externo de un XML string con SAX*/
	public static void parseAndValidateXMLWithExternalXSD(String xmlContent, String schemaContent, ContentHandler handler) throws ParserConfigurationException, SAXException, IOException{
		//Convertimos el schema puro a source
		Source schemaSource = new StreamSource(new ByteArrayInputStream(schemaContent.getBytes()));
		
		handler= new ConfigurationContentHandler();
		// Construcción del schema
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = schemaFactory.newSchema(schemaSource);
	
		// Construcción del parser del documento.
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		parserFactory.setValidating(false);
		parserFactory.setNamespaceAware(true);
		parserFactory.setSchema(schema);
	
		// Se añade el manejador de errores
		SAXParser parser = parserFactory.newSAXParser();
		XMLReader xmlReader = parser.getXMLReader();
		xmlReader.setContentHandler(handler);
		xmlReader.setErrorHandler(new SimpleErrorHandler());
	
		// Parsing
		try (StringReader stringReader = new StringReader(xmlContent)) {
			xmlReader.parse(new InputSource(stringReader));
		}
	}
	
	/** Aplica XSLT a un XML y devuelve el HTML generado. Un ejemplo podría ser con el fichero ./configuration.xsl para los ficheros de configuracion si queremos mostrarlo*/
	public static String transformXMLwithXSLT(String xmlContent, String schemaContent) throws TransformerException {
		ByteArrayOutputStream stream= new ByteArrayOutputStream();
		//Convertimos el schema puro a source
		Source xmlSource = new StreamSource(new ByteArrayInputStream(xmlContent.getBytes()));
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer(new StreamSource(new ByteArrayInputStream(schemaContent.getBytes())));

		//Transforma el XML y lo guarda en stream
		transformer.transform(xmlSource, new StreamResult(stream));
		
		//Devolvemos el HTML
		return new String(stream.toByteArray());
	}
}
