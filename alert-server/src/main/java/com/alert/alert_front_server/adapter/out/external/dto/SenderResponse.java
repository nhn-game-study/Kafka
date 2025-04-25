package com.alert.alert_front_server.adapter.out.external.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 공통 응답 DTO
 * resultCode: "SUCCESS" 또는 "FAIL"
 */
@Getter
@NoArgsConstructor
@Slf4j
public class SenderResponse {
	private String resultCode;

	public SenderResponse(String resultCode) {
		log.info("resultCode: {}", resultCode);
		this.resultCode = resultCode;
	}
}
