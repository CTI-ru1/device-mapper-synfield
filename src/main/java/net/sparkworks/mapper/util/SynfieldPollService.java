package net.sparkworks.mapper.util;

import com.sensorflare.io.SummaryFactory;
import gr.cti.ru1.synfield.client.Synfield;
import gr.cti.ru1.synfield.client.model.measurements.SynfieldMeasurement;
import gr.cti.ru1.synfield.client.model.measurements.SynfieldMeasurementsPage;
import gr.cti.ru1.synfield.client.model.sensors.SynfieldSensor;
import net.sparkworks.mapper.service.SenderService;
import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.URL;
import java.util.*;

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
    @Value("${sparkworks.storage}")
    private String sparkWorksStorage;

    @Autowired
    SenderService senderService;

    private final Synfield synfield = new Synfield();
    private final Set<String> synfieldDevices = new HashSet<>();
    private final SummaryFactory sf = new SummaryFactory();

    private final DateTimeFormatter dateStringFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

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
            LOGGER.info("polling " + mac);
            try {
                final List<SynfieldSensor> sensors = synfield.getSensors(mac).getResponse().getSensors();
                final Map<String, Long> uris = new HashMap<>();
                for (final SynfieldSensor sensor : sensors) {
                    final String uri = "synfield-" + mac + "/" + WordUtils.capitalize(sensor.getService());
                    final URL summaryURL = new URL(sparkWorksStorage + uri.replaceAll(" ", "%20"));
                    LOGGER.info(summaryURL);
                    uris.put(uri, sf.fromURL(summaryURL).getLatestTime().getMillis());
                }

                for (final String key : uris.keySet()) {
                    LOGGER.info(key);
                    final String gateway = key.split("/")[0];
                    final String capability = key.split("/")[1];
                    if (uris.get(key) == 0) {
                        final SynfieldMeasurementsPage measurementsForGateway = synfield.getMeasurements(mac);
                        for (final SynfieldMeasurement synfieldMeasurement : measurementsForGateway.getResponse().getMeasurements()) {
                            if (synfieldMeasurement.getService().toLowerCase().endsWith(capability.toLowerCase())) {
                                send(gateway, capability, synfieldMeasurement.getDoubleValue(), dateStringFormat.parseMillis(synfieldMeasurement.getTimestamp()));
                            }

                        }
                    } else {
                        final DateTime then = new DateTime(uris.get(key));
                        final DateTime now = new DateTime();
                        final SynfieldMeasurementsPage measurementsForGateway = synfield.getMeasurements(mac,
                                then.getYear() + "-" + then.getMonthOfYear() + "-" + then.getDayOfMonth()
                                , now.getYear() + "-" + now.getMonthOfYear() + "-" + now.getDayOfMonth());
                        for (final SynfieldMeasurement synfieldMeasurement : measurementsForGateway.getResponse().getMeasurements()) {
                            if (synfieldMeasurement.getService().toLowerCase().endsWith(capability.toLowerCase())) {
                                final DateTime measurementTime = new DateTime(dateStringFormat.parseMillis(synfieldMeasurement.getTimestamp()));
                                if (measurementTime.isAfter(then)) {
                                    send(gateway, capability, synfieldMeasurement.getDoubleValue(), dateStringFormat.parseMillis(synfieldMeasurement.getTimestamp()));
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error(e, e);
            }
        }
    }

    private void send(String gateway, String capability, double doubleValue, long timestamp) {
        senderService.sendMeasurement(gateway + "/" + capability, doubleValue, timestamp);
    }
}
