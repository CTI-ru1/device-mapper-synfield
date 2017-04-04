package net.sparkworks.mapper.util;

import gr.cti.ru1.synfield.client.Synfield;
import gr.cti.ru1.synfield.client.model.measurements.SynfieldMeasurement;
import gr.cti.ru1.synfield.client.model.measurements.SynfieldMeasurementsPage;
import gr.cti.ru1.synfield.client.model.sensors.SynfieldSensor;
import net.sparkworks.cs.client.DataClient;
import net.sparkworks.cs.client.GatewayClient;
import net.sparkworks.cs.client.ResourceClient;
import net.sparkworks.cs.common.dto.GatewayDTO;
import net.sparkworks.cs.common.dto.GatewayListDTO;
import net.sparkworks.cs.common.dto.LatestDTO;
import net.sparkworks.cs.common.dto.ResourceDTO;
import net.sparkworks.mapper.service.SenderService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

@Service
public class SynfieldPollService {
    /**
     * LOGGER.
     */
    private static final Logger LOGGER = Logger.getLogger(SynfieldPollService.class);
    
    @Value("${synfield.username}")
    private String synfieldUsername;
    @Value("${synfield.password}")
    private String synfieldPassword;
    @Value("${synfield.devices}")
    private String synfieldDevicesString;
    
    @Autowired
    DataClient dataClient;
    @Autowired
    ResourceClient resourceClient;
    @Autowired
    GatewayClient gatewayClient;
    
    @Autowired
    SenderService senderService;
    
    private final Synfield synfield = new Synfield();
    private final Set<String> synfieldDevices = new HashSet<>();
    private final SimpleDateFormat dateStringFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    @PostConstruct
    public void init() {
        synfield.authenticate(synfieldUsername, synfieldPassword);
        for (final String device : synfieldDevicesString.split(",")) {
            synfieldDevices.add(device);
        }
    }
    
    @Scheduled(fixedDelay = 120000)
    public void sendMeasurement() {
        
        for (final String mac : synfieldDevices) {
            LOGGER.info("==================================================");
            LOGGER.info(String.format("Polling weather station with mac: %s", mac));
            try {
                final List<SynfieldSensor> sensors = synfield.getSensors(mac).getResponse().getSensors();
                final Map<String, Long> uris = new HashMap<>();
                final Optional<GatewayListDTO> gw = gatewayClient.listByName("synfield-" + mac);
                for (final GatewayDTO gatewayDTO : gw.get().getGateways()) {
                    for (final ResourceDTO dto : gatewayDTO.getResources().getResources()) {
                        if (dto.getUri().contains("System Temperature")) {
                            continue;
                        }
                        log(dto.getUri(), "Retrieving latest measurement for resource.");
                        
                        Optional<LatestDTO> data = dataClient.getLatestValues(dto.getResourceId());
                        if (data.isPresent()) {
                            try {
                                uris.put(dto.getUri(), data.get().getLatestTime());
                                log(dto.getUri(), String.format("Latest measurement for resource at %s.", new Date(uris.get(dto.getUri()))));
                            } catch (Exception e) {
                                LOGGER.error(e, e);
                            }
                        }
                    }
                }
                //                for (final SynfieldSensor sensor : sensors) {
                //                    final String uri = "synfield-" + mac + "/" + WordUtils.capitalize(sensor.getService());
                //                    try {
                //
                //                        Optional<ResourceDTO> resource = resourceClient.getByUri(uri);
                //                        LOGGER.info(uri);
                //                        if (resource.isPresent()) {
                //                            Optional<LatestDTO> data = dataClient.getLatestValues(resource.get().getResourceId());
                //                            if (data.isPresent()) {
                //                                uris.put(uri, data.get().getLatestTime());
                //                            }
                //                        }
                //                    } catch (RestClientException e) {
                //
                //                    }
                //                }
                
                for (final String key : uris.keySet()) {
                    final String gateway = key.split("/")[0];
                    final String capability = key.split("/")[1];
                    if (uris.get(key) == 0) {
                        //                        final SynfieldMeasurementsPage measurementsForGateway = synfield.getMeasurements(mac);
                        //                        for (final SynfieldMeasurement synfieldMeasurement : measurementsForGateway.getResponse().getMeasurements()) {
                        //                            if (synfieldMeasurement.getService().toLowerCase().endsWith(capability.toLowerCase())) {
                        //                                send(gateway, capability, synfieldMeasurement.getDoubleValue(), dateStringFormat.parseMillis(synfieldMeasurement.getTimestamp()));
                        //                            }
                        //
                        //                        }
                    } else {
                        final Date then = new Date(uris.get(key));
                        final Date now = new Date();
                        Calendar calThen = Calendar.getInstance();
                        calThen.setTime(then);
                        Calendar calNow = Calendar.getInstance();
                        calNow.setTime(now);
                        
                        final String thenString = calThen.get(Calendar.YEAR) + "-" + (calThen.get(Calendar.MONTH) + 1) + "-" + calThen.get(Calendar.DAY_OF_MONTH);
                        final String nowString = calNow.get(Calendar.YEAR) + "-" + (calNow.get(Calendar.MONTH) + 1) + "-" + calNow.get(Calendar.DAY_OF_MONTH);
                        log(mac + "/" + capability, String.format("Searching for data from %s until %s", thenString, thenString));
                        final SynfieldMeasurementsPage measurementsForGateway = synfield.getMeasurements(mac, thenString, nowString);
                        log(mac + "/" + capability, String.format("Received %d measurements for resource.", measurementsForGateway.getResponse().getMeasurements().size()));
                        
                        final TreeSet<SynfieldMeasurement> measurements = new TreeSet<>((o1, o2) -> {
                            try {
                                final Date measurementTime1 = dateStringFormat.parse(o1.getTimestamp());
                                final Date measurementTime2 = dateStringFormat.parse(o2.getTimestamp());
                                return measurementTime1.after(measurementTime2) ? 1 : -1;
                            } catch (Exception e) {
                            }
                            return 0;
                        });
                        
                        measurements.addAll(measurementsForGateway.getResponse().getMeasurements());
                        
                        log(mac + "/" + capability, String.format("Will check %d measurements for resource.", measurements.size()));
                        
                        int count = 0;
                        for (final SynfieldMeasurement synfieldMeasurement : measurements) {
                            if (synfieldMeasurement.getService().toLowerCase().endsWith(capability.toLowerCase())) {
                                final Date measurementTime = dateStringFormat.parse(synfieldMeasurement.getTimestamp());
                                LOGGER.debug(measurementTime + "<<" + then);
                                if (measurementTime.after(then)) {
                                    count++;
                                    send(gateway, capability, synfieldMeasurement.getDoubleValue(), dateStringFormat.parse(synfieldMeasurement.getTimestamp()).getTime());
                                }
                            }
                        }
                        log(mac + "/" + capability, String.format("Sent a total of %d measurements.", count));
                    }
                }
            } catch (Exception e) {
                LOGGER.error(e, e);
            }
        }
        
        LOGGER.info("DONE!");
    }
    
    private void send(String gateway, String capability, double doubleValue, long timestamp) {
        senderService.sendMeasurement(gateway + "/" + capability, doubleValue, timestamp);
    }
    
    
    private void log(String uri, String message) {
        LOGGER.info(String.format("[%s] %s", StringUtils.rightPad(uri, 50), message));
    }
}
