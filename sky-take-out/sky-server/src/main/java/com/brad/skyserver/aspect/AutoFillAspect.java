package com.brad.skyserver.aspect;

import annotation.AutoFill;
import constant.AutoSetConstant;
import context.BaseContext;
import enumeration.OperationType;
import exception.BusinessException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
public class AutoFillAspect {
    @Before(value = "execution(* mapper.*.*(..)) && @annotation(autoFill)",
            argNames = "joinPoint,autoFill")
    public void autoFill(JoinPoint joinPoint, AutoFill autoFill) {
        Object[] args = joinPoint.getArgs();
        if (args.length == 0 || args[0] == null) {
            throw new IllegalArgumentException("自动填充方法必须传入实体");
        }
        Object entity = args[0];
        Long currentId = BaseContext.getUserId();
        if (currentId == null) throw new BusinessException("当前用户未登录");
        LocalDateTime now = LocalDateTime.now();
        setValue(entity, AutoSetConstant.SET_UPDATE_TIME, LocalDateTime.class, now);
        setValue(entity, AutoSetConstant.SET_UPDATE_USER, Long.class, currentId);
        if (autoFill.value() == OperationType.INSERT) {
            setValue(entity, AutoSetConstant.SET_CREATE_TIME, LocalDateTime.class, now);
            setValue(entity, AutoSetConstant.SET_CREATE_USER, Long.class, currentId);
        }
    }

    private void setValue(Object entity, String methodName, Class<?> type, Object value) {
        try {
            Method setter = entity.getClass().getMethod(methodName, type);
            setter.invoke(entity, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("公共字段自动填充失败：" + methodName, exception);
        }
    }
}
