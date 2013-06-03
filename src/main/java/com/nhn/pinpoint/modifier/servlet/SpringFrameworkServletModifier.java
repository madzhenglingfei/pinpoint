package com.nhn.pinpoint.modifier.servlet;

import java.security.ProtectionDomain;
import com.nhn.pinpoint.logging.Logger;
import com.nhn.pinpoint.logging.LoggerFactory;

import com.nhn.pinpoint.Agent;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.interceptor.Interceptor;
import com.nhn.pinpoint.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.interceptor.bci.InstrumentException;
import com.nhn.pinpoint.modifier.AbstractModifier;

/**
 * 
 * @author netspider
 * 
 */
public class SpringFrameworkServletModifier extends AbstractModifier {

	private final Logger logger = LoggerFactory.getLogger(SpringFrameworkServletModifier.class);

	public SpringFrameworkServletModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
	}

	public String getTargetClass() {
		return "org/springframework/web/servlet/FrameworkServlet";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isInfoEnabled()) {
			logger.info("Modifing. {}", javassistClassName);
		}

		byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);

		try {
			Interceptor doGetInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.modifier.servlet.MethodInterceptor");
            setServiceType(doGetInterceptor, ServiceType.SPRING_MVC);

            Interceptor doPostInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.modifier.servlet.MethodInterceptor");
            setServiceType(doPostInterceptor, ServiceType.SPRING_MVC);



			InstrumentClass servlet = byteCodeInstrumentor.getClass(javassistClassName);
			servlet.addInterceptor("doGet", new String[] { "javax.servlet.http.HttpServletRequest", "javax.servlet.http.HttpServletResponse" }, doGetInterceptor);

			servlet.addInterceptor("doPost", new String[] { "javax.servlet.http.HttpServletRequest", "javax.servlet.http.HttpServletResponse" }, doPostInterceptor);

			return servlet.toBytecode();
		} catch (InstrumentException e) {
			logger.warn("modify fail. Cause:" + e.getMessage(), e);
			return null;
		}
	}
}