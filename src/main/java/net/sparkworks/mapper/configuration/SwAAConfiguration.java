package net.sparkworks.mapper.configuration;

import net.sparkworks.cs.client.config.ClientAuthConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kanakisn on 04/02/2017.
 */
@Component
@ConfigurationProperties(prefix = "sparkworks.aa.oauth2")
public class SwAAConfiguration implements ClientAuthConfig {
    
    @NotNull
    private String accessTokenUrl;
    
    @NotNull
    private String clientId;
    
    @NotNull
    private String clientSecret;
    
    @NotNull
    private String grantType;
    
    private List<String> scope = new ArrayList();
    
    @NotNull
    private String username;
    
    @NotNull
    private String password;
    
    @Override
    public String getAccessTokenUrl() {
        return accessTokenUrl;
    }
    
    public void setAccessTokenUrl(String accessTokenUrl) {
        this.accessTokenUrl = accessTokenUrl;
    }
    
    @Override
    public String getClientSecret() {
        return clientSecret;
    }
    
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
    
    @Override
    public String getGrantType() {
        return grantType;
    }
    
    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }
    
    @Override
    public List<String> getScope() {
        return scope;
    }
    
    public void setScope(List<String> scope) {
        this.scope = scope;
    }
    
    @Override
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    @Override
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
}
