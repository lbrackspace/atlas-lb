package org.rackspace.vtm.client.exception;

/**
 * Created by IntelliJ IDEA.
 * User: mich6365
 * Date: 6/13/13
 * Time: 3:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class VTMRestClientObjectNotFoundException extends Exception {

    private static final long serialVersionUID = 6032526009484790155L;

       public VTMRestClientObjectNotFoundException(String message) {
           super(message);
       }

       public VTMRestClientObjectNotFoundException(String msg, Throwable th) {
           super(msg, th);
       }

       public VTMRestClientObjectNotFoundException(Throwable th) {
           super(th);
       }

       public VTMRestClientObjectNotFoundException() {
           super();
       }



}
