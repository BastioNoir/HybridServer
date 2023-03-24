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
package es.uvigo.esei.dai.hybridserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Launcher {
	public static void main(String[] args) {
		String docPath= args[0];

		if(args.length==1){ //Solo un fichero
			//Fichero properties
			if(docPath.contains(".properties")){
				System.out.println("Start with properties file");
				Properties properties= new Properties();

				try(FileInputStream fis= new FileInputStream(new File(args[0]))){
					properties.load(fis);
					System.out.println("Start with XML configuration file "+docPath);
					new HybridServer(properties).start();
					
				}catch (FileNotFoundException e) {
					System.err.println("Error: "+docPath+" not found");
				}catch (IOException e){
					System.out.println("IOException al cargar "+docPath);
				}

			}else if(docPath.contains(".xml")){
				//Arranque con configuration.xml
				System.err.println("Start with xml configuration file");
				try {
					Configuration configuration= XMLConfigurationLoader.load(new File(docPath));
					new HybridServer(configuration).start();
				}catch (FileNotFoundException e) {
					System.err.println("Error: "+docPath+" not found");
				}catch (Exception e) {
					System.err.println("\nCan't charge the xml configuration file");
				}
			}else{
				System.err.println("You only can use a properties or configuration file to run the server");
			}
			
		}else if(args.length>1){ //Varios parametros
			System.err.println("No se puede lanzar el servidor con mas de un parametro, solo puede hacerse referencia a un archivo de configuracion");
		}else{ //Defecto
			System.out.println("Se arranca servidor por defecto");
			new HybridServer().start();
		}
	}
	
}
