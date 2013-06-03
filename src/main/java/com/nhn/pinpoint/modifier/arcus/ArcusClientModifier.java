package com.nhn.pinpoint.modifier.arcus;

import java.security.ProtectionDomain;

import com.nhn.pinpoint.interceptor.bci.Type;
import com.nhn.pinpoint.logging.Logger;

import com.nhn.pinpoint.Agent;
import com.nhn.pinpoint.interceptor.Interceptor;
import com.nhn.pinpoint.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.logging.LoggerFactory;
import com.nhn.pinpoint.modifier.AbstractModifier;

/**
 * @author netspider
 */
public class ArcusClientModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(ArcusClientModifier.class.getName());

    public ArcusClientModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "net/spy/memcached/ArcusClient";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. " + javassistClassName);
        }

        try {
            InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);

            Interceptor setCacheManagerInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.modifier.arcus.interceptors.SetCacheManagerInterceptor");
            aClass.addInterceptor("setCacheManager", new String[]{"net.spy.memcached.CacheManager"}, setCacheManagerInterceptor,  Type.before);

            return aClass.toBytecode();
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn( e.getMessage(), e);
            }
            return null;
        }
    }
}