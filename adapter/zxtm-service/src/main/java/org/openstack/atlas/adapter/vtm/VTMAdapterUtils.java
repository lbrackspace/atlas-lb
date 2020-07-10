package org.openstack.atlas.adapter.vtm;


import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.service.domain.entities.LoadBalancer;

import java.util.HashMap;
import java.util.Map;

public class VTMAdapterUtils {
    public enum VSType {
        DEFAULT_VS, REDIRECT_VS, SECURE_VS
    }

    public static Map<VSType, String> getVSNamesForLB(LoadBalancer loadBalancer) throws InsufficientRequestException {
        Integer lbId = loadBalancer.getId();
        Integer accountId = loadBalancer.getAccountId();
        String virtualServerName = ZxtmNameBuilder.genVSName(lbId, accountId);
        String virtualSecureServerName = ZxtmNameBuilder.genSslVSName(lbId, accountId);
        String virtualRedirectServerName = ZxtmNameBuilder.genRedirectVSName(lbId, accountId);
        String[] vsNames;
        Map<VSType, String> vsNamesMap = new HashMap<VSType, String>();

        boolean isSecureServer = loadBalancer.isUsingSsl();
        boolean isSslOnly = loadBalancer.isUsingSsl() && loadBalancer.getSslTermination().getSecureTrafficOnly();
        boolean isRedirectServer = loadBalancer.getHttpsRedirect() != null && loadBalancer.getHttpsRedirect();

        if (isSecureServer && isRedirectServer) {
            vsNames = new String[2];
            vsNames[0] = virtualRedirectServerName;
            vsNames[1] = virtualSecureServerName;
            vsNamesMap.put(VSType.REDIRECT_VS, virtualRedirectServerName);
            vsNamesMap.put(VSType.SECURE_VS, virtualSecureServerName);
        } else if (isSecureServer) {
            vsNames = new String[2];
            vsNames[0] = virtualServerName;
            vsNames[1] = virtualSecureServerName;
            if (!isSslOnly) {
                vsNamesMap.put(VSType.DEFAULT_VS, virtualServerName);
                vsNamesMap.put(VSType.SECURE_VS, virtualSecureServerName);
            } else {
                vsNamesMap.put(VSType.SECURE_VS, virtualSecureServerName);
            }
        } else if (isRedirectServer) {
            vsNames = new String[2];
            vsNames[0] = virtualServerName;
            vsNames[1] = virtualRedirectServerName;
            vsNamesMap.put(VSType.DEFAULT_VS, virtualServerName);
            vsNamesMap.put(VSType.REDIRECT_VS, virtualRedirectServerName);
        } else {
            vsNames = new String[1];
            vsNames[0] = virtualServerName;
            vsNamesMap.put(VSType.DEFAULT_VS, virtualServerName);
        }

        return vsNamesMap;
    }

    public static Map<VSType, String> getAllPossibleVSNamesForLB(LoadBalancer loadBalancer) throws InsufficientRequestException {
        Integer lbId = loadBalancer.getId();
        Integer accountId = loadBalancer.getAccountId();
        String virtualServerName = ZxtmNameBuilder.genVSName(lbId, accountId);
        String virtualSecureServerName = ZxtmNameBuilder.genSslVSName(lbId, accountId);
        String virtualRedirectServerName = ZxtmNameBuilder.genRedirectVSName(lbId, accountId);
        Map<VSType, String> vsNamesMap = new HashMap<>();

        vsNamesMap.put(VSType.DEFAULT_VS, virtualServerName);
        vsNamesMap.put(VSType.REDIRECT_VS, virtualRedirectServerName);
        vsNamesMap.put(VSType.SECURE_VS, virtualSecureServerName);
        return vsNamesMap;
    }
}
