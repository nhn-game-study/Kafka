package com.alert.alert_front_server.exception;


import com.alert.alert_front_server.common.response.ErrorCode;

public class DataNotFoundException extends BusinessException{

	public DataNotFoundException(ErrorCode errorCode) {
		super(errorCode);
	}

}
