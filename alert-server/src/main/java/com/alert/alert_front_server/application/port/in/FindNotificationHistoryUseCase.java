package com.alert.alert_front_server.application.port.in;

import com.alert.alert_front_server.domain.NotificationDomain;
import org.springframework.data.domain.Page;

public interface FindNotificationHistoryUseCase {
	Page<NotificationDomain> findNotificationHistory(String customerId, int page, int size);
}
