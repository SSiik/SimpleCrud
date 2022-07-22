package com.example.simplecrud.Domain.Dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class signupDto {

    @NotBlank(message = "회원가입시 이름에 빈 내용 혹은 공백은 허용되지 않습니다.")
    private String name;

    @NotBlank(message = "회원가입시 Id에 빈 내용 혹은 공백은 허용되지 않습니다.")
    private String id;

    @NotBlank(message = "회원가입시 패스워드에 빈 내용 혹은 공백은 허용되지 않습니다.")
    private String password;
}
