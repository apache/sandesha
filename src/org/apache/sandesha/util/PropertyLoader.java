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

import org.apache.sandesha.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

public class PropertyLoader {
    public static int getClientSideListenerPort() {
        Properties prop = loadProperties();
        if (prop != null) {
            return new Integer(prop.getProperty(Constants.ClientProperties.CLIENT_LISTENER_PORT)).intValue();
        } else
            return Constants.DEFAULR_CLIENT_SIDE_LISTENER_PORT;

    }

    public static int getSimpleAxisServerPort() {
        Properties prop = loadProperties();
        if (prop != null) {
            return new Integer(prop.getProperty(Constants.ClientProperties.SIMPLE_AXIS_SERVER_PORT_POPERTY)).intValue();
        } else
            return Constants.DEFAULT_SIMPLE_AXIS_SERVER_PORT;

    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try {
            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(Constants.ClientProperties.PROPERTY_FILE);
            properties.load(in);
            return properties;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    //CHANGE FOR SECURITY ADDITION
    public static ArrayList getRequestHandlerNames() {
        return getHandlerNames(Constants.ClientProperties.REQUEST_HANDLER);
    }

    public static ArrayList getResponseHandlerNames() {
        return getHandlerNames(Constants.ClientProperties.RESPONSE_HANDLER);
    }

    public static ArrayList getHandlerNames(String type) {
        Properties properties = loadProperties();
        ArrayList ret = new ArrayList();

        try {
            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(Constants.ClientProperties.PROPERTY_FILE);
            properties.load(in);

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
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList getListenerRequestHandlerNames() {
        return getHandlerNames(Constants.ClientProperties.LISTENER_REQUEST_HANDLER);
    }

    public static ArrayList getListenerResponseHandlerNames() {
        return getHandlerNames(Constants.ClientProperties.LISTENER_RESPONSE_HANDLER);
    }

    public static String getProvider() {
        Properties prop = loadProperties();
        String s;

        s = prop.getProperty(Constants.ClientProperties.PROVIDER_CLASS);

        if (s == null)
            return Constants.ClientProperties.PROVIDER_CLASS;

        return s;
    }

}
