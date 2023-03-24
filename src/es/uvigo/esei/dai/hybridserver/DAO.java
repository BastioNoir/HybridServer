package es.uvigo.esei.dai.hybridserver;

public interface DAO {
	/** Obtiene el contenido del uuid enviado, esta implementado para cada tipo de DAO */
	public String get(String uuid) throws DBConnectionException;
	/** Obtiene el uuid del XSD asociado a un XSLT, solo se implementa para XSLTDAO*/
	public String getXSD(String uuid) throws DBConnectionException; 
	/** Publica un contenido sobre un uuid, se implementa para todos menos XSLTDAO*/
	public void post(String uuid, String content) throws DBConnectionException;
	/** Publica sobre un uuid el contenido XSLT y el uuid del XSD asociado, solo lo usa XSLTDAO*/
	public void post(String uuid, String xml, String xsd) throws DBConnectionException;
	/** Elimina el contenido en base a uuid, implementado para todos*/
	public void delete(String uuid) throws DBConnectionException;
	/** Devuelve una lista con los recursos del DAO, implementado para todos*/
	public String list() throws DBConnectionException;
	public String toString();
}
