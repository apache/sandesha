package org.apache.sandesha.util;

import org.apache.sandesha.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Jaliya
 * Date: Feb 23, 2005
 * Time: 12:31:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class PropertyLoader {
    public static int getClientSideListenerPort() {
        Properties prop = loadProperties();
        if (prop != null) {
            return new Integer(prop.getProperty("CLIENT_LISTENER_PORT")).intValue();
        } else
            return Constants.DEFAULR_CLIENT_SIDE_LISTENER_PORT;

    }

    public static int getSimpleAxisServerPort() {
        Properties prop = loadProperties();
        if (prop != null) {
            return new Integer(prop.getProperty(Constants.SIMPLE_AXIS_SERVER_PORT_POPERTY)).intValue();
        } else
            return Constants.DEFAULR_CLIENT_SIDE_LISTENER_PORT;

    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try {
            InputStream in=Thread.currentThread().getContextClassLoader().getResourceAsStream("sandesha.properties") ;
            properties.load(in);
            return properties;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    //CHANGE FOR SECURITY ADDITION
    public static ArrayList getRequestHandlerNames(){
        Properties properties = loadProperties();
        ArrayList ret = new ArrayList();

        try {
            InputStream in=Thread.currentThread().getContextClassLoader().getResourceAsStream("sandesha.properties") ;
            properties.load(in);

            int temp=0;
            String propVal;
            do{
                temp++;
                String tempStr = "requestHandler"+temp;
                propVal = properties.getProperty(tempStr);
                if(propVal!=null){
                    ret.add(propVal);
                }
            }while(propVal!=null);

            return ret;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }


    }

    public static ArrayList getResponseHandlerNames(){
        Properties properties = loadProperties();
        ArrayList ret = new ArrayList();

        try {
            InputStream in=Thread.currentThread().getContextClassLoader().getResourceAsStream("sandesha.properties") ;
            properties.load(in);

            int temp=0;
            String propVal;
            do{
                temp++;
                String tempStr = "responseHandler"+temp;
                propVal = properties.getProperty(tempStr);
                if(propVal!=null){
                    ret.add(propVal);
                }
            }while(propVal!=null);

            return ret;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getProvider(){
        Properties prop = loadProperties();
        String s;

       s =  prop.getProperty("providerClass");

       if(s==null)
           return "org.apache.axis.providers.java.RPCProvider";

       return s;
    }

    //END CHANGE FOR SECURITY ADDITION
}
