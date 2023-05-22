package com.vo.common;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @param <T>
 * 
 * @author zhangzhen
 * @date 2020-12-08 13:41:18
 * 
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CR<T> implements Serializable {

	public static final int CODE_ERROR = 50000;
	public static final int CODE_OK = 0;

	private static final long serialVersionUID = 1L;

	private int code;
	private String message;
	private T data;

	public static <T> CR<T> ok(final T data) {
		final CR<T> cr = new CR<>();
		cr.setCode(CODE_OK);
		cr.setData(data);
		return cr;
	}

	public static <T> CR<T> error(final String message) {
		final CR<T> cr = new CR<>();
		cr.setCode(CODE_ERROR);
		cr.setMessage(message);
		return cr;
	}

}
