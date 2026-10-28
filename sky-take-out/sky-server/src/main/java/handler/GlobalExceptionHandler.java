package handler;

import constant.MessageConstant;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import exception.BusinessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import result.Result;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Result> authentication(AuthenticationException exception) {
        String message = exception instanceof DisabledException
                ? MessageConstant.ACCOUNT_LOCKED : "用户名或密码错误";
        return ResponseEntity.status(401).body(Result.error(message));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result> validation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage()).findFirst().orElse("请求参数错误");
        return ResponseEntity.badRequest().body(Result.error(message));
    }
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result> constraintViolation(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations().stream()
                .map(violation -> violation.getMessage()).findFirst().orElse("请求参数错误");
        return ResponseEntity.badRequest().body(Result.error(message));
    }
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Result> methodValidation(HandlerMethodValidationException exception) {
        String message = exception.getAllErrors().stream()
                .map(error -> error.getDefaultMessage()).findFirst().orElse("请求参数错误");
        return ResponseEntity.badRequest().body(Result.error(message));
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result> invalidJson(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest().body(Result.error("请求体格式错误"));
    }
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result> business(BusinessException exception) {
        return ResponseEntity.badRequest().body(Result.error(exception.getMessage()));
    }
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Result> duplicate(DuplicateKeyException exception) {
        String message = "数据已存在，请勿重复添加";
        String detail = exception.getMostSpecificCause().getMessage();
        if (detail != null) {
            if (detail.contains("uk_dish_name")) {
                message = "菜品名称已存在";
            } else if (detail.contains("uk_employee_username")) {
                message = "用户名已存在";
            } else if (detail.contains("uk_category_name")) {
                message = "分类名称已存在";
            }
        }
        return ResponseEntity.status(409).body(Result.error(message));
    }
}
