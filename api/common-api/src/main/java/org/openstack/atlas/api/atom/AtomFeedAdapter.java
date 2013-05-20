package org.openstack.atlas.api.atom;

import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.cfg.PublicApiServiceConfigurationKeys;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;

import java.util.List;
import java.util.Map;

public class AtomFeedAdapter implements FeedSourceAdapter {

    private EntryBuilder entryBuilder;
    private Configuration configuration;

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Feed getFeed(Map<String, Object> attributes) throws UnsupportedOperationException {
        FeedType feedType = (FeedType) attributes.get("feedType");
        final String baseUri = configuration.getString(PublicApiServiceConfigurationKeys.base_uri);
        final Feed feed = createNewFeed(attributes, baseUri);

        List<Entry> entries = entryBuilder.createEntries(attributes, baseUri);
        for (Entry storedEntry : entries) {
            feed.addEntry(storedEntry);
        }

        return feed;
    }

    @Override
    public Feed getFeed(Map<String, Object> attributes, int page, String markerId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Entry getEntry(Map<String, Object> attributes, String id) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public void setEntryBuilder(EntryBuilder entryBuilder) {
        this.entryBuilder = entryBuilder;
    }

    private Feed createNewFeed(Map<String, Object> attributes, String baseUri) {
        final Feed feed = AbderaSupport.getAbderaInstance().getFactory().newFeed();
        FeedType feedType = (FeedType) attributes.get("feedType");
        Integer accountId = (Integer) attributes.get("accountId");
        Integer page = (Integer) attributes.get("page");
        Integer loadBalancerId = null;
        Link prevLink = null;
        Link nextLink = null;

        if (!feedType.equals(FeedType.PARENT_FEED)) {
            loadBalancerId = (Integer) attributes.get("loadBalancerId");
        }

        if (page != null && page > 0) {
            if (page > 1) {
                prevLink = AbderaSupport.getAbderaInstance().getFactory().newLink();
                prevLink.setRel("previous");
                feed.addLink(prevLink);
            }
        }

        nextLink = AbderaSupport.getAbderaInstance().getFactory().newLink();
        nextLink.setRel("next");
        feed.addLink(nextLink);


        switch (feedType) {
            case PARENT_FEED:
                feed.setTitle("Parent Feed");
                feed.setId(String.format("%d-loadbalancers", accountId));
                if (prevLink != null) {
                    prevLink.setHref(entryBuilder.buildCompleteUri(String.format("/%d/loadbalancers.atom", accountId), baseUri));
                }
                if (nextLink != null) {
                    nextLink.setHref(entryBuilder.buildCompleteUri(String.format("/%d/loadbalancers.atom", accountId), baseUri));
                }
                break;
            case LOADBALANCER_FEED:
                feed.setTitle("Load Balancer Feed");
                feed.setId(String.format("%d-loadbalancers-%d", accountId, loadBalancerId));
                if (prevLink != null) {
                    prevLink.setHref(entryBuilder.buildCompleteUri(String.format("/%d/loadbalancers/%d.atom", accountId, loadBalancerId), baseUri));
                }
                if (nextLink != null) {
                    nextLink.setHref(entryBuilder.buildCompleteUri(String.format("/%d/loadbalancers/%d.atom", accountId, loadBalancerId), baseUri));
                }
                break;
            case NODES_FEED:
                feed.setTitle("Nodes Feed");
                feed.setId(String.format("%d-loadbalancers-%d-nodes", accountId, loadBalancerId));
                if (prevLink != null) {
                    prevLink.setHref(entryBuilder.buildCompleteUri(String.format("/%d/loadbalancers/%d/nodes.atom", accountId, loadBalancerId), baseUri));
                }
                if (nextLink != null) {
                    nextLink.setHref(entryBuilder.buildCompleteUri(String.format("/%d/loadbalancers/%d/nodes.atom", accountId, loadBalancerId), baseUri));
                }
                break;
            case NODE_SERVICE_FEED:
                feed.setTitle("Node Service Feed");
                feed.setId(String.format("%d-loadbalancers-%d-nodeservice", accountId, loadBalancerId));
                if (prevLink != null) {
                    prevLink.setHref(entryBuilder.buildCompleteUri(String.format("/%d/loadbalancers/%d/nodes/events.atom", accountId, loadBalancerId), baseUri));
                }
                if (nextLink != null) {
                    nextLink.setHref(entryBuilder.buildCompleteUri(String.format("/%d/loadbalancers/%d/nodes/events.atom", accountId, loadBalancerId), baseUri));
                }
                break;
            case NODE_FEED:
                Integer nodeId = (Integer) attributes.get("nodeId");
                feed.setTitle("Node Feed");
                feed.setId(String.format("%d-loadbalancers-%d-nodes-%d", accountId, loadBalancerId, nodeId));
                if (prevLink != null) {
                    prevLink.setHref(entryBuilder.buildCompleteUri(String.format("/%d/loadbalancers/%d/nodes/%d.atom", accountId, loadBalancerId, nodeId), baseUri));
                }
                if (nextLink != null) {
                    nextLink.setHref(entryBuilder.buildCompleteUri(String.format("/%d/loadbalancers/%d/nodes/%d.atom", accountId, loadBalancerId, nodeId), baseUri));
                }
                break;
            case VIRTUAL_IPS_FEED:
                feed.setTitle("Virtual Ips Feed");
                feed.setId(String.format("%d-loadbalancers-%d-virtualips", accountId, loadBalancerId));
                if (prevLink != null) {
                    prevLink.setHref(entryBuilder.buildCompleteUri(String.format("/%d/loadbalancers/%d/virtualips.atom", accountId, loadBalancerId), baseUri));
                }
                if (nextLink != null) {
                    nextLink.setHref(entryBuilder.buildCompleteUri(String.format("/%d/loadbalancers/%d/virtualips.atom", accountId, loadBalancerId), baseUri));
                }
                break;
            case ACCESS_LIST_FEED:
                feed.setTitle("Access List Feed");
                feed.setId(String.format("%d-loadbalancers-%d-accesslist", accountId, loadBalancerId));
                if (prevLink != null) {
                    prevLink.setHref(entryBuilder.buildCompleteUri(String.format("/%d/loadbalancers/%d/accesslist.atom", accountId, loadBalancerId), baseUri));
                }
                if (nextLink != null) {
                    nextLink.setHref(entryBuilder.buildCompleteUri(String.format("/%d/loadbalancers/%d/accesslist.atom", accountId, loadBalancerId), baseUri));
                }
                break;
            case CONNECTION_THROTTLE_FEED:
                feed.setTitle("Connection Throttle Feed");
                feed.setId(String.format("%d-loadbalancers-%d-connectionthrottle", accountId, loadBalancerId));
                if (prevLink != null) {
                    prevLink.setHref(entryBuilder.buildCompleteUri(String.format("/%d/loadbalancers/%d/connectionthrottle.atom", accountId, loadBalancerId), baseUri));
                }
                if (nextLink != null) {
                    nextLink.setHref(entryBuilder.buildCompleteUri(String.format("/%d/loadbalancers/%d/connectionthrottle.atom", accountId, loadBalancerId), baseUri));
                }
                break;
            case HEALTH_MONITOR_FEED:
                feed.setTitle("Health Monitor Feed");
                feed.setId(String.format("%d-loadbalancers-%d-healthmonitor", accountId, loadBalancerId));
                if (prevLink != null) {
                    prevLink.setHref(entryBuilder.buildCompleteUri(String.format("/%d/loadbalancers/%d/healthmonitor.atom", accountId, loadBalancerId), baseUri));
                }
                if (nextLink != null) {
                    nextLink.setHref(entryBuilder.buildCompleteUri(String.format("/%d/loadbalancers/%d/healthmonitor.atom", accountId, loadBalancerId), baseUri));
                }
                break;
            case SESSION_PERSISTENCE_FEED:
                feed.setTitle("Session Persistence Feed");
                feed.setId(String.format("%d-loadbalancers-%d-sessionpersistence", accountId, loadBalancerId));
                if (prevLink != null) {
                    prevLink.setHref(entryBuilder.buildCompleteUri(String.format("/%d/loadbalancers/%d/sessionpersistence.atom", accountId, loadBalancerId), baseUri));
                }
                if (nextLink != null) {
                    nextLink.setHref(entryBuilder.buildCompleteUri(String.format("/%d/loadbalancers/%d/sessionpersistence.atom", accountId, loadBalancerId), baseUri));
                }
                break;
        }

        if (prevLink != null) {
            prevLink.setHref(prevLink.getHref() + "?page=" + (page - 1));
        }

        if (nextLink != null) {
            if(page == null)
                nextLink.setHref(nextLink.getHref() + "?page=2");
            else
                nextLink.setHref(nextLink.getHref() + "?page=" + (page + 1));
        }

        feed.addAuthor("Rackspace Cloud");

        return feed;
    }

}
