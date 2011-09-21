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
    public void inResources() {
    }

    @Around("execution(!private * org.openstack.atlas.api.resource.*.*(..))")
    public Object profile(ProceedingJoinPoint pjp) throws Throwable {
        String methodName = pjp.getSignature().getName();
        String className = pjp.getTarget().getClass().getName();

        // retrieve the runtime method arguments (dynamic)
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        java.lang.Object[] args = pjp.getArgs();
        for (Object arg : args) {
            sb.append(arg.getClass().getName()).append(" ").append(arg);
            sb.append(", ");
        }
        sb.append(")");

        long start = System.currentTimeMillis();
        LOG.debug("Entering: " + className + "." + methodName + sb.toString());

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
        LOG.debug("Leaving: " + className + "." + methodName + sb.toString() + " Total Time taken in ms: " + elapsedTime);
        return output;
    }

    /* public Object invoke(MethodInvocation method) throws Throwable {
        String classMethodIdentifier = method.getMethod().getDeclaringClass() + "." + method.getMethod().getName();
        LOG.debug("Entering: " + classMethodIdentifier);
        long start = System.currentTimeMillis();
        Object result = method.proceed();
        long diff = System.currentTimeMillis() - start;;
        LOG.debug("Leaving: " + classMethodIdentifier + "Total Time taken in ms: " + diff);
        return result;
    }*/

    /*@Around("inResources()")
    public Object doBasicProfiling(ProceedingJoinPoint pjp) throws Throwable {
    // start stopwatch

    Object retVal = pjp.proceed();
    // stop stopwatch
    return retVal;
    }*/
}
