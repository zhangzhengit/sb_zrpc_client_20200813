/*
 * 
 */
package com.vo.e;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// TODO: Auto-generated Javadoc
/**
 * 	执行远程方法的异常,项目的RestControllerAdvice应该加入捕捉这个异常.
 *
 * @author zhangzhen
 * @date 2021-12-27 19:58:29
 */

/**
 * To string.
 *
 * @return the java.lang. string
 */
@Data

/**
 * Instantiates a new ZRPC remote method exception.
 *
 * @param message the message
 */
@AllArgsConstructor

/**
 * Instantiates a new ZRPC remote method exception.
 */
@NoArgsConstructor
public class ZRPCRemoteMethodException extends RuntimeException{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The message. */
	private String message;

}
