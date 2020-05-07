package org.rackspace.vtm.client;

import org.rackspace.vtm.client.bandwidth.Bandwidth;
import org.rackspace.vtm.client.config.Configuration;
import org.rackspace.vtm.client.counters.*;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;
import org.rackspace.vtm.client.glb.GlobalLoadBalancing;
import org.rackspace.vtm.client.list.Child;
import org.rackspace.vtm.client.list.Children;
import org.rackspace.vtm.client.location.Location;
import org.rackspace.vtm.client.manager.VTMRequestManager;
import org.rackspace.vtm.client.manager.VTMRestClientManager;
import org.rackspace.vtm.client.manager.impl.VTMRequestManagerImpl;
import org.rackspace.vtm.client.monitor.Monitor;
import org.rackspace.vtm.client.persistence.Persistence;
import org.rackspace.vtm.client.pool.Pool;
import org.rackspace.vtm.client.protection.Protection;
import org.rackspace.vtm.client.rate.Rate;
import org.rackspace.vtm.client.settings.GlobalSettings;
import org.rackspace.vtm.client.ssl.client.keypair.ClientKeypair;
import org.rackspace.vtm.client.ssl.keypair.Keypair;
import org.rackspace.vtm.client.status.Backup;
import org.rackspace.vtm.client.status.BackupProperties;
import org.rackspace.vtm.client.status.Backup_;
import org.rackspace.vtm.client.tm.TrafficManager;
import org.rackspace.vtm.client.traffic.ip.TrafficIp;
import org.rackspace.vtm.client.util.ClientConstants;
import org.rackspace.vtm.client.virtualserver.VirtualServer;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class VTMRestClient extends VTMRestClientManager {
    private VTMRequestManager requestManager = new VTMRequestManagerImpl();

    public VTMRestClient(URI endpoint, Configuration config, Client client) {
        super(config, endpoint, client, false, null, null);
    }

    public VTMRestClient(URI endpoint, Configuration config) {
        super(config, endpoint, null, false, null, null);
    }

    public VTMRestClient(URI endpoint) {
        super(null, endpoint, null, false, null, null);
    }

    public VTMRestClient(URI endpoint, String adminUser, String adminKey) {
        super(null, endpoint, null, false, adminUser, adminKey);
    }

    public VTMRestClient(URI endpoint, boolean isDebugging, String adminUser, String adminKey) {
        super(null, endpoint, null, isDebugging, adminUser, adminKey);
    }

    public VTMRestClient(boolean isDebugging) {
        super(null, null, null, isDebugging, null, null);
    }

    public VTMRestClient(Configuration configuration) {
        super(configuration, null, null, false, null, null);
    }

    public VTMRestClient(String adminKey){
        super(null, null, null, false, null, adminKey);
    }

    public VTMRestClient() {
        super(null, null, null, false, null, null);
    }

    public void setRequestManager(VTMRequestManager requestManager) {
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
                || path.equals(ClientConstants.TRAFFICMANAGER_PATH) || path.equals(ClientConstants.TRAFFICSCRIPT_PATH)
                || path.equals(ClientConstants.GLOBAL_SETTINGS) || path.equals(ClientConstants.GLOBAL_COUNTERS)
                || path.equals(ClientConstants.STATUS_BACKUPS);
    }

    /**
     * Generic method to retrieve a list of the object at the specified path
     *
     * @param path Path to object endpoint in the rest client
     * @return the generic list retrieval method
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException , VTMRestClientObjectNotFoundException
     */
    public List<Child> getItems(String path) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        if (isPathValid(path)) {
            Response response = requestManager.getList(endpoint, client, path);
            Children children = (Children) interpretResponse(response, Children.class);

            return children.getChildren();
        } else {
            throw new VTMRestClientException("There was an error communicating with the resource endpoint: " + path);
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
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException , VTMRestClientObjectNotFoundException
     */
    public <T> T getItem(String name, Class<T> clazz, String path) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
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
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException , VTMRestClientObjectNotFoundException
     */
    public <T> T getItem(String name, Class<T> clazz, String path, MediaType cType) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
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
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException , VTMRestClientObjectNotFoundException
     */
    public <T> T getItem(String name, Class<T> clazz, String path, URI endpoint) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
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
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException , VTMRestClientObjectNotFoundException
     */
    public <T> T getItem(String name, Class<T> clazz, String path, URI endpoint, MediaType cType) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        if (isPathValid(path)) {
            Response response = requestManager.getItem(endpoint, client, path + name, cType);
            T obj = (T) interpretResponse(response, clazz);
            return obj;
        } else {
            throw new VTMRestClientException("There was an error communicating with the resource endpoint: " + path);
        }
    }

    /**
     * @param name
     * @param clazz
     * @param path
     * @param obj
     * @param <T>
     * @return
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public <T> T createItem(String name, Class<T> clazz, String path, T obj) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
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
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public <T> T createItem(String name, Class<T> clazz, String path, T obj, MediaType cType) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return updateItem(name, clazz, path, obj, cType);
    }


    /**
     * @param name
     * @param clazz
     * @param path
     * @param obj
     * @param <T>
     * @return
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public <T> T updateItem(String name, Class<T> clazz, String path, T obj) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
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
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public <T> T updateItem(String name, Class<T> clazz, String path, T obj, MediaType cType) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        if (isPathValid(path)  || clazz == BackupProperties.class) {
            Response response = requestManager.updateItem(endpoint, client, path + name, obj, cType);
            return (T) interpretResponse(response, clazz);
        } else {
            throw new VTMRestClientException("There was an error communicating with the resource endpoint: " + path);
        }
    }


    /**
     * @param name
     * @param path
     * @return
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public Response deleteItem(String name, String path) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        if (isPathValid(path))
            return requestManager.deleteItem(endpoint, client, path + name);
        else
            throw new VTMRestClientException();
    }

    public VirtualServer createVirtualServer(String name, VirtualServer vs) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return createItem(name, VirtualServer.class, ClientConstants.V_SERVER_PATH, vs);
    }

    /**
     * @return A list of children representing individual virtual server names and URI's
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public List<Child> getVirtualServers() throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return getItems(ClientConstants.V_SERVER_PATH);
    }

    /**
     * @param name
     * @return
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public VirtualServer getVirtualServer(String name) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return getItem(name, VirtualServer.class, ClientConstants.V_SERVER_PATH);
    }

    /**
     * @param name
     * @param virtualServer
     * @return
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public VirtualServer updateVirtualServer(String name, VirtualServer virtualServer) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return updateItem(name, VirtualServer.class, ClientConstants.V_SERVER_PATH, virtualServer);
    }

    /**
     * @param name
     * @return
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public Response deleteVirtualServer(String name) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return deleteItem(name, ClientConstants.V_SERVER_PATH);
    }


    /**
     * @return the generic list for pools providing the name and the endpoint for a specific request
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public List<Child> getPools() throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return getItems(ClientConstants.POOL_PATH);
    }
    /*
     * POOLS
     */

    /**
     * @param name the virtual server name for pool retrieval
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public Pool getPool(String name) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return getItem(name, Pool.class, ClientConstants.POOL_PATH);
    }

    /**
     * @param name The virtual server name related to the pool
     * @param pool The pool object used to create a Stingray Pool
     * @return The configured pool object
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public Pool createPool(String name, Pool pool) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return createItem(name, Pool.class, ClientConstants.POOL_PATH, pool);
    }

    /**
     * @param name The virtual server name related to the pool
     * @param pool The pool object used to create a Stingray Pool
     * @return The configured pool object
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public Pool updatePool(String name, Pool pool) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return updateItem(name, Pool.class, ClientConstants.POOL_PATH, pool);
    }

    /**
     * @param name The virtual server name related to the pool
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     * @return
     */
    public Response deletePool(String name) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return deleteItem(name, ClientConstants.POOL_PATH);
    }


    /**
     * @return the generic list for actionScripts providing the name and the endpoint for a specific request
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public List<Child> getActionScripts() throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return getItems(ClientConstants.ACTIONSCRIPT_PATH);
    }

    /**
     * @param name the virtual server name for action script retrieval
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public File getActionScript(String name) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return getItem(name, File.class, ClientConstants.ACTIONSCRIPT_PATH, MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }

    /**
     * @param name         The virtual server name related to the actionScript
     * @param actionScript The actionScript object used to create a Stingray Action Script
     * @return The configured Action Script object
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public File createActionScript(String name, File actionScript) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return createItem(name, File.class, ClientConstants.ACTIONSCRIPT_PATH, actionScript, MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }


    /**
     * @param name         The virtual server name related to the action script
     * @param actionScript The action script object used to create a Stingray action script
     * @return The configured action script object
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public File updateActionScript(String name, File actionScript) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return updateItem(name, File.class, ClientConstants.ACTIONSCRIPT_PATH, actionScript, MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }

    /**
     * @param name The virtual server name related to the action script
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     * @return
     */
    public Response deleteActionScript(String name) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return deleteItem(name, ClientConstants.ACTIONSCRIPT_PATH);
    }

    /**
     * @return the generic list for bandwidths providing the name and the endpoint for a specific request
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public List<Child> getBandwidths() throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return getItems(ClientConstants.BANDWIDTH_PATH);
    }

    /**
     * @param name the virtual server name for bandwidth retrieval
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public Bandwidth getBandwidth(String name) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return getItem(name, Bandwidth.class, ClientConstants.BANDWIDTH_PATH);
    }

    /**
     * @param name      The virtual server name related to the bandwidth
     * @param bandwidth The bandwidth object used to create a Stingray bandwidth
     * @return The configured Bandwidth object
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public Bandwidth createBandwidth(String name, Bandwidth bandwidth) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return createItem(name, Bandwidth.class, ClientConstants.BANDWIDTH_PATH, bandwidth);
    }


    /**
     * @param name      The virtual server name related to the bandwidth
     * @param bandwidth The bandwidth object used to create a Stingray bandwidth
     * @return The configured bandwidth object
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public Bandwidth updateBandwidth(String name, Bandwidth bandwidth) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return updateItem(name, Bandwidth.class, ClientConstants.BANDWIDTH_PATH, bandwidth);
    }

    /**
     * @param name The virtual server name related to the bandwidth
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     * @return
     */
    public Response deleteBandwidth(String name) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return deleteItem(name, ClientConstants.BANDWIDTH_PATH);
    }


    /**
     * @return the generic list for extra files providing the name and the endpoint for a specific request
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public List<Child> getExtraFiles() throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return getItems(ClientConstants.EXTRAFILE_PATH);
    }

    /**
     * @param fileName the virtual server name for extra file retrieval
     * @return File
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public File getExtraFile(String fileName) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return getExtraFile(fileName, MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }

    /**
     * @param fileName
     * @param cType
     * @return File
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public File getExtraFile(String fileName, MediaType cType) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return getItem(fileName, File.class, ClientConstants.EXTRAFILE_PATH, cType);
    }

    /**
     * @param fileName  The virtual server name related to the extra file
     * @param extraFile The extra file object used to create a Stingray extra file
     * @return The configured ExtraFile object
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public File createExtraFile(String fileName, File extraFile) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return createExtraFile(fileName, extraFile, MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }

    /**
     * @param name
     * @param extraFile
     * @param cType
     * @return File
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public File createExtraFile(String name, File extraFile, MediaType cType) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return createItem(name, File.class, ClientConstants.EXTRAFILE_PATH, extraFile, cType);
    }


    /**
     * @param fileName  The virtual server name related to the extra file
     * @param extraFile The extra file object used to create a Stingray extra files
     * @return File
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public File updateExtraFile(String fileName, File extraFile) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return updateExtraFile(fileName, extraFile, MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }

    /**
     * @param fileName
     * @param extraFile
     * @param cType
     * @return File
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public File updateExtraFile(String fileName, File extraFile, MediaType cType) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return updateItem(fileName, File.class, ClientConstants.EXTRAFILE_PATH, extraFile, cType);
    }

    /**
     * @param name The virtual server name related to the extra file
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     * @return
     */
    public Response deleteExtraFile(String name) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return deleteItem(name, ClientConstants.EXTRAFILE_PATH);
    }

    /**
     * @return the generic list for global load balancers providing the name and the endpoint for a specific request
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public List<Child> getGlbs() throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return getItems(ClientConstants.GLB_PATH);
    }

    /**
     * @param name the virtual server name for global load balancing retrieval
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public GlobalLoadBalancing getGlb(String name) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return getItem(name, GlobalLoadBalancing.class, ClientConstants.GLB_PATH);
    }

    /**
     * @param name                The virtual server name related to the Glb
     * @param globalLoadBalancing The global load balancing object used to create a Stingray global load balancer
     * @return The configured ExtraFile object
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public GlobalLoadBalancing createGlb(String name, GlobalLoadBalancing globalLoadBalancing) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return createItem(name, GlobalLoadBalancing.class, ClientConstants.GLB_PATH, globalLoadBalancing);
    }


    /**
     * @param name                The virtual server name related to the Glb
     * @param globalLoadBalancing The global load balancing object used to create a Stingray global load balancer
     * @return The configured global load balancing object
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public GlobalLoadBalancing updateGlb(String name, GlobalLoadBalancing globalLoadBalancing) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return updateItem(name, GlobalLoadBalancing.class, ClientConstants.GLB_PATH, globalLoadBalancing);

    }

    /**
     * @param name The virtual server name related to the global load balancing
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     * @return
     */
    public Response deleteGlb(String name) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return deleteItem(name, ClientConstants.GLB_PATH);
    }


    /**
     * @return the generic list for locations providing the name and the endpoint for a specific request
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public List<Child> getLocations() throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return getItems(ClientConstants.LOCATION_PATH);
    }

    /**
     * @param name the virtual server name for extra file retrieval
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public Location getLocation(String name) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return getItem(name, Location.class, ClientConstants.LOCATION_PATH);
    }

    /**
     * @param name     The virtual server name related to the extra file
     * @param location The location object used to create a Stingray location
     * @return The configured Location object
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public Location createLocation(String name, Location location) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return createItem(name, Location.class, ClientConstants.LOCATION_PATH, location);
    }


    /**
     * @param name     The virtual server name related to the location
     * @param location The extra file object used to create a Stingray locations
     * @return The configured Location object
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public Location updateLocation(String name, Location location) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return updateItem(name, Location.class, ClientConstants.LOCATION_PATH, location);
    }

    /**
     * @param name The virtual server name related to the location
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     * @return
     */
    public Response deleteLocation(String name) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return deleteItem(name, ClientConstants.LOCATION_PATH);
    }


    /**
     * @return the generic list for extra files providing the name and the endpoint for a specific request
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public List<Child> getMonitors() throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return getItems(ClientConstants.MONITOR_PATH);
    }

    /**
     * @param name the virtual server name for monitor retrieval
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public Monitor getMonitor(String name) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return getItem(name, Monitor.class, ClientConstants.MONITOR_PATH);
    }

    /**
     * @param name    The virtual server name related to the monitor
     * @param monitor The monitor object used to create a Stingray monitor
     * @return The configured Monitor object
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public Monitor createMonitor(String name, Monitor monitor) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return createItem(name, Monitor.class, ClientConstants.MONITOR_PATH, monitor);
    }


    /**
     * @param name    The virtual server name related to the monitor
     * @param monitor The monitor object used to create a Stingray monitors
     * @return The configured Monitor object
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public Monitor updateMonitor(String name, Monitor monitor) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return updateItem(name, Monitor.class, ClientConstants.MONITOR_PATH, monitor);
    }

    /**
     * @param name The virtual server name related to the monitor
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     * @return
     */
    public Response deleteMonitor(String name) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return deleteItem(name, ClientConstants.MONITOR_PATH);
    }


    /**
     * @return the generic list for monitor scripts providing the name and the endpoint for a specific request
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public List<Child> getMonitorScripts() throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return getItems(ClientConstants.MONITORSCRIPT_PATH);
    }

    /**
     * @param fileName the virtual server name for monitor script retrieval
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public File getMonitorScript(String fileName) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return getItem(fileName, File.class, ClientConstants.MONITORSCRIPT_PATH, MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }

    /**
     * @param fileName      The virtual server name related to the monitor script
     * @param monitorScript The monitor script object used to create a Stingray monitor script
     * @return The configured MonitorScript object
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public File createMonitorScript(String fileName, File monitorScript) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return createItem(fileName, File.class, ClientConstants.MONITORSCRIPT_PATH, monitorScript, MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }


    /**
     * @param fileName      The virtual server name related to the monitor script
     * @param monitorScript The monitor script object used to create a Stingray monitor scripts
     * @return The configured MonitorScript object
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public File updateMonitorScript(String fileName, File monitorScript) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return updateItem(fileName, File.class, ClientConstants.MONITORSCRIPT_PATH, monitorScript, MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }

    /**
     * @param fileName The virtual server name related to the monitor script
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     * @return
     */
    public Response deleteMonitorScript(String fileName) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return deleteItem(fileName, ClientConstants.MONITORSCRIPT_PATH);
    }


    /**
     * @return the generic list for persistences providing the name and the endpoint for a specific request
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public List<Child> getPersistences() throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return getItems(ClientConstants.PERSISTENCE_PATH);
    }

    /**
     * @param name the virtual server name for persistence retrieval
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public Persistence getPersistence(String name) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return getItem(name, Persistence.class, ClientConstants.PERSISTENCE_PATH);
    }

    /**
     * @param name        The virtual server name related to the extra file
     * @param persistence The persistence object used to create a Stingray persistence
     * @return The configured Persistence object
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public Persistence createPersistence(String name, Persistence persistence) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return createItem(name, Persistence.class, ClientConstants.PERSISTENCE_PATH, persistence);
    }

    /**
     * @param name        The name of the backup
     * @param backup The Backup object used to create a host backup
     * @return The configured Backup object
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public BackupProperties createBackup(String name, BackupProperties backup, String trafficManagerName) throws VTMRestClientException, VTMRestClientObjectNotFoundException {

        String path = "status/" + trafficManagerName + "/"+ ClientConstants.STATUS_BACKUPS + "/";
        return createItem(name, BackupProperties.class, path, backup);
    }

    /**
     * @param name        The virtual server name related to the persistence
     * @param persistence The persistence object used to create a Stingray persistence
     * @return The configured Persistence object
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public Persistence updatePersistence(String name, Persistence persistence) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return updateItem(name, Persistence.class, ClientConstants.PERSISTENCE_PATH, persistence);
    }

    /**
     * @param name The virtual server name related to the persistence
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     * @return
     */
    public Response deletePersistence(String name) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return deleteItem(name, ClientConstants.PERSISTENCE_PATH);
    }


    /**
     * @return the generic list for protections providing the name and the endpoint for a specific request
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public List<Child> getProtections() throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return getItems(ClientConstants.PROTECTION_PATH);
    }

    /**
     * @param name the virtual server name for protection retrieval
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public Protection getProtection(String name) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return getItem(name, Protection.class, ClientConstants.PROTECTION_PATH);
    }

    /**
     * @param name       The virtual server name related to the protection
     * @param protection The protection object used to create a Stingray protection
     * @return The configured Protection object
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public Protection createProtection(String name, Protection protection) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return createItem(name, Protection.class, ClientConstants.PROTECTION_PATH, protection);
    }


    /**
     * @param name       The virtual server name related to the bandwidth
     * @param protection The protection object used to create a Stingray protections
     * @return The configured Protection object
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public Protection updateProtection(String name, Protection protection) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return updateItem(name, Protection.class, ClientConstants.PROTECTION_PATH, protection);
    }

    /**
     * @param name The virtual server name related to the extra file
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     * @return
     */
    public Response deleteProtection(String name) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return deleteItem(name, ClientConstants.PROTECTION_PATH);
    }


    /**
     * @return the generic list for rates providing the name and the endpoint for a specific request
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public List<Child> getRates() throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return getItems(ClientConstants.RATE_PATH);
    }

    /**
     * @param name the virtual server name for rate retrieval
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public Rate getRate(String name) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return getItem(name, Rate.class, ClientConstants.RATE_PATH);
    }

    /**
     * @param name The virtual server name related to the rate
     * @param rate The rate object used to create a Stingray rate
     * @return The configured Rate object
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public Rate createRate(String name, Rate rate) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return createItem(name, Rate.class, ClientConstants.RATE_PATH, rate);
    }


    /**
     * @param name The virtual server name related to the rate
     * @param rate The rate object used to create a Stingray rates
     * @return The configured Rate object
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public Rate updateRate(String name, Rate rate) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return createItem(name, Rate.class, ClientConstants.RATE_PATH, rate);
    }

    /**
     * @param name The virtual server name related to the rate
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     * @return
     */
    public Response deleteRate(String name) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return deleteItem(name, ClientConstants.RATE_PATH);
    }

    /**
     * @return the generic list for cacrls providing the name and the endpoint for a specific request
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public List<Child> getCacrls() throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return getItems(ClientConstants.CACRL_PATH);
    }

    /**
     * @param fileName the virtual server name for cacrl retrieval
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public File getCacrl(String fileName) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return getItem(fileName, File.class, ClientConstants.CACRL_PATH, MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }

    /**
     * @param fileName The virtual server name related to the cacrl
     * @param cacrl    The cacrl object used to create a Stingray cacrl
     * @return The configured Cacrl object
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public File createCacrl(String fileName, File cacrl) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return createItem(fileName, File.class, ClientConstants.CACRL_PATH, cacrl, MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }


    /**
     * @param fileName The virtual server name related to the cacrl
     * @param cacrl    The cacrl object used to create a Stingray cacrl
     * @return The configured Cacrl object
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public File updateCacrl(String fileName, File cacrl) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return updateItem(fileName, File.class, ClientConstants.CACRL_PATH, cacrl, MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }

    /**
     * @param fileName The virtual server name related to the cacrl
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     * @return
     */
    public Response deleteCacrl(String fileName) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return deleteItem(fileName, ClientConstants.CACRL_PATH);
    }


    /**
     * @return the generic list for client keypairs providing the name and the endpoint for a specific request
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public List<Child> getClientKeypairs() throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return getItems(ClientConstants.CLIENTKEYPAIR_PATH);
    }

    /**
     * @param name the virtual server name for client keypair retrieval
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public ClientKeypair getClientKeypair(String name) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return getItem(name, ClientKeypair.class, ClientConstants.CLIENTKEYPAIR_PATH);
    }

    /**
     * @param name          The virtual server name related to the client keypair
     * @param clientKeypair The client keypair object used to create a Stingray client keypair
     * @return The configured ClientKeypair object
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public ClientKeypair createClientKeypair(String name, ClientKeypair clientKeypair) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return createItem(name, ClientKeypair.class, ClientConstants.CLIENTKEYPAIR_PATH, clientKeypair);
    }


    /**
     * @param name          The virtual server name related to the clientkeypair
     * @param clientKeypair The client keypair object used to create a Stingray client keypairs
     * @return The configured ClientKeypair object
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public ClientKeypair updateClientKeypair(String name, ClientKeypair clientKeypair) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return createItem(name, ClientKeypair.class, ClientConstants.CLIENTKEYPAIR_PATH, clientKeypair);
    }

    /**
     * @param name The virtual server name related to the client keypair
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     * @return
     */
    public Response deleteClientKeypair(String name) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return deleteItem(name, ClientConstants.CLIENTKEYPAIR_PATH);
    }

    /**
     * @return the generic list for keypairs providing the name and the endpoint for a specific request
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public List<Child> getKeypairs() throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return getItems(ClientConstants.KEYPAIR_PATH);
    }

    /**
     * @param name the virtual server name for keypair retrieval
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public Keypair getKeypair(String name) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return getItem(name, Keypair.class, ClientConstants.KEYPAIR_PATH);
    }

    /**
     * @param name    The virtual server name related to the keypair
     * @param keypair The keypair object used to create a Stingray keypair
     * @return The configured Keypair object
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public Keypair createKeypair(String name, Keypair keypair) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return createItem(name, Keypair.class, ClientConstants.KEYPAIR_PATH, keypair);
    }


    /**
     * @param name    The virtual server name related to the keypair
     * @param keypair The keypair object used to create a Stingray keypairs
     * @return The configured Keypair object
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public Keypair updateKeypair(String name, Keypair keypair) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return createItem(name, Keypair.class, ClientConstants.KEYPAIR_PATH, keypair);
    }

    /**
     * @param name The virtual server name related to the keypair
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     * @return
     */
    public Response deleteKeypair(String name) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return deleteItem(name, ClientConstants.KEYPAIR_PATH);
    }


    /**
     * @return the generic list for traffic managers providing the name and the endpoint for a specific request
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public List<Child> getTrafficManagers() throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return getItems(ClientConstants.TRAFFICMANAGER_PATH);
    }

    /**
     * @param name the virtual server name for rate retrieval
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public TrafficManager getTrafficManager(String name) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return getItem(name, TrafficManager.class, ClientConstants.TRAFFICMANAGER_PATH);
    }

    /**
     * @param name           The virtual server name related to the traffic manager
     * @param trafficManager The traffic manager object used to create a Stingray traffic manager
     * @return The configured TrafficManager object
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public TrafficManager createTrafficManager(String name, TrafficManager trafficManager) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return createItem(name, TrafficManager.class, ClientConstants.TRAFFICMANAGER_PATH, trafficManager);
    }


    /**
     * @param name           The virtual server name related to the traffic manager
     * @param trafficManager The traffic manager object used to create a Stingray traffic manager
     * @return The configured TrafficManager object
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public TrafficManager updateTrafficManager(String name, TrafficManager trafficManager) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return createItem(name, TrafficManager.class, ClientConstants.TRAFFICMANAGER_PATH, trafficManager);
    }

    /**
     * @param name The virtual server name related to the rate
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     * @return
     */
    public Response deleteTrafficManager(String name) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return deleteItem(name, ClientConstants.TRAFFICMANAGER_PATH);
    }

    /**
     * @return the generic list for trafficscripts providing the name and the endpoint for a specific request
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public List<Child> getTrafficscripts() throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return getItems(ClientConstants.TRAFFICSCRIPT_PATH);
    }

    /**
     * @param fileName the virtual server name for trafficscript retrieval
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public File getTraffiscript(String fileName) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return getItem(fileName, File.class, ClientConstants.TRAFFICSCRIPT_PATH, MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }

    /**
     * @param fileName      The virtual server name related to the trafficscript
     * @param trafficscript The rate object used to create a Stingray trafficscript
     * @return The configured Trafficscript object
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public File createTrafficscript(String fileName, File trafficscript) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return createItem(fileName, File.class, ClientConstants.TRAFFICSCRIPT_PATH, trafficscript, MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }


    /**
     * @param name          The virtual server name related to the trafficscript
     * @param trafficscript The trafficscript object used to create a Stingray trafficscript
     * @return The configured Trafficscript object
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public File updateTrafficScript(String name, File trafficscript) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return createItem(name, File.class, ClientConstants.TRAFFICSCRIPT_PATH, trafficscript, MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }

    /**
     * @param name The virtual server name related to the trafficscript
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     * @return
     */
    public Response deleteTrafficscript(String name) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return deleteItem(name, ClientConstants.TRAFFICSCRIPT_PATH);
    }

    /**
     * @return the generic list for TrafficIps providing the name and the endpoint for a specific request
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public List<Child> getTrafficIps() throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return getItems(ClientConstants.IP_PATH);
    }

    /**
     * @param name the virtual server name for rate retrieval
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public TrafficIp getTrafficIp(String name) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return getItem(name, TrafficIp.class, ClientConstants.IP_PATH);
    }

    /**
     * @param name      The virtual server name related to the Traffic Ip
     * @param trafficIp The rate object used to create a Stingray Traffic Ip
     * @return The configured TrafficIp object
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public TrafficIp createTrafficIp(String name, TrafficIp trafficIp) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return createItem(name, TrafficIp.class, ClientConstants.IP_PATH, trafficIp);
    }


    /**
     * @param name      The virtual server name related to the Traffic Ip
     * @param trafficIp The Traffic Ip object used to create a Stingray Traffic Ip
     * @return The configured TrafficIp object
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public TrafficIp updateTrafficIp(String name, TrafficIp trafficIp) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return createItem(name, TrafficIp.class, ClientConstants.IP_PATH, trafficIp);
    }

    /**
     * @param name The virtual server name related to the TrafficIp
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     * @return
     */
    public Response deleteTrafficIp(String name) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return deleteItem(name, ClientConstants.IP_PATH);
    }

    /**
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    public GlobalSettings getGlobalSettings() throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return getItem("", GlobalSettings.class, ClientConstants.GLOBAL_SETTINGS);
    }

    /**
     * @param name the virtual server name for stats retrieval
     * @throws VTMRestClientObjectNotFoundException
     */
    public VirtualServerStats getVirtualServerStats(String name, URI endpoint) {
        VirtualServerStats stats = new VirtualServerStats();
        try {
            stats = getItem(name, VirtualServerStats.class, ClientConstants.V_SERVER_PATH, endpoint);
        } catch (VTMRestClientObjectNotFoundException e) {
            stats.setProperties(getZeroedVirtulServerStats());
        } catch (VTMRestClientException e) {
            stats.setProperties(getZeroedVirtulServerStats());
        }
        return stats;
    }

    /**
     * @throws VTMRestClientObjectNotFoundException
     */
    public GlobalCounters getGlobalCounters(URI endpoint) throws URISyntaxException, VTMRestClientObjectNotFoundException, VTMRestClientException {
        GlobalCounters stats = new GlobalCounters();
        String statsEndpoint = endpoint.toString().split("config")[0];
        GlobalCountersProperties props = new GlobalCountersProperties();
        props = getItem("", GlobalCountersProperties.class, ClientConstants.GLOBAL_COUNTERS, new URI(statsEndpoint));
        stats.setProperties(props);
        return stats;
    }

    /**
     * Destroy the VTMRestClient
     */
    public void destroy() {
        client.close();
    }

    private VirtualServerStatsProperties getZeroedVirtulServerStats() {
        VirtualServerStatsProperties props = new VirtualServerStatsProperties();
        VirtualServerStatsStatistics stats = new VirtualServerStatsStatistics();
        stats.setConnectTimedOut(0L);
        stats.setConnectionErrors(0L);
        stats.setConnectionFailures(0L);
        stats.setDataTimedOut(0L);
        stats.setKeepaliveTimedOut(0L);
        stats.setMaxConn(0L);
        stats.setCurrentConn(0L);
        props.setStatistics(stats);
        return props;
    }
}
