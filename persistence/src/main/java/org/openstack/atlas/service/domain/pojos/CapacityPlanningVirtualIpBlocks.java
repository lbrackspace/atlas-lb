package org.openstack.atlas.service.domain.pojos;

public class CapacityPlanningVirtualIpBlocks {


    private final static long serialVersionUID = 532512316L;


    /*
    * # of configured public IP addresses for the cluster     --select count(*) from virtual_is where cluster id = :1 and type = PUBLIC
   * # of configured ServicerNet addresses for the cluster.    ----select count(*) from virtual_is where cluster id = :1 and type = SERVICENET
   * # of free and clear public IP addresses within cluster     --rows in virtual_ip table but not in loadbalancer_virtual ip and tyep = Servicenet
   * # of free and clear ServiceNet IP addresses within cluster
   * # of recently deallocated public IP addresses in holding state. (recently 24 hours) count() from virtual_id where type=public cluster id = :id and last_last deallocation date between current and curretn -24
   * # of recently deallocated ServiceNet IP addresses in holding state.
   * # of public IP addresses allocated today   --last allocated time within last 24 hours
   * # of ServiceNet IP addresses allocated today
   * Average# of public IP addresses allocated (within the last week)
   * Average# of ServiceNet IP addresses allocated (within the last week)
   * Estimate of remaining # of days of availability of public IP addresses - number of IPs were allocated per day divided by the number of unallocated IPs for the last 7 days 
   * Estimate of remaining # of days of availability of ServiceNet addresses. - look at over the 7 days on average how many IPs were allocated per day divided by the number of unallocated IPs we have
    */
    protected long configuredIP;
    protected long configuredServiceNet;
    protected long freeIP;
    protected long freeServiceNet;
    protected long deallocatiedIP;
    protected long deallocatedServiceNet;
    protected long allocatedIP;
    protected long allocatedServiceNet;
    protected long recentlyAllocatedIP;
    protected long receltyAllocatedServiceNet;
    protected long ipdaysavailable;
    protected long serviceNetdaysavailable;                               


    public void setConfiguredIP(long configuredIP) {
        this.configuredIP = configuredIP;
    }

    public void setConfiguredServiceNet(long configuredServiceNet) {
        this.configuredServiceNet = configuredServiceNet;
    }

    public void setFreeIP(long freeIP) {
        this.freeIP = freeIP;
    }

    public void setFreeServiceNet(long freeServiceNet) {
        this.freeServiceNet = freeServiceNet;
    }

    public void setDeallocatiedIP(long deallocatiedIP) {
        this.deallocatiedIP = deallocatiedIP;
    }

    public void setDeallocatedServiceNet(long deallocatedServiceNet) {
        this.deallocatedServiceNet = deallocatedServiceNet;
    }

    public void setAllocatedIP(long allocatedIP) {
        this.allocatedIP = allocatedIP;
    }

    public void setAllocatedServiceNet(long allocatedServiceNet) {
        this.allocatedServiceNet = allocatedServiceNet;
    }

    public void setRecentlyAllocatedIP(long recentlyAllocatedIP) {
        this.recentlyAllocatedIP = recentlyAllocatedIP;
    }

    public void setReceltyAllocatedServiceNet(long receltyAllocatedServiceNet) {
        this.receltyAllocatedServiceNet = receltyAllocatedServiceNet;
    }

    public void setIpdaysavailable(long ipdaysavailable) {
        this.ipdaysavailable = ipdaysavailable;
    }

    public void setServiceNetdaysavailable(long serviceNetdaysavailable) {
        this.serviceNetdaysavailable = serviceNetdaysavailable;
    }

}

