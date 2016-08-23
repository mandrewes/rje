package com.roguesquadron.rje.helpers;

/**
 * Created by mandrewes on 22/08/2016.
 */
public class RJEHelperTest {

    RJEHelper helper = new RJEHelper();

    @org.testng.annotations.Test
    public void shouldFindWebApp() throws Exception {
        helper.resolveWebApp("web.xml", "WEB-INF", "test");
    }

}