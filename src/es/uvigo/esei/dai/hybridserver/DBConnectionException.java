package es.uvigo.esei.dai.hybridserver;

public class DBConnectionException extends Exception{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** Crea una excepcion cuando la base de datos no esta accesible */
    public DBConnectionException(String msg){
        super(msg);
    }
}
