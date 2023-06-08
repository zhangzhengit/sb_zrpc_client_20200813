package com.vo.netty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Lists;
import com.vo.aspect.ZDistributedTransactionAspect;
import com.vo.aspect.ZDistributedTransactionAspect.InvokeDTO;
import com.vo.cache.ZRC_RMI_CACHE;
import com.vo.common.ZProtobufUtil;
import com.vo.e.ZRPCRemoteMethodException;
import com.votool.common.ZPU;

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
		}

		final Map<Integer, Object> am = zrpe.getAttachMap();
		final boolean isDT = Objects.nonNull(am) && Objects.nonNull(am.get(ZRPCProtocolAttachEnum.DISTRIBUTED_TRANSACTION.getAttachType()));

		if (isDT) {
			final InvokeDTO invokeDTO = ZDistributedTransactionAspect.invokeTL.get();
			final byte[] bs = ZPU.serialize(invokeDTO);
			am.put(ZRPCProtocolAttachEnum.DISTRIBUTED_TRANSACTION_ATTACH.getAttachType(), bs);
		}

		this.zrch.send(zrpe);

		if (isAsync) {
			return null;
		}

		final String keyword = ZRC_RMI_CACHE.gKeyword(zrpe);
		final ZRPCProtocol rv = ZRC_RMI_CACHE.getResultUntilReturn(keyword);
		final ZRPETE zrpete = ZRPETE.valueOfType(rv.getType());
		switch (zrpete) {

		case PRODUCER_NOT_FOUND:
			throw new ZRPCRemoteMethodException("请求错误,目标producer方法不存在.zrpe = " + zrpe);

		case INVOEK_EXCEPTION:
			final String eMessage = ZProtobufUtil.deserialize((byte[])rv.getRv(), String.class);
			throw new ZRPCRemoteMethodException("请求错误,INVOEK_EXCEPTION.message=" + eMessage);

		case PRODUCER_CTX_CLOSED:
			throw new ZRPCRemoteMethodException("请求错误,producer已关闭.zrpe = " + zrpe);

		case RESULT:
			return (RT) rv.getRv();


		default:
			break;
		}
		return null;
	}

}
