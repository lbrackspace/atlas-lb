
package org.openstack.atlas.util.b64aes;

public class PaddingException extends Exception{
    public PaddingException(){
       super();
    }

    public PaddingException(String msg){
        super(msg);
    }

    public PaddingException(Throwable th){
        super(th);
    }

    public PaddingException(String msg,Throwable th){
        super(msg,th);
    }
}
