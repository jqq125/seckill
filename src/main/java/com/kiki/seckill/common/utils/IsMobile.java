package com.kiki.seckill.common.utils;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })  //用于描述注解的使用范围
@Retention(RUNTIME)  //表示需要在什么级别保存该注解信息，用于描述注解的生命周期
@Documented  //文档注释:说明该注解将被包含在javadoc中
@Constraint(validatedBy = { IsMobileValidator.class })//限定自定义注解的方法,继承校验器
public @interface IsMobile {
    boolean required() default true;
    String message() default "手机号码格式有误!";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}