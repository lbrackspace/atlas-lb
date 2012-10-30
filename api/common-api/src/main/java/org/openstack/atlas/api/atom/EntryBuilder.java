package org.openstack.atlas.api.atom;

import org.openstack.atlas.service.domain.events.entities.*;
import org.openstack.atlas.service.domain.events.repository.LoadBalancerEventRepository;
import org.apache.abdera.model.Entry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class EntryBuilder {

    private LoadBalancerEventRepository eventRepository;

    public void setEventRepository(LoadBalancerEventRepository eventsRepository) {
        this.eventRepository = eventsRepository;
    }

    public List<Entry> createEntries(Map<String, Object> attributes, String baseUri) {
        List<Entry> entries = new ArrayList<Entry>();
        FeedType feedType = (FeedType) attributes.get("feedType");
        Integer accountId = (Integer) attributes.get("accountId");
        Integer page = (Integer) attributes.get("page");
        Integer loadBalancerId = null;

        if (!feedType.equals(FeedType.PARENT_FEED)) {
            loadBalancerId = (Integer) attributes.get("loadBalancerId");
        }

        switch (feedType) {
            case PARENT_FEED:
                List<LoadBalancerServiceEvent> lbServiceEvents = eventRepository.getAllEventsForAccount(accountId, page);

                for (LoadBalancerServiceEvent event : lbServiceEvents) {
                    Entry entry = createGenericEntry(event, baseUri);
                    entries.add(entry);
                }

                break;
            case LOADBALANCER_FEED:
                List<LoadBalancerEvent> lbEvents = eventRepository.getAllLoadBalancerEvents(accountId, loadBalancerId, page);

                for (LoadBalancerEvent event : lbEvents) {
                    Entry entry = createGenericEntry(event, baseUri);
                    entries.add(entry);
                }

                break;
            case NODES_FEED:
                List<NodeEvent> lbNodeEvents = eventRepository.getAllNodeEvents(accountId, loadBalancerId, page);

                for (NodeEvent event : lbNodeEvents) {
                    Entry entry = createGenericEntry(event, baseUri);
                    entries.add(entry);
                }

                break;
            case NODE_SERVICE_FEED:
                List<NodeServiceEvent> lbNodeServiceEvents = eventRepository.getNodeServiceEvents(accountId, loadBalancerId, page);

                for (NodeServiceEvent event : lbNodeServiceEvents) {
                    Entry entry = createGenericEntry(event, baseUri);
                    entry.setSummary(event.getDescription());
                    entry.setContent("Details: " + event.getDetailedMessage());
                    entries.add(entry);
                }

                break;
            case NODE_FEED:
                Integer nodeId = (Integer) attributes.get("nodeId");
                List<NodeEvent> nodeEvents = eventRepository.getNodeEvents(accountId, loadBalancerId, nodeId, page);

                for (NodeEvent event : nodeEvents) {
                    Entry entry = createGenericEntry(event, baseUri);
                    entries.add(entry);
                }

                break;
            case VIRTUAL_IPS_FEED:
                List<VirtualIpEvent> virtualIpEvents = eventRepository.getAllVirtualIpEvents(accountId, loadBalancerId, page);

                for (VirtualIpEvent event : virtualIpEvents) {
                    Entry entry = createGenericEntry(event, baseUri);
                    entries.add(entry);
                }

                break;
            case ACCESS_LIST_FEED:
                List<AccessListEvent> accessListEvents = eventRepository.getAllAccessListEvents(accountId, loadBalancerId, page);

                for (AccessListEvent event : accessListEvents) {
                    Entry entry = createGenericEntry(event, baseUri);
                    entries.add(entry);
                }

                break;
            case CONNECTION_THROTTLE_FEED:
                List<ConnectionLimitEvent> connectionLimitEvents = eventRepository.getAllConnectionLimitEvents(accountId, loadBalancerId, page);

                for (ConnectionLimitEvent event : connectionLimitEvents) {
                    Entry entry = createGenericEntry(event, baseUri);
                    entries.add(entry);
                }

                break;
            case HEALTH_MONITOR_FEED:
                List<HealthMonitorEvent> healthMonitorEvents = eventRepository.getAllHealthMonitorEvents(accountId, loadBalancerId, page);

                for (HealthMonitorEvent event : healthMonitorEvents) {
                    Entry entry = createGenericEntry(event, baseUri);
                    entries.add(entry);
                }

                break;
            case SESSION_PERSISTENCE_FEED:
                List<SessionPersistenceEvent> sessionPersistenceEvents = eventRepository.getAllSessionPersistenceEvents(accountId, loadBalancerId, page);

                for (SessionPersistenceEvent event : sessionPersistenceEvents) {
                    Entry entry = createGenericEntry(event, baseUri);
                    entries.add(entry);
                }

                break;
        }

        return entries;
    }

    private Entry createGenericEntry(Event event, String baseUri) {
        Entry entry = AbderaSupport.getAbderaInstance().newEntry();
        entry.setTitle(event.getTitle());
        entry.setSummary(event.getDescription());
        if(event.getAuthor() != null) entry.addAuthor(event.getAuthor());
        entry.addLink(buildCompleteUri(event.getRelativeUri(), baseUri));
        entry.setId(generateEntryId(event));
        entry.addCategory(event.getCategory().toString());
        entry.setUpdated(event.getCreated().getTime());
        return entry;
    }

    public String buildCompleteUri(String relativeUri, String baseUri) {
        return relativeUri == null ? null : baseUri + relativeUri;
    }

    public String generateEntryId(Event event) {
        if (event.getRelativeUri() == null || event.getRelativeUri().equals(""))
            return generateIdCompatibleTimestamp(event.getCreated());

        String[] uriComponenets = event.getRelativeUri().split("/");
        String entryId = "";

        for (String uriComponenet : uriComponenets) {
            if (!uriComponenet.equals("")) entryId = entryId + uriComponenet + "-";
        }

        entryId = entryId + (generateIdCompatibleTimestamp(event.getCreated()));

        return entryId;
    }

    public String generateIdCompatibleTimestamp(Calendar cal) {
        return String.valueOf(cal.get(Calendar.YEAR))
                + String.valueOf(cal.get(Calendar.DAY_OF_YEAR))
                + String.valueOf(cal.get(Calendar.HOUR_OF_DAY))
                + String.valueOf(cal.get(Calendar.MINUTE))
                + String.valueOf(cal.get(Calendar.SECOND))
                + String.valueOf(cal.get(Calendar.MILLISECOND));
    }
}
