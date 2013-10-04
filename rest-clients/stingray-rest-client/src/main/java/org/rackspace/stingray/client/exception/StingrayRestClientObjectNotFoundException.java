package org.rackspace.stingray.client.exception;

/**
 * Created by IntelliJ IDEA.
 * User: mich6365
 * Date: 6/13/13
 * Time: 3:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class StingrayRestClientObjectNotFoundException extends Exception {

    private static final long serialVersionUID = -1984130663036438982L;

       public StingrayRestClientObjectNotFoundException(String message) {
           super(message);
       }

       public StingrayRestClientObjectNotFoundException(String msg, Throwable th) {
           super(msg, th);
       }

       public StingrayRestClientObjectNotFoundException(Throwable th) {
           super(th);
       }

       public StingrayRestClientObjectNotFoundException() {
           super();
       }



}
