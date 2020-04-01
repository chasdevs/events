package com.github.chasdevs.events.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties("schema-registry")
public class SchemaRegistryConfig {
    private String url;
    private int identityMapLimit;
    private String username;
    private String password;

    public SchemaRegistryConfig() {
    }

    public SchemaRegistryConfig(String url, int identityMapLimit) {
        this.url = url;
        this.identityMapLimit = identityMapLimit;
    }

    public SchemaRegistryConfig(String url, int identityMapLimit, String username, String password) {
        this.url = url;
        this.identityMapLimit = identityMapLimit;
        this.username = username;
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getIdentityMapLimit() {
        return identityMapLimit;
    }

    public void setIdentityMapLimit(int identityMapLimit) {
        this.identityMapLimit = identityMapLimit;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // convenience method that returns required Confluent auth configs
    public Map<String, String> getAuthConfigs() {
        Map<String, String> authConfigs = new HashMap<>();
        authConfigs.put("basic.auth.credentials.source", "USER_INFO");
        authConfigs.put("basic.auth.user.info", getUsername() + ":" + getPassword());
        return authConfigs;
    }
}
