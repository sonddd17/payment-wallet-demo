package com.sonddd.paywallet.exception;

import com.sonddd.paywallet.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Xử lý ngoại lệ tập trung cho toàn bộ REST API.
 * Chuyển các exception thành {@link ErrorResponse} với mã HTTP phù hợp,
 * nhờ đó controller không phải lặp lại try/catch.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficient(InsufficientBalanceException ex, HttpServletRequest req) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), req);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler({BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "Sai tên đăng nhập hoặc mật khẩu", req);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "Bạn không có quyền truy cập tài nguyên này", req);
    }

    /** Lỗi validation của @Valid: gom các lỗi theo tên trường. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Dữ liệu không hợp lệ",
                req.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity.badRequest().body(body);
    }

    /** URL không tồn tại (không khớp controller/tài nguyên tĩnh) -> 404. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "Không tìm thấy đường dẫn: " + req.getRequestURI(), req);
    }

    /** Lưới an toàn cuối cùng cho các lỗi chưa lường trước -> 500. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "Đã có lỗi xảy ra: " + ex.getMessage(), req);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest req) {
        ErrorResponse body = ErrorResponse.of(status.value(), status.getReasonPhrase(), message, req.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
