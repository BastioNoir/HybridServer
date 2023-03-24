package es.uvigo.esei.dai.hybridserver;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import es.uvigo.esei.dai.hybridserver.http.HTTPHeaders;
import es.uvigo.esei.dai.hybridserver.http.HTTPParseException;
import es.uvigo.esei.dai.hybridserver.http.HTTPRequest;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponse;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponseStatus;

public class ServiceThread implements Runnable {

    private final Socket socket;
    private int service_port;
    
    //Lista con los servidores cargados en configuracion
    List<ServerConfiguration> servers;
    
    //Controllers
	private Controller HTMLController;
	private Controller XMLController;
	private Controller XSDController;
	private Controller XSLTController;

	//Controller que se usará en cada oeración, puede ser HTML, XML, XSD o XSLT
    private Controller actualController;

    public ServiceThread(Socket socket, HTMLDAO HTMLdao, XMLDAO XMLdao, XSDDAO XSDdao, XSLTDAO XSLTdao, List<ServerConfiguration> servers) throws IOException {
        System.out.println("SERVICE THREAD\n\n");
        
    	this.socket = socket;
        this.servers= servers;
      
        //Creamos controllers pasandole los servidores con los que contactarán cuando no encuentren algo en su base de datos
        HTMLController= new HTMLController(HTMLdao, servers);
	    XMLController = new XMLController(XMLdao, servers);
	    XSDController = new XSDController(XSDdao, servers);
	    XSLTController= new XSLTController(XSLTdao, servers);

        actualController=HTMLController; //DEFECTO
    }

    @Override
    public void run() {
        try (Socket socket = this.socket) {
            System.out.println("-------------------------------------------------------------------------------------------------");
        try {
            System.out.println("CREATED REQUEST");
            HTTPRequest request = new HTTPRequest(new InputStreamReader(socket.getInputStream()));
            
            System.out.println("\nREQUEST HAS FINISHED");
            generarResponseDAO(request, socket);
    
        } catch (HTTPParseException e) {
            e.getStackTrace();
        }    
        } catch (IOException e) {
            e.getStackTrace();
        }
    }

