package com.fbytes.llmka.config.profiles.merics;

import io.micrometer.core.instrument.MeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.lang.reflect.Parameter;

@Aspect
@Profile("metrics")
@Component
public class ParamTimedMetricAspect {

    private final MeterRegistry meterRegistry;

    public ParamTimedMetricAspect(@Autowired MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Around("@annotation(paramTimedMetric)") // Intercepts methods annotated with @TimedMetric
    public Object recordMetrics(ProceedingJoinPoint joinPoint, ParamTimedMetric paramTimedMetric) throws Throwable {
        // Extract the method and class details
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringTypeName(); // Fully qualified class name
        String methodName = signature.getName();            // Method name
        String metricName = className + "." + methodName;

        // Extract the parameter name specified in @TimedMetric.key
        String targetParameterName = paramTimedMetric.key();
        Object[] args = joinPoint.getArgs();
        Parameter[] parameters = signature.getMethod().getParameters();

        // Find the value of the parameter specified in @TimedMetric
        String targetParameterValue = null; // Default value if parameter is not found
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getName().equals(targetParameterName)) {
                targetParameterValue = args[i].toString(); // Extract the value
                break;
            }
        }
        if (targetParameterValue == null) {
            throw new RuntimeException("TimedMetric is configured with wrong parameter name");
        }

        // Record the timer metric with dynamic name and tags
        return meterRegistry.timer(
                metricName,
                "key", targetParameterValue
        ).record(() -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        });
    }
}
