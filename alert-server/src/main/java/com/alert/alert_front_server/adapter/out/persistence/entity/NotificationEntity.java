package com.alert.alert_front_server.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String customerId;     // 고객 ID
	private String channelType;    // SMS/KAKAOTALK/EMAIL 등
	private String destination;    // 전화번호 / 카톡ID / 이메일
	private String title;
	private String contents;

	private LocalDateTime scheduledTime; // 예약발송 시각 (null 이면 즉시)
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	private String status;         // REGISTERED, SENT, FAILED 등
	private int retryCount;

	public void updateStatusAndUpdatedAt(String status) {
		this.status = status;
		this.updatedAt = LocalDateTime.now();
	}

}
