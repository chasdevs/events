package com.github.chasdevs.events.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("local-repo")
public class LocalRepoConfig {

    private String rootPath;
    private int allowedNamespaceDepth;

    public LocalRepoConfig() {
    }

    public LocalRepoConfig(String rootPath, int allowedNamespaceDepth) {
        this.rootPath = rootPath;
        this.allowedNamespaceDepth = allowedNamespaceDepth;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public int getAllowedNamespaceDepth() {
        return allowedNamespaceDepth;
    }

    public void setAllowedNamespaceDepth(int allowedNamespaceDepth) {
        this.allowedNamespaceDepth = allowedNamespaceDepth;
    }
}
