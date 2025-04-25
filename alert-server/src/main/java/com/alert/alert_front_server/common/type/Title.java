package com.alert.alert_front_server.common.type;

import com.alert.alert_front_server.common.response.ErrorCode;
import com.alert.alert_front_server.exception.InvalidValueException;
import lombok.Getter;

@Getter
public class Title {
	private static final int TITLE_MAX_SIZE = 255;

	private String title;

	public Title(String title) {

		if (title == null || title.isEmpty()) {
			throw new InvalidValueException(ErrorCode.INVALID_TITLE);
		}

		if(title.length() > TITLE_MAX_SIZE) {
			throw new InvalidValueException(ErrorCode.TITLE_TOO_LONG);
		}

		this.title = title;
	}

}
