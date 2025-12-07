package com.tennispulse.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class MethodLoggingAspect {

    /**
     * Match all public methods in:
     * - any package containing ".controller."
     * - any package containing ".service."
     * under com.tennispulse..
     */
    @Around("execution(public * com.tennispulse..api..*(..)) || " +
            "execution(public * com.tennispulse..service..*(..))")
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {

        // Avoid string building overhead if TRACE is off
        if (!log.isTraceEnabled()) {
            return pjp.proceed();
        }

        String methodSignature = pjp.getSignature().toShortString();
        Object[] args = pjp.getArgs();

        log.trace("Entering {} with args={}", methodSignature, Arrays.toString(args));

        try {
            Object result = pjp.proceed();
            log.trace("Exiting {} with result={}", methodSignature, result);
            return result;
        } catch (Throwable ex) {
            log.trace("Exception in {}: {}", methodSignature, ex.toString());
            throw ex;
        }
    }
}
