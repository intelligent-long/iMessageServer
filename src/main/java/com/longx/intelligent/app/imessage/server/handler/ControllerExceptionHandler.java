package com.longx.intelligent.app.imessage.server.handler;

import com.longx.intelligent.app.imessage.server.aspect.SessionCheckAspect;
import com.longx.intelligent.app.imessage.server.data.response.OperationStatus;
import com.longx.intelligent.app.imessage.server.exception.BadRequestException;
import com.longx.intelligent.app.imessage.server.util.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by LONG on 2024/1/13 at 7:23 PM.
 */
@ControllerAdvice
public class ControllerExceptionHandler {
    private static final int VALIDATION_FAILURE_OPERATION_STATUS_CODE = -300;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<OperationStatus> handleValidationExceptions(MethodArgumentNotValidException e, Errors errors){
        Logger.err("方法参数不合法 > " + e.getMessage());
        OperationStatus operationStatus = OperationStatus.buildValidationErrorInstance(errors, VALIDATION_FAILURE_OPERATION_STATUS_CODE);
        return new ResponseEntity<>(operationStatus, HttpStatus.OK);
    }

    @ExceptionHandler(SessionCheckAspect.UserNotLoggedInException.class)
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException e) {
        Logger.err("用户未登录 > " + e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<String> handleBadRequest(BadRequestException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
