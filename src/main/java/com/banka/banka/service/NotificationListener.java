package com.banka.banka.service;

import com.banka.banka.config.RabbitMQConfig;
import com.banka.banka.dto.TransferNotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationListener {

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void consumeTransferNotification(TransferNotificationEvent event) {
        log.info("RABBITMQ MESAJI ALINDI: {} nolu IBAN'dan {} nolu IBAN'a {} TL transfer edildi. Müşteriye SMS gönderiliyor...",
                event.getFromIban(),
                event.getToIban(),
                event.getAmount());
    }
}
