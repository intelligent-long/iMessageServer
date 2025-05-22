package com.longx.intelligent.app.imessage.server.data.response;

import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.Map;

/**
 * @author 龍天翔
 * @date 2022/12/9 9:24 PM
 */
public class OperationData extends OperationStatus{

    private Object data;

    public static OperationData success(Object data){
        return new OperationData(0, "成功", data);
    }

    public static OperationData failure(){
        return new OperationData(-200, "失败");
    }

    public OperationData() {
        super();
    }

    public OperationData(int code, String message) {
        super(code, message);
    }

    public OperationData(int code, String message, Object data) {
        super(code, message);
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public static OperationData buildValidationErrorInstance(Errors errors, int code) {
        OperationData operationData = new OperationData(code, "数据校验不通过");
        for (FieldError error : errors.getFieldErrors()) {
            operationData.putDetail(error.getField(), error.getDefaultMessage());
        }
        return operationData;
    }

    public OperationData putDetail(String key, String value){
        super.putDetail(key, value);
        return this;
    }

    public OperationData putDetails(Map<String, List<String>> details){
        super.putDetails(details);
        return this;
    }
}
