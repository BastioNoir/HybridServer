package es.uvigo.esei.dai.hybridserver;

import javax.jws.WebService;
@WebService(endpointInterface="es.uvigo.esei.dai.hybridserver.HybridServerService")
public class HybridServerServiceImpl implements HybridServerService{
	
	//DAO pasados al servicio
	private HTMLDAO HTMLdao;
	private XMLDAO XMLdao;
	private XSDDAO XSDdao;
	private XSLTDAO XSLTdao;
	
	public HybridServerServiceImpl(HTMLDAO htmldao, XMLDAO xmldao, XSDDAO xsddao, XSLTDAO xsltdao) {
		HTMLdao=htmldao;
		XMLdao=xmldao;
		XSDdao=xsddao;
		XSLTdao=xsltdao;
	}
	
	@Override
	/** Devuelve la lista HTML de un servidor*/
	public String listServerHTML() throws DBConnectionException {
		return HTMLdao.list().replace("<html><head></head><body><p>LocalServer</p>","").replace("</body></html>","");
	}

	@Override
	/** Devuelve la lista XML de un servidor*/
	public String listServerXML() throws DBConnectionException {
		return XMLdao.list().replace("<html><head></head><body><p>LocalServer</p>","").replace("</body></html>","");
	}

	@Override
	/** Devuelve la lista XSD de un servidor*/
	public String listServerXSD() throws DBConnectionException {
		return XSDdao.list().replace("<html><head></head><body><p>LocalServer</p>","").replace("</body></html>","");
	}

	@Override
	/** Devuelve la lista XSLT de un servidor*/
	public String listServerXSLT() throws DBConnectionException {
		return XSLTdao.list().replace("<html><head></head><body><p>LocalServer</p>","").replace("</body></html>","");
	}

	@Override
	/** Devuelve un recurso HTML de un servidor por su uuid*/
	public String getHTML(String uuid) throws DBConnectionException {
		return HTMLdao.get(uuid);
	}

	@Override
	/** Devuelve un recurso XML de un servidor por su uuid*/
	public String getXML(String uuid) throws DBConnectionException {
		return XMLdao.get(uuid);
	}

	@Override
	/** Devuelve un recurso XSD de un servidor por su uuid*/
	public String getXSD(String uuid) throws DBConnectionException {
		return XSDdao.get(uuid);
	}

	@Override
	/** Devuelve un recurso XSLT de un servidor por su uuid*/
	public String getXSLT(String uuid) throws DBConnectionException {
		return XSLTdao.get(uuid);
	}

	@Override
	/** Devuelve el uuid del recurso XSD asociado a un XSLT de un servidor por su uuid*/
	public String getXSDwithXSLT(String uuid) throws DBConnectionException {
		return XSLTdao.getXSD(uuid);
	}
}
