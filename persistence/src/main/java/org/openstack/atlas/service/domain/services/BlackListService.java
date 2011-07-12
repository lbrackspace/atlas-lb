package org.openstack.atlas.service.domain.services;


import org.openstack.atlas.service.domain.entities.BlacklistItem;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.util.ip.exception.IPStringConversionException;
import org.openstack.atlas.util.ip.exception.IpTypeMissMatchException;
import java.util.Set;

public interface BlackListService {

     public void deleteBlackList(BlacklistItem msgBlacklist) throws Exception;

     public Node getBlackListedItemNode(Set<Node> nodes) throws IPStringConversionException, IpTypeMissMatchException;
}