    /** Genera un response que tenga que ver con la modificacion de la base de datos */
    public void generarResponseDAO(HTTPRequest request, Socket socket) {
        
    	//WELCOME
        if(request.getResourceChain().equals("/")){
            generarWelcome(socket);
        }

        //Selecciona la respuesta adecuada según sea HTML, XML, XSD o XSLT
        else if (request.getResourceChain().contains("html")) {
                switch (request.getMethod()) {
                    case GET:
                        if (request.getResourceChain().contains("html?uuid=")) {
                            System.out.println("GET HTML");
                            try{
                                generarGETResponse("HTML",request, socket);
                            }catch(DBConnectionException e){
                                //No hay conexion con la base de datos, se devuelve un error 500
                                System.err.println("Connection Error 500");
                                generarERRORResponse(request, socket, HTTPResponseStatus.S500);
                            }
                            break;
                        }else if(request.getResourceChain().equals("/html")){
                        	//LIST SÓLO cuando llega /html
                            System.out.println("LIST HTML");
                            try{
                                generarLISTResponse("HTML",request, socket);
                            }catch(DBConnectionException e){
                                System.err.println("Connection Error 500");
                                generarERRORResponse(request, socket, HTTPResponseStatus.S500);
                            }
                            break;
                        } 
                    case POST:
                        System.out.println("POST HTML");
                        try{
                            generarPOSTResponse("HTML",request, socket);
                        }catch(DBConnectionException e){
                            System.err.println("Connection Error 500");
                            generarERRORResponse(request, socket, HTTPResponseStatus.S500);
                        }
                        break;
                    case DELETE:
                        System.out.println("DELETE HTML");
                        try{    
                            generarDELETEResponse("HTML", request, socket);
                        }catch(DBConnectionException e){
                            System.err.println("Connection Error 500");
                            generarERRORResponse(request, socket, HTTPResponseStatus.S500);
                        }
                        break;
                    default:
                        System.out.println("Unable to generate a response");
                        break;
                }
        }else if(request.getResourceChain().contains("xml")){
            switch (request.getMethod()) {
                case GET:
                    if (request.getResourceChain().contains("xml?uuid=")) {
                        System.out.println("GET XML");
                        try{
                        	if(request.getResourceParameters().containsKey("xslt")) {
                        		generarHTMLconXML(request, socket);
                        	}else {
                        		generarGETResponse("XML",request, socket);
                        	}
                        }catch(DBConnectionException e){
                            //No hay conexion con la base de datos, se devuelve un error 500
                            System.err.println("Connection Error 500");
                            generarERRORResponse(request, socket, HTTPResponseStatus.S500);
                        }
                        break;
                    }else if(request.getResourceChain().equals("/xml")){
                    	//LIST XML cuando /xml
                        System.out.println("LIST XML");
                        try{
                            generarLISTResponse("XML",request, socket);
                        }catch(DBConnectionException e){
                            System.err.println("Connection Error 500");
                            generarERRORResponse(request, socket, HTTPResponseStatus.S500);
                        }
                        break;
                    } 
                case POST:
                    System.out.println("POST XML");
                    try{
                        generarPOSTResponse("XML",request, socket);
                    }catch(DBConnectionException e){
                        System.err.println("Connection Error 500");
                        generarERRORResponse(request, socket, HTTPResponseStatus.S500);
                    }
                    break;
                case DELETE:
                    System.out.println("DELETE XML");
                    try{    
                        generarDELETEResponse("XML", request, socket);
                    }catch(DBConnectionException e){
                        System.err.println("Connection Error 500");
                        generarERRORResponse(request, socket, HTTPResponseStatus.S500);
                    }
                    break;
                default:
                    System.out.println("Unable to generate a response");
                    break;
            }
        }else if(request.getResourceChain().contains("xsd")){
            switch (request.getMethod()) {
                case GET:
                    if (request.getResourceChain().contains("xsd?uuid=")) {
                        System.out.println("GET XSD");
                        try{
                            generarGETResponse("XSD",request, socket);
                        }catch(DBConnectionException e){
                            //No hay conexion con la base de datos, se devuelve un error 500
                            System.err.println("Connection Error 500");
                            generarERRORResponse(request, socket, HTTPResponseStatus.S500);
                        }
                        break;
                    }else if(request.getResourceChain().equals("/xsd")){
                        //LIST XSD cuando /xsd
                    	System.out.println("LIST XSD");
                        try{
                            generarLISTResponse("XSD",request, socket);
                        }catch(DBConnectionException e){
                            System.err.println("Connection Error 500");
                            generarERRORResponse(request, socket, HTTPResponseStatus.S500);
                        }
                        break;
                    } 
                case POST:
                    System.out.println("POST XSD");
                    try{
                        generarPOSTResponse("XSD",request, socket);
                    }catch(DBConnectionException e){
                        System.err.println("Connection Error 500");
                        generarERRORResponse(request, socket, HTTPResponseStatus.S500);
                    }
                    break;
                case DELETE:
                    System.out.println("DELETE XSD");
                    try{    
                        generarDELETEResponse("XSD", request, socket);
                    }catch(DBConnectionException e){
                        System.err.println("Connection Error 500");
                        generarERRORResponse(request, socket, HTTPResponseStatus.S500);
                    }
                    break;
                default:
                    System.out.println("Unable to generate a response");
                    break;
            }
        }else if(request.getResourceChain().contains("xslt")){
            switch (request.getMethod()) {
                case GET:
                    if (request.getResourceChain().contains("xslt?uuid=")) {
                        System.out.println("GET XSLT");
                        try{
                            generarGETResponse("XSLT",request, socket);
                        }catch(DBConnectionException e){
                            //No hay conexion con la base de datos, se devuelve un error 500
                            System.err.println("Connection Error 500");
                            generarERRORResponse(request, socket, HTTPResponseStatus.S500);
                        }
                        break;
                    }else if(request.getResourceChain().equals("/xslt")){
                        //LIST XSLT cuando /xslt
                    	System.out.println("LIST XSLT");
                        try{
                            generarLISTResponse("XSLT",request, socket);
                        }catch(DBConnectionException e){
                            System.err.println("Connection Error 500");
                            generarERRORResponse(request, socket, HTTPResponseStatus.S500);
                        }
                        break;
                    } 
                case POST:
                    System.out.println("POST XSLT");
                    try{
                        generarPOSTResponse("XSLT",request, socket);
                    }catch(DBConnectionException e){
                        System.err.println("Connection Error 500");
                        generarERRORResponse(request, socket, HTTPResponseStatus.S500);
                    }
                    break;
                case DELETE:
                    System.out.println("DELETE XSLT");
                    try{    
                        generarDELETEResponse("XSLT", request, socket);
                    }catch(DBConnectionException e){
                        System.err.println("Connection Error 500");
                        generarERRORResponse(request, socket, HTTPResponseStatus.S500);
                    }
                    break;
                default:
                    System.out.println("Unable to generate a response");
                    break;
            }
        }else{
            generarERRORResponse(request, socket, HTTPResponseStatus.S400);
        }

    }

