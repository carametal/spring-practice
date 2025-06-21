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
            
            String errorCode = getErrorCode(error);
            ErrorCode errorCodeEnum = ErrorCode.findByCode(errorCode);
            String message = errorCodeEnum != null ? errorCodeEnum.getMessage() : error.getDefaultMessage();
            
            fieldError.put("message", message);
            fieldError.put("error_code", errorCode);
            fieldErrors.add(fieldError);
        });
        
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> {
                    String errorCode = getErrorCode(error);
                    ErrorCode errorCodeEnum = ErrorCode.findByCode(errorCode);
                    String message = errorCodeEnum != null ? errorCodeEnum.getMessage() : error.getDefaultMessage();
                    return error.getField() + ": " + message;
                })
                .collect(Collectors.joining(", "));
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.put("error_message", errorMessage);
        response.put("field_errors", fieldErrors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    private String getErrorCode(FieldError error) {
        String defaultMessage = error.getDefaultMessage();
        
        if (defaultMessage != null && defaultMessage.startsWith("{") && defaultMessage.endsWith("}")) {
            String errorCodeStr = defaultMessage.substring(1, defaultMessage.length() - 1);
            ErrorCode errorCode = ErrorCode.findByCode(errorCodeStr);
            return errorCode != null ? errorCode.getCode() : "UNKNOWN_ERROR";
        }
        
        return "UNKNOWN_ERROR";
    }
}