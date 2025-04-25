package com.alert.alert_front_server.application.port.in;

import com.alert.alert_front_server.application.command.NotificationRegisterCommand;
import com.alert.alert_front_server.domain.NotificationDomain;

public interface RegisterNotificationUseCase {
	NotificationDomain registerNotification(NotificationRegisterCommand command);
}
