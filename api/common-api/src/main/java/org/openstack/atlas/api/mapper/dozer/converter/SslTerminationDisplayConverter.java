package org.openstack.atlas.api.mapper.dozer.converter;

public class SslTerminationDisplayConverter extends SslTerminationConverter {

    @Override
    public Object convert(Object dstValue, Object srcValue, Class dstClass, Class srcClass) {
        Object rValue = super.convert(dstValue, srcValue, dstClass, srcClass);
        // If its a db object going to an API object then strip out the id, crt, key, imd
        if (rValue instanceof org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination) {
            org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination apiSslTerm;
            apiSslTerm = (org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination) rValue;
            apiSslTerm.setId(null);
            apiSslTerm.setPrivatekey(null);
            apiSslTerm.setCertificate(null);
            apiSslTerm.setIntermediateCertificate(null);
            return apiSslTerm;
        }
        return rValue;
    }
}
