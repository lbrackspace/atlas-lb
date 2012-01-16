package org.openstack.atlas.jetty;

import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.ProtectionDomain;

public class AtlasServer {
    private final int port;

    public AtlasServer() {
        port = Integer.parseInt(System.getProperty("port", "8080"));
    }

    public static void main(String[] args) throws Exception {
        AtlasServer as = new AtlasServer();
        if ("start".equals(args[0])) {
            as.start();
        } else if ("stop".equals(args[0])) {
            as.stop();
        } else {
            as.usage();
        }
    }

    private void usage() {
        System.out.println("Usage: java -jar <file.jar> [start|stop]\n\t" +
                "start    Start the server (default)\n\t" +
                "stop     Stop the server gracefully\n\t"
        );
        System.exit(-1);
    }

    private void start() throws IOException {
        System.out.println("Starting:");
        try {

            Server server = new Server();

            // Increase thread pool
            QueuedThreadPool threadPool = new QueuedThreadPool();
            threadPool.setMaxThreads(100);
            server.setThreadPool(threadPool);

            // Ensure using the non-blocking connector (NIO)
            Connector connector = new SelectChannelConnector();
            connector.setMaxIdleTime(3600000);
            connector.setPort(port);
            connector.setMaxIdleTime(30000);
            server.setConnectors(new Connector[]{connector});

            WebAppContext context = new WebAppContext();
            context.setServer(server);
            context.setContextPath("/");

            ProtectionDomain protectionDomain = AtlasServer.class.getProtectionDomain();
            URL location = protectionDomain.getCodeSource().getLocation();
            context.setWar(location.toExternalForm());

            String currentDir = new File(protectionDomain.getCodeSource().getLocation().getPath()).getParent();
            File workDir = new File(currentDir, "work");
            FileUtils.deleteDirectory(workDir);
            context.setTempDirectory(workDir);

            HandlerList handlers = new HandlerList();
            handlers.addHandler(context);
            server.setHandler(handlers);

            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(100);
        }
    }

    private void stop() throws Exception {
        System.out.println("Shutting Down:  To be Implemented");
    }

}
