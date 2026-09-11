package com.agh.polymorphia_backend.exception;

import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

@ControllerAdvice
@Order(2)
@Slf4j
public class GeneralExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler({MethodArgumentTypeMismatchException.class, ConstraintViolationException.class, InvalidTypeIdException.class})
    public ResponseEntity<ProblemDetail> handleArgumentNotValidException(Exception ex) {
        log.error(ex.getMessage());
        return getResponseEntity(HttpStatus.BAD_REQUEST, "Niepoprawny typ parametru.");
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
                                                                          HttpHeaders headers,
                                                                          HttpStatusCode status,
                                                                          WebRequest request) {
        log.error(ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Brakujący parametr requestu: " + ex.getParameterName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {

        List<String> errorMessages = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::mapErrorToMessage)
                .toList();

        HttpStatus returnStatus = HttpStatus.BAD_REQUEST;
        String detail = "Walidacja się nie powiodła: " + String.join(", ", errorMessages) + ".";
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(returnStatus, detail);

        return ResponseEntity.status(returnStatus).body(problemDetail);
    }

    private String mapErrorToMessage(FieldError error) {
        String code = error.getCode();
        if (code == null) {
            return error.getDefaultMessage();
        }

        return switch (code) {
            case "NotNull" -> String.format("pole \"%s\" jest wymagane", error.getField());
            case "NotBlank" -> String.format("pole \"%s\" nie może być puste", error.getField());
            case "Email" -> "nieprawidłowy adres email";
            default -> error.getDefaultMessage();
        };
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex) {
        return getResponseEntity(HttpStatus.FORBIDDEN, "Brak uprawnień.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex, HttpServletRequest request) throws Exception {
        Object handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);

        if (handler instanceof HandlerMethod handlerMethod) {
            if (handlerMethod.hasMethodAnnotation(SkipUnexpectedExceptionHandler.class)) {
                throw ex;
            }
            if (handlerMethod.getBeanType().isAnnotationPresent(SkipUnexpectedExceptionHandler.class)) {
                throw ex;
            }
        }

        log.error("Unexpected error: ", ex);
        return getResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "Wystąpił nieoczekiwany problem.");
    }

    private static ResponseEntity<ProblemDetail> getResponseEntity(HttpStatus status, String details) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, details);
        return ResponseEntity.status(status).body(problemDetail);
    }

}
