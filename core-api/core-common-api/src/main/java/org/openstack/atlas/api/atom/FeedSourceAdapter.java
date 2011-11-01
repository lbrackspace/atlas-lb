package org.openstack.atlas.api.atom;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

import java.util.Map;

public interface FeedSourceAdapter {

    public Feed getFeed(Map<String, Object> attributes) throws UnsupportedOperationException;

    public Feed getFeed(Map<String, Object> attributes, int page, String markerId) throws UnsupportedOperationException;

    public Entry getEntry(Map<String, Object> attributes, String id) throws UnsupportedOperationException;
}
