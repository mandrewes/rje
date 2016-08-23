package com.roguesquadron.rje;

import com.roguesquadron.rje.helpers.RJEHelper;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by mandrewes on 22/08/2016.
 */
public class RJEServer {
    private Logger log = LoggerFactory.getLogger(RJEServer.class);

    // Configuration
    private int httpPort = 8080;
    private int sslPort = 8443;
    private int setOutputBufferSize = 32768;
    private String keyStoreResource;
    private String keyStorePassword;
    private String keyManagerPassword;
    private List<RJEApp> webApps = new ArrayList<>();

    // Runtime
    private Server server = new Server(httpPort);
    private RJEHelper rjeHelper = new RJEHelper();
    private List<WebAppContext> webAppContexts = new ArrayList<>();


    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    public void setSslPort(int sslPort) {
        this.sslPort = sslPort;
    }

    public void setSetOutputBufferSize(int setOutputBufferSize) {
        this.setOutputBufferSize = setOutputBufferSize;
    }

    public void setKeyStoreResource(String keyStoreResource) {
        this.keyStoreResource = keyStoreResource;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public void setKeyManagerPassword(String keyManagerPassword) {
        this.keyManagerPassword = keyManagerPassword;
    }

    public void setWebApps(List<RJEApp> webApps) {
        this.webApps = webApps;
    }

    public void stopAndAwait() {
        log.info("Server stop requested");

        CountDownLatch cdl = new CountDownLatch(1);

        new Thread() {
            @Override
            public void run() {
                try {
                    log.info("setting stop timeout");
//                    server.setStopTimeout(10000L);
                    for (WebAppContext webAppContext : webAppContexts) {
                        log.info("stopping context " + webAppContext.getContextPath());
                        webAppContext.stop();
                    }
                    log.info("stopping Jetty server");
                    server.stop();
                } catch (Exception ex) {
                    log.error("Failed to stop Jetty", ex.getMessage());
                } finally {
                    cdl.countDown();
                }
            }
        }.start();

        try {
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void runAndBlockUntilExit() {
        server = new Server(httpPort);
        HandlerCollection handlers = new HandlerCollection();

        // Setup JMX
//        MBeanContainer mbContainer = new MBeanContainer(
//                ManagementFactory.getPlatformMBeanServer() );
//        server.addBean( mbContainer );

        for (RJEApp webApp : webApps) {
            WebAppContext ctx = rjeHelper.resolveWebApp(webApp.getDescriptorPath(), webApp.getResourceBase(), webApp.getContextPath());
            ctx.setAttribute(
                    "org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
                    ".*/[^/]*servlet-api-[^/]*\\.jar$|.*/javax.servlet.jsp.jstl-.*\\.jar$|.*/[^/]*taglibs.*\\.jar$");


            log.info("Configuring RJE App for Jetty {}", 0);
            webAppContexts.add(ctx);

            handlers.addHandler(ctx.getHandler());
        }

        Configuration.ClassList classlist = Configuration.ClassList
                .setServerDefault(server);
        classlist.addBefore(
                "org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
                "org.eclipse.jetty.annotations.AnnotationConfiguration"
        );

        server.setHandler(handlers);

        try {
            server.start();
            server.dumpStdErr();

            System.out.println("PRE");
            server.join();
            System.out.println("POST");

            log.info("server exited..");
        } catch (InterruptedException ex) {

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private ServerConnector initHttpsConnector(Server server) {
        HttpConfiguration http_config = new HttpConfiguration();
        http_config.setSecureScheme("https");
        http_config.setSecurePort(sslPort);
        http_config.setOutputBufferSize(setOutputBufferSize);

        SslContextFactory sslContextFactory = new SslContextFactory();
//        sslContextFactory.setKeyStorePath(keystoreFile.getAbsolutePath());
        sslContextFactory.setKeyStorePassword("OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4");
        sslContextFactory.setKeyManagerPassword("OBF:1u2u1wml1z7s1z7a1wnl1u2g");

        // HTTPS Configuration
        // A new HttpConfiguration object is needed for the next connector and
        // you can pass the old one as an argument to effectively clone the
        // contents. On this HttpConfiguration object we add a
        // SecureRequestCustomizer which is how a new connector is able to
        // resolve the https connection before handing control over to the Jetty
        // Server.
        HttpConfiguration https_config = new HttpConfiguration(http_config);
        SecureRequestCustomizer src = new SecureRequestCustomizer();
        src.setStsMaxAge(2000);
        src.setStsIncludeSubDomains(true);
        https_config.addCustomizer(src);

        // HTTPS connector
        // We create a second ServerConnector, passing in the http configuration
        // we just made along with the previously created ssl context factory.
        // Next we set the port and a longer idle timeout.
        ServerConnector https = new ServerConnector(server,
                new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                new HttpConnectionFactory(https_config));
        https.setPort(8443);
        https.setIdleTimeout(500000);

        // Here you see the server having multiple connectors registered with
        // it, now requests can flow into the server from both http and https
        // urls to their respective ports and be processed accordingly by jetty.
        // A simple handler is also registered with the server so the example
        // has something to pass requests off to.

        // Set the connectors
//        server.setConnectors(new Connector[] { http, https });
        return https;
    }

    /*
    Basic Aauth - shuld use filter

        // Configure a LoginService.
        // Since this example is for our test webapp, we need to setup a
        // LoginService so this shows how to create a very simple hashmap based
        // one. The name of the LoginService needs to correspond to what is
        // configured in the webapp's web.xml and since it has a lifecycle of
        // its own we register it as a bean with the Jetty server object so it
        // can be started and stopped according to the lifecycle of the server
        // itself.
        HashLoginService loginService = new HashLoginService();
        loginService.setName( "Test Realm" );
        loginService.setConfig( "src/test/resources/realm.properties" );
        server.addBean( loginService );

     */

    /*
 WebAppContext webapp = new WebAppContext();
        webapp.setContextPath( "/" );
        File warFile = new File(
                "../../jetty-distribution/target/distribution/demo-base/webapps/test.war" );
        if (!warFile.exists())
        {
            throw new RuntimeException( "Unable to find WAR File: "
                    + warFile.getAbsolutePath() );
        }
        webapp.setWar( warFile.getAbsolutePath() );
        webapp.setExtractWAR(true);
        */
}
