package com.site.exception;

import com.site.grace.result.ResponseStatusEnum;

// 处理异常的统一封装
public class GraceException {

    public static void display(ResponseStatusEnum responseStatusEnum){
        throw new MyCustomException(responseStatusEnum);
    }
}
