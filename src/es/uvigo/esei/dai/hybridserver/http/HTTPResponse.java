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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HTTPResponse {
	private HTTPResponseStatus status;
	private String version;
	private String content;
	private Map<String,String> parameterMap;

	public HTTPResponse() {
		status= HTTPResponseStatus.S503;
		version="";
		content=null;
		parameterMap = new HashMap<String, String>();
	}

	public HTTPResponseStatus getStatus() {
		return status;
	}

	public void setStatus(HTTPResponseStatus s) {
		this.status=s;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String v) {
		this.version=v;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content=content;
	}

	/** Devuelve la lista de parametros de la respuesta*/
	public Map<String, String> getParameters() {
		return parameterMap;
	}
	
	/** Anade un parametro*/
	public String putParameter(String name, String value) {
		parameterMap.put(name, value);
		return parameterMap.toString();
	}

	/** Devuelve cierto si el parametro existe*/
	public boolean containsParameter(String name) {
		return parameterMap.containsKey(name);
	}
	
	/** Elimina un parametro*/
	public String removeParameter(String name) {
		parameterMap.remove(name);
		return parameterMap.toString();
	}

	public void clearParameters() {
		parameterMap= new HashMap<String, String>();
	}
	
	/** Devuelve la lista de parametros*/
	public List<String> listParameters() {
		return  new ArrayList<String>(parameterMap.keySet());
	}

	/** Metemos los parametros en el writer */
	public void print(Writer writer) throws IOException {
		writer.write(version+" "+status.getCode()+" "+status.getStatus());	

		//Hay contenido
		if(content != null){
			writer.write("\r\n"+"Content-Length: "+content.length());
		}

		for(String parameter: listParameters()){
			writer.write("\r\n"+parameter+": "+parameterMap.get(parameter));
		}

		if(content != null){
			writer.write("\r\n\r\n"+content);
		}else{
			writer.write("\r\n\r\n");
		}
		
		writer.flush();
	}

	// Version Status CodStatus ContentType ContentEncoding ContentLanguage \r\n\r\n
	@Override
	public String toString() {
		final StringWriter writer = new StringWriter();

		try {
			this.print(writer);
		} catch (IOException e) {
		}

		return writer.toString();
	}
}
