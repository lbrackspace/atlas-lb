package org.openstack.atlas.service.domain.events.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;

@MappedSuperclass
public abstract class Event implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", unique = true, nullable = false)
    private Integer id;

    @Column(name = "account_id", nullable = false)
    private Integer accountId;

    @Column(name = "loadbalancer_id", nullable = false)
    private Integer loadbalancerId;

    @Column(name = "author", length = 32, nullable = true) // TODO: Should this be forced to not-null?
    private String author;

    @Column(name = "event_title", nullable = false)
    private String title;

    @Column(name = "event_description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 32, nullable = false)
    private EventType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 32, nullable = false)
    private CategoryType category;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", length = 32, nullable = false)
    private EventSeverity severity;

    @Column(name = "relative_uri")
    private String relativeUri;

    @Temporal(TemporalType.TIMESTAMP)
    private Calendar created;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public Integer getLoadbalancerId() {
        return loadbalancerId;
    }

    public void setLoadbalancerId(Integer loadbalancerId) {
        this.loadbalancerId = loadbalancerId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        int threshhold = 255;
        if (title.length() >= threshhold) {
            this.title = title.substring(0, threshhold -1);
        } else {
            this.title = title;
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        int threshhold = 1023;
        if (description.length() >= threshhold) {
            this.description = description.substring(0, threshhold -1);
        } else {
            this.description = description;
        }
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public CategoryType getCategory() {
        return category;
    }

    public void setCategory(CategoryType category) {
        this.category = category;
    }

    public EventSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(EventSeverity severity) {
        this.severity = severity;
    }

    public String getRelativeUri() {
        return relativeUri;
    }

    public void setRelativeUri(String relativeUri) {
        this.relativeUri = relativeUri;
    }

    public Calendar getCreated() {
        return created;
    }

    public void setCreated(Calendar created) {
        this.created = created;
    }

    @Override
    public String toString() {
        String attrString = getAttributesAsString();
        return String.format("{%s}", attrString);
    }

    public String getAttributesAsString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("id=%s ", vorn(getId())));
        sb.append(String.format("accountId=%s ", vorn(getAccountId())));
        sb.append(String.format("lb_id=%s ", vorn(getLoadbalancerId())));
        sb.append(String.format("category=\"%s\" ", vorn(getCategory())));
        sb.append(String.format("severity=\"%s\" ", vorn(getSeverity())));
        sb.append(String.format("desc=\"%s\" ", vorn(getDescription())));
        sb.append(String.format("author=\"%s\" ", vorn(getAuthor())));
        sb.append(String.format("created=\"%s\" ", vorn(getCreated())));
        return sb.toString();
    }

    protected String vorn(Object obj) {
        if (obj == null) {
            return "";
        }
        if (obj instanceof Calendar) {
            return ((Calendar) obj).getTime().toString();
        }
        return obj.toString();
    }
}
