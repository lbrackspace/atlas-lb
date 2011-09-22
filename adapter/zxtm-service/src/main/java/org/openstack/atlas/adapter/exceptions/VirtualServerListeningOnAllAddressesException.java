package org.openstack.atlas.adapter.exceptions;

public class VirtualServerListeningOnAllAddressesException extends Exception {
    private static final long serialVersionUID = -1197590882399930192L;

    public VirtualServerListeningOnAllAddressesException(String s) {
        super(s);
    }
}