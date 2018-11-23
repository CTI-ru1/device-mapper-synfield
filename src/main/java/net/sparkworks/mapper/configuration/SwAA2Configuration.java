package net.sparkworks.mapper.configuration;

import net.sparkworks.cs.client.config.ClientConfig;
import org.springframework.stereotype.Component;

/**
 * Created by kanakisn on 04/02/2017.
 */
@Component
public class SwAA2Configuration implements ClientConfig {
    
    @Override
    public String getAddress() {
        return "https://api.sparkworks.net";
    }
}
