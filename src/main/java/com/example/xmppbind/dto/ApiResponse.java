package com.example.xmppbind.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(staticName = "of")
public class ApiResponse {
    private int code;
    private String message;
}
