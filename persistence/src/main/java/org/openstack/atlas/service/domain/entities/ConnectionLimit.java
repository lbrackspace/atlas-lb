package org.openstack.atlas.service.domain.entities;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;

@javax.persistence.Entity
@Table(name = "connection_limit")
public class ConnectionLimit extends Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;

	@OneToOne
	@JoinColumn(name = "loadbalancer_id")
	private LoadBalancer loadbalancer;

	@Column(name = "min_connections", nullable = false)
	private Integer minConnections;

	@Column(name = "max_connection", nullable = false)
	private Integer maxConnections;

	@Column(name = "max_connectionrate", nullable = false)
	private Integer maxConnectionRate;

	@Column(name = "rate_interval", nullable = false)
	private Integer rateInterval;

	public LoadBalancer getLoadBalancer() {
		return loadbalancer;
	}

	public void setLoadBalancer(LoadBalancer loadBalancer) {
		this.loadbalancer = loadBalancer;
	}

	private static String vorn(Object obj) {
		return obj == null ? "null" : obj.toString();
	}

	public Integer getMinConnections() {
		return minConnections;
	}

	public void setMinConnections(Integer minConnections) {
		this.minConnections = minConnections;
	}

	public Integer getMaxConnections() {
		return maxConnections;
	}

	public void setMaxConnections(Integer maxConnections) {
		this.maxConnections = maxConnections;
	}

	public Integer getMaxConnectionRate() {
		return maxConnectionRate;
	}

	public void setMaxConnectionRate(Integer maxConnectionRate) {
		this.maxConnectionRate = maxConnectionRate;
	}

	public Integer getRateInterval() {
		return rateInterval;
	}

	public void setRateInterval(Integer rateInterval) {
		this.rateInterval = rateInterval;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append(String.format("id=%s, ", vorn(this.getId())));
		sb.append(String.format("loadbalancer_id=%s, ",
				vorn(this.getLoadBalancer())));
		sb.append(String.format("MaxConnectionRate=%s, ",
				this.getMaxConnectionRate()));
		sb.append(String.format("RateInterval=%s, ", this.getRateInterval()));
		sb.append(String.format("MaxConnections=%s, ",
				vorn(this.getMaxConnections())));
		sb.append(String.format("MinConnections=%s",
				vorn(this.getMinConnections())));
		sb.append("}");
		return sb.toString();
	}

}
