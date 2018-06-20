package com.corning.test.dumpsubmit.core;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.UUID;

@Aspect
@Component
@Slf4j
public class DuplicateSubmitAspect {

    public static final String DUPLICATE_TOKEN_KEY = "duplicate_token_key";

    @Pointcut("execution(public * com.corning.test.dumpsubmit.controller..*.*(..))")
    public void controllerPointCut() {
    }

    @Before("controllerPointCut() && @annotation(token)")
    public void before(final JoinPoint joinPoint, DuplicateSubmitToken token) {
        if (token != null && token.save()) {
            Arrays.stream(joinPoint.getArgs())
                    .filter(arg -> arg instanceof HttpServletRequest)
                    .findFirst()
                    .ifPresent(request -> {
                        HttpSession session = ((HttpServletRequest) request).getSession();
                        if (session.getAttribute(DUPLICATE_TOKEN_KEY) == null) {
                            session.setAttribute(DUPLICATE_TOKEN_KEY, UUID.randomUUID().toString());
                            log.debug("方法开始执行，添加token。");
                        } else {
                            throw new DumplicateSubmitException("请不要重复请求！");
                        }
                    });
        }
    }

    @AfterReturning("controllerPointCut() && @annotation(token)")
    public void doAfterReturning(JoinPoint joinPoint, DuplicateSubmitToken token) {
        if (token != null && token.save()) {
            Arrays.stream(joinPoint.getArgs())
                    .filter(arg -> arg instanceof HttpServletRequest)
                    .findFirst()
                    .ifPresent(request -> {
                        HttpSession session = ((HttpServletRequest) request).getSession(false);
                        if (session.getAttribute(DUPLICATE_TOKEN_KEY) != null) {
                            session.removeAttribute(DUPLICATE_TOKEN_KEY);
                            log.debug("方法执行完毕移除请求重复标记！");
                        }
                    });
        }
    }


}
