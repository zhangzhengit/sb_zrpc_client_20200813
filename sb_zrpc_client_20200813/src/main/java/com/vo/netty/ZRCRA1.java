package com.vo.netty;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.vo.common.ZBeanFactoryPostProcessor;
import com.vo.conf.ZrpcConfiguration;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 *
 *
 * @author zhangzhen
 * @data Aug 13, 2020
 *
 */
@Order(value = Integer.MIN_VALUE  + 1)
@Component
public class ZRCRA1 implements ApplicationRunner, DisposableBean {

	private EventLoopGroup group;

	@Autowired
	private ZRPCClientHandlerAdapter zrch;
	@Autowired
	private ZrpcConfiguration zrpcConf;

	@Override
	public void run(final ApplicationArguments args) throws Exception {
		System.out.println(
				java.time.LocalDateTime.now() + "\t" + Thread.currentThread().getName() + "\t" + "ZRCRA1.run()");

		final boolean enableZRPC = ZBeanFactoryPostProcessor.isEnableZRPC();
		if (!enableZRPC) {
			return;
		}

		final Bootstrap bootstrap = new Bootstrap();
		this.group = new NioEventLoopGroup();
		bootstrap.group(this.group);
		bootstrap.channel(NioSocketChannel.class);

		bootstrap.remoteAddress(new InetSocketAddress(this.zrpcConf.getServerHost(), this.zrpcConf.getServerPort()));
		bootstrap.option(ChannelOption.SO_BACKLOG, 20000);
		bootstrap.option(ChannelOption.TCP_NODELAY, true);
		bootstrap.option(ChannelOption.SO_REUSEADDR, true);
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true);

		bootstrap.handler(new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(final SocketChannel ch) throws Exception {
				System.out.println(Thread.currentThread().getName() + "\t" + LocalDateTime.now() + "\t"
						+ "ZRCRA1.run(...).new ChannelInitializer() {...}.initChannel()");
				System.out.println();

				final ChannelPipeline p = ch.pipeline();
				p.addLast(new ZRPCClientMessageDecoder());
				p.addLast(ZRCRA1.this.zrch);
			}
		});

		try {
			final ChannelFuture future = bootstrap.connect().sync();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void destroy() throws Exception {
		System.out.println(Thread.currentThread().getName() + "\t" + LocalDateTime.now() + "\t" + "ZRCRA1.destroy()");
		System.out.println();

		this.zrch.send(ZRPCProtocol.builder().type(ZRPETE.SHUTDOWN.getType()).build());
		this.group.shutdownGracefully().sync();
		System.out.println("group.shutdownGracefully().sync()");
	}
}
