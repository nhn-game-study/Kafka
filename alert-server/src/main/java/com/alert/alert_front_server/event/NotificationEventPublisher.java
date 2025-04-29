package com.alert.alert_front_server.event;

import com.alert.alert_front_server.application.port.out.UpdateNotificationPort;
import com.alert.alert_front_server.domain.NotificationDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class NotificationEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publishNotificationEvent(NotificationDomain notificationDomain) {
        NotificationEvent event = new NotificationEvent(this, notificationDomain);
        eventPublisher.publishEvent(event);
    }
}