package com.rory.apimock.utils;

import cn.hutool.core.lang.Singleton;
import com.rory.apimock.exceptions.ValidationException;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.hibernate.validator.internal.engine.ValidatorFactoryImpl;
import org.hibernate.validator.internal.engine.ValidatorImpl;

import java.util.Set;

public class BeanValidationUtil {


    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private BeanValidationUtil() {

    }
    public static BeanValidationUtil getInstance() {
        return Singleton.get(BeanValidationUtil.class);
    }

    public <T> Future<T> validate(T bean) {
        Promise<T> promise = Promise.promise();
        Set<ConstraintViolation<Object>> constraintViolations = this.validator.validate(bean);
        if(!constraintViolations.isEmpty()) {
            promise.fail(new ValidationException(constraintViolations));
        } else {
            promise.complete(bean);
        }
        return promise.future();
    }

}
