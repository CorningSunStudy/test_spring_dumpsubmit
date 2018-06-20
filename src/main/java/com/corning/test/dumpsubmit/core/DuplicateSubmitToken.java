package com.corning.test.dumpsubmit.core;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface DuplicateSubmitToken {

    /**
     * 保存重复提交标记 默认为需要保存
     */
    boolean save() default true;

    /**
     * 超时时间：默认5秒
     */
    long timeout() default 5000;

}
