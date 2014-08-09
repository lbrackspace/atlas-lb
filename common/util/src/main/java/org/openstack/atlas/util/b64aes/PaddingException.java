
package org.openstack.atlas.util.b64aes;

import java.io.IOException;

public class PaddingException extends IOException{
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
