package com.corning.test.dumpsubmit.core;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
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
                        String key = getDuplicateTokenKey(joinPoint);
                        if (session.getAttribute(key) == null) {
                            session.setAttribute(key, UUID.randomUUID().toString());
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
                        String key = getDuplicateTokenKey(joinPoint);

                        if (session.getAttribute(key) != null) {
                            session.removeAttribute(key);
                            log.debug("方法执行完毕移除请求重复标记！");
                        }
                    });
        }
    }


    /**
     * 异常
     *
     * @param joinPoint
     * @param e
     */
    @AfterThrowing(pointcut = "controllerPointCut() && @annotation(token)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, DuplicateSubmitToken token, Throwable e) {
        if (token != null && token.save() && !(e instanceof DumplicateSubmitException)) {
            // 处理处理重复提交本身之外的异常

            Arrays.stream(joinPoint.getArgs())
                    .filter(arg -> arg instanceof HttpServletRequest)
                    .findFirst()
                    .ifPresent(request -> {
                        HttpSession session = ((HttpServletRequest) request).getSession(false);
                        String key = getDuplicateTokenKey(joinPoint);

                        if (session.getAttribute(key) != null && token.type() == DuplicateSubmitToken.REQUEST) {
                            session.removeAttribute(key);
                            log.debug("异常情况--移除请求重复标记！");
                        }
                    });
        }
    }

    /**
     * 获取重复提交key-->duplicate_token_key+','+请求方法名
     *
     * @param joinPoint
     * @return
     */
    public String getDuplicateTokenKey(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        StringBuilder key = new StringBuilder(DUPLICATE_TOKEN_KEY);
        key.append(",").append(methodName);
        return key.toString();
    }

}
