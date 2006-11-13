
            /**
            * ExtensionMapper.java
            *
            * This file was auto-generated from WSDL
            * by the Apache Axis2 version: #axisVersion# #today#
            */

            package org.tempuri;
            /**
            *  ExtensionMapper class
            */
        
        public  class ExtensionMapper{

          public static java.lang.Object getTypeObject(java.lang.String namespaceURI,
                                                       java.lang.String typeName,
                                                       javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{

              
                  if (
                  "http://tempuri.org/".equals(namespaceURI) &&
                  "EchoStringRequest.BodyType".equals(typeName)){
                   
                            return  org.tempuri.EchoStringRequestBodyType.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://tempuri.org/".equals(namespaceURI) &&
                  "EchoStringResponse.BodyType".equals(typeName)){
                   
                            return  org.tempuri.EchoStringResponseBodyType.Factory.parse(reader);
                        

                  }

              
             throw new java.lang.RuntimeException("Unsupported type " + namespaceURI + " " + typeName);
          }

        }
    