package org.apache.sandesha.util;

import org.apache.sandesha.Constants;

import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Jaliya
 * Date: Feb 23, 2005
 * Time: 12:31:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class PropertyLoader {
    public static int getClientSideListenerPort(){
       Properties prop=loadProperties();
       if(prop!=null){
          return  new Integer(prop.getProperty("CLIENT_LISTENER_PORT")).intValue();
       }else
           return Constants.DEFAULR_CLIENT_SIDE_LISTENER_PORT;

    }

     public static int getSimpleAxisServerPort(){
       Properties prop=loadProperties();
       if(prop!=null){
          return  new Integer(prop.getProperty("SIMPLE_AXIS_SERVER_PORT")).intValue();
       }else
           return Constants.DEFAULR_CLIENT_SIDE_LISTENER_PORT;

    }

    private static Properties loadProperties(){
          Properties properties = new Properties();
    try {
        System.out.println((new File(".")).getAbsolutePath());
        properties.load(new FileInputStream("config/sandesha.properties"));
        return properties;
    } catch (IOException e) {
        e.printStackTrace();
        return null;
    }
    }
}
