package com.roguesquadron.rje;

/**
 * Created by mandrewes on 22/08/2016.
 */
public class RJEApp {
    private String descriptorPath;
    private String resourceBase;
    private String contextPath;

    public String getDescriptorPath() {
        return descriptorPath;
    }

    public void setDescriptorPath(String descriptorPath) {
        this.descriptorPath = descriptorPath;
    }

    public String getResourceBase() {
        return resourceBase;
    }

    public void setResourceBase(String resourceBase) {
        this.resourceBase = resourceBase;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    @Override
    public String toString() {
        return "RJEApp{" +
                "descriptorPath='" + descriptorPath + '\'' +
                ", resourceBase='" + resourceBase + '\'' +
                ", contextPath='" + contextPath + '\'' +
                '}';
    }
}
