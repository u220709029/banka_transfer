package com.banka.banka.service;

import com.banka.banka.config.RabbitMQConfig;
import com.banka.banka.dto.TransferNotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationListener {

    // RabbitMQConfig'de tanımladığımız kuyruğu sürekli dinleyen kulak burasıdır.
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void consumeTransferNotification(TransferNotificationEvent event) {
        // Mesaj kuyruğa düştüğü milisaniye bu metot tetiklenir ve ekrana aşağıdaki logu basar.
        log.info("RABBITMQ MESAJI ALINDI: {} nolu IBAN'dan {} nolu IBAN'a {} TL transfer edildi. Müşteriye SMS/Email gönderiliyor...",
                event.getFromIban(), event.getToIban(), event.getAmount());
    }
}