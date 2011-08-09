
package org.openstack.atlas.api.async;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.Message;
import javax.jms.ObjectMessage;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;

public class UpdateErrorFileListener extends BaseListener{
    private final Log LOG = LogFactory.getLog(UpdateErrorFileListener.class);

    @Override
    public void doOnMessage(Message message) throws Exception {
       String msg = String.format("Inside %s.doMessage",this.getClass().getName());
       LOG.info(msg);
       MessageDataContainer data = getDataContainerFromMessage(message);
       Integer aid = data.getAccountId();
       Integer lid = data.getLoadBalancerId();
       String content = data.getErrorFileContents();
       try{
           reverseProxyLoadBalancerService.setErrorFile(lid, aid, content);
       }catch(Exception e){
           String tmpMsg = String.format("Error setting Errorfile for %d_%d",aid,lid);
           LOG.error(tmpMsg,e);
           return; // Put alert stuff here
       }
    }

}
