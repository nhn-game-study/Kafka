package com.alert.alert_front_server.adapter.out.persistence.repository;


import com.alert.alert_front_server.adapter.out.persistence.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
}