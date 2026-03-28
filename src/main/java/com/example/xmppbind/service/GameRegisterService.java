package com.example.xmppbind.service;

import com.example.xmppbind.extension.GameAccountBinding;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GameRegisterService {

    private final ReactiveExtensionClient extensionClient;

    private final WebClient webClient = WebClient.builder().build();

    // TODO: 按你的环境改这里
    private static final String GAME_SERVER_REGISTER_URL = "http://115.191.7.227:8081/register";
    private static final String GAME_SERVER_INTERNAL_KEY = "CHANGE_ME_TO_YOUR_REAL_INTERNAL_KEY";

    public Mono<Void> registerGameAccount(String gameUsername, String gamePassword) {
        return currentHaloUsername()
            .switchIfEmpty(Mono.error(new IllegalStateException("未获取到当前 Halo 用户")))
            .flatMap(haloUsername -> ensureHaloUserNotRegistered(haloUsername)
                .then(ensureGameUsernameNotBound(gameUsername))
                .then(callGameServer(gameUsername, gamePassword))
                .flatMap(body -> {
                    Object code = body.get("code");
                    if (code == null || !"0".equals(String.valueOf(code))) {
                        return Mono.error(new IllegalStateException(
                            String.valueOf(body.getOrDefault("message", "游戏服务器注册失败"))
                        ));
                    }
                    return saveBinding(haloUsername, gameUsername).then();
                })
            );
    }

    private Mono<String> currentHaloUsername() {
        return ReactiveSecurityContextHolder.getContext()
            .map(securityContext -> securityContext.getAuthentication())
            .filter(Authentication::isAuthenticated)
            .map(Authentication::getName);
    }

    private Mono<Void> ensureHaloUserNotRegistered(String haloUsername) {
        return extensionClient.fetch(GameAccountBinding.class, haloUsername)
            .flatMap(existing -> Mono.<Void>error(new IllegalStateException("当前网站账户已经注册过游戏账号")))
            .switchIfEmpty(Mono.empty());
    }

    private Mono<Void> ensureGameUsernameNotBound(String gameUsername) {
        return extensionClient.listAll(GameAccountBinding.class, ListOptions.NONE, PageRequestImpl.defaultSort())
            .filter(item -> item.getSpec() != null)
            .filter(item -> Objects.equals(item.getSpec().getGameUsername(), gameUsername))
            .next()
            .flatMap(existing -> Mono.<Void>error(new IllegalStateException("该游戏账号已被使用")))
            .switchIfEmpty(Mono.empty());
    }

    private Mono<Map> callGameServer(String gameUsername, String gamePassword) {
        return webClient.post()
            .uri(GAME_SERVER_REGISTER_URL)
            .header("X-Internal-Key", GAME_SERVER_INTERNAL_KEY)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .bodyValue(Map.of(
                "username", gameUsername,
                "password", gamePassword
            ))
            .retrieve()
            .bodyToMono(Map.class)
            .onErrorMap(ex -> new IllegalStateException("调用游戏服务器失败: " + ex.getMessage(), ex));
    }

    private Mono<GameAccountBinding> saveBinding(String haloUsername, String gameUsername) {
        GameAccountBinding binding = new GameAccountBinding();
        Metadata metadata = new Metadata();
        metadata.setName(haloUsername);
        binding.setMetadata(metadata);

        GameAccountBinding.Spec spec = new GameAccountBinding.Spec();
        spec.setHaloUsername(haloUsername);
        spec.setGameUsername(gameUsername);
        spec.setCreatedAt(Instant.now().toEpochMilli());
        binding.setSpec(spec);

        return extensionClient.create(binding);
    }
}
