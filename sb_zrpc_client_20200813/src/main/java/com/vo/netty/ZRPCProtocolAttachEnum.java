package com.vo.netty;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ZRPCProtocol附近信息枚举值
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

	/**
	 * 分布式事务
	 */
	DISTRIBUTED_TRANSACTION(4),

	/**
	 * 分布式事务的唯一ID
	 */
	DISTRIBUTED_TRANSACTION_ID(5),

	/**
	 * 分布式事务的附加信息
	 */
	DISTRIBUTED_TRANSACTION_ATTACH(6),

	/**
	 * ZDTLocal标记的方法执行时候的ID
	 */
	ZDT_LOCAL_ID(7),

	;


	private Integer attachType;

}
