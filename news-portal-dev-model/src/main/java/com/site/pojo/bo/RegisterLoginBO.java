package com.site.pojo.bo;


import javax.validation.constraints.NotBlank;

public class RegisterLoginBO {

    // verify the data come form front end
    @NotBlank(message = "Mobile phone number cannot be empty.")
    private String mobile;
    @NotBlank(message = "SMS code cannot be empty.")
    private String smsCode;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getSmsCode() {
        return smsCode;
    }

    public void setSmsCode(String smsCode) {
        this.smsCode = smsCode;
    }

    @Override
    public String toString() {
        return "RegisterLoginBO{" +
                "mobile='" + mobile + '\'' +
                ", smsCode='" + smsCode + '\'' +
                '}';
    }
}
