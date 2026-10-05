package kz.iitu.springlab.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(2)
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Before("kz.iitu.springlab.aspect.Pointcuts.serviceOperation()")
    public void before(JoinPoint jp) {
        log.info("[LOG] -> {}", jp.getSignature().toShortString());
    }

    @AfterReturning(value = "kz.iitu.springlab.aspect.Pointcuts.serviceOperation()", returning = "result")
    public void afterReturning(JoinPoint jp, Object result) {
        log.info("[LOG] <- {} returned {}",
                jp.getSignature().toShortString(),
                result);
    }

    @AfterThrowing(value = "kz.iitu.springlab.aspect.Pointcuts.serviceOperation()", throwing = "ex")
    public void afterThrowing(JoinPoint jp, Throwable ex) {
        log.error("[LOG] <- {} thrown {}: {}",
                jp.getSignature().toShortString(),
                ex.getClass().getSimpleName(),
                ex.getMessage());
    }
}