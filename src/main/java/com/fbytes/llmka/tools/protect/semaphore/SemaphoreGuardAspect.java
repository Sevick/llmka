package com.fbytes.llmka.tools.protect.semaphore;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
public class SemaphoreGuardAspect {

    private final ConcurrentHashMap<String, Semaphore> semaphoreMap = new ConcurrentHashMap<>();

    @Around("@annotation(semaphoreGuard)")
    public Object applySemaphore(ProceedingJoinPoint joinPoint, SemaphoreGuard semaphoreGuard) throws Throwable {
        int limit = semaphoreGuard.limit();
        String methodName = joinPoint.getSignature().toShortString();

        Semaphore semaphore = semaphoreMap.computeIfAbsent(methodName, key -> new Semaphore(limit));

        semaphore.acquire();
        try {
            return joinPoint.proceed();
        } finally {
            semaphore.release();
        }
    }
}
