package com.vo.netty;

import java.util.Objects;

import com.google.common.collect.Lists;
import com.vo.common.ZProtobufUtil;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

/**
 * 
 *
 * @author zhangzhen
 * @data Aug 13, 2020
 * 
 */
public class ZRPCClientSender {

	public static void send(final ZRPCProtocol zrpe, final ChannelHandlerContext ctx) {
		final Object rv = zrpe.getRv();
		if (Objects.nonNull(rv)) {
			final byte[] rvBA = ZProtobufUtil.serialize(rv);
			zrpe.setRv(rvBA);
		}

		final byte[] w = ZProtobufUtil.wanzhangbytearray(ZProtobufUtil.serialize(zrpe));
		ctx.writeAndFlush(Unpooled.copiedBuffer(w));
	}
}
