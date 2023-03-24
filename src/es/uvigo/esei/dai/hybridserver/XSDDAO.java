package es.uvigo.esei.dai.hybridserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class XSDDAO implements DAO{

    String dbURL;
    String dbUser;
    String dbPassword;
    int service_port;

    Connection connection;
    
    public XSDDAO() {
    	//EMPTY CONSTRUCTOR TO WEB SERVICE
    }
    
    public XSDDAO(String dbURL, String dbUser, String dbPassword, int service_port){
        this.dbURL=dbURL;
        this.dbUser=dbUser;
        this.dbPassword= dbPassword;
        this.service_port=service_port;
    }

    @Override
    public String get(String uuid) throws DBConnectionException {
        System.out.println("GET DB");
        String toret="";
        try {
            System.out.println("ABRO CONEXION "+dbURL+" "+dbUser+" "+dbPassword);
            connection= DriverManager.getConnection(dbURL, dbUser, dbPassword);
        } catch (SQLException e1) {
        	System.out.println("ERROR CONEXION");
            throw new DBConnectionException("CONNECTION ERROR");
        }

        try(PreparedStatement statement = this.connection.prepareStatement("SELECT content FROM XSD WHERE uuid=?")){
            //Generamos tabla
            statement.setString(1, uuid);
            
            try(ResultSet result= statement.executeQuery()){
            	System.out.println();
                result.next();
                toret= result.getString("content");

                System.out.println("GET:  "+ toret);
                return toret;
            }catch(Exception e) {
            	System.out.println("ERROR RESULT");
            	return toret;
            }
        }catch(SQLException e){
        	System.out.println("CONNECTION ERROR STATEMENT");
            throw new DBConnectionException("CONNECTION ERROR");
        }finally{
            try {
                connection.close();
            } catch (SQLException e) {
                throw new DBConnectionException("CONNECTION ERROR");
            }
        }
    }

    @Override
    public void post(String uuid, String content) throws DBConnectionException {

        try {
            connection= DriverManager.getConnection(dbURL, dbUser, dbPassword);
        } catch (SQLException e1) {
            throw new DBConnectionException("CONNECTION ERROR");
        }

        try(PreparedStatement statement = this.connection.prepareStatement("INSERT INTO XSD (uuid, content) VALUES(?,?)")){
        //Rellenamos campos ?,?                                                                   
        statement.setString(1,uuid);
        statement.setString(2,content);    

        //Generamos tabla
        int result= statement.executeUpdate();
        
        if(result==0){
            throw new SQLException("Unable to insert on database "+result);
        }

        }catch(Exception e){
        }finally{
            try {
                connection.close();
            } catch (SQLException e) {
                throw new DBConnectionException("CONNECTION ERROR");
            }
        }
        
    }

    @Override
    public void post(String uuid, String content, String xsd) throws DBConnectionException {}
    
    @Override
    public void delete(String uuid) throws DBConnectionException {
        try {
            System.out.println("CONNECTION");
            connection= DriverManager.getConnection(dbURL, dbUser, dbPassword);
        } catch (SQLException e1) {
            throw new DBConnectionException("CONNECTION ERROR");
        }
        
        try(PreparedStatement statement = this.connection.prepareStatement("DELETE FROM XSD WHERE uuid = ?")){
            System.out.println("STATEMENT");
            statement.setString(1, uuid);
            
            int result= statement.executeUpdate();

            if(result!=1){
                System.out.println("ERROR DELETE");
            }
        }catch(SQLException e){
            throw new DBConnectionException("CONNECTION ERROR");
        }finally{
            try {
                connection.close();
            } catch (SQLException e) {
                throw new DBConnectionException("CONNECTION ERROR");
            }
        }
    }

    public String list() throws DBConnectionException {
        String list= "";
        try {
            System.out.println("CONNECTION");
            connection= DriverManager.getConnection(dbURL, dbUser, dbPassword);
        } catch (SQLException e1) {
            throw new DBConnectionException("CONNECTION ERROR");
        }
        
        try(Statement statement = this.connection.createStatement()){
            System.out.println("STATEMENT");
            list="<html><head></head><body><p>XSD</p><p>LocalServer</p>";

            try(ResultSet result= statement.executeQuery("SELECT * FROM XSD")){
                System.out.println("RESULT");
                while (result.next()) {
                    list= list+"<a href=http://localhost:"+service_port+"/xsd?uuid="+result.getString("uuid")+">"+result.getString("uuid")+"</a><br>";
                }

                return list+"</body></html>";
            }
        }catch(SQLException e){
            e.printStackTrace();
            return list;

        }finally{
            try {
                connection.close();
            } catch (SQLException e) {
                throw new DBConnectionException("CONNECTION ERROR");
            }
        }
    }

	@Override
	public String getXSD(String uuid) throws DBConnectionException {
		//  Auto-generated method stub
		return null;
	}
    
	public String toString(){
		return dbURL+" "+dbUser+" "+dbPassword+" "+service_port;
	}
}
