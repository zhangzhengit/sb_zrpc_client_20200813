package com.vo.aspect;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.google.common.collect.ArrayListMultimap;
import com.vo.core.ZLog2;
import com.vo.netty.ZRPCClientSender;
import com.vo.netty.ZRPCProtocol;
import com.vo.netty.ZRPCProtocolAttachEnum;
import com.vo.netty.ZRPETE;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import io.netty.channel.ChannelHandlerContext;

/**
 * 拦截 @ZDTLocal 标记的方法，进行编程式事务控制
 *
 * @author zhangzhen
 * @date 2023年5月31日
 *
 */
@Aspect
@Component
public class ZDTLocalAspect {

	private static final ZLog2 LOG = ZLog2.getInstance();

	public static ThreadLocal<ZRPCProtocol> zrpeThreadLocal = new ThreadLocal<>();
	public static ThreadLocal<Boolean> isZDTLMethyodTL = new ThreadLocal<>();
	public static ThreadLocal<ChannelHandlerContext> ctxThreadLocal = new ThreadLocal<>();

	@Autowired
	private PlatformTransactionManager transactionManager;

	public void commit(final ZRPCProtocol zrpe) {

		final Map<Integer, Object> attachMap = zrpe.getAttachMap();
		final String zdtID = String.valueOf(attachMap.get(ZRPCProtocolAttachEnum.ZDT_LOCAL_ID.getAttachType()));

		final List<TransactionStatus> tsList = ZDTLocalAspect.this.getTransactionStatusByUUID(zdtID);

		if (CollUtil.isNotEmpty(tsList)) {
			for (final TransactionStatus transactionStatus : tsList) {
				if (!transactionStatus.isCompleted()) {
					try {
						ZDTLocalAspect.this.transactionManager.commit(transactionStatus);
					} catch (final Exception e) {
						e.printStackTrace();
						continue;
					}
				}
			}
		}

		ZDTLocalAspect.this.removeAllByUUID(zdtID);

	}

	public void rollback(final ZRPCProtocol zrpe) {

		final Map<Integer, Object> attachMap = zrpe.getAttachMap();
		final String zdtID = String.valueOf(attachMap.get(ZRPCProtocolAttachEnum.ZDT_LOCAL_ID.getAttachType()));

		final List<TransactionStatus> tsList = ZDTLocalAspect.this.getTransactionStatusByUUID(zdtID);

		if (CollUtil.isNotEmpty(tsList)) {
			for (final TransactionStatus transactionStatus : tsList) {
				if (!transactionStatus.isCompleted()) {
					try {
						ZDTLocalAspect.this.transactionManager.commit(transactionStatus);
					} catch (final Exception e) {
						e.printStackTrace();
						continue;
					}
				}
			}
		}

		ZDTLocalAspect.this.removeAllByUUID(zdtID);

	}

	private synchronized void removeAllByUUID(final String zdtID) {
		this.tStatusMap.removeAll(zdtID);
	}

	private synchronized List<TransactionStatus> getTransactionStatusByUUID(final String zdtID) {
		return this.tStatusMap.get(zdtID);
	}

	/**
	 * <ZDTLocal执行的ID, TransactionStatus>
	 */
	private final ArrayListMultimap<String, TransactionStatus> tStatusMap = ArrayListMultimap.create();

	@SuppressWarnings("boxing")
	@Around("zDistributedTransaction()")
	public Object around(final ProceedingJoinPoint joinPoint) throws Throwable {

		final DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		final TransactionStatus transactionStatus = ZDTLocalAspect.this.transactionManager.getTransaction(def);
		try {

			final String zdtID = UUID.randomUUID().toString();
			this.putTransactionStatus(zdtID, transactionStatus);

			final Object r = joinPoint.proceed();

			final ZRPCProtocol zrpeRESULT = ZRPCProtocol.builder()
							.id(zrpeThreadLocal.get().getId())
							.serviceName(zrpeThreadLocal.get().getServiceName())
							.name(zrpeThreadLocal.get().getName())
							.type(ZRPETE.RESULT.getType()).rv(r)
							.build();

			LOG.info("处理INVOEK消息-本地方法执行结束,返回执行结果,id={},method={},resultId={},result={}", zrpeThreadLocal.get().getId(),
					zrpeThreadLocal.get().getName(), zrpeRESULT.getId(), zrpeRESULT.getRv());

			if (isZDTLMethyodTL.get()) {
				final Map<Integer, Object> attachMap = zrpeThreadLocal.get().getAttachMap();
				attachMap.put(ZRPCProtocolAttachEnum.ZDT_LOCAL_ID.getAttachType(), zdtID);
				zrpeRESULT.setAttachMap(attachMap);
			}

			ZRPCClientSender.send(zrpeRESULT, ctxThreadLocal.get());

			return r;

		} catch (final Throwable e) {

			final String eMessage = gExceptionMessage(e);

			ZDTLocalAspect.this.transactionManager.rollback(transactionStatus);

			final ZRPCProtocol zrpeROLLBACK = ZRPCProtocol.builder()
							.id(zrpeThreadLocal.get().getId())
							.serviceName(zrpeThreadLocal.get().getServiceName())
							.name(zrpeThreadLocal.get().getName())
							.attachMap(zrpeThreadLocal.get().getAttachMap())
							.type(ZRPETE.ROLLBACK.getType())
							.build();


			final ZRPCProtocol zrpeINVOEK_EXCEPTION = ZRPCProtocol.builder()
						.id(zrpeThreadLocal.get().getId())
						.serviceName(zrpeThreadLocal.get().getServiceName())
						.name(zrpeThreadLocal.get().getName())
						.attachMap(zrpeThreadLocal.get().getAttachMap())
						.type(ZRPETE.INVOEK_EXCEPTION.getType())
						.rv(eMessage)
						.build();

			ZRPCClientSender.send(zrpeROLLBACK, ctxThreadLocal.get());

			// 异常：发送一个INVOEK_EXCEPTION消息通知消费者
			ZRPCClientSender.send(zrpeINVOEK_EXCEPTION, ctxThreadLocal.get());

			throw e;
		}

	}

	private synchronized void putTransactionStatus(final String zdtID, final TransactionStatus transactionStatus) {
		this.tStatusMap.put(zdtID, transactionStatus);
	}

	public static String gExceptionMessage(final Throwable e) {

		if (Objects.isNull(e)) {
			return "";
		}

		final StringWriter stringWriter = new StringWriter();
		final PrintWriter writer = new PrintWriter(stringWriter);
		e.printStackTrace(writer);

		final String eMessage = stringWriter.toString();

		return eMessage;
	}

	@Pointcut("@annotation(com.vo.annotation.ZDTLocal)")
	public void zDistributedTransaction() {

	}
}
