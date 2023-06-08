package com.vo.netty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 *
 * @author zhangzhen
 * @data Aug 13, 2020
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ZRPCProtocol {

	/**
	 * 	服务名称
	 */
	private String serviceName;

	/**
	  * 每次请求的唯一ID,server和client每次通讯都带上这个ID
	 */
	private String id;

	/**
	 * ZRPETEnum.type
	 */
	private Integer type;

	/**
	 * 	调用的方法名称
	 */
	private String name;

	/**
	 * 	调用方法需要的参数
	 */
	private List<Object> args;

	/**
	 * 	调用方法返回的结果
	 */
	private Object rv;

	/**
	 * 	附加信息	Key 见 ZRPCProtocolAttachEnum
	 */
	private Map<Integer, Object> attachMap;

	public static Map<Integer, Object> dt() {
		final Map<Integer, Object> attachMap = new HashMap<>(2,1F);
		attachMap.put(ZRPCProtocolAttachEnum.DISTRIBUTED_TRANSACTION.getAttachType(), 1);
		return attachMap;
	}


	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class ARG {

		private Object value;

	}
}
