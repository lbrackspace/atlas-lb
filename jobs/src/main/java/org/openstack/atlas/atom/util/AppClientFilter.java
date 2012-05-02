package org.openstack.atlas.atom.util;


import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.xml.bind.marshaller.CharacterEscapeHandler;

import java.io.IOException;
import java.io.Writer;

public class AppClientFilter extends ClientFilter {
        public ClientResponse handle(ClientRequest cr) {
            // Modify the request

             cr.getProperties().put(CharacterEscapeHandler.class.getName(), new CharacterEscapeHandler() {
                    @Override
                    public void escape(char[] ch, int start, int length, boolean isAttVal, Writer out) throws IOException {
                        out.write(ch, start, length);
                    }
                });

            return getNext().handle(cr);
        }
    }