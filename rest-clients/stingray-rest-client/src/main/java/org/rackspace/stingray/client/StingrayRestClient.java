package org.rackspace.stingray.client;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import org.rackspace.stingray.client.bandwidth.Bandwidth;
import org.rackspace.stingray.client.config.Configuration;
import org.rackspace.stingray.client.counters.VirtualServerStats;
import org.rackspace.stingray.client.counters.VirtualServerStatsProperties;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.glb.GlobalLoadBalancing;
import org.rackspace.stingray.client.list.Child;
import org.rackspace.stingray.client.list.Children;
import org.rackspace.stingray.client.location.Location;
import org.rackspace.stingray.client.manager.RequestManager;
import org.rackspace.stingray.client.manager.StingrayRestClientManager;
import org.rackspace.stingray.client.manager.impl.RequestManagerImpl;
import org.rackspace.stingray.client.monitor.Monitor;
import org.rackspace.stingray.client.persistence.Persistence;
import org.rackspace.stingray.client.pool.Pool;
import org.rackspace.stingray.client.protection.Protection;
import org.rackspace.stingray.client.rate.Rate;
import org.rackspace.stingray.client.ssl.client.keypair.ClientKeypair;
import org.rackspace.stingray.client.ssl.keypair.Keypair;
import org.rackspace.stingray.client.tm.TrafficManager;
import org.rackspace.stingray.client.traffic.ip.TrafficIp;
import org.rackspace.stingray.client.util.ClientConstants;
import org.rackspace.stingray.client.virtualserver.VirtualServer;
import org.rackspace.stingray.client.virtualserver.VirtualServerProperties;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class StingrayRestClient extends StingrayRestClientManager {
    private RequestManager requestManager = new RequestManagerImpl();

    public StingrayRestClient(URI endpoint, Configuration config, Client client) {
        super(config, endpoint, client, false, null, null);
    }

    public StingrayRestClient(URI endpoint, Configuration config) {
        super(config, endpoint, null, false, null, null);
    }

    public StingrayRestClient(URI endpoint) {
        super(null, endpoint, null, false, null, null);
    }

    public StingrayRestClient(URI endpoint, String adminUser, String adminKey) {
        super(null, endpoint, null, false, adminUser, adminKey);
    }

    public StingrayRestClient(URI endpoint, boolean isDebugging, String adminUser, String adminKey) {
        super(null, endpoint, null, isDebugging, adminUser, adminKey);
    }

    public StingrayRestClient(boolean isDebugging) {
        super(null, null, null, isDebugging, null, null);
    }

    public StingrayRestClient(Configuration configuration) {
        super(configuration, null, null, false, null, null);
    }

    public StingrayRestClient(String adminKey){
        super(null, null, null, false, null, adminKey);
    }

    public StingrayRestClient() {
        super(null, null, null, false, null, null);
    }

    public void setRequestManager(RequestManager requestManager) {
        this.requestManager = requestManager;
    }

    /**
     * This method will check that a path is defined in one of the constants described in REST api documentation
     *
     * @param path Variable holding the path used in a request
     * @return Result from checking the path's validity
     */
    private Boolean isPathValid(String path) {
        return path.equals(ClientConstants.RATE_PATH) || path.equals(ClientConstants.PERSISTENCE_PATH)
                || path.equals(ClientConstants.POOL_PATH) || path.equals(ClientConstants.ACTIONSCRIPT_PATH)
                || path.equals(ClientConstants.BANDWIDTH_PATH) || path.equals(ClientConstants.CACRL_PATH)
                || path.equals(ClientConstants.CLIENTKEYPAIR_PATH) || path.equals(ClientConstants.EXTRAFILE_PATH)
                || path.equals(ClientConstants.GLB_PATH) || path.equals(ClientConstants.IP_PATH)
                || path.equals(ClientConstants.KEYPAIR_PATH) || path.equals(ClientConstants.LOCATION_PATH)
                || path.equals(ClientConstants.MONITOR_PATH) || path.equals(ClientConstants.MONITORSCRIPT_PATH)
                || path.equals(ClientConstants.PROTECTION_PATH) || path.equals(ClientConstants.V_SERVER_PATH)
                || path.equals(ClientConstants.TRAFFICMANAGER_PATH) || path.equals(ClientConstants.TRAFFICSCRIPT_PATH);
    }

    /**
     * Generic method to retrieve a list of the object at the specified path
     *
     * @param path Path to object endpoint in the rest client
     * @return the generic list retrieval method
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException , StingrayRestClientObjectNotFoundException
     */
    private List<Child> getItems(String path) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        if (isPathValid(path)) {
            ClientResponse response = requestManager.getList(endpoint, client, path);
            Children children = interpretResponse(response, Children.class);

            return children.getChildren();
        } else {
            throw new StingrayRestClientException("There was an error communicating with the resource endpoint: " + path);
        }
    }

    /**
     * Generic method to retrieve an item at the specified path
     *
     * @param name  Name of the object to retrieve
     * @param clazz Class type of the object being retrieved
     * @param path  Path to the object
     * @param <T>   Object generic declaration
     * @return Calls another method to retrieve an item of a specified type
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException , StingrayRestClientObjectNotFoundException
     */
    private <T> T getItem(String name, Class<T> clazz, String path) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getItem(name, clazz, path, MediaType.APPLICATION_JSON_TYPE);
    }

    /**
     * Overloaded method to retrieve an item at the specified path
     *
     * @param name  Name of the object to retrieve
     * @param clazz Class type of the object being retrieved
     * @param path  Path to the object
     * @param <T>   Object generic declaration
     * @param cType Type of request being sent (IE application/json)
     * @return Return object of a specified type
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException , StingrayRestClientObjectNotFoundException
     */
    private <T> T getItem(String name, Class<T> clazz, String path, MediaType cType) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getItem(name, clazz, path, endpoint, cType);
    }

    /**
     * Overloaded method to retrieve an item at the specified path
     *
     * @param name  Name of the object to retrieve
     * @param clazz Class type of the object being retrieved
     * @param path  Path to the object
     * @param endpoint Endpoint for object
     * @param <T>   Object generic declaration
     * @return Return object of a specified type
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException , StingrayRestClientObjectNotFoundException
     */
    private <T> T getItem(String name, Class<T> clazz, String path, URI endpoint) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getItem(name, clazz, path, endpoint, MediaType.APPLICATION_JSON_TYPE);
    }

    /**
     * Overloaded method to retrieve an item at the specified path and endpoint
     *
     * @param name  Name of the object to retrieve
     * @param clazz Class type of the object being retrieved
     * @param path  Path to the object
     * @param endpoint Endpoint for object
     * @param <T>   Object generic declaration
     * @param cType Type of request being sent (IE application/json)
     * @return Return object of a specified type
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException , StingrayRestClientObjectNotFoundException
     */
    private <T> T getItem(String name, Class<T> clazz, String path, URI endpoint, MediaType cType) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        if (isPathValid(path)) {
            ClientResponse response = requestManager.getItem(endpoint, client, path + name, cType);
            T obj = interpretResponse(response, clazz);
            return obj;
        } else {
            throw new StingrayRestClientException("There was an error communicating with the resource endpoint: " + path);
        }
    }

    /**
     * @param name
     * @param clazz
     * @param path
     * @param obj
     * @param <T>
     * @return
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    private <T> T createItem(String name, Class<T> clazz, String path, T obj) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return createItem(name, clazz, path, obj, MediaType.APPLICATION_JSON_TYPE);
    }

    /**
     * @param name
     * @param clazz
     * @param path
     * @param obj
     * @param cType
     * @param <T>
     * @return
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    private <T> T createItem(String name, Class<T> clazz, String path, T obj, MediaType cType) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return updateItem(name, clazz, path, obj, cType);
    }


    /**
     * @param name
     * @param clazz
     * @param path
     * @param obj
     * @param <T>
     * @return
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    private <T> T updateItem(String name, Class<T> clazz, String path, T obj) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return updateItem(name, clazz, path, obj, MediaType.APPLICATION_JSON_TYPE);
    }

    /**
     * @param name
     * @param clazz
     * @param path
     * @param obj
     * @param cType
     * @param <T>
     * @return
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    private <T> T updateItem(String name, Class<T> clazz, String path, T obj, MediaType cType) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        if (isPathValid(path)) {
            ClientResponse response = requestManager.updateItem(endpoint, client, path + name, obj, cType);
            return interpretResponse(response, clazz);
        } else {
            throw new StingrayRestClientException("There was an error communicating with the resource endpoint: " + path);
        }
    }


    /**
     * @param name
     * @param path
     * @return
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    private Boolean deleteItem(String name, String path) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        if (isPathValid(path))
            return requestManager.deleteItem(endpoint, client, path + name);
        else
            throw new StingrayRestClientException();
    }

    public VirtualServer createVirtualServer(String name, VirtualServer vs) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return createItem(name, VirtualServer.class, ClientConstants.V_SERVER_PATH, vs);
    }

    /**
     * @return A list of children representing individual virtual server names and URI's
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public List<Child> getVirtualServers() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getItems(ClientConstants.V_SERVER_PATH);
    }

    /**
     * @param name
     * @return
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public VirtualServer getVirtualServer(String name) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getItem(name, VirtualServer.class, ClientConstants.V_SERVER_PATH);
    }

    /**
     * @param name
     * @param virtualServer
     * @return
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public VirtualServer updateVirtualServer(String name, VirtualServer virtualServer) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return updateItem(name, VirtualServer.class, ClientConstants.V_SERVER_PATH, virtualServer);
    }

    /**
     * @param name
     * @return
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public boolean deleteVirtualServer(String name) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return deleteItem(name, ClientConstants.V_SERVER_PATH);
    }


    /**
     * @return the generic list for pools providing the name and the endpoint for a specific request
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public List<Child> getPools() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getItems(ClientConstants.POOL_PATH);
    }
    /*
     * POOLS
     */

    /**
     * @param name the virtual server name for pool retrieval
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Pool getPool(String name) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getItem(name, Pool.class, ClientConstants.POOL_PATH);
    }

    /**
     * @param name The virtual server name related to the pool
     * @param pool The pool object used to create a Stingray Pool
     * @return The configured pool object
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Pool createPool(String name, Pool pool) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return createItem(name, Pool.class, ClientConstants.POOL_PATH, pool);
    }

    /**
     * @param name The virtual server name related to the pool
     * @param pool The pool object used to create a Stingray Pool
     * @return The configured pool object
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Pool updatePool(String name, Pool pool) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return updateItem(name, Pool.class, ClientConstants.POOL_PATH, pool);
    }

    /**
     * @param name The virtual server name related to the pool
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Boolean deletePool(String name) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return deleteItem(name, ClientConstants.POOL_PATH);
    }


    /**
     * @return the generic list for actionScripts providing the name and the endpoint for a specific request
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public List<Child> getActionScripts() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getItems(ClientConstants.ACTIONSCRIPT_PATH);
    }

    /**
     * @param name the virtual server name for action script retrieval
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public File getActionScript(String name) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getItem(name, File.class, ClientConstants.ACTIONSCRIPT_PATH, MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }

    /**
     * @param name         The virtual server name related to the actionScript
     * @param actionScript The actionScript object used to create a Stingray Action Script
     * @return The configured Action Script object
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public File createActionScript(String name, File actionScript) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return createItem(name, File.class, ClientConstants.ACTIONSCRIPT_PATH, actionScript, MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }


    /**
     * @param name         The virtual server name related to the action script
     * @param actionScript The action script object used to create a Stingray action script
     * @return The configured action script object
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public File updateActionScript(String name, File actionScript) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return updateItem(name, File.class, ClientConstants.ACTIONSCRIPT_PATH, actionScript, MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }

    /**
     * @param name The virtual server name related to the action script
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Boolean deleteActionScript(String name) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return deleteItem(name, ClientConstants.ACTIONSCRIPT_PATH);
    }

    /**
     * @return the generic list for bandwidths providing the name and the endpoint for a specific request
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public List<Child> getBandwidths() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getItems(ClientConstants.BANDWIDTH_PATH);
    }

    /**
     * @param name the virtual server name for bandwidth retrieval
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Bandwidth getBandwidth(String name) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getItem(name, Bandwidth.class, ClientConstants.BANDWIDTH_PATH);
    }

    /**
     * @param name      The virtual server name related to the bandwidth
     * @param bandwidth The bandwidth object used to create a Stingray bandwidth
     * @return The configured Bandwidth object
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Bandwidth createBandwidth(String name, Bandwidth bandwidth) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return createItem(name, Bandwidth.class, ClientConstants.BANDWIDTH_PATH, bandwidth);
    }


    /**
     * @param name      The virtual server name related to the bandwidth
     * @param bandwidth The bandwidth object used to create a Stingray bandwidth
     * @return The configured bandwidth object
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Bandwidth updateBandwidth(String name, Bandwidth bandwidth) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return updateItem(name, Bandwidth.class, ClientConstants.BANDWIDTH_PATH, bandwidth);
    }

    /**
     * @param name The virtual server name related to the bandwidth
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Boolean deleteBandwidth(String name) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return deleteItem(name, ClientConstants.BANDWIDTH_PATH);
    }


    /**
     * @return the generic list for extra files providing the name and the endpoint for a specific request
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public List<Child> getExtraFiles() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getItems(ClientConstants.EXTRAFILE_PATH);
    }

    /**
     * @param fileName the virtual server name for extra file retrieval
     * @return File
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public File getExtraFile(String fileName) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getExtraFile(fileName, MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }

    /**
     * @param fileName
     * @param cType
     * @return File
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public File getExtraFile(String fileName, MediaType cType) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getItem(fileName, File.class, ClientConstants.EXTRAFILE_PATH, cType);
    }

    /**
     * @param fileName  The virtual server name related to the extra file
     * @param extraFile The extra file object used to create a Stingray extra file
     * @return The configured ExtraFile object
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public File createExtraFile(String fileName, File extraFile) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return createExtraFile(fileName, extraFile, MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }

    /**
     * @param name
     * @param extraFile
     * @param cType
     * @return File
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public File createExtraFile(String name, File extraFile, MediaType cType) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return createItem(name, File.class, ClientConstants.EXTRAFILE_PATH, extraFile, cType);
    }


    /**
     * @param fileName  The virtual server name related to the extra file
     * @param extraFile The extra file object used to create a Stingray extra files
     * @return File
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public File updateExtraFile(String fileName, File extraFile) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return updateExtraFile(fileName, extraFile, MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }

    /**
     * @param fileName
     * @param extraFile
     * @param cType
     * @return File
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public File updateExtraFile(String fileName, File extraFile, MediaType cType) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return updateItem(fileName, File.class, ClientConstants.EXTRAFILE_PATH, extraFile, cType);
    }

    /**
     * @param name The virtual server name related to the extra file
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Boolean deleteExtraFile(String name) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return deleteItem(name, ClientConstants.EXTRAFILE_PATH);
    }

    /**
     * @return the generic list for global load balancers providing the name and the endpoint for a specific request
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public List<Child> getGlbs() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getItems(ClientConstants.GLB_PATH);
    }

    /**
     * @param name the virtual server name for global load balancing retrieval
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public GlobalLoadBalancing getGlb(String name) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getItem(name, GlobalLoadBalancing.class, ClientConstants.GLB_PATH);
    }

    /**
     * @param name                The virtual server name related to the Glb
     * @param globalLoadBalancing The global load balancing object used to create a Stingray global load balancer
     * @return The configured ExtraFile object
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public GlobalLoadBalancing createGlb(String name, GlobalLoadBalancing globalLoadBalancing) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return createItem(name, GlobalLoadBalancing.class, ClientConstants.GLB_PATH, globalLoadBalancing);
    }


    /**
     * @param name                The virtual server name related to the Glb
     * @param globalLoadBalancing The global load balancing object used to create a Stingray global load balancer
     * @return The configured global load balancing object
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public GlobalLoadBalancing updateGlb(String name, GlobalLoadBalancing globalLoadBalancing) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return updateItem(name, GlobalLoadBalancing.class, ClientConstants.GLB_PATH, globalLoadBalancing);

    }

    /**
     * @param name The virtual server name related to the global load balancing
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Boolean deleteGlb(String name) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return deleteItem(name, ClientConstants.GLB_PATH);
    }


    /**
     * @return the generic list for locations providing the name and the endpoint for a specific request
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public List<Child> getLocations() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getItems(ClientConstants.LOCATION_PATH);
    }

    /**
     * @param name the virtual server name for extra file retrieval
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Location getLocation(String name) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getItem(name, Location.class, ClientConstants.LOCATION_PATH);
    }

    /**
     * @param name     The virtual server name related to the extra file
     * @param location The location object used to create a Stingray location
     * @return The configured Location object
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Location createLocation(String name, Location location) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return createItem(name, Location.class, ClientConstants.LOCATION_PATH, location);
    }


    /**
     * @param name     The virtual server name related to the location
     * @param location The extra file object used to create a Stingray locations
     * @return The configured Location object
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Location updateLocation(String name, Location location) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return updateItem(name, Location.class, ClientConstants.LOCATION_PATH, location);
    }

    /**
     * @param name The virtual server name related to the location
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Boolean deleteLocation(String name) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return deleteItem(name, ClientConstants.LOCATION_PATH);
    }


    /**
     * @return the generic list for extra files providing the name and the endpoint for a specific request
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public List<Child> getMonitors() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getItems(ClientConstants.MONITOR_PATH);
    }

    /**
     * @param name the virtual server name for monitor retrieval
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Monitor getMonitor(String name) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getItem(name, Monitor.class, ClientConstants.MONITOR_PATH);
    }

    /**
     * @param name    The virtual server name related to the monitor
     * @param monitor The monitor object used to create a Stingray monitor
     * @return The configured Monitor object
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Monitor createMonitor(String name, Monitor monitor) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return createItem(name, Monitor.class, ClientConstants.MONITOR_PATH, monitor);
    }


    /**
     * @param name    The virtual server name related to the monitor
     * @param monitor The monitor object used to create a Stingray monitors
     * @return The configured Monitor object
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Monitor updateMonitor(String name, Monitor monitor) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return updateItem(name, Monitor.class, ClientConstants.MONITOR_PATH, monitor);
    }

    /**
     * @param name The virtual server name related to the monitor
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Boolean deleteMonitor(String name) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return deleteItem(name, ClientConstants.MONITOR_PATH);
    }


    /**
     * @return the generic list for monitor scripts providing the name and the endpoint for a specific request
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public List<Child> getMonitorScripts() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getItems(ClientConstants.MONITORSCRIPT_PATH);
    }

    /**
     * @param fileName the virtual server name for monitor script retrieval
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public File getMonitorScript(String fileName) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getItem(fileName, File.class, ClientConstants.MONITORSCRIPT_PATH, MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }

    /**
     * @param fileName      The virtual server name related to the monitor script
     * @param monitorScript The monitor script object used to create a Stingray monitor script
     * @return The configured MonitorScript object
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public File createMonitorScript(String fileName, File monitorScript) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return createItem(fileName, File.class, ClientConstants.MONITORSCRIPT_PATH, monitorScript, MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }


    /**
     * @param fileName      The virtual server name related to the monitor script
     * @param monitorScript The monitor script object used to create a Stingray monitor scripts
     * @return The configured MonitorScript object
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public File updateMonitorScript(String fileName, File monitorScript) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return updateItem(fileName, File.class, ClientConstants.MONITORSCRIPT_PATH, monitorScript, MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }

    /**
     * @param fileName The virtual server name related to the monitor script
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Boolean deleteMonitorScript(String fileName) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return deleteItem(fileName, ClientConstants.MONITORSCRIPT_PATH);
    }


    /**
     * @return the generic list for persistences providing the name and the endpoint for a specific request
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public List<Child> getPersistences() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getItems(ClientConstants.PERSISTENCE_PATH);
    }

    /**
     * @param name the virtual server name for persistence retrieval
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Persistence getPersistence(String name) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getItem(name, Persistence.class, ClientConstants.PERSISTENCE_PATH);
    }

    /**
     * @param name        The virtual server name related to the extra file
     * @param persistence The persistence object used to create a Stingray persistence
     * @return The configured Persistence object
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Persistence createPersistence(String name, Persistence persistence) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return createItem(name, Persistence.class, ClientConstants.PERSISTENCE_PATH, persistence);
    }


    /**
     * @param name        The virtual server name related to the persistence
     * @param persistence The persistence object used to create a Stingray persistence
     * @return The configured Persistence object
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Persistence updatePersistence(String name, Persistence persistence) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return updateItem(name, Persistence.class, ClientConstants.PERSISTENCE_PATH, persistence);
    }

    /**
     * @param name The virtual server name related to the persistence
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Boolean deletePersistence(String name) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return deleteItem(name, ClientConstants.PERSISTENCE_PATH);
    }


    /**
     * @return the generic list for protections providing the name and the endpoint for a specific request
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public List<Child> getProtections() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getItems(ClientConstants.PROTECTION_PATH);
    }

    /**
     * @param name the virtual server name for protection retrieval
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Protection getProtection(String name) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getItem(name, Protection.class, ClientConstants.PROTECTION_PATH);
    }

    /**
     * @param name       The virtual server name related to the protection
     * @param protection The protection object used to create a Stingray protection
     * @return The configured Protection object
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Protection createProtection(String name, Protection protection) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return createItem(name, Protection.class, ClientConstants.PROTECTION_PATH, protection);
    }


    /**
     * @param name       The virtual server name related to the bandwidth
     * @param protection The protection object used to create a Stingray protections
     * @return The configured Protection object
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Protection updateProtection(String name, Protection protection) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return updateItem(name, Protection.class, ClientConstants.PROTECTION_PATH, protection);
    }

    /**
     * @param name The virtual server name related to the extra file
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Boolean deleteProtection(String name) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return deleteItem(name, ClientConstants.PROTECTION_PATH);
    }


    /**
     * @return the generic list for rates providing the name and the endpoint for a specific request
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public List<Child> getRates() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getItems(ClientConstants.RATE_PATH);
    }

    /**
     * @param name the virtual server name for rate retrieval
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Rate getRate(String name) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getItem(name, Rate.class, ClientConstants.RATE_PATH);
    }

    /**
     * @param name The virtual server name related to the rate
     * @param rate The rate object used to create a Stingray rate
     * @return The configured Rate object
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Rate createRate(String name, Rate rate) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return createItem(name, Rate.class, ClientConstants.RATE_PATH, rate);
    }


    /**
     * @param name The virtual server name related to the rate
     * @param rate The rate object used to create a Stingray rates
     * @return The configured Rate object
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Rate updateRate(String name, Rate rate) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return createItem(name, Rate.class, ClientConstants.RATE_PATH, rate);
    }

    /**
     * @param name The virtual server name related to the rate
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Boolean deleteRate(String name) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return deleteItem(name, ClientConstants.RATE_PATH);
    }

    /**
     * @return the generic list for cacrls providing the name and the endpoint for a specific request
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public List<Child> getCacrls() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getItems(ClientConstants.CACRL_PATH);
    }

    /**
     * @param fileName the virtual server name for cacrl retrieval
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public File getCacrl(String fileName) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getItem(fileName, File.class, ClientConstants.CACRL_PATH, MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }

    /**
     * @param fileName The virtual server name related to the cacrl
     * @param cacrl    The cacrl object used to create a Stingray cacrl
     * @return The configured Cacrl object
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public File createCacrl(String fileName, File cacrl) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return createItem(fileName, File.class, ClientConstants.CACRL_PATH, cacrl, MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }


    /**
     * @param fileName The virtual server name related to the cacrl
     * @param cacrl    The cacrl object used to create a Stingray cacrl
     * @return The configured Cacrl object
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public File updateCacrl(String fileName, File cacrl) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return updateItem(fileName, File.class, ClientConstants.CACRL_PATH, cacrl, MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }

    /**
     * @param fileName The virtual server name related to the cacrl
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Boolean deleteCacrl(String fileName) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return deleteItem(fileName, ClientConstants.CACRL_PATH);
    }


    /**
     * @return the generic list for client keypairs providing the name and the endpoint for a specific request
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public List<Child> getClientKeypairs() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getItems(ClientConstants.CLIENTKEYPAIR_PATH);
    }

    /**
     * @param name the virtual server name for client keypair retrieval
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public ClientKeypair getClientKeypair(String name) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getItem(name, ClientKeypair.class, ClientConstants.CLIENTKEYPAIR_PATH);
    }

    /**
     * @param name          The virtual server name related to the client keypair
     * @param clientKeypair The client keypair object used to create a Stingray client keypair
     * @return The configured ClientKeypair object
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public ClientKeypair createClientKeypair(String name, ClientKeypair clientKeypair) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return createItem(name, ClientKeypair.class, ClientConstants.CLIENTKEYPAIR_PATH, clientKeypair);
    }


    /**
     * @param name          The virtual server name related to the clientkeypair
     * @param clientKeypair The client keypair object used to create a Stingray client keypairs
     * @return The configured ClientKeypair object
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public ClientKeypair updateClientKeypair(String name, ClientKeypair clientKeypair) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return createItem(name, ClientKeypair.class, ClientConstants.CLIENTKEYPAIR_PATH, clientKeypair);
    }

    /**
     * @param name The virtual server name related to the client keypair
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Boolean deleteClientKeypair(String name) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return deleteItem(name, ClientConstants.CLIENTKEYPAIR_PATH);
    }

    /**
     * @return the generic list for keypairs providing the name and the endpoint for a specific request
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public List<Child> getKeypairs() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getItems(ClientConstants.KEYPAIR_PATH);
    }

    /**
     * @param name the virtual server name for keypair retrieval
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Keypair getKeypair(String name) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getItem(name, Keypair.class, ClientConstants.KEYPAIR_PATH);
    }

    /**
     * @param name    The virtual server name related to the keypair
     * @param keypair The keypair object used to create a Stingray keypair
     * @return The configured Keypair object
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Keypair createKeypair(String name, Keypair keypair) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return createItem(name, Keypair.class, ClientConstants.KEYPAIR_PATH, keypair);
    }


    /**
     * @param name    The virtual server name related to the keypair
     * @param keypair The keypair object used to create a Stingray keypairs
     * @return The configured Keypair object
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Keypair updateKeypair(String name, Keypair keypair) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return createItem(name, Keypair.class, ClientConstants.KEYPAIR_PATH, keypair);
    }

    /**
     * @param name The virtual server name related to the keypair
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Boolean deleteKeypair(String name) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return deleteItem(name, ClientConstants.KEYPAIR_PATH);
    }


    /**
     * @return the generic list for traffic managers providing the name and the endpoint for a specific request
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public List<Child> getTrafficManagers() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getItems(ClientConstants.TRAFFICMANAGER_PATH);
    }

    /**
     * @param name the virtual server name for rate retrieval
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public TrafficManager getTrafficManager(String name) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getItem(name, TrafficManager.class, ClientConstants.TRAFFICMANAGER_PATH);
    }

    /**
     * @param name           The virtual server name related to the traffic manager
     * @param trafficManager The traffic manager object used to create a Stingray traffic manager
     * @return The configured TrafficManager object
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public TrafficManager createTrafficManager(String name, TrafficManager trafficManager) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return createItem(name, TrafficManager.class, ClientConstants.TRAFFICMANAGER_PATH, trafficManager);
    }


    /**
     * @param name           The virtual server name related to the traffic manager
     * @param trafficManager The traffic manager object used to create a Stingray traffic manager
     * @return The configured TrafficManager object
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public TrafficManager updateTrafficManager(String name, TrafficManager trafficManager) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return createItem(name, TrafficManager.class, ClientConstants.TRAFFICMANAGER_PATH, trafficManager);
    }

    /**
     * @param name The virtual server name related to the rate
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Boolean deleteTrafficManager(String name) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return deleteItem(name, ClientConstants.TRAFFICMANAGER_PATH);
    }

    /**
     * @return the generic list for trafficscripts providing the name and the endpoint for a specific request
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public List<Child> getTrafficscripts() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getItems(ClientConstants.TRAFFICSCRIPT_PATH);
    }

    /**
     * @param fileName the virtual server name for trafficscript retrieval
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public File getTraffiscript(String fileName) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getItem(fileName, File.class, ClientConstants.TRAFFICSCRIPT_PATH, MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }

    /**
     * @param fileName      The virtual server name related to the trafficscript
     * @param trafficscript The rate object used to create a Stingray trafficscript
     * @return The configured Trafficscript object
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public File createTrafficscript(String fileName, File trafficscript) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return createItem(fileName, File.class, ClientConstants.TRAFFICSCRIPT_PATH, trafficscript, MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }


    /**
     * @param name          The virtual server name related to the trafficscript
     * @param trafficscript The trafficscript object used to create a Stingray trafficscript
     * @return The configured Trafficscript object
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public File updateTrafficScript(String name, File trafficscript) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return createItem(name, File.class, ClientConstants.TRAFFICSCRIPT_PATH, trafficscript, MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }

    /**
     * @param name The virtual server name related to the trafficscript
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Boolean deleteTrafficscript(String name) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return deleteItem(name, ClientConstants.TRAFFICSCRIPT_PATH);
    }

    /**
     * @return the generic list for TrafficIps providing the name and the endpoint for a specific request
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public List<Child> getTrafficIps() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getItems(ClientConstants.IP_PATH);
    }

    /**
     * @param name the virtual server name for rate retrieval
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public TrafficIp getTrafficIp(String name) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return getItem(name, TrafficIp.class, ClientConstants.IP_PATH);
    }

    /**
     * @param name      The virtual server name related to the Traffic Ip
     * @param trafficIp The rate object used to create a Stingray Traffic Ip
     * @return The configured TrafficIp object
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public TrafficIp createTrafficIp(String name, TrafficIp trafficIp) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return createItem(name, TrafficIp.class, ClientConstants.IP_PATH, trafficIp);
    }


    /**
     * @param name      The virtual server name related to the Traffic Ip
     * @param trafficIp The Traffic Ip object used to create a Stingray Traffic Ip
     * @return The configured TrafficIp object
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public TrafficIp updateTrafficIp(String name, TrafficIp trafficIp) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return createItem(name, TrafficIp.class, ClientConstants.IP_PATH, trafficIp);
    }

    /**
     * @param name The virtual server name related to the TrafficIp
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public Boolean deleteTrafficIp(String name) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        return deleteItem(name, ClientConstants.IP_PATH);
    }

    /**
     * @param name the virtual server name for stats retrieval
     * @throws StingrayRestClientObjectNotFoundException
     */
    public VirtualServerStats getVirtualServerStats(String name, URI endpoint) {
        VirtualServerStats stats = new VirtualServerStats();
        try {
            stats = getItem(name, VirtualServerStats.class, ClientConstants.V_SERVER_PATH, endpoint);
        } catch (StingrayRestClientObjectNotFoundException e) {
            stats.setStatistics(getZeroStats());
        } catch (StingrayRestClientException e) {
            stats.setStatistics(getZeroStats());
        }
        return stats;
    }

    /**
     * Destroy the StingrayRestClient
     */
    public void destroy() {
        client.destroy();
    }

    private VirtualServerStatsProperties getZeroStats() {
        VirtualServerStatsProperties props = new VirtualServerStatsProperties();
        props.setConnect_timed_out(0);
        props.setConnection_errors(0);
        props.setConnection_failures(0);
        props.setData_timed_out(0);
        props.setKeepalive_timed_out(0);
        props.setMax_conn(0);
        props.setCurrent_conn(0);
        return props;
    }
}
