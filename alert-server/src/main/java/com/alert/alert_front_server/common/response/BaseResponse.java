package com.alert.alert_front_server.common.response;

import lombok.Getter;

// 공통 응답 구조
@Getter
public class BaseResponse {
	private String resultCode;
	private String resultMessage;

	public BaseResponse() {
		this.resultCode = "0000";
		this.resultMessage = "success";
	}

	public BaseResponse(String resultMessage) {
		this.resultCode = "0000";
		this.resultMessage = resultMessage;
	}

	public BaseResponse(String resultCode, String resultMessage) {
		this.resultCode = resultCode;
		this.resultMessage = resultMessage;
	}

}
