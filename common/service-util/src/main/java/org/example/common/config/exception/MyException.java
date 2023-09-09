package org.example.common.config.exception;

import lombok.Data;
import org.example.common.result.ResultCodeEnum;
@Data
public class MyException extends RuntimeException{

    private Integer code;

    private String msg;

    public MyException(Integer code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public MyException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
        this.msg = resultCodeEnum.getMessage();
    }

    @Override
    public String toString() {
        return "MyException{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}
