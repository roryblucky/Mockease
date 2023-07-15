package com.rory.apimock.utils;

import cn.hutool.core.lang.Singleton;
import com.rory.apimock.dto.web.ProblemDetails;
import com.rory.apimock.exceptions.ValidationException;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BeanValidationUtil {


    private final Validator validator;

    private BeanValidationUtil() {
        try(ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            this.validator = validatorFactory.getValidator();
        }
    }
    public static BeanValidationUtil getInstance() {
        return Singleton.get(BeanValidationUtil.class);
    }

    public <T> Future<T> validate(T bean) {
        Promise<T> promise = Promise.promise();
        Set<ConstraintViolation<Object>> constraintViolations = this.validator.validate(bean);
        if(!constraintViolations.isEmpty()) {
            List<ProblemDetails.ErrorDetail> errorDetails = constraintViolations.stream()
                .map(error -> new ProblemDetails.ErrorDetail(error.getPropertyPath().toString(), error.getMessage()))
                .collect(Collectors.toList());
            promise.fail(new ValidationException("Fields Validation Error", errorDetails));
        } else {
            promise.complete(bean);
        }
        return promise.future();
    }

}
