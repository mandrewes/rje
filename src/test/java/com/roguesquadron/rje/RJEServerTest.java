package com.roguesquadron.rje;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mandrewes on 22/08/2016.
 */
public class RJEServerTest {


    @Test
    public void shouldStartTestServer() throws Exception {

        RJEServer server = new RJEServer();

        server.setHttpPort(8088);

        RJEApp testApp = new RJEApp();
        testApp.setContextPath("/test1");
        testApp.setDescriptorPath("test/resources/WEB-INF/web.xml");
        testApp.setResourceBase("test/resources");

        List l = new ArrayList();
        l.add(testApp);
        server.setWebApps(l);

        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                    server.stopAndAwait();
                } catch (Exception ex) {
                    System.out.println("Failed to stop test server " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }.start();

        server.runAndBlockUntilExit();

    }
}
