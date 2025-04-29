package com.alert.alert_front_server.event;

import com.alert.alert_front_server.domain.NotificationDomain;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class NotificationEvent extends ApplicationEvent {
    private final NotificationDomain notificationDomain;

    public NotificationEvent(Object source, NotificationDomain notificationDomain) {
        super(source);
        this.notificationDomain = notificationDomain;
    }
}