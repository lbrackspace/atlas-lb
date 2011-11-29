package org.openstack.atlas.api.helper;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class MethodLoggingInterceptor {
    protected final Logger LOG = Logger.getLogger(MethodLoggingInterceptor.class);

    @Pointcut("within(org.openstack.atlas.api.resource..*)")
    public void resources() {
    }

    @Pointcut("within(org.openstack.atlas.service.domain.service..*)")
    public void services() {
    }

    @Pointcut("within(org.openstack.atlas.adapter..*)")
    public void adapters() {
    }

    @Around("execution(!private * org.openstack.atlas.api.resource.*.*(..))")
    public Object profileResource(ProceedingJoinPoint pjp) throws Throwable {
        return timerLog(pjp);
    }

    @Around("execution(!private * org.openstack.atlas.service.domain.service.*.*(..))")
    public Object profileService(ProceedingJoinPoint pjp) throws Throwable {
        return timerLog(pjp);
    }

    @Around("execution(!private * org.openstack.atlas.adapter.*.*(..))")
    public Object profileAdapter(ProceedingJoinPoint pjp) throws Throwable {
        return timerLog(pjp);
    }

    private Object timerLog(ProceedingJoinPoint pjp) throws Throwable {
        String methodName = pjp.getSignature().getName();
        String className = pjp.getTarget().getClass().getName();

        // retrieve the runtime method arguments (dynamic)
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        Object[] args = pjp.getArgs();
        for (Object arg : args) {
            if (arg == null) continue;
            String fullParameterClassName = arg.getClass().getName();
            String parameterClassName = fullParameterClassName.substring(fullParameterClassName.lastIndexOf(".") +1, fullParameterClassName.length());
            sb.append(parameterClassName).append(" ").append(arg);
            sb.append(", ");
        }
        String sbString = sb.toString();
        String arguments = null;
        if(sb.toString().length() >= 3) {
            arguments = sbString.substring(0, sbString.length()-2);
            arguments = arguments + ")";
        } else {
            arguments = sbString + ")";
        }

        long start = System.currentTimeMillis();
        LOG.debug("Entering: " + className + "." + methodName + arguments);

        // retrieve the methods parameter types (static):
        final Signature signature = pjp.getStaticPart().getSignature();
        if (signature instanceof MethodSignature) {
            final MethodSignature ms = (MethodSignature) signature;
            final Class<?>[] parameterTypes = ms.getParameterTypes();
            for (final Class<?> pt : parameterTypes) {
                LOG.debug("Parameter type:" + pt);
            }
        }


        Object output = pjp.proceed();
        long elapsedTime = System.currentTimeMillis() - start;
        LOG.debug("Leaving: " + className + "." + methodName + arguments + " Total Time taken in ms: " + elapsedTime);
        return output;
    }


}
