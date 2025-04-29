package com.alert.alert_front_server.event;

import com.alert.alert_front_server.adapter.out.external.ExternalSenderService;
import com.alert.alert_front_server.domain.NotificationDomain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final ExternalSenderService externalSenderService;

    @Async
    @EventListener
    public void handleNotificationEvent(NotificationEvent event) {
        NotificationDomain notificationDomain = event.getNotificationDomain();
        log.info("[NotificationEventListener] Handling notification event for ID: {}", notificationDomain.getId());
        externalSenderService.send(notificationDomain);
    }
}