package org.openstack.atlas.service.domain.entity;


import javax.persistence.*;
import java.io.Serializable;

@javax.persistence.Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
        name = "vendor",
        discriminatorType = DiscriminatorType.STRING
)
@DiscriminatorValue("CORE")
@Table(name = "connection_throttle")
public class ConnectionThrottle extends Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;

	@OneToOne
	@JoinColumn(name = "load_balancer_id")
	private LoadBalancer loadBalancer;

	@Column(name = "max_request_rate", nullable = false)
	private Integer maxRequestRate = 100000;

	@Column(name = "rate_interval", nullable = false)
	private Integer rateInterval = 3600;

	public LoadBalancer getLoadBalancer() {
		return loadBalancer;
	}

	public void setLoadBalancer(LoadBalancer loadBalancer) {
		this.loadBalancer = loadBalancer;
	}

	public Integer getMaxRequestRate() {
		return maxRequestRate;
	}

	public void setMaxRequestRate(Integer maxRequestRate) {
		this.maxRequestRate = maxRequestRate;
	}

	public Integer getRateInterval() {
		return rateInterval;
	}

	public void setRateInterval(Integer rateInterval) {
		this.rateInterval = rateInterval;
	}

    @Override
    public String toString() {
        return "ConnectionThrottle{" +
                "rateInterval=" + rateInterval +
                ", maxRequestRate=" + maxRequestRate +
                '}';
    }
}
