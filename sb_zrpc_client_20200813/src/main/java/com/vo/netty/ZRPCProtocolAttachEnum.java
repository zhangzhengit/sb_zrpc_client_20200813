package com.vo.netty;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *
 *
 * @author zhangzhen
 * @date 2022年1月4日
 *
 */
@Getter
@AllArgsConstructor
public enum ZRPCProtocolAttachEnum {

	/**
	 * 本应用的ip
	 */
	LOCAL_IP(1),

	/**
	 * 本应用端口
	 */
	LOCAL_PORT(2),

	/**
	 * 本机器名称
	 */
	HOST_NAME(3),

	;


	private Integer attachType;

}
