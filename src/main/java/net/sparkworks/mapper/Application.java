package net.sparkworks.mapper;

import net.sparkworks.cs.client.DataClient;
import net.sparkworks.cs.client.GatewayClient;
import net.sparkworks.cs.client.ResourceClient;
import net.sparkworks.cs.client.SiteClient;
import net.sparkworks.cs.client.impl.DataClientImpl;
import net.sparkworks.cs.client.impl.GatewayClientImpl;
import net.sparkworks.cs.client.impl.ResourceClientImpl;
import net.sparkworks.cs.client.impl.SiteClientImpl;
import net.sparkworks.mapper.configuration.SwAAConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.stream.Collectors;

@EnableAsync
@EnableScheduling
@SpringBootApplication
public class Application implements CommandLineRunner {
    
    
    @Value("${sparkworks.api.url}")
    String sparkworksApiUrl;
    
    @Autowired
    SwAAConfiguration swAAConfiguration;
    
    @Bean
    GatewayClient gatewayClient() {
        return new GatewayClientImpl(sparkworksApiUrl, swAAConfiguration.getClientId(), swAAConfiguration.getClientSecret(), swAAConfiguration.getUsername(), swAAConfiguration.getPassword(), swAAConfiguration.getAccessTokenUrl(), swAAConfiguration.getGrantType(), swAAConfiguration.getScope().stream().
                collect(Collectors.joining(",")));
    }
    
    @Bean
    SiteClient siteClient() {
        return new SiteClientImpl(sparkworksApiUrl, swAAConfiguration.getClientId(), swAAConfiguration.getClientSecret(), swAAConfiguration.getUsername(), swAAConfiguration.getPassword(), swAAConfiguration.getAccessTokenUrl(), swAAConfiguration.getGrantType(), swAAConfiguration.getScope().stream().
                collect(Collectors.joining(",")));
    }
    
    @Bean
    ResourceClient resourceClient() {
        return new ResourceClientImpl(sparkworksApiUrl, swAAConfiguration.getClientId(), swAAConfiguration.getClientSecret(), swAAConfiguration.getUsername(), swAAConfiguration.getPassword(), swAAConfiguration.getAccessTokenUrl(), swAAConfiguration.getGrantType(), swAAConfiguration.getScope().stream().
                collect(Collectors.joining(",")));
    }
    
    @Bean
    DataClient dataClient() {
        return new DataClientImpl(sparkworksApiUrl, swAAConfiguration.getClientId(), swAAConfiguration.getClientSecret(), swAAConfiguration.getUsername(), swAAConfiguration.getPassword(), swAAConfiguration.getAccessTokenUrl(), swAAConfiguration.getGrantType(), swAAConfiguration.getScope().stream().
                collect(Collectors.joining(",")));
    }
    
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Override
    public void run(String... strings) throws Exception {
        
    }
    
}
