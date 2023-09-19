package com.site.pojo.bo;


import com.fasterxml.jackson.annotation.JsonFormat;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.*;
import java.util.Date;

public class UpdateUserInfoBO {
    @NotBlank(message = "User ID cannot be empty.")
    private String id;

    @NotBlank(message = "Nick name cannot be empty.")
    @Length(max = 12, message = "User name cannot exceed 12 characters.")
    private String nickname;

    @NotBlank(message = "User profile cannot be empty.")
    private String face;

    @NotBlank(message = "User name cannot be empty.")
    private String realname;

    @Email
    @NotBlank(message = "Email address cannot be empty.")
    private String email;

    @NotNull(message = "Please select one:")
    @Min(value = 0, message = "Incorrect selection.")
    @Max(value = 1, message = "Incorrect selection.")
    private Integer sex;

    @NotNull(message = "Please select the Date of Birth.")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd") // 解决前端日期字符串传到后端后，转换为Date类型
    private Date birthday;

    @NotBlank(message = "Please select your state / province")
    private String province;

    @NotBlank(message = "Please select your city")
    private String city;

    @NotBlank(message = "Please input your address")
    private String district;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getFace() {
        return face;
    }

    public void setFace(String face) {
        this.face = face;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }
}