    /** Genera respuesta GET dependiendo del tipo type*/
    public void generarGETResponse(String type, HTTPRequest request, Socket socket) throws DBConnectionException {
        
    	//Escogemos controlador según el tipo
    	selectController(type);
        
        HTTPResponse response = new HTTPResponse();
        String resourceChain = request.getResourceChain();


        if (resourceChain.contains("html?uuid=")) {
            System.out.println("ResourceChain: " + resourceChain);

            String[] url_uuid = resourceChain.split(Pattern.quote("html?uuid=")); // Pattern.quote para que pueda splitear, ya que la / no funciona

            if (actualController.list().contains(url_uuid[1])) { // Si el uuid es valido 
                System.out.println("RESPUESTA CORRECTA");
                response.setStatus(HTTPResponseStatus.S200); // OK
                
                System.out.println("CONTROLLER: "+actualController.toString());
                response.setContent(actualController.get(url_uuid[1])); // Obtiene el contenido del uuid
            } else{
                System.out.println("RESPUESTA FALLIDA GET HTML");
                response.setStatus(HTTPResponseStatus.S404);
            }

            response.putParameter("Content-Type", "text/html");

        }else if(resourceChain.contains("xml?uuid=")){
            System.out.println("ResourceChain: " + resourceChain);

            String[] url_uuid = resourceChain.split(Pattern.quote("xml?uuid=")); // Pattern.quote para que pueda splitear, ya que la / no funciona

            if (actualController.list().contains(url_uuid[1])) { // Si el uuid es valido 
                System.out.println("RESPUESTA CORRECTA");
                response.setStatus(HTTPResponseStatus.S200); // OK
                
                response.setContent(actualController.get(url_uuid[1])); // Obtiene el contenido del uuid
            } else{
                System.out.println("RESPUESTA FALLIDA, NO EXISTE ESTE XML");
                response.setStatus(HTTPResponseStatus.S404);
            }

            response.putParameter("Content-Type", "application/xml");

        }else if(resourceChain.contains("xsd?uuid=")){
            System.out.println("ResourceChain: " + resourceChain);

            String[] url_uuid = resourceChain.split(Pattern.quote("xsd?uuid=")); // Pattern.quote para que pueda splitear, ya que la / no funciona

            if (actualController.list().contains(url_uuid[1])) { // Si el uuid es valido 
                System.out.println("RESPUESTA CORRECTA");
                response.setStatus(HTTPResponseStatus.S200); // OK
                
                response.setContent(actualController.get(url_uuid[1])); // Obtiene el contenido del uuid
            } else{
                System.out.println("RESPUESTA FALLIDA XSD GET");
                response.setStatus(HTTPResponseStatus.S404);
            }

            response.putParameter("Content-Type", "application/xml");
        }
        else if(resourceChain.contains("xslt?uuid=")){
            System.out.println("ResourceChain: " + resourceChain);

            String[] url_uuid = resourceChain.split(Pattern.quote("xslt?uuid=")); // Pattern.quote para que pueda splitear, ya que la / no funciona
            System.out.println("MIRAMOS SI LA LISTA CONTIENE DE XSLT CONTIENE EL NUEVO POSTEADO\nUUID: "+url_uuid[1]);
            
            if (actualController.list().contains(url_uuid[1])) { // Si el uuid es valido 
                System.out.println("RESPUESTA CORRECTA, LA LISTA CONTIENE DE XSLT CONTIENE EL NUEVO POSTEADO\nUUID: "+url_uuid[1]);
                response.setStatus(HTTPResponseStatus.S200); // OK
                
                response.setContent(actualController.get(url_uuid[1])); // Obtiene el contenido del uuid
            } else{
                System.out.println("RESPUESTA FALLIDA XSLT GET");
                response.setStatus(HTTPResponseStatus.S404);
            }

            response.putParameter("Content-Type", "application/xml");
        }

        response.setVersion(HTTPHeaders.HTTP_1_1.getHeader()); // HTTP11
        response.putParameter("Content-Language", "en");

        try {
            response.print(new OutputStreamWriter(socket.getOutputStream()));
            System.out.println("RESPONSE ENVIADO");

        } catch (IOException e) {
            e.getStackTrace();
        }
    }
    
