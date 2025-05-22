package com.longx.intelligent.app.imessage.server.data.response;

import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 龍天翔
 * @date 2022/11/24 5:42 AM
 */
public class OperationStatus {
    private int code;
    private String message;
    private final Map<String, List<String>> details = new LinkedHashMap<>();

    public OperationStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public OperationStatus() {

    }

    public static OperationStatus success(){
        return new OperationStatus(0, "成功");
    }

    public static OperationStatus failure(){
        return new OperationStatus(-200, "失败");
    }

    public OperationStatus putDetail(String key, String value){
        if(details.containsKey(key)){
            details.get(key).add(value);
        }else {
            List<String> messages = new ArrayList<>();
            messages.add(value);
            details.put(key, messages);
        }
        return this;
    }

    public OperationStatus putDetails(Map<String, List<String>> details){
        this.details.putAll(details);
        return this;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, List<String>> getDetails() {
        return details;
    }

    public static OperationStatus buildValidationErrorInstance(Errors errors, int code) {
        OperationStatus operationStatus = new OperationStatus(code, "数据校验不通过");
        for (FieldError error : errors.getFieldErrors()) {
            operationStatus.putDetail(error.getField(), error.getDefaultMessage());
        }
        return operationStatus;
    }
}
