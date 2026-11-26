package com.chubb.claims.claims_service.api;

import com.chubb.claims.claims_service.domain.InvalidStatusTransitionException;
import com.chubb.claims.claims_service.domain.ClaimConflictException;
import com.chubb.claims.claims_service.domain.ClaimAccessDeniedException;
import com.chubb.claims.claims_service.service.InfoRequestNotFoundException;
import com.chubb.claims.claims_service.service.ClaimNotFoundException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validation(MethodArgumentNotValidException ex) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(f -> errors.put(f.getField(), f.getDefaultMessage()));
        p.setProperty("errors", errors);
        return p;
    }

    @ExceptionHandler(ClaimNotFoundException.class)
    ProblemDetail notFound(ClaimNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    ProblemDetail badTransition(InvalidStatusTransitionException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    ProblemDetail concurrent(OptimisticLockingFailureException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "The claim was modified by someone else. Reload and try again.");
    }

        @ExceptionHandler(ClaimConflictException.class)
    ProblemDetail conflict(ClaimConflictException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(ClaimAccessDeniedException.class)
    ProblemDetail forbidden(ClaimAccessDeniedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(InfoRequestNotFoundException.class)
    ProblemDetail infoRequestNotFound(InfoRequestNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }
}