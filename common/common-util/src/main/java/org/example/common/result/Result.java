package org.example.common.result;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code; // 状态码
    private String message; // 返回信息
    private T data; // 统一返回的结果数据

    public static <T> Result<T> build(T body, ResultCodeEnum resultCodeEnum){
        Result<T> result = new Result<>();
        // 封装数据
        if (body!=null){
            result.setData(body);
        }
        // 状态码
        result.setCode(resultCodeEnum.getCode());
        //返回信息
        result.setMessage(resultCodeEnum.getMessage());
        return result;
    }

    public static <T> Result<T> build(T body, int code, String message){
        Result<T> result = new Result<>();
        // 封装数据
        if (body!=null){
            result.setData(body);
        }
        // 状态码
        result.setCode(code);
        //返回信息
        result.setMessage(message);
        return result;
    }

    private Result(){}

    //成功 空结果
    public static <T>Result<T> ok(){
        return build(null,ResultCodeEnum.SUCCESS);
    }

    //成功 返回有数据的结果
    public static <T>Result<T> ok(T data){
        return build(data,ResultCodeEnum.SUCCESS);
    }

    //失败 空结果
    public static <T>Result<T> fail(){
        return build(null,ResultCodeEnum.FAIL);
    }

    //失败 返回有数据的结果
    public static <T>Result<T> fail(T data){
        return build(data,ResultCodeEnum.FAIL);
    }

    public Result<T> message(String msg){
        this.setMessage(msg);
        return this;
    }

    public Result<T> code(Integer code){
        this.setCode(code);
        return this;
    }
}
