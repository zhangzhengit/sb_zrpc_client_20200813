package com.vo.netty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.vo.cache.ZRC_RMI_CACHE;
import com.vo.common.ZProtobufUtil;
import com.vo.e.ZRPCRemoteMethodException;
import com.vo.netty.ZRPCProtocol.ARG;

import cn.hutool.core.collection.CollUtil;

/**
 * 
 *
 * @author zhangzhen
 * @data Aug 14, 2020
 * 
 */
public final class ZR_RMIH {

	private final ZRPCClientHandlerAdapter zrch;
	public ZR_RMIH(final ZRPCClientHandlerAdapter zrch) {
		super();
		this.zrch = zrch;
	}
	
	@SuppressWarnings("unchecked")
	public <RT> RT invoke(final ZRPCProtocol zrpe, final boolean isAsync) {
		final List<Object> args = zrpe.getArgs();
		if (CollUtil.isNotEmpty(args)) {
			final List<Object> bl = Lists.newArrayList();
			
			final Map<Integer, Object> pm = new HashMap<>();
			int index = 1;

			for (final Object a1 : args) {
				if (Objects.isNull(a1)) {
					bl.add(null);
					pm.put(index, null);
				} else {
					final byte[] ba1 = ZProtobufUtil.serialize(a1);
					bl.add(ba1);
					pm.put(index, ba1);
				}
				index++;
			}
			zrpe.setArgs(Lists.newArrayList(pm));
//			zrpe.setArgs(bl);
		}

		this.zrch.send(zrpe);
		
		if (isAsync) {
			return null;
		}
		
		
		final ZRPCProtocol rv = ZRC_RMI_CACHE.getResultUntilReturn(zrpe.getId());
		final ZRPETE zrpete = ZRPETE.valueOfType(rv.getType());
		switch (zrpete) {

		case RESULT:
			return (RT) rv.getRv();
			
		case INVOEK_EXCEPTION:
			final String teMessage = ZProtobufUtil.deserialize((byte[])rv.getRv(), String.class);
			throw new ZRPCRemoteMethodException("请求错误,INVOEK_EXCEPTION.message=" + teMessage);
		
		case PRODUCER_NOT_FOUND:
			throw new ZRPCRemoteMethodException("请求错误,目标producer方法不存在.zrpe = " + zrpe);
			
		case PRODUCER_CTX_CLOSED:
			throw new ZRPCRemoteMethodException("请求错误,producer已关闭.zrpe = " + zrpe);
			
		default:
			break;
		}
		return null;
	}

}
