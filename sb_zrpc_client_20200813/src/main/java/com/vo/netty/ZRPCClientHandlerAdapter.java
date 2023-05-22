package com.vo.netty;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vo.async.ZRPCAsyncAfterReturnCallable;
import com.vo.cache.ZRC_RMI_CACHE;
import com.vo.common.ZBeanUtil;
import com.vo.common.ZE;
import com.vo.common.ZIDG;
import com.vo.common.ZProtobufUtil;
import com.vo.conf.ZrpcConfiguration;
import com.vo.core.ZLog;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * The Class ZRPCClientHandlerAdapter.
 *
 * @author zhangzhen
 * @data Aug 13, 2020
 */
@Component
public class ZRPCClientHandlerAdapter extends ChannelInboundHandlerAdapter {

	@Value("${server.port}")
	private Integer serverPort;

	private final ZLog log = ZLog.getInstance();

	/**
	 * Send.
	 *
	 * @param zrpe the zrpe
	 */
	public void send(final ZRPCProtocol zrpe) {
		final ChannelHandlerContext ctx = ZRCTXC.get();
		ZRPCClientSender.send(zrpe, ctx);
	}

	/**
	 * Channel active.
	 *
	 * @param ctx the ctx
	 * @throws Exception the exception
	 */
	@Override
	public void channelActive(final ChannelHandlerContext ctx) throws Exception {
		this.log.info("channelActive,ctx={}", ctx);
		ZRCTXC.set(ctx);
		final Map<String, Method> methodMap = ZRMethodCache.allMethod();

		if (CollUtil.isEmpty(methodMap)) {
			this.log.info("channelActive,methodMap is empty");
			return;
		}

		this.log.info("channelActive,methodMap.size={},methodSet={}", methodMap.size(), methodMap.keySet());

		final Set<Entry<String, Method>> es = methodMap.entrySet();
		final ZrpcConfiguration zrpcConf = ZBeanUtil.getBean(ZrpcConfiguration.class);


		final InetAddress address = InetAddress.getLocalHost();
		final String hostName = address.getHostName();
		final String hostAddress = address.getHostAddress();

		for (final Entry<String, Method> entry : es) {
			final ZRPCProtocol zrpe = ZRPCProtocol.builder()
						.serviceName(zrpcConf.getServiceName())
						.id(ZIDG.generateId())
						.name(entry.getKey())
						.type(ZRPETE.INIT.getType())
					.build();

			final Map<Integer, Object> attchMap = Maps.newHashMap();
			attchMap.put(ZRPCProtocolAttachEnum.LOCAL_IP.getAttachType(), hostAddress);
			attchMap.put(ZRPCProtocolAttachEnum.LOCAL_PORT.getAttachType(), this.serverPort);
			attchMap.put(ZRPCProtocolAttachEnum.HOST_NAME.getAttachType(), hostName);

			zrpe.setAttachMap(attchMap);

			this.log.info("开始注册远程方法,id={},method={}", zrpe.getId(), zrpe.getName());
			ZRPCClientSender.send(zrpe, ctx);

		}
	}

