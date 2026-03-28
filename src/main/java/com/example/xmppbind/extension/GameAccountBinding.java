package com.example.xmppbind.extension;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@GVK(
    group = "xmpp-bind.halo.run",
    version = "v1alpha1",
    kind = "GameAccountBinding",
    plural = "gameaccountbindings",
    singular = "gameaccountbinding"
)
public class GameAccountBinding extends AbstractExtension {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Spec spec;

    @Data
    @Schema(name = "GameAccountBindingSpec")
    public static class Spec {
        private String haloUsername;
        private String gameUsername;
        private Long createdAt;
    }
}
