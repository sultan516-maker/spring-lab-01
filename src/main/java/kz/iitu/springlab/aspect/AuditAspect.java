package kz.iitu.springlab.aspect;

import kz.iitu.springlab.audit.Audited;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;

@Aspect
@Component
@Order(1)
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    @Around("@annotation(audited)")
    public Object audit(ProceedingJoinPoint pjp, Audited audited) throws Throwable {
        String action = audited.action();
        String args = audited.logArguments() ? " args=" + Arrays.toString(pjp.getArgs()) : "";

        log.info("[AUDIT] start {} at {}{}", action, Instant.now(), args);

        try {
            Object result = pjp.proceed();
            log.info("[AUDIT] {} success", action);
            return result;
        } catch (Throwable ex) {
            log.error("[AUDIT] {} failure: {}", action, ex.getMessage());
            throw ex;
        }
    }
}