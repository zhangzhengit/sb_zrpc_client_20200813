package com.vo.common;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.codehaus.groovy.control.CompilationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Lazy;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.vo.annotation.EnableZRPC;
import com.vo.annotation.ZRPCAsync;
import com.vo.annotation.ZRPCRemoteDT;
import com.vo.annotation.ZRPCRemoteMethodAnnotation;
import com.vo.async.ZRPCAsyncAfterReturn;
import com.vo.cache.ZMethodAysncResultCache;
import com.vo.cache.ZMethodAysncResultCache.ZMethodInfo;
import com.vo.cache.ZRC_RMI_CACHE;
import com.vo.conf.ZrpcConfiguration;
import com.vo.core.ZClass;
import com.vo.core.ZMethod;
import com.vo.core.ZMethodAccessEnum;
import com.vo.core.ZMethodArg;
import com.vo.core.ZPackage;
import com.vo.netty.ZRPCClientHandlerAdapter;
import com.vo.netty.ZRPCProtocol;
import com.vo.netty.ZRPCProtocolAttachEnum;
import com.vo.netty.ZRPETE;
import com.vo.netty.ZR_RMIH;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ClassUtil;
import groovy.lang.GroovyClassLoader;

/**
 * 给@ZRPCRemoteMethodAnnotation标记的interface动态生成一个实现类，并且注入到容器中.
 *
 * @author zhangzhen
 * @date 2021-12-15 1:10:17
 *
 */
public class ZRPCRemoteMethodAnnotationBeanGenerator {

	/** The Constant AFTER_RETURN_DELIMITER. */
	public static final String AFTER_RETURN_DELIMITER = ",";

	/** The Constant VOID. */
	private static final String VOID = "void";

	/** The Constant ZRC_RMI_CACHE. */
	public final static Class<ZRC_RMI_CACHE> ZRC_RMI_CACHE = ZRC_RMI_CACHE.class;

	/** The Constant ZMETHODAYSNCRESULTCACHE. */
	public final static Class<ZMethodAysncResultCache> ZMETHODAYSNCRESULTCACHE = ZMethodAysncResultCache.class;

	/** The Constant ZMETHODINFO. */
	public final static Class<ZMethodInfo> ZMETHODINFO = ZMethodInfo.class;

	/** The Constant ZRPE. */
	public final static Class<ZRPCProtocol> ZRPE = ZRPCProtocol.class;

	/** The Constant ZIDG. */
	public final static Class<ZIDG> ZIDG_CLS = ZIDG.class;

	/** The Constant ZRPETE. */
	public final static Class<ZRPETE> ZRPETE = ZRPETE.class;

	/** The Constant ZRPCASYNC. */
	public final static Class<ZRPCAsync> ZRPCASYNC = ZRPCAsync.class;

	/** The Constant LAZY. */
	public final static Class<Lazy> LAZY = Lazy.class;

	/** The Constant AUTOWIRED. */
	public final static Class<Autowired> AUTOWIRED = Autowired.class;

	/** The Constant ZRCHandler. */
	public final static Class<ZRPCClientHandlerAdapter> ZRCHandler = ZRPCClientHandlerAdapter.class;

	/** The Constant ZPU. */
	public final static Class<ZProtobufUtil> ZPU = ZProtobufUtil.class;

	/** The Constant ZR_RMIH. */
	public final static Class<ZR_RMIH> ZR_RMIH = ZR_RMIH.class;

	/** The Constant ZRemoteMethodAnnotation. */
	public final static Class<ZRPCRemoteMethodAnnotation> ZRPCRemoteMethodAnnotation = ZRPCRemoteMethodAnnotation.class;

	/** The Constant Z_CLASS_SUFFIX. */
	private static final String Z_CLASS_SUFFIX = "ZClass";

	/**
	 * Generate.
	 *
	 * @param bean the bean
	 */
	public static void generate(final Object bean) {

		final ConfigurableListableBeanFactory beanFactory = ZBeanFactoryPostProcessor.getBeanFactory();

		final Map<String, Object> en = beanFactory.getBeansWithAnnotation(EnableZRPC.class);
		if(CollUtil.isEmpty(en)) {
			return;
		}

		System.out.println("@开始生成bean");

		final Object beanZRCHandler = beanFactory.getBean(ZRPCRemoteMethodAnnotationBeanGenerator.ZRCHandler.getSimpleName());
		final ZR_RMIH beanZR_RMIH = new ZR_RMIH((ZRPCClientHandlerAdapter)beanZRCHandler);
		beanFactory.registerSingleton(ZRPCRemoteMethodAnnotationBeanGenerator.ZR_RMIH.getSimpleName(), beanZR_RMIH);
		final Set<ZClass> sourceSet = getZRemoteMethodAnnotationClassSet(bean);
		System.out.println("@带有ZRemoteMethodAnnotation注解的class个数 =  " + sourceSet.size());
		for (final ZClass zClass : sourceSet) {
			try {
				try {
					final Object obj = newInstance(zClass.toString());
					System.out.println("@生成bean完成,beanName = " + obj.getClass().getCanonicalName());
					final Method setMethod = obj.getClass().getDeclaredMethod("setZrch", ZRPCRemoteMethodAnnotationBeanGenerator.ZR_RMIH);
					System.out.println("setMethod = " + setMethod);
					setMethod.invoke(obj, beanZR_RMIH);
					final String zClassName = zClass.getName();
					final String beanName1 = zClassName.substring(0, zClassName.indexOf(ZRPCRemoteMethodAnnotationBeanGenerator.Z_CLASS_SUFFIX));
					System.out.println("substring = " + beanName1);
					beanFactory.registerSingleton(beanName1, obj);
					System.out.println("@注入成功,bean = " + zClassName);
				} catch (InstantiationException | IllegalAccessException | NoSuchMethodException
						| InvocationTargetException e) {
					e.printStackTrace();
				}
			} catch (SecurityException | IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * New instance.
	 *
	 * @param source the source
	 * @return the object
	 * @throws InstantiationException the instantiation exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws SecurityException the security exception
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	public static Object newInstance(final String source) throws InstantiationException, IllegalAccessException,
			SecurityException, IllegalArgumentException {
		try (GroovyClassLoader groovyClassLoader = new GroovyClassLoader()) {
			final Class<?> clazz = groovyClassLoader.parseClass(source);
			return clazz.newInstance();
		} catch (CompilationFailedException | IOException e) {
			e.printStackTrace();
		}
		return source;
	}

	/**
	 * Gets the z remote method annotation class set.
	 *
	 * @param bean the bean
	 * @return the z remote method annotation class set
	 */
	private static Set<ZClass> getZRemoteMethodAnnotationClassSet(final Object bean) {

		final Set<ZClass> sourceSet = new HashSet<>();
		final ZrpcConfiguration zrpcConf = (ZrpcConfiguration) bean;
		final Set<String> set = zrpcConf.getScanPackageNameSet();
		if (CollUtil.isEmpty(set)) {
			throw new IllegalArgumentException("ScanPackageName不能为空");
		}

		for (final String packageName : set) {
			final Set<Class<?>> comZMA = ClassUtil.scanPackageByAnnotation(packageName, ZRPCRemoteMethodAnnotationBeanGenerator.ZRPCRemoteMethodAnnotation);
			for (final Class<?> cls : comZMA) {
				final String remoteMethodAnnotationValue = getRemoteMethodAnnotationValue(cls);
				final String packageName2 = cls.getPackage().getName();
				final ZClass zClass = generateClass(packageName2, cls, remoteMethodAnnotationValue);
				sourceSet.add(zClass);
			}
		}

		return sourceSet;

	}

	/**
	 * Gets the remote method annotation value.
	 *
	 * @param cls the cls
	 * @return the remote method annotation value
	 */
	private static String getRemoteMethodAnnotationValue(final Class cls) {
		final Annotation[] as = cls.getAnnotations();
		if (ArrayUtil.isEmpty(as)) {
			return null;
		}

		for (final Annotation a : as) {
 			if (a.annotationType() != ZRPCRemoteMethodAnnotationBeanGenerator.ZRPCRemoteMethodAnnotation) {
				continue;
			}

			final String canonicalName = ZRPCRemoteMethodAnnotationBeanGenerator.ZRPCRemoteMethodAnnotation.getCanonicalName() + "(serviceName=";
			final String s = a.toString();
			final int i1 = s.indexOf(canonicalName);
			if (i1 > -1) {
				final int i2 = s.lastIndexOf(")");
				if (i2 > i1) {
					final String value = s.substring(i1 + canonicalName.length(), i2);
					System.out.println("value = " + value);
					return value;
				}
			}
		}

		return null;
	}

	/**
	 * 处理带有注解的类，给生成一个子类.
	 *
	 * @param packageName the package name
	 * @param cls the cls
	 * @param remoteMethodAnnotationValue the remote method annotation value
	 * @return the z class
	 */
	private static ZClass generateClass(final String packageName, final Class<?> cls, final String remoteMethodAnnotationValue) {
		final Method[] methods = cls.getDeclaredMethods();
		if (ArrayUtil.isEmpty(methods)) {
			return null;
		}

		final ZPackage zPackage = new ZPackage();
		zPackage.setPackageString("package " + packageName.substring(0, packageName.lastIndexOf(".")));

		final ZClass zClass = new ZClass();
		zClass.setPackage1(zPackage);
		System.out.println("ANNOTATION.getCanonicalName() = " + ZRPCRemoteMethodAnnotationBeanGenerator.ZRPCRemoteMethodAnnotation.getCanonicalName());
		zClass.setImportSet(Sets.newHashSet(
								ZRPCRemoteMethodAnnotationBeanGenerator.ZMETHODAYSNCRESULTCACHE.getCanonicalName(),
								ZRPCRemoteMethodAnnotationBeanGenerator.ZMETHODINFO.getCanonicalName(),
								ZRPCRemoteMethodAnnotationBeanGenerator.ZRPCRemoteMethodAnnotation.getCanonicalName(),
								ZRPCRemoteMethodAnnotationBeanGenerator.ZPU.getCanonicalName(),
								ZRPCRemoteMethodAnnotationBeanGenerator.LAZY.getCanonicalName(),
								cls.getCanonicalName(),
								ZRPCRemoteMethodAnnotationBeanGenerator.ZRC_RMI_CACHE.getCanonicalName(),
								ZRPCRemoteMethodAnnotationBeanGenerator.ZRPE.getCanonicalName(),
								ZRPCRemoteMethodAnnotationBeanGenerator.ZIDG_CLS.getCanonicalName(),
								java.util.Map.class.getCanonicalName(),
								ZRPCRemoteMethodAnnotationBeanGenerator.ZRPETE.getCanonicalName(),
								ZRPCRemoteMethodAnnotationBeanGenerator.AUTOWIRED.getCanonicalName()));
		zClass.setAnnotationSet(Sets.newHashSet("@" + ZRPCRemoteMethodAnnotationBeanGenerator.ZRPCRemoteMethodAnnotation.getSimpleName() + "(serviceName = \"" + remoteMethodAnnotationValue + "\")",
												"@" + ZRPCRemoteMethodAnnotationBeanGenerator.LAZY.getSimpleName()));
		zClass.setAccessRights(ZMethodAccessEnum.PUBLIC);
		zClass.setName(cls.getSimpleName() + ZRPCRemoteMethodAnnotationBeanGenerator.Z_CLASS_SUFFIX);
		zClass.setImplementsSet(Sets.newHashSet(cls.getSimpleName()));
		zClass.setBody(
				" @" + ZRPCRemoteMethodAnnotationBeanGenerator.LAZY.getCanonicalName() + ZClass.NEW_LINE +
				" public " + cls.getSimpleName() + ZRPCRemoteMethodAnnotationBeanGenerator.Z_CLASS_SUFFIX + "(){}" + ZClass.NEW_LINE +
				" private " + ZRPCRemoteMethodAnnotationBeanGenerator.ZR_RMIH.getCanonicalName() + " zr_RMIH;" + ZClass.NEW_LINE +
				" public void setZrch(com.vo.netty.ZR_RMIH zr_RMIH) {" + ZClass.NEW_LINE +
				"		this.zr_RMIH = zr_RMIH;" + ZClass.NEW_LINE +
				" }");

		final Set<ZMethod> methodSet = Sets.newHashSet();
		for (final Method method : methods) {
			if (!Modifier.isPublic(method.getModifiers())) {
				continue;
			}

			final boolean methodAsync = isMethodAsync(method);

			final ZMethod zMethod = new ZMethod();
			if(methodAsync) {
				zMethod.addAnnotation("@" + ZRPCAsync.class.getCanonicalName());
			}
			zMethod.setAccessRights(ZMethodAccessEnum.PUBLIC);
			zMethod.setFinal(Modifier.isFinal(method.getModifiers()));
			zMethod.setStatic(Modifier.isStatic(method.getModifiers()));
			zMethod.setSynchronized(Modifier.isSynchronized(method.getModifiers()));
			zMethod.setReturnType(method.getReturnType().getCanonicalName());
			zMethod.setName(method.getName());

			final Parameter[] pa = method.getParameters();
			if (ArrayUtil.isEmpty(pa)) {
				final String generateMethodBodyHasNoParameter = generateMethodBodyHasNoParameter(method, remoteMethodAnnotationValue);
				zMethod.setBody(generateMethodBodyHasNoParameter);
			} else {
				final List<String> argList = Lists.newArrayList();
				final ArrayList<ZMethodArg> mal = Lists.newArrayList();
				for (int i = 0; i < pa.length; i++) {
					final ZMethodArg methodArg = new ZMethodArg(pa[i].getType().getCanonicalName(), pa[i].getName());
					mal.add(methodArg);
					argList.add(pa[i].getName());
				}

				zMethod.setMethodArgList(mal);
				final String h = generateMethodBodyHasParameter(method, argList, remoteMethodAnnotationValue);
				zMethod.setBody(h);
			}

			methodSet.add(zMethod);
		}
		zClass.setMethodSet(methodSet);

		return zClass;

	}

	/**
	 * Generate method body has parameter.
	 *
	 * @param method the method
	 * @param argList the arg list
	 * @param remoteMethodAnnotationValue the remote method annotation value
	 * @return the string
	 */
	private static String generateMethodBodyHasParameter(final Method method, final List<String> argList,
			final String remoteMethodAnnotationValue) {
		final String returnTypeCanonicalName = method.getReturnType().getCanonicalName();

		final String g = generateMethodProtocolHasParameter(method, argList, remoteMethodAnnotationValue);
		final boolean isMethodAsync = isMethodAsync(method);
		final boolean isMethodVOID = ZRPCRemoteMethodAnnotationBeanGenerator.VOID.equals(returnTypeCanonicalName.toLowerCase());

		final String mis = generateMethodBody_ZMethodInfoSource(method, isMethodAsync, isMethodVOID);
		final String b = g + mis;

		if (isMethodVOID) {
			return b + "this.zr_RMIH.invoke(zrpe," + isMethodAsync + ");";
		}

		checkIsAsync(method.getName(), isMethodAsync);

		final String b3 = b  +
				" try { " +  ZClass.NEW_LINE +
					" final Object rv = this.zr_RMIH.invoke(zrpe,false);" +  ZClass.NEW_LINE +
					" final byte[] rvBA = (byte[])rv;" +  ZClass.NEW_LINE +
					" " + returnTypeCanonicalName + " rv2 = " + ZRPCRemoteMethodAnnotationBeanGenerator.ZPU.getCanonicalName() + ".deserialize(rvBA, " + returnTypeCanonicalName +".class);" +  ZClass.NEW_LINE +
					" return rv2;" +  ZClass.NEW_LINE +
				" } catch (Exception e) { " +  ZClass.NEW_LINE +
					"throw e; " +  ZClass.NEW_LINE +
				" } " +  ZClass.NEW_LINE +
				" return null;";

		return b3;
	}

	/**
	 * Check is async.
	 *
	 * @param methodName the method name
	 * @param isMethodAsync the is method async
	 */
	private static void checkIsAsync(final String methodName, final boolean isMethodAsync) {
		if (isMethodAsync) {
			final String m = "带有返回值的方法不支持使用 @" + ZRPCRemoteMethodAnnotationBeanGenerator.ZRPCASYNC.getCanonicalName() + " 异步执行! 方法名=" + methodName;
			throw new IllegalArgumentException(m);
		}
	}

	/**
	 * Generate method protocol has parameter.
	 *
	 * @param method the method
	 * @param argList the arg list
	 * @param remoteMethodAnnotationValue the remote method annotation value
	 * @return the string
	 */
	private static String generateMethodProtocolHasParameter(final Method method, final List<String> argList,
			final String remoteMethodAnnotationValue) {

		final String a = argList.stream()
			   .collect(Collectors.joining(","));

		final Map<Integer, Object> map = new HashMap<>(2, 1F);
		final boolean dt = method.isAnnotationPresent(ZRPCRemoteDT.class);
		if (dt) {
			map.put(ZRPCProtocolAttachEnum.DISTRIBUTED_TRANSACTION.getAttachType(), 1);
			final String body = " final ZRPCProtocol zrpe = ZRPCProtocol.builder()" + ZClass.NEW_LINE +
					".serviceName(\"" + remoteMethodAnnotationValue + "\")" +  ZClass.NEW_LINE +
					".id(ZIDG.generateId())" +  ZClass.NEW_LINE +
					".type(ZRPETE.INVOEK.getType())" + ZClass.NEW_LINE +
					".attachMap(ZRPCProtocol.dt())" +  ZClass.NEW_LINE +
					".name(\"" + method.getName() + "\")" +  ZClass.NEW_LINE +
					".args(new com.vo.common.ZArrayList(" + a+ "))" +  ZClass.NEW_LINE +
					".build();" + ZClass.NEW_LINE;

			final String zrpe = body;

			return zrpe;
		}

		final String body = " final ZRPCProtocol zrpe = ZRPCProtocol.builder()" + ZClass.NEW_LINE +
				".serviceName(\"" + remoteMethodAnnotationValue + "\")" +  ZClass.NEW_LINE +
				".id(ZIDG.generateId())" +  ZClass.NEW_LINE +
				".type(ZRPETE.INVOEK.getType())" + ZClass.NEW_LINE +
				".name(\"" + method.getName() + "\")" +  ZClass.NEW_LINE +
				".args(new com.vo.common.ZArrayList(" + a+ "))" +  ZClass.NEW_LINE +
				".build();" + ZClass.NEW_LINE;

		final String zrpe = body;

		return zrpe;
	}

	/**
	 * Generate method body has no parameter.
	 *
	 * @param method the method
	 * @param remoteMethodAnnotationValue the remote method annotation value
	 * @return the string
	 */
	private static String generateMethodBodyHasNoParameter(final Method method, final String remoteMethodAnnotationValue) {

		final String returnTypeCanonicalName = method.getReturnType().getCanonicalName();
		final boolean isMethodVOID = ZRPCRemoteMethodAnnotationBeanGenerator.VOID.equals(returnTypeCanonicalName.toLowerCase());
		final boolean isMethodAsync = isMethodAsync(method);

		final String g = generateMethodProtocolHasNoParameter(method, remoteMethodAnnotationValue);
		final String mis = generateMethodBody_ZMethodInfoSource(method, isMethodAsync, isMethodVOID);

		final String b = g + mis;

		if (isMethodVOID) {
			final String b2 = b + " this.zr_RMIH.invoke(zrpe," + isMethodAsync + ");";
			return b2;
		}

		checkIsAsync(method.getName(), isMethodAsync);

		final String b3 = b  +
				" try { " +  ZClass.NEW_LINE +
					" final Object rv = this.zr_RMIH.invoke(zrpe,false);" +  ZClass.NEW_LINE +
					" final byte[] rvBA = (byte[])rv;" +  ZClass.NEW_LINE +
					" " + returnTypeCanonicalName + " rv2 = " + ZRPCRemoteMethodAnnotationBeanGenerator.ZPU.getCanonicalName() + ".deserialize(rvBA, " + returnTypeCanonicalName +".class);" +  ZClass.NEW_LINE +
					" return rv2;" +  ZClass.NEW_LINE +
				" } catch (Exception e) { " +  ZClass.NEW_LINE +
					"throw e; " +  ZClass.NEW_LINE +
				" } " +  ZClass.NEW_LINE +
				" return null;";

		return b3;
	}

	/**
	 * Generate method body Z method info source.
	 *
	 * @param method the method
	 * @param isMethodAsync the is method async
	 * @param isMethodVOID the is method VOID
	 * @return the string
	 */
	public static String generateMethodBody_ZMethodInfoSource(final Method method, final boolean isMethodAsync,
			final boolean isMethodVOID) {

		final Class<? extends ZRPCAsyncAfterReturn>[] methodAfterReturn = getMethodAfterReturn(method);
		if (ArrayUtil.isEmpty(methodAfterReturn)) {
			// 没有返回后要执行的操作，不需要生成下面的代码
			return "";
		}

		final ArrayList<Class<? extends ZRPCAsyncAfterReturn>> arList = Lists.newArrayList(methodAfterReturn);
		final String afterReturn = arList.stream().map(c -> c.getCanonicalName())
				.collect(Collectors.joining(ZRPCRemoteMethodAnnotationBeanGenerator.AFTER_RETURN_DELIMITER, "\"", "\""));

		final String mis =

					" ZMethodInfo mi = ZMethodInfo.builder()" +  ZClass.NEW_LINE +
							" .requestId(zrpe.getId())" + ZClass.NEW_LINE +
							" .name(\"" + method.getName() + "\")" + ZClass.NEW_LINE +
							" .isAsync(" + isMethodAsync + ")" +  ZClass.NEW_LINE +
							" .isVoid(" + isMethodVOID + ")" +  ZClass.NEW_LINE +
							" .afterReturn(" + afterReturn + ")" +  ZClass.NEW_LINE +
							" .build();"+  ZClass.NEW_LINE +
					" ZMethodAysncResultCache.put(zrpe.getId(), mi);";

		return mis;
	}

	/**
	 * Gets the method after return.
	 *
	 * @param method the method
	 * @return the method after return
	 */
	private static Class<? extends ZRPCAsyncAfterReturn>[] getMethodAfterReturn(final Method method) {
		final ZRPCAsync a = method.getDeclaredAnnotation(ZRPCRemoteMethodAnnotationBeanGenerator.ZRPCASYNC);
		if (Objects.isNull(a)) {
			return new Class[] {};
		}

		final Class<? extends ZRPCAsyncAfterReturn>[] afterReturn = a.afterReturn();
		return afterReturn;
	}

	/**
	 * Checks if is method async.
	 *
	 * @param method the method
	 * @return true, if is method async
	 */
	private static boolean isMethodAsync(final Method method) {
		final ZRPCAsync aa = method.getDeclaredAnnotation(ZRPCRemoteMethodAnnotationBeanGenerator.ZRPCASYNC);
		return Objects.nonNull(aa);
	}

	/**
	 * Generate method protocol has no parameter.
	 *
	 * @param method the method
	 * @param remoteMethodAnnotationValue the remote method annotation value
	 * @return the string
	 */
	private static String generateMethodProtocolHasNoParameter(final Method method, final String remoteMethodAnnotationValue) {

		final Map<Integer, Object> map = new HashMap<>(2, 1F);
		final boolean dt = method.isAnnotationPresent(ZRPCRemoteDT.class);
		if (dt) {
			map.put(ZRPCProtocolAttachEnum.DISTRIBUTED_TRANSACTION.getAttachType(), 1);
			final String body =
					" final ZRPCProtocol zrpe = ZRPCProtocol.builder()" +  ZClass.NEW_LINE +
					".serviceName(\"" + remoteMethodAnnotationValue + "\")" +  ZClass.NEW_LINE +
					".id(ZIDG.generateId())" +  ZClass.NEW_LINE +
					".type(ZRPETE.INVOEK.getType())" +  ZClass.NEW_LINE +
					".attachMap(ZRPCProtocol.dt())" +  ZClass.NEW_LINE +
					".name(\"" + method.getName() + "\")" +  ZClass.NEW_LINE +
					".build();" + ZClass.NEW_LINE;
			return body;
		}

		final String body =
				" final ZRPCProtocol zrpe = ZRPCProtocol.builder()" +  ZClass.NEW_LINE +
				".serviceName(\"" + remoteMethodAnnotationValue + "\")" +  ZClass.NEW_LINE +
				".id(ZIDG.generateId())" +  ZClass.NEW_LINE +
				".type(ZRPETE.INVOEK.getType())" +  ZClass.NEW_LINE +
				".name(\"" + method.getName() + "\")" +  ZClass.NEW_LINE +
				".build();" + ZClass.NEW_LINE;
		return body;

	}

}
