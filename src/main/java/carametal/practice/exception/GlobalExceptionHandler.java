package carametal.practice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation error occurred: {}", ex.getMessage());
        
        List<Map<String, String>> fieldErrors = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            Map<String, String> fieldError = new HashMap<>();
            fieldError.put("field", error.getField());
            fieldError.put("message", error.getDefaultMessage());
            fieldError.put("error_code", getErrorCode(error));
            fieldErrors.add(fieldError);
        });
        
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.put("error_message", errorMessage);
        response.put("field_errors", fieldErrors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    private String getErrorCode(FieldError error) {
        String field = error.getField();
        String code = error.getCode();
        
        if ("username".equals(field)) {
            if ("NotBlank".equals(code)) {
                return ErrorCode.USER_REGISTER_USERNAME_REQUIRED.getCode();
            } else if ("Size".equals(code)) {
                return ErrorCode.USER_REGISTER_USERNAME_SIZE.getCode();
            }
        } else if ("email".equals(field)) {
            if ("NotBlank".equals(code)) {
                return ErrorCode.USER_REGISTER_EMAIL_REQUIRED.getCode();
            } else if ("Email".equals(code)) {
                return ErrorCode.USER_REGISTER_EMAIL_INVALID.getCode();
            } else if ("Size".equals(code)) {
                return ErrorCode.USER_REGISTER_EMAIL_SIZE.getCode();
            }
        } else if ("password".equals(field)) {
            if ("NotBlank".equals(code)) {
                return ErrorCode.USER_REGISTER_PASSWORD_REQUIRED.getCode();
            } else if ("Size".equals(code)) {
                return ErrorCode.USER_REGISTER_PASSWORD_SIZE.getCode();
            }
        }
        
        return "UNKNOWN_ERROR";
    }
}