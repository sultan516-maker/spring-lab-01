package kz.iitu.springlab.aspect;

import kz.iitu.springlab.audit.Sensitive;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Aspect
@Component
@Order(0)
public class ArgumentMaskingAspect {

    private static final Logger log = LoggerFactory.getLogger(ArgumentMaskingAspect.class);

    @Before("kz.iitu.springlab.aspect.Pointcuts.serviceOperation()")
    public void maskSensitiveArgs(JoinPoint jp) {
        MethodSignature signature = (MethodSignature) jp.getSignature();
        Method method = signature.getMethod();
        Object[] args = jp.getArgs();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        List<Object> maskedArgs = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            boolean isSensitive = false;
            if (parameterAnnotations.length > i) {
                for (Annotation annotation : parameterAnnotations[i]) {
                    if (annotation instanceof Sensitive) {
                        isSensitive = true;
                        break;
                    }
                }
            }

            if (isSensitive) {
                maskedArgs.add("***");
            } else {
                maskedArgs.add(args[i]);
            }
        }

        log.info("[MASK] -> Method {} called with args: {}", method.getName(), maskedArgs);
    }
}