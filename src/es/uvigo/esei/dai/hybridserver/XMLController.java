package es.uvigo.esei.dai.hybridserver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

public class XMLController implements Controller{
    String name;
    private XMLDAO XMLDao;
    private List<ServerConfiguration> serversList;
    private int numServers;
    
    private Map<String, String> wsdlMap;

    public XMLController(XMLDAO dao, List<ServerConfiguration> servers){
    	System.out.println(dao.toString());
    	
        this.name="XMLCTRL";
        this.XMLDao= (XMLDAO) dao;
        
        System.out.println(XMLDao.toString());
        this.serversList=servers;
        this.wsdlMap= new HashMap<>();
        
        if(servers!=null) {
        	for(ServerConfiguration sc: servers) {
        		
        		//Si no es un server inaccesible lo metemos en el mapa
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
    	
    	System.out.println("GET XML");
    	
    	//Si lo contiene lo devuelve
    	if(!XMLDao.get(uuid).equals("")) {
    		System.out.println("RESOURCE LOCALLY PRESENT");
    		toret= XMLDao.get(uuid);
    	}else{
    		System.out.println("RESOURCE NOT PRESENT IN THE LOCAL SERVER");
    		int i= 0;
    		boolean resourceFound= false;
    		
    		while(i<numServers && resourceFound==false){
    			System.out.println("########################################################WHILE CON SERVER"+i+" Y NUMSERVER: "+numServers);
    			ServerConfiguration selectedServer=serversList.get(i);
    			HybridServerService hs= connectionWithOtherServers(selectedServer.getWsdl(), selectedServer.getService());
    			String serviceResponse=hs.getXML(uuid);
    			
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
        XMLDao.post(uuid, content);
    }

    @Override
    public void delete(String uuid) throws DBConnectionException {
        XMLDao.delete(uuid);
        
    }

    @Override
    public String list() throws DBConnectionException {
    	String toretList="<html><head></head><body><p>XML</p><p>LocalServer</p>";
    	HybridServerService hs;
    	//Si lo contiene lo devuelve
    	System.out.println("LIST LOCAL SERVER");
    	toretList= XMLDao.list();
    	
    	int i= 0;	
    	while(i < numServers){
    		System.out.println("WHILE CON SERVER"+i+" Y NUMSERVER: "+numServers);
    		ServerConfiguration selectedServer=serversList.get(i);
    		System.out.println(selectedServer.toString()+"\n");
    		
    		hs= connectionWithOtherServers(selectedServer.getWsdl(), selectedServer.getService());
    		
    		String serviceResponse=hs.listServerXML();
    			
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
