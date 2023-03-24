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

 
package es.uvigo.esei.dai.hybridserver.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTTPRequest {
	private String method;
	private String resourceName;
	private String[] resourcePath={};
	private String resourceChain;
	private String content;
	private int contentLength;
	private Map<String,String> parametersMap;
	private Map<String,String> resparamMap;
	private String version;
	
	public HTTPRequest(Reader reader) throws IOException, HTTPParseException {
	
		System.out.println("HTTPREQUEST: ");

		method="";
		resourceName="";
		resourceChain="";
		content= null;
		contentLength= 0;
		version="";

		parametersMap= new LinkedHashMap<String, String>(); //Mapa ordenado
		resparamMap= new LinkedHashMap<String, String>();
     
		String line = "";
		BufferedReader buffReader =new BufferedReader(reader);
		
		line = buffReader.readLine(); //Primera linea -> "METHOD RESOURCE VERSION" o error
		System.out.println(line);
	
		try{
			//Comprobar si tiene error
			checkErroronLine(line);
		}catch(HTTPParseException e){
			throw new HTTPParseException(e.getMessage());
		}

		line = buffReader.readLine(); //Segunda linea
		String[] header_line=null;
	
		while(!line.isEmpty()) {
			//Linea es header
			if (line.contains(": ") && headervalido(line)) {
				// Linea es "header: content" entonces procesamos
				header_line = line.replace("\r\n", "").split(": ");
				parametersMap.put(header_line[0], header_line[1]);
			} else if(!headervalido(line)) {
				throw new HTTPParseException("Invalid header");
			}
		
			line = buffReader.readLine();

			System.out.println(line);
		}

		//Si hay contenido siempre hay content-length
		if(parametersMap.get("Content-Length")!= null){
			contentLength= Integer.parseInt(parametersMap.get("Content-Length"));

			line= "";
			int caracter= 0;

			while(buffReader.ready() && caracter!=-1){
				caracter= buffReader.read();
				line=line+(char)caracter;
			}
		
			if(line.charAt(line.length()-1) =='￿'){ //Si hay caracter extraño
				line= line.replaceFirst(".$", "");//Quitamos el caracter extrano del final				
			}
			
			System.out.println("CONTENIDO: "+line);

			content= line;
			if (parametersMap.get("Content-Type") != null && parametersMap.get("Content-Type").startsWith("application/x-www-form-urlencoded")) {
			    content = URLDecoder.decode(line, "UTF-8");
			}

			construirResMap(content);
		}
	}

	private boolean headervalido(String line) {
		return line.split(": ").length > 1;
	}

	public HTTPRequestMethod getMethod() {
		return HTTPRequestMethod.valueOf(method);
	}

	public String getResourceChain() {
		return resourceChain;
	}

	public String[] getResourcePath() {
		return resourcePath;
	}

	public String getResourceName() {
		return resourceName;
	}

	public Map<String, String> getResourceParameters() {
		return resparamMap;
	}

	public String getHttpVersion() {		
		return version;
	}

	public Map<String, String> getHeaderParameters() {		
		return parametersMap;
	}

	public String getContent() {
		return content;
	}

	public int getContentLength() {
		return contentLength;
	}

	/** Comprueba si hay algun error en la linea, sino extrae lo necesario de la linea*/
	public void checkErroronLine(String line) throws HTTPParseException {
		//System.out.println("Check error on line");
		String[] firstline=null;

		if(line!=null){
			firstline=line.split(" ");
		}else{
			System.out.println("Linea vacia");
		}
		

		if(line.split(" ").length==3){ //3 campos
			//Si no contiene error extrae lo que nos interesa de la primera linea
			extractResources(line);
		}else if(line.contains(": ")){
			throw new HTTPParseException("Missing first line");
		}else{
			if(!line.contains(HTTPHeaders.HTTP_1_1.getHeader())){
				throw new HTTPParseException("Missing version");
			}else if(!HTTPRequestMethod.values().toString().contains(firstline[0])){
				throw new HTTPParseException("Missing method");
			}else{
				throw new HTTPParseException("Missing resource");
			}
			// String[] mensajeError= line.split(" ");
			// throw new HTTPParseException(mensajeError[0]+line.replace(mensajeError[0],""));
		}
	}

	/** Inicializa method, resource* y version*/
	public void extractResources(String line) throws HTTPParseException{
		//System.out.println("Extract resources");

		Pattern patron= Pattern.compile(".*[?]", Pattern.CASE_INSENSITIVE); //Patron para encontrar el resource name
		String[] splittedFirstLine=line.split(" ");
			
		method= splittedFirstLine[0].toUpperCase(); //Obtenemos el metodo GET, POST, etc
		resourceChain=splittedFirstLine[1]; //RESOURCE CHAIN
			
		//Si hay resource params https://localhost:8888/html?uuid=251361732
		if(line.contains("?")){
			
			Matcher match= patron.matcher(splittedFirstLine[1]);
			if(match.find()) {
				resourceName=match.group().replaceFirst("/", "").replace("?", ""); //RESOURCE NAME
			}else {
				System.out.print("No se han encontrado coincidencias");
			}
				
			String tupla=resourceChain.toString().replace("/"+resourceName+"?",""); 
			//Completamos el mapa de recursos
			construirResMap(tupla);
		
		}else{
			resourceName=splittedFirstLine[1].replaceFirst("/","");
		}
			
		//Si solo hay un "/" en el nombre dejamos al path vacio, sino lo componemos
		if(!resourceName.equals("")){
			resourcePath= resourceName.split("/");
		}
		try{
			version= splittedFirstLine[2];
		}catch(IndexOutOfBoundsException ie){
			throw new HTTPParseException("Version does not exist");
		}
			
	}

	/** Construye resmap con el contenido */
	private void construirResMap(String line) {
		System.out.println("Construir resource map");
		String[] contenStrings= line.split("&");

		for(int i=0;i<contenStrings.length;i++) {
			String[] linea= contenStrings[i].split("=");
			try {
				resparamMap.put(linea[0],URLDecoder.decode(contenStrings[i], "UTF-8").replace(linea[0]+"=",""));

			} catch (UnsupportedEncodingException e) {
				System.out.println("UnsupportedEncoding");
				e.getStackTrace();
			}
			System.out.println("RESOURCEMAP completado: "+resparamMap.toString());
		}
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(this.getMethod().name()).append(' ').append(this.getResourceChain())
				.append(' ').append(this.getHttpVersion()).append("\r\n");

		for (Map.Entry<String, String> param : this.getHeaderParameters().entrySet()) {
			sb.append(param.getKey()).append(": ").append(param.getValue()).append("\r\n");
		}

		if (this.getContentLength() > 0) {
			sb.append("\r\n").append(this.getContent());
		}

		return sb.toString();
	}

}