    /** Devuelve el HTML correspondiente a XML dado si valida un XSD asociado*/
    public void generarHTMLconXML(HTTPRequest request, Socket socket) throws DBConnectionException {
    	System.out.println("###########################################################generarHTMLconXML");
    	HTTPResponse response = new HTTPResponse();
        String resourceChain = request.getResourceChain();

        if(resourceChain.contains("xml?uuid=")){
            System.out.println("ResourceChain GEN HTML WITH XSLT: " + resourceChain);

            if (XMLController.list().contains(request.getResourceParameters().get("uuid"))) { // Si el uuid es valido 
                System.out.println("XML VALIDO");
                
                // 1. Recupera el XML
                String XMLDoc= XMLController.get(request.getResourceParameters().get("uuid"));
                System.out.println("XML DOC: "+XMLDoc);
                
                System.out.println("\n#######################################################################");
                System.out.println("XML RECUPERADO HTML/XSLT");
                if(XSLTController.list().contains(request.getResourceParameters().get("xslt"))) {
                	System.out.println("XSLT VALIDO");
                	
                	// 2. Recuperar el id del XSD asociado al XSLT
                	System.out.println("XSLT UUID: "+request.getResourceParameters().get("xslt"));
                	String XSDUUID= XSLTController.getXSD(request.getResourceParameters().get("xslt"));
                    System.out.println("UUID del XSD asociado al XSLT: "+XSDUUID);
                    System.out.println("RECUPERADO XSD HTML/XSLT");
                    
                    // 3. Recuperar el XSD
                    String XSDDoc= XSDController.get(XSDUUID);
                    System.out.println("XSD DOC: "+XSDDoc);
                    
                    try {
                    	// 4. Validar el XML con el XSD
                    	XMLConfigurationLoader.parseAndValidateXMLWithExternalXSD(XMLDoc, XSDDoc, new DefaultHandler());
                    	
                    	// 5. Si valida recuperamos el XSLT
                    	String XSLTDoc = XSLTController.get(request.getResourceParameters().get("xslt"));
                    	System.out.println("XSLT DOC: "+XSLTDoc);
                    	
                        // 6. Transformar el XML con el XSLT
                    	String HTMLByXMLDoc= XMLConfigurationLoader.transformXMLwithXSLT(XMLDoc, XSLTDoc);
                    	System.out.println("HTML DOC: "+HTMLByXMLDoc);
                    	
                        // 7. Devolver HTML (El Content-Type del response tiene que ser html)
                    	response.setStatus(HTTPResponseStatus.S200);
                    	response.setContent(HTMLByXMLDoc);
                    	response.putParameter("Content-Type", "text/html");
                    	
                    }catch(SAXException|IOException|ParserConfigurationException e){
                    	//El schema seleccionado no lo valida o no es un schema valido
                    	System.out.println("RESPUESTA FALLIDA, EXCEPCION IO");
                        response.setStatus(HTTPResponseStatus.S400);
                    }catch(TransformerException e) {
                    	e.printStackTrace();
                    	System.out.println("RESPUESTA FALLIDA, NO SE HA PODIDO TRANSFORMAR");
                        response.setStatus(HTTPResponseStatus.S400);
                    }
                }else {
                	System.out.println("XSLT NO VALIDO");
                    response.setStatus(HTTPResponseStatus.S404);
                }
                
            } else{
            	System.out.println("XML NO VALIDO");
                response.setStatus(HTTPResponseStatus.S404);
            }

        }

        response.setVersion(HTTPHeaders.HTTP_1_1.getHeader()); // HTTP11
        response.putParameter("Content-Language", "en");

        try {
            response.print(new OutputStreamWriter(socket.getOutputStream()));
            System.out.println("RESPONSE ENVIADO");

        } catch (IOException e) {
            e.getStackTrace();
        }
    }
    
