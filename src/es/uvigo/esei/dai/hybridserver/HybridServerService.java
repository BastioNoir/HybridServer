package es.uvigo.esei.dai.hybridserver;

import javax.jws.WebService;

import javax.jws.WebMethod;
import javax.jws.soap.SOAPBinding;

@WebService
@SOAPBinding(style=SOAPBinding.Style.RPC)
public interface HybridServerService {
	@WebMethod
	public String listServerHTML() throws DBConnectionException;
	@WebMethod
	public String listServerXML() throws DBConnectionException;
	@WebMethod
	public String listServerXSD() throws DBConnectionException;
	@WebMethod
	public String listServerXSLT() throws DBConnectionException;
	@WebMethod
	public String getHTML(String uuid) throws DBConnectionException;
	@WebMethod
	public String getXML(String uuid) throws DBConnectionException;
	@WebMethod
	public String getXSD(String uuid) throws DBConnectionException;
	@WebMethod
	public String getXSLT(String uuid) throws DBConnectionException;
	@WebMethod
	public String getXSDwithXSLT(String uuid) throws DBConnectionException;
}
