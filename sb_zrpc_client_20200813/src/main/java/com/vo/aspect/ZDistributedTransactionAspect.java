package com.vo.aspect;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.vo.annotation.ZDistributedTransaction;
import com.vo.netty.ZRPCClientSender;
import com.vo.netty.ZRPCProtocol;
import com.vo.netty.ZRPCProtocolAttachEnum;
import com.vo.netty.ZRPETE;
import com.votool.common.ZPU;
import com.votool.ze.ZE;
import com.votool.ze.ZERunnable;
import com.votool.ze.ZES;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 拦截@ZDistributedTransaction
 *
 * @author zhangzhen
 * @date 2023年5月31日
 *
 */
@Aspect
@Component
public class ZDistributedTransactionAspect {

	private final static ZE ZE_SINGLE = ZES.newSingleZE();

	private final AtomicBoolean done = new AtomicBoolean(false);

	private final AtomicBoolean exception = new AtomicBoolean(false);

	public static ThreadLocal<InvokeDTO> invokeTL = new ThreadLocal<>();

	/**
	 * <UUID,InvokeDTO>
	 */
	private final ConcurrentMap<String, InvokeDTO> invokeDTOmap = Maps.newConcurrentMap();

	private final ArrayListMultimap<String, String> methodMap = ArrayListMultimap.create();

	@Around("zDistributedTransaction()")
	public Object around(final ProceedingJoinPoint joinPoint) throws Throwable {

		final ZDistributedTransaction zDistributedTransaction = ((MethodSignature)joinPoint.getSignature()).getMethod().getAnnotation(ZDistributedTransaction.class);
		final String[] nameArray = zDistributedTransaction.name();

		final Date date = new Date();
		final String now = DateUtil.format(date, DatePattern.PURE_DATETIME_MS_FORMAT);
		final String uuid = generateZDTID();
		final InvokeDTO invokeDTO = new InvokeDTO();
		invokeDTO.setAroundTime(now);
		invokeDTO.setUuid(uuid);
		invokeDTO.setMethodNameArray(nameArray);

		this.invokeDTOmap.put(invokeDTO.getUuid(), invokeDTO);
		invokeTL.set(invokeDTO);

		try {

			this.exception.set(false);
			this.done.set(false);
			final Object result = joinPoint.proceed();
			this.done.set(true);

			return result;
		} catch (final Exception e) {
			this.exception.set(true);
			this.done.set(true);
			// throw 出去给全局异常类捕获
			throw e;
		}
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class InvokeDTO {
		/**
		 * 	around方法执行的时间点
		 */
		private String aroundTime;

		/**
		 * UUID
		 */
		private String uuid;

		/**
		 * 一组方法名 @see ZDistributedTransaction
		 */
		private String[] methodNameArray;

	}

	synchronized void putMethodName(final String uuid, final String methodName) {
		this.methodMap.put(uuid, methodName);
	}

	synchronized List<String> getMethodList(final String uuid) {
		return this.methodMap.get(uuid);
	}

	public void addRESULTName(final ZRPCProtocol zrpe, final ChannelHandlerContext ctx) {

		ZDistributedTransactionAspect.ZE_SINGLE.executeInQueue(new ZERunnable<String>() {

			@Override
			public void run() {

				while (!ZDistributedTransactionAspect.this.done.get()) {

				}

				if (ZDistributedTransactionAspect.this.exception.get()) {
					return;
				}

				final InvokeDTO d2 = ZPU.deserialize(
						(byte[]) zrpe.getAttachMap()
								.get(ZRPCProtocolAttachEnum.DISTRIBUTED_TRANSACTION_ATTACH.getAttachType()),
						InvokeDTO.class);

				final InvokeDTO invokeDTO = ZDistributedTransactionAspect.this.invokeDTOmap.get(d2.getUuid());

				final String[] methodNameArray = invokeDTO.getMethodNameArray();

				boolean allMatch = false;
				if (methodNameArray.length <= 1) {
					if (methodNameArray[0].equals(zrpe.getName())) {
						allMatch = true;
					}
				} else {
					ZDistributedTransactionAspect.this.methodMap.put(invokeDTO.getUuid(), zrpe.getName());

					final List<String> methodList = ZDistributedTransactionAspect.this.methodMap.get(invokeDTO.getUuid());

					for (final String m : methodNameArray) {
						final boolean anyMatch = methodList.stream().anyMatch(m2 -> m2.equals(m));
						if (anyMatch) {
							allMatch = true;
							break;
						}
					}
				}

				if (!allMatch) {
					return;
				}

				final ZRPCProtocol zrpeCommit = ZRPCProtocol.builder()
						.id(zrpe.getId())
						.serviceName(zrpe.getServiceName())
						.name(zrpe.getName())
						.attachMap(zrpe.getAttachMap())
						.type(ZRPETE.COMMIT.getType())
						.build();
				ZRPCClientSender.send(zrpeCommit, ctx);

				ZDistributedTransactionAspect.this.methodMap.removeAll(invokeDTO.getUuid());
			}
		});

	}


	/**
	 * 生成一个唯一的事务ID
	 *
	 * @return
	 *
	 */
	private static String generateZDTID() {
		final String zdtID = UUID.randomUUID().toString();
		return zdtID;
	}


	@Pointcut("@annotation(com.vo.annotation.ZDistributedTransaction)")
	public void zDistributedTransaction() {

	}

}
