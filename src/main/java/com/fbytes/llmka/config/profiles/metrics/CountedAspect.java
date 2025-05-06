package com.fbytes.llmka.config.profiles.metrics;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.instrument.MeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class CountedAspect {
    private final MeterRegistry meterRegistry;

    public CountedAspect(@Autowired MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Around("@annotation(counted)")
    public Object around(ProceedingJoinPoint pjp, Counted counted) throws Throwable {
        meterRegistry.counter(counted.value()).increment();
        return pjp.proceed();
    }
}
