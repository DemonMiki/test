package com.example.xmppbind.dto;

import lombok.Data;

@Data
public class GameRegisterRequest {
    private String username;
    private String password;
    private String confirmPassword;
}
