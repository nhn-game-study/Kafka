package com.alert.alert_front_server.application.port.out;

import com.alert.alert_front_server.domain.NotificationDomain;

public interface UpdateNotificationPort {
	void updateNotification(NotificationDomain notificationDomain);
}
