package com.alert.alert_front_server.exception;


import com.alert.alert_front_server.common.response.ErrorCode;

public class InvalidValueException extends BusinessException {

	public InvalidValueException(ErrorCode errorCode) {
		super(errorCode);
	}

	public InvalidValueException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}

}
