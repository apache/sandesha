
    /**
     * RMInteropServiceCallbackHandler.java
     *
     * This file was auto-generated from WSDL
     * by the Apache Axis2 version: #axisVersion# #today#
     */
    package org.apache.sandesha2.interop;

    /**
     *  RMInteropServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class RMInteropServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public RMInteropServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public RMInteropServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for EchoString method
            *
            */
           public void receiveResultEchoString(
                    org.tempuri.EchoStringResponse param28) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorEchoString(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                


    }
    