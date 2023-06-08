package com.vo.netty;

import com.google.common.collect.Maps;
import java.util.HashMap;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *
 *
 * @author zhangzhen
 * @data Aug 13, 2020
 *
 */
@Getter
@AllArgsConstructor
public enum ZRPETE {

	INIT(1, "INIT"),

	INIT_SUCCESS(11, "INIT_SUCCESS"),

	INVOEK(2, "INVOKE"),

	INVOEK_EXCEPTION(21, "INVOKE_EXCEPTION"),

	RESULT(3, "RESULT"),

	SHUTDOWN(4, "SHUTDOWN"),

	PRODUCER_NOT_FOUND(5, "PRODUCER_NOT_FOUND"),

	PRODUCER_CTX_CLOSED(6, "PRODUCER_CTX_CLOSED"),

	/**
	 * 提交事务，分布式事务用到
	 */
	COMMIT(7, "COMMIT"),

	/**
	 * 回滚事务，分布式事务用到
	 */
	ROLLBACK(8, "ROLLBACK"),
	;

	public static ZRPETE valueOfType(final Integer type) {
		return map.get(type);
	}

	static HashMap<Integer, ZRPETE> map = Maps.newHashMap();
	static {
		final ZRPETE[] vssss = values();
		for (final ZRPETE zrpete : vssss) {
			map.put(zrpete.getType(), zrpete);
		}
	}

	private Integer type;
	private String name;

}