    /** Genera respuesta POST */
    public void generarPOSTResponse(String type, HTTPRequest request, Socket socket) throws DBConnectionException{
        selectController(type);

        HTTPResponse response = new HTTPResponse();
        String uuid= generarUUID();

        if (actualController.list().contains(uuid)) { // Si el uuid ya existe
            System.out.println("RESPUESTA FALLIDA POST");
            response.setStatus(HTTPResponseStatus.S404);
        } else {
    
            final Pattern patternHTML = Pattern.compile("html=");
            final Pattern patternXML = Pattern.compile("xml=");
            final Pattern patternXSD = Pattern.compile("xsd=");
            final Pattern patternXSLT = Pattern.compile("xslt=");

            final Matcher matcherHTML = patternHTML.matcher(request.getContent());
            final Matcher matcherXML = patternXML.matcher(request.getContent());
            final Matcher matcherXSD = patternXSD.matcher(request.getContent());
            final Matcher matcherXSLT = patternXSLT.matcher(request.getContent());

            //No es html ni xml ni xsd ni xslt ERROR 400, el contenido empieza por html=/xml=/xsd=/xslt=
            if(matcherHTML.find()){
                System.out.println("RESPUESTA CORRECTA HTML");
                String content= request.getContent().replaceFirst("^.*=","");

                actualController.post(uuid, content);
    
                System.out.println("He añadido el contenido "+content+" al DAOmap con UUID: "+uuid);
        
                response.setStatus(HTTPResponseStatus.S200);
                
                //Enviamos la pagina, ponemos 2 enlaces porque uno es necesario para uno de los test, y el otro es para que sea accesible el contenido del uuid al hacer un post
                response.setContent("<html><head></head><body><a href=\"html?uuid=" + uuid + "\">" + uuid + "</a><br>"+
                                                             "<p>Recurso Accesible: </p><a href=http://localhost:"+service_port+"/html?uuid="+uuid+">"+uuid+"</a></body></html>"); //Para poder acceder al recurso

                response.putParameter("Content-Type", "text/html");

            }else if(matcherXML.find()){
                System.out.println("RESPUESTA CORRECTA XML");
                String content= request.getContent().replaceFirst("^.*=","");

                actualController.post(uuid, content);
    
                System.out.println("He añadido el contenido "+content+" al DAOmap con UUID: "+uuid);
        
                response.setStatus(HTTPResponseStatus.S200);
                
                response.setContent("<html><head></head><body><a href=\"xml?uuid=" + uuid + "\">" + uuid + "</a><br><p>Recurso Accesible: </p><a href=http://localhost:"+service_port+"/xml?uuid="+uuid+">"+uuid+"</a></body></html>");

                response.putParameter("Content-Type", "application/xml");

            }else if(matcherXSLT.find()){
                System.out.println("RESPUESTA CORRECTA XSLT");
                Map <String, String> linkedXSD= request.getResourceParameters();
                
                if(!linkedXSD.containsKey("xsd")){
                    //Si no llega con XSD asociado
                    System.out.println("NO EXISTE XSD EN EL CONTENIDO");
                    response.setStatus(HTTPResponseStatus.S400);
                }else{
                	System.out.println("LISTA XSD: "+XSDController.list());
                    if(!XSDController.list().contains(linkedXSD.get("xsd"))){
                        //El xsd no existe
                        System.out.println("NO EXISTE XSD EN LA TABLA DE XSD");
                        response.setStatus(HTTPResponseStatus.S404);
                    }else if(XSDController.list().contains(linkedXSD.get("xsd"))){
                        //El xsd existe
                        System.out.println("EXISTE XSD EN TABLA DE XSD");

                        System.out.println("Contenido XML: "+linkedXSD.get("xslt"));
                        System.out.println("Contenido XSD: "+linkedXSD.get("xsd"));

                        actualController.post(uuid, linkedXSD.get("xslt"), linkedXSD.get("xsd"));
    
                        System.out.println("He añadido el contenido "+linkedXSD.get("xslt")+" con el XSD "+linkedXSD.get("xsd")+" al XSLTDAO con UUID: "+uuid);

                        response.setStatus(HTTPResponseStatus.S200);
                        response.setContent("<html><head></head><body><a href=\"xslt?uuid=" + uuid + "\">" + uuid + "</a><br><p>Recurso Accesible: </p><a href=http://localhost:"+service_port+"/xslt?uuid="+uuid+">"+uuid+"</a></body></html>");
                    }
                }
            }else if(matcherXSD.find()){
                System.out.println("RESPUESTA CORRECTA XSD");
                String content= request.getContent().replaceFirst("^.*=","");

                actualController.post(uuid, content);
    
                System.out.println("He añadido el contenido "+content+" al DAOmap con UUID: "+uuid);
        
                response.setStatus(HTTPResponseStatus.S200);
                
                response.setContent("<html><head></head><body><a href=\"xsd?uuid=" + uuid + "\">" + uuid + "</a><br><p>Recurso Accesible: </p><a href=http://localhost:"+service_port+"/xsd?uuid="+uuid+">"+uuid+"</a></body></html>");

                response.putParameter("Content-Type", "application/xml");
                
            }else{
                System.out.println("RESPUESTA FALLIDA XSD");
                response.setStatus(HTTPResponseStatus.S400); 
            }
            response.putParameter("Content-Type", "application/xml");
        }
        
        response.setVersion(HTTPHeaders.HTTP_1_1.getHeader()); // HTTP11
        response.putParameter("Content-Language", "en");
        
        try {
            response.print(new OutputStreamWriter(socket.getOutputStream()));
            System.out.println("RESPONSE ENVIADO");
        } catch (IOException e) {
            e.getStackTrace();
        }
    }

