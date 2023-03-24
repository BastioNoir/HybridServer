package es.uvigo.esei.dai.hybridserver;

public interface Controller {
	/** Devuelve un recurso del DAO apropiado por uuid*/
    public String get(String uuid) throws DBConnectionException; 
    /** Devuelve XSD del XSLT apropiado por uuid, solo lo usa XSLTController*/
    public String getXSD(String uuid) throws DBConnectionException;
    /** Anade un contenido por uuid*/
	public void post(String uuid, String content) throws DBConnectionException;
	/** Anade un contenido y un XSD por uuid, solo lo usa XSLTController*/
	public void post(String uuid, String xml, String xsd) throws DBConnectionException;
	/** Elimina recursos por uuid*/
	public void delete(String uuid) throws DBConnectionException;
	/** Lista los recursos de cierto tipo*/
	public String list() throws DBConnectionException;
	/** Devuelve el nombre del controller*/
	public String getName();
	/** Se conecta con los servicios que necesita */
	public HybridServerService connectionWithOtherServers(String strurl, String service);
}

