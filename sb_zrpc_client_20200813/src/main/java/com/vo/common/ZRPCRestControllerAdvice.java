package com.vo.common;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.vo.e.ZRPCRemoteMethodException;

/**
 *
 *
 * @author zhangzhen
 * @date 2021-12-27 20:02:17
 *
 */
@RestControllerAdvice
public class ZRPCRestControllerAdvice {

	@ResponseBody
	@ExceptionHandler(value = Exception.class)
	public CR<Object> hanlde(final Exception e) {
		System.out.println("ZRPCRestControllerAdvice.hanlde()" + "\t" + LocalDateTime.now() + "\t"
				+ Thread.currentThread().getName());

		e.printStackTrace();

		if (e instanceof ZRPCRemoteMethodException) {
			final ZRPCRemoteMethodException ze = (ZRPCRemoteMethodException) e;
			final CR<Object> error = CR.error("_from_ZRPC_client_@ZRPCRemoteMethodException | " + ze.getMessage());
			return error;
		}
		return CR.error(e.getMessage());
	}

}