    /** Genera respuesta LIST */
    public void generarLISTResponse(String type, HTTPRequest request, Socket socket) throws DBConnectionException {
        selectController(type);

        HTTPResponse response = new HTTPResponse();
        String resourceChain = request.getResourceChain();

        System.out.println("Response Creado");

        System.out.println("ResourceChain: " + resourceChain);

        response.setStatus(HTTPResponseStatus.S200); // OK
        response.setContent(actualController.list()); // Obtiene el contenido del uuid
        response.setVersion(HTTPHeaders.HTTP_1_1.getHeader()); // HTTP11

        response.putParameter("Content-Type", "text/html");
        response.putParameter("Content-Language", "en");

        System.out.println(actualController.list());

        try {
            response.print(new OutputStreamWriter(socket.getOutputStream()));
            System.out.println("RESPONSE ENVIADO");

        } catch (IOException e) {
            e.getStackTrace();
        }

    }

    /** Genera respuesta DELETE */
    public void generarDELETEResponse(String type, HTTPRequest request, Socket socket) throws DBConnectionException {
        selectController(type);
        
        HTTPResponse response = new HTTPResponse();
        String resourceChain = request.getResourceChain();

        System.out.println("Response Creado");

        if (resourceChain.contains("html?uuid=")) {
            System.out.println("ResourceChain: " + resourceChain);

            String[] url_uuid = resourceChain.split(Pattern.quote("html?uuid=")); // Pattern.quote para que pueda
                                                                                  // splitear, ya que la / no funciona

            System.out.println("Ruta: " + url_uuid[0]);
            System.out.println("UUID: "+resourceChain.replace(url_uuid[0] + "html?uuid=", ""));

            if (actualController.list().contains(url_uuid[1])) { // Si el uuid es valido
                response.setStatus(HTTPResponseStatus.S200); // OK

                actualController.delete(url_uuid[1]);
                response.setContent("<html><head></head><body>El contenido con uuid " + url_uuid[1]+ " ha sido eliminado</body></html>");
            } else {
                System.out.println("RESPUESTA FALLIDA DELETE HTML");
                response.setStatus(HTTPResponseStatus.S404);
            }

            response.putParameter("Content-Type", "text/html");
        }else if(resourceChain.contains("xml?uuid=")){
            System.out.println("ResourceChain: " + resourceChain);

            String[] url_uuid = resourceChain.split(Pattern.quote("xml?uuid=")); // Pattern.quote para que pueda
                                                                                  // splitear, ya que la / no funciona

            System.out.println("Ruta: " + url_uuid[0]);
            System.out.println("UUID: "+resourceChain.replace(url_uuid[0] + "xml?uuid=", ""));

            if (actualController.list().contains(url_uuid[1])) { // Si el uuid es valido
                response.setStatus(HTTPResponseStatus.S200); // OK

                actualController.delete(url_uuid[1]);
                response.setContent("<html><head></head><body>El contenido con uuid " + url_uuid[1]+ " ha sido eliminado</body></html>");
            } else {
                System.out.println("RESPUESTA FALLIDA DELETE XML");
                response.setStatus(HTTPResponseStatus.S404);
            }

            response.putParameter("Content-Type", "application/xml");
        }else if(resourceChain.contains("xsd?uuid=")){
            System.out.println("ResourceChain: " + resourceChain);

            String[] url_uuid = resourceChain.split(Pattern.quote("xsd?uuid=")); // Pattern.quote para que pueda
                                                                                  // splitear, ya que la / no funciona

            System.out.println("Ruta: " + url_uuid[0]);
            System.out.println("UUID: "+resourceChain.replace(url_uuid[0] + "xsd?uuid=", ""));

            if (actualController.list().contains(url_uuid[1])) { // Si el uuid es valido
                response.setStatus(HTTPResponseStatus.S200); // OK

                actualController.delete(url_uuid[1]);
                response.setContent("<html><head></head><body>El contenido con uuid " + url_uuid[1]+ " ha sido eliminado</body></html>");
            } else {
                System.out.println("RESPUESTA FALLIDA DELETE XSD");
                response.setStatus(HTTPResponseStatus.S404);
            }

            response.putParameter("Content-Type", "application/xml");
        }else if(resourceChain.contains("xslt?uuid=")){
            System.out.println("ResourceChain: " + resourceChain);

            String[] url_uuid = resourceChain.split(Pattern.quote("xslt?uuid=")); // Pattern.quote para que pueda
                                                                                  // splitear, ya que la / no funciona

            System.out.println("Ruta: " + url_uuid[0]);
            System.out.println("UUID: "+resourceChain.replace(url_uuid[0] + "xslt?uuid=", ""));

            if (actualController.list().contains(url_uuid[1])) { // Si el uuid es valido
                response.setStatus(HTTPResponseStatus.S200); // OK

                actualController.delete(url_uuid[1]);
                response.setContent("<html><head></head><body>El contenido con uuid " + url_uuid[1]+ " ha sido eliminado</body></html>");
            } else {
                System.out.println("RESPUESTA FALLIDA DELETE XSLT");
                response.setStatus(HTTPResponseStatus.S404);
            }

            response.putParameter("Content-Type", "application/xml");
        }

        response.setVersion(HTTPHeaders.HTTP_1_1.getHeader()); // HTTP11
        response.putParameter("Content-Language", "en");

        try {
            response.print(new OutputStreamWriter(socket.getOutputStream()));
            System.out.println("RESPONSE ENVIADO");

        } catch (IOException e) {
            e.getStackTrace();
        }
    }

