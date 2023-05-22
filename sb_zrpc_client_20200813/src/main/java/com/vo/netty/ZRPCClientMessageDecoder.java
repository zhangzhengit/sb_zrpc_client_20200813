package com.vo.netty;

import java.time.LocalDateTime;
import java.util.List;

import com.vo.common.ZProtobufUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 *  
 *
 * @author zhangzhen
 * @data Aug 13, 2020
 * 
 */
public class ZRPCClientMessageDecoder extends ByteToMessageDecoder{

	@Override
	protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) throws Exception {
		
		final int readableBytes = in.readableBytes();
		if (readableBytes < ZProtobufUtil.L_LENGTH) {
			return;
		}

		in.markReaderIndex();
		
		final byte[] lba = new byte[ZProtobufUtil.L_LENGTH];
		in.readBytes(lba);

		final int vlength = ZProtobufUtil.byteArrayToInt(lba);
		if (vlength <= 0) {
			in.resetReaderIndex();
			return;
		}

		final int readerIndex = in.readerIndex();
		final int writerIndex = in.writerIndex();
		if (readerIndex + vlength > writerIndex) {
			in.resetReaderIndex();
			return;
		}

		final byte[] vba = new byte[vlength];
		in.readBytes(vba);
		
		out.add(ZProtobufUtil.deserialize(vba, ZRPCProtocol.class));
	}
	
	@Override
	public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
		System.out.println(
				Thread.currentThread().getName() + "\t" + LocalDateTime.now() + "\t" + "ZRNSE.exceptionCaught()");
		System.out.println();
		
		cause.printStackTrace();
	}

}
