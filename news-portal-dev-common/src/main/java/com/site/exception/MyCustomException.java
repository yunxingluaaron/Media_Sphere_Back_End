package com.site.exception;

import com.site.grace.result.ResponseStatusEnum;

/*
*  Customized Exception
*  Goal: Handle exceptions in a standardized format;
*        good for decouple.
*        service与controller错误的解耦，不会被service返回的类型而限制
* */
public class MyCustomException extends RuntimeException {

    private ResponseStatusEnum responseStatusEnum;

    public MyCustomException(ResponseStatusEnum responseStatusEnum) {
        super("Error! " + "Status: " + responseStatusEnum.status()
                + "; Message: " + responseStatusEnum.msg());
        this.responseStatusEnum = responseStatusEnum;
    }

    public ResponseStatusEnum getResponseStatusEnum() {
        return responseStatusEnum;
    }

    public void setResponseStatusEnum(ResponseStatusEnum responseStatusEnum) {
        this.responseStatusEnum = responseStatusEnum;
    }
}
