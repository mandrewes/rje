package com.roguesquadron.rje.helpers;

import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.plugin2.message.Message;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mandrewes on 22/08/2016.
 */
public class RJEHelper {
    private Logger log = LoggerFactory.getLogger(RJEHelper.class);

//    private static final String WEB_XML_FILE_NAME = "web.xml";

    private File findFile(String name, List<File> dirs, String fn) {
        for (File dir : dirs) {
            File f = findFile(dir, fn);
            if (f != null) {
                return f;
            }
        }

        throw new RuntimeException(MessageFormat.format("No {0} - {1} found in {2}", name, fn, dirs));
    }

    private File findFile(File dir, String fn) {

        log.info(MessageFormat.format("Searching for file {0} in {1} ", fn, dir.getAbsolutePath()));

        File lFile = new File(dir, fn);
        if (lFile.exists() && lFile.isFile()) {
            log.info(MessageFormat.format("Found file {0} in {1} ", fn, dir.getAbsolutePath()));
            return lFile;
        }

        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                File ret = findFile(f, fn);
                if (ret != null) {
                    return ret;
                }
            }
        }
        log.info(MessageFormat.format("Did not find file {0} in {1} ", fn, dir.getAbsolutePath()));
        return null;
    }

    private File findDir(String name, List<File> dirs, String fn) {
        for (File dir : dirs) {
            File f = findDir(dir, fn);
            if (f != null) {
                return f;
            }
        }
        throw new RuntimeException(MessageFormat.format("No {0} - {1} found in {2}", name, fn, dirs));
    }

    private File findDir(File dir, String fn) {
        log.info(MessageFormat.format("Searching for dir {0} in {1} ", fn, dir.getAbsolutePath()));

        File lFile = new File(dir, fn);
        if (lFile.exists() && lFile.isDirectory()) {
            log.info(MessageFormat.format("Found dir {0} in {1} ", fn, dir.getAbsolutePath()));
            return lFile;
        }

        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                File ret = findDir(f, fn);
                if (ret != null) {
                    return ret;
                }
            }
        }
        log.info(MessageFormat.format("Did not find dir {0} in {1} ", fn, dir.getAbsolutePath()));
        return null;
    }


    public WebAppContext resolveWebApp(String descriptorPath, String resourceBase, String contextPath) {
        List<File> searchDirs = new ArrayList<>();
        searchDirs.add(new File(".").getAbsoluteFile());

        WebAppContext webAppContext = new WebAppContext();

        File descriptorFile = findFile("webapp-descriptor", searchDirs, descriptorPath);
        File resourceBaseFile = findDir("resource-base-dir", searchDirs, resourceBase);
//        File cont = findFile("webapp-descriptor", searchDirs, descriptorPath);

        //tood - throw exceptione

        log.info(MessageFormat.format(contextPath + " descriptor file is: {0}",descriptorFile.getAbsolutePath()));
        log.info(MessageFormat.format(contextPath + " resource Base is: {0}",resourceBaseFile.getAbsolutePath()));

        webAppContext.setResourceBase(resourceBaseFile.getAbsolutePath());
        webAppContext.setContextPath(contextPath);
        webAppContext.setDefaultsDescriptor(descriptorFile.getAbsolutePath());

        return webAppContext;
//        handlers.addHandler(webapp1);
    }


}
