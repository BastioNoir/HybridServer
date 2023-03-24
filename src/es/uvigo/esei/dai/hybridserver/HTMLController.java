package es.uvigo.esei.dai.hybridserver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

public class HTMLController implements Controller{
    String name;
    private HTMLDAO HTMLDao;
    
    //Lista de servidores a los que pedir recursos
    private List<ServerConfiguration> serversList;
    
    //Numero de servidores a los que pedir recursos
    private int numServers;
    
    //Mapa con el nombre de cada server y su WSDL
    private Map<String, String> wsdlMap;

    public HTMLController(HTMLDAO dao, List<ServerConfiguration> servers){
        this.name="HTMLCTRL";
        this.HTMLDao= (HTMLDAO) dao;
        
        System.out.println(HTMLDao.toString());
        this.serversList=servers;
        this.wsdlMap= new HashMap<>();
        
        //Si existen servidores a los que preguntar
        if(servers!=null) {
        	for(ServerConfiguration sc: servers) {
        		//Si no es un server no accesible(Down Server) lo metemos en el mapa
        		if(!sc.getName().equals("Down Server")) {
        			wsdlMap.put(sc.getName(), sc.getWsdl());
                	System.out.println("ADDED: "+sc.getName()+" "+sc.getWsdl());
        		}
            }
            
            numServers= wsdlMap.size();
            System.out.println("NUM SERVER: "+numServers);
        }
        
    }

    @Override
    public String get(String uuid) throws DBConnectionException {
    	String toret="";
    	
    	System.out.println("GET HTML");
    	
    	//Si lo contiene lo devuelve
    	if(!HTMLDao.get(uuid).equals("")) {
    		System.out.println("RESOURCE LOCALLY PRESENT");
    		toret= HTMLDao.get(uuid);
    	}else{
    		System.out.println("RESOURCE NOT PRESENT IN THE LOCAL SERVER");
    		int i= 0;
    		boolean resourceFound= false;
    		
    		//Recorremos los servidores buscando el recurso hasta que lo encontremos o no haya mas servidores
    		while(i<numServers && resourceFound==false){
    			ServerConfiguration selectedServer=serversList.get(i);
    			HybridServerService hs= connectionWithOtherServers(selectedServer.getWsdl(), selectedServer.getService());
    			String serviceResponse=hs.getHTML(uuid);
    			
    			//Se encuentra en otro servidor
    			if(!serviceResponse.equals("")) {
    				System.out.println("RESOURCE DETECTED");
    				toret= serviceResponse;
    				resourceFound=true;
    				i++;
    			}else {
    				System.out.println("RESOURCE NOT PRESENT IN "+selectedServer.getName());
    				i++;
    			}
    		}
    		
    	}
    	
    	return toret;
    }

    @Override
    public void post(String uuid, String content) throws DBConnectionException {
        HTMLDao.post(uuid, content);
    }

    @Override
    public void delete(String uuid) throws DBConnectionException {
        HTMLDao.delete(uuid);
        
    }

    @Override
    /** Devuelve una lista con todos los recursos HTML de todos los servidores*/
    public String list() throws DBConnectionException {
    	String toretList="<html><head></head><body><p>HTML</p><p>LocalServer</p>";
    	HybridServerService hs;
    	//Si lo contiene lo devuelve
    	System.out.println("LIST LOCAL SERVER");
    	toretList= HTMLDao.list();
    	
    	int i= 0;	
    	while(i < numServers){
    		System.out.println("WHILE CON SERVER"+i+" Y NUMSERVER: "+numServers);
    		ServerConfiguration selectedServer=serversList.get(i);
    		System.out.println(selectedServer.toString()+"\n");
    		
    		hs= connectionWithOtherServers(selectedServer.getWsdl(), selectedServer.getService());
    		
    		String serviceResponse=hs.listServerHTML();
    			
    		toretList+= "<p>"+selectedServer.getName()+"</p>";
    		toretList += serviceResponse;
    		i++;
    	}
    	
    	System.out.println(toretList);
    	return toretList+"</body></html>";
    }

    @Override
    public String getName(){
        return name;
    }

    @Override
    public void post(String uuid, String xml, String xsd) throws DBConnectionException {
        //  NOT IMPLEMENTED 
    }

	@Override
	public String getXSD(String uuid) throws DBConnectionException {
		//  NOT IMPLEMENTED 
		return null;
	}
	
	@Override
	/** Se conecta con los servicios que necesita */
	public HybridServerService connectionWithOtherServers(String strurl, String service) {
		HybridServerService toret= null;
		try {
			URL url = new URL(strurl);
			QName name = new QName("http://hybridserver.dai.esei.uvigo.es/",service+"ImplService");
			Service webService = Service.create(url, name);
			
			toret= webService.getPort(HybridServerService.class); 
			System.out.println("SERVICE CONNECTION OK");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		System.out.println("TORET: "+toret);
		return toret;
	}
}