    /** Genera response ERROR status */
    public void generarERRORResponse(HTTPRequest request, Socket socket, HTTPResponseStatus status){
        HTTPResponse response = new HTTPResponse();

        response.setStatus(status);

        response.setVersion(HTTPHeaders.HTTP_1_1.getHeader()); // HTTP11
        response.putParameter("Content-Type", "text/html");
        response.putParameter("Content-Language", "en");

        try {
            response.print(new OutputStreamWriter(socket.getOutputStream()));
            System.out.println("RESPONSE ENVIADO");
        } catch (IOException e) {
            e.getStackTrace();
        }
    }

    /** Genera pagina de bienvenida */
    public void generarWelcome(Socket socket) {
        HTTPResponse response = new HTTPResponse();

        response.setStatus(HTTPResponseStatus.S200);
        response.setVersion(HTTPHeaders.HTTP_1_1.getHeader());

        response.putParameter("Content-Type", "text/html");
        response.putParameter("Content-Language", "en");

        response.setContent("<html><head><title>Welcome page</title></head><body><div><h1>Hybrid Server</h1><h1>Autores:</h1><h2>Daniel Rodriguez Estevez</h2><h2>Ruben Gomez Martinez</h2></div></body></html>");
        try {
            response.print(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            e.getStackTrace();
        }
    }

    /** Genera UUID para POST */
    private String generarUUID() {
        UUID randomUuid = UUID.randomUUID();
        return randomUuid.toString();
    }

    /** Selecciona un controlador dependiendo del contenido de las peticiones */
    public void selectController(String type){
        switch(type){
            case "HTML":
                actualController= HTMLController;
                break;
            case "XML":
                actualController= XMLController;
                break;
            case "XSD":
                actualController= XSDController;
                break;
            case "XSLT":
                actualController= XSLTController;
                break;
            default:
                System.out.println("Unable to select a controller");
        }
        System.out.println("El controlador actual es: "+actualController.getName());
    }
}
