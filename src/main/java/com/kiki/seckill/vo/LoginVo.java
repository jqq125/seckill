package com.kiki.seckill.vo;

import javax.validation.constraints.NotNull;

import com.kiki.seckill.common.utils.IsMobile;
import org.hibernate.validator.constraints.Length;



public class LoginVo {
    private String mobile;
    private String password;

    @NotNull
    @IsMobile  //自定义注解
    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    @NotNull
    @Length(min=32) //JSR303数据校验，长度最小为32位
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

}
