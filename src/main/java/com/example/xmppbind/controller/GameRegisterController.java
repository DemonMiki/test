package com.example.xmppbind.controller;

import com.example.xmppbind.dto.ApiResponse;
import com.example.xmppbind.dto.GameRegisterRequest;
import com.example.xmppbind.service.GameRegisterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/apis/xmpp-bind.halo.run/v1alpha1/game-register")
public class GameRegisterController {

    private final GameRegisterService gameRegisterService;

    @PostMapping
    public Mono<ResponseEntity<ApiResponse>> register(@RequestBody GameRegisterRequest request) {
        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        String password = request.getPassword() == null ? "" : request.getPassword().trim();
        String confirmPassword = request.getConfirmPassword() == null ? "" : request.getConfirmPassword().trim();

        if (!username.matches("^[A-Za-z0-9_]{3,20}$")) {
            return Mono.just(ResponseEntity.badRequest().body(ApiResponse.of(1, "游戏账号格式不正确")));
        }
        if (password.length() < 6 || password.length() > 64) {
            return Mono.just(ResponseEntity.badRequest().body(ApiResponse.of(2, "游戏密码长度不正确")));
        }
        if (!password.equals(confirmPassword)) {
            return Mono.just(ResponseEntity.badRequest().body(ApiResponse.of(3, "两次输入的密码不一致")));
        }

        return gameRegisterService.registerGameAccount(username, password)
            .thenReturn(ResponseEntity.ok(ApiResponse.of(0, "注册成功")))
            .onErrorResume(ex -> Mono.just(ResponseEntity.badRequest().body(ApiResponse.of(4, ex.getMessage()))));
    }
}
