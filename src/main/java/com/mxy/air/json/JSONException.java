package com.mxy.air.json;

/**
 * JSON相关的异常
 * 
 * @author mengxiangyun
 *
 */
public class JSONException extends RuntimeException {
	private static final long serialVersionUID = -8909946687679367534L;

	public JSONException() {
		super();
	}

	public JSONException(String message) {
		super(message);
	}

	public JSONException(Throwable cause) {
		super(cause);
	}

	public JSONException(String message, Throwable cause) {
		super(message, cause);
	}

}
