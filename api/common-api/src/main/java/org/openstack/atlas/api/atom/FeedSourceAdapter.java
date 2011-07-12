package org.openstack.atlas.api.atom;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

import java.util.Map;

public interface FeedSourceAdapter {

    public Feed getFeed(Map<String, Object> attributes) throws java.lang.UnsupportedOperationException;

    public Feed getFeed(Map<String, Object> attributes, int page, String markerId) throws java.lang.UnsupportedOperationException;

    public Entry getEntry(Map<String, Object> attributes, String id) throws java.lang.UnsupportedOperationException;
}
