package org.age.hz.core.tasks.utils;

import org.age.hz.core.services.communication.event.ReceiveEvent;
import org.age.hz.core.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;

@Named
public class ReceivedMessageProcessor {

    private static final Logger log = LoggerFactory.getLogger(ReceivedMessageProcessor.class);

    public void process(ReceiveEvent receiveEvent) {

        long timestamp = System.currentTimeMillis();
        log.warn("{},rcv,{},{},{}", TimeUtils.toString(timestamp), timestamp, receiveEvent.getSendTimestamp(), receiveEvent.getMessageId());

        String sendTime = TimeUtils.toString(receiveEvent.getSendTimestamp());
        long receiveTimestamp = System.currentTimeMillis();
        String receiveTime = TimeUtils.toString(receiveTimestamp);
        log.info("Received:\n" +
                        "MSG_ID:    {}\n\n" +
                        "FROM:      {}\n" +
                        "           {} [{}]\n\n" +
                        "TO:        {}\n" +
                        "           {} [{}]",
                receiveEvent.getMessageId(),
                receiveEvent.getSenderId(),
                sendTime,
                receiveEvent.getSendTimestamp(),
                receiveEvent.getRecipientId(),
                receiveTime,
                receiveTimestamp);
    }

}
