/*
 * Copyright  1999-2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.sandesha.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.sandesha.Constants;
 /**
  * This is the property loader for Sandesha. All the properties will be loaded from the
  * sandesha.properties file that is found in the classpath.
  *
  * @author Jaliya Ekanayake
  * @author Patrick Collins
  */
public class PropertyLoader {


    public static int getClientSideListenerPort() {
       return getIntProperty( Constants.ClientProperties.CLIENT_LISTENER_PORT, 
             Constants.DEFAULR_CLIENT_SIDE_LISTENER_PORT );
    }

    public static int getSimpleAxisServerPort() {
       return getIntProperty( Constants.ClientProperties.SIMPLE_AXIS_SERVER_PORT_POPERTY,
              Constants.DEFAULT_SIMPLE_AXIS_SERVER_PORT);
    }
    
    protected static int getIntProperty( String aKey, int aValue )
    {
       int retVal = aValue;
       String intValue = getStringProperty( aKey, String.valueOf(aValue) );
       try
       {
          retVal =  Integer.parseInt( intValue );
       }
       catch( NumberFormatException nfe )
       {
          nfe.printStackTrace();
       }
       return retVal;
    }

    //CHANGE FOR SECURITY ADDITION
    public static ArrayList getRequestHandlerNames() {
        return getHandlerNames(Constants.ClientProperties.REQUEST_HANDLER);
    }

    public static ArrayList getResponseHandlerNames() {
        return getHandlerNames(Constants.ClientProperties.RESPONSE_HANDLER);
    }

    public static ArrayList getHandlerNames(String type) {
        Properties properties = new Properties();
        load( properties );
        ArrayList ret = new ArrayList();

         int temp = 0;
         String propVal;
         do {
             temp++;
             String tempStr = type + temp;
             propVal = properties.getProperty(tempStr);
             if (propVal != null) {
                 ret.add(propVal);
             }
         } while (propVal != null);

         return ret;
    }

    public static ArrayList getListenerRequestHandlerNames() {
        return getHandlerNames(Constants.ClientProperties.LISTENER_REQUEST_HANDLER);
    }

    public static ArrayList getListenerResponseHandlerNames() {
        return getHandlerNames(Constants.ClientProperties.LISTENER_RESPONSE_HANDLER);
    }

    public static String getProvider() {
       return getStringProperty( Constants.ClientProperties.PROVIDER_CLASS,  
             Constants.ClientProperties.DEFAULT_PROVIDER_CLASS );
    }
    

    public static String getInvokeStrategyClassName()
    {
       String invokeStrategyProperty = getStringProperty(Constants.INVOKE_STRATEGY, Constants.DEFAULT_STRATEGY );
       String[] splitData = invokeStrategyProperty.split(":");
       return splitData[0];
    }
    
    public static Map getInvokeStrategyParams()
    {
       String invokeStrategyProperty = getStringProperty( Constants.INVOKE_STRATEGY,Constants.DEFAULT_STRATEGY );
       return getParamData( invokeStrategyProperty );
    }
    
    protected static Map getParamData( String aRawString )
    {
       Map params = new HashMap();
       String[] splitData = aRawString.split(Constants.COLON);
       if( splitData.length == 2 )
       {
          addParams( params, splitData[1] );
       }
       return params;
    }
    

    public static String getInvokeHandlerClassName()
    {
       String invokeHandlerProperty = getStringProperty(Constants.INVOKE_HANDLER, Constants.DEFAULT_HANDLER );
       String[] splitData = invokeHandlerProperty.split(Constants.COLON);
       return splitData[0];
    }
    
    public static Map getInvokeHandlerParams()
    {
       String invokeHandlerProperty = getStringProperty(Constants.INVOKE_HANDLER,Constants.DEFAULT_HANDLER );
       return getParamData( invokeHandlerProperty );
    }
    
    protected static void addParams(Map aParams, String aParamString )
    {
       StringTokenizer st = new StringTokenizer( aParamString, "&" );
       while( st.hasMoreTokens() )
       {
          String nameValuePair = st.nextToken();
          String[] nameValueArray = nameValuePair.split("=");
          if( nameValueArray.length == 2 )
          {
             aParams.put( nameValueArray[0], nameValueArray[1] );
          }
       }
    }
    
    protected static String getStringProperty( String aKey, String aDefault )
    {
       Properties props = new Properties();
       load( props );
       return props.getProperty( aKey, aDefault );
    }
    
    private static void load( Properties aProps ) {
       try {
           InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(
                   Constants.ClientProperties.PROPERTY_FILE);
           aProps.load(in);
       } catch (IOException e) {
           e.printStackTrace();
       }
   }
}
