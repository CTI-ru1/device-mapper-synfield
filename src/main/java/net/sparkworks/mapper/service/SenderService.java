package net.sparkworks.mapper.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
public class SenderService {
    private static final String MESSAGE_TEMPLATE = "%s,%f,%d";
    
    @Value("${rabbitmq.queue.send}")
    String rabbitQueueSend;
    @Autowired
    RabbitTemplate rabbitTemplate;
    
    public void sendMeasurement(final String uri, final Double reading, final long timestamp) {
        final String message = String.format(MESSAGE_TEMPLATE, uri, reading, timestamp);
        log(uri, String.format("At %s '%s'", new Date(timestamp), message));
        rabbitTemplate.send(rabbitQueueSend, rabbitQueueSend, new Message(message.getBytes(), new MessageProperties()));
    }
    
    private void log(String uri, String message) {
        log.info(String.format("[%s] %s", StringUtils.rightPad(uri, 50), message));
    }
}