	/**
	 * Channel read.
	 *
	 * @param ctx the ctx
	 * @param msg the msg
	 * @throws Exception the exception
	 */
	@Override
	public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
		this.log.info("读取到消息,msg={}", msg);
		if (msg instanceof ZRPCProtocol) {
			final ZRPCProtocol zrpe = (ZRPCProtocol) msg;
			this.log.info("读取到ZRPE消息,id={},zrpe={}", zrpe.getId(), zrpe);

			final int type = zrpe.getType();
			final ZRPETE zrpete = ZRPETE.valueOfType(type);
			switch (zrpete) {
			case RESULT:
				this.log.info("处理RESULT消息,id={},rv={},zrpe={}", zrpe.getId(), zrpe.getRv(), zrpe);
				ZRC_RMI_CACHE.setResult(zrpe.getId(), zrpe);

				if (Objects.isNull(zrpe.getRv())) {
					return;
				}

				// 在此异步执行远程方法返回的结果
				ZE.submit(new ZRPCAsyncAfterReturnCallable(zrpe));
				return;

			case INIT_SUCCESS:
				this.log.info("处理INIT_SUCCESS消息,id={},zrpe={}", zrpe.getId(), zrpe);
				return;

			case INVOEK_EXCEPTION:
				this.log.warn("处理INVOEK_EXCEPTION消息,id={},message={}", zrpe.getId(), zrpe.getRv());
				ZRC_RMI_CACHE.setResult(zrpe.getId(), zrpe);
				return;

			case PRODUCER_CTX_CLOSED:
				this.log.info("处理PRODUCER_CTX_CLOSED消息,id={},zrpe={}", zrpe.getId(), zrpe);
				ZRC_RMI_CACHE.setResult(zrpe.getId(), zrpe);

				return;

			case PRODUCER_NOT_FOUND:
				this.log.info("处理PRODUCER_NOT_FOUND消息,id={},zrpe={}", zrpe.getId(), zrpe);
				ZRC_RMI_CACHE.setResult(zrpe.getId(), zrpe);
				return;

			case INVOEK:
				this.log.info("处理INVOEK消息,id={},method={},zrpe={}", zrpe.getId(), zrpe.getName(), zrpe);
				ZE.execute(new Runnable() {
					@Override
					public void run() {
						ZRPCClientHandlerAdapter.this.log.info("处理INVOEK消息开始执行本地方法,id={},method={},zrpe={}", zrpe.getId(), zrpe.getName(), zrpe);
						try {

							// FIXME 2021-12-27 19:30:51 zhangzhen :  改为try 只try invoke方法，然后catch里面send
							final Object rv2 = ZRPCClientHandlerAdapter.this.invoke(zrpe);
							final ZRPCProtocol zrpeRESULT = ZRPCProtocol.builder()
											.id(zrpe.getId())
											.serviceName(zrpe.getServiceName())
											.name(zrpe.getName())
											.type(ZRPETE.RESULT.getType())
											.rv(rv2)
											.build();
							ZRPCClientHandlerAdapter.this.log.info("处理INVOEK消息开始执行本地方法,返回执行结果,id={},method={},resultId={},result={}",
									zrpe.getId(), zrpe.getName(), zrpeRESULT.getId(), zrpeRESULT.getRv());

							ZRPCClientSender.send(zrpeRESULT, ctx);
						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
							ZRPCClientHandlerAdapter.this.log.error("处理INVOEK消息开始执行本地方法异常,id={},method={},message={},e={}",
									zrpe.getId(), zrpe.getName(), e.getMessage(), e);
							ZRPCClientHandlerAdapter.sendLocalMethodExceptionToConsumer(ctx, zrpe, e);

							e.printStackTrace();
						}
					}

				});
				break;

			case INIT:
			case SHUTDOWN:
				break;

			default:
				break;
			}

		}

	}

	/**
	 * Invoke.
	 *
	 * @param zrpe the zrpe
	 * @return the object
	 * @throws IllegalAccessException the illegal access exception
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	private Object invoke(final ZRPCProtocol zrpe)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		final Method localMethod = ZRMethodCache.getMethod(zrpe.getName());
		final Object localObject = ZRMethodCache.getObject(localMethod);
		// 应该根据远程方法是否有参数来判断是否传参，根据传来的参数是否empty是不准的，
		// 因为可能参数本身是null
		final Class<?>[] parameterTypes = localMethod.getParameterTypes();

		if (ArrayUtil.isEmpty(parameterTypes)) {
			final Object r = localMethod.invoke(localObject, null);
			return r;
		}

		final List<Object> argList = zrpe.getArgs();
		// 远程方法有参数，但是传来参数是empty,invoke传入空数组
		if (CollUtil.isEmpty(argList)) {
			final Object[] o = new Object[parameterTypes.length];
			final Object r = localMethod.invoke(localObject, o);
			return r;
		}

		final Map<Integer, Object> pm = (Map<Integer, Object>) argList.get(0);

		final List<Object> methodArgList = Lists.newArrayListWithCapacity(parameterTypes.length);
		for (int i = 0; i < parameterTypes.length; i++) {
			final int paramIndex = i + 1;
			final Object object = pm.get(paramIndex);
			System.out.println("参数 i = " + i + "\t" + "value = " + object);
			if (Objects.isNull(object)) {
				methodArgList.add(null);
			}else {
				final Class<?> pTypeClass = parameterTypes[i];
				pTypeClass.getClass();
				final Class c = getJavaLangClass(pTypeClass);
				final byte[] ba = (byte[]) object;
				final Object arg = ZProtobufUtil.deserialize(ba, c);
				methodArgList.add(arg);
			}
		}
		final Object r = localMethod.invoke(localObject, methodArgList.toArray());
		return r;
	}

	/**
	 * Gets the java lang class.
	 *
	 * @param c the c
	 * @return the java lang class
	 */
	private static Class<?> getJavaLangClass(final Class<?> c) {
		switch (c.getSimpleName()) {
		case "int":
			return java.lang.Integer.class;

		case "byte":
			return java.lang.Byte.class;

		case "short":
			return java.lang.Short.class;

		case "long":
			return java.lang.Long.class;

		case "boolean":
			return java.lang.Boolean.class;

		case "char":
			return java.lang.Character.class;

		case "float":
			return java.lang.Float.class;

		case "double":
			return java.lang.Double.class;

		default:
			break;
		}

		return c;
	}

	/**
	 * Send local method exception to consumer.
	 *
	 * @param ctx the ctx
	 * @param zrpe the zrpe
	 * @param e the e
	 */
	private static void sendLocalMethodExceptionToConsumer(final ChannelHandlerContext ctx,
			final ZRPCProtocol zrpe, final Exception e) {
		if (e instanceof InvocationTargetException) {
			final InvocationTargetException e2 = (InvocationTargetException) e;
			final ZRPCProtocol invoekEXCEPTION = ZRPCProtocol.builder()
						.id(zrpe.getId())
						.type(ZRPETE.INVOEK_EXCEPTION.getType())
						.rv(String.valueOf(e2.getTargetException()))
						.build();
			ZRPCClientSender.send(invoekEXCEPTION, ctx);
		} else {
			final ZRPCProtocol invoekEXCEPTION = ZRPCProtocol.builder()
						.id(zrpe.getId())
						.type(ZRPETE.INVOEK_EXCEPTION.getType())
						.rv(String.valueOf(e))
						.build();
			ZRPCClientSender.send(invoekEXCEPTION, ctx);
		}
	}

	@Override
	public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
		// TODO Auto-generated method stub
		System.out.println(java.time.LocalDateTime.now() + "\t" + Thread.currentThread().getName() + "\t"
				+ "ZRPCClientHandlerAdapter.exceptionCaught()");

		super.exceptionCaught(ctx, cause);
	}

}
