package com.example.simplecrud.Domain.Dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class LoginDto {

    @NotBlank(message = "로그인시 Id에 빈 내용 혹은 공백은 허용되지 않습니다.")
    private String id;

    @NotBlank(message = "로그인시 패스워드에 빈 내용 혹은 공백은 허용되지 않습니다.")
    private String password;

}
