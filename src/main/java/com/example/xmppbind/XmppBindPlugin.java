package com.example.xmppbind;

import com.example.xmppbind.extension.GameAccountBinding;
import org.springframework.stereotype.Component;
import run.halo.app.extension.SchemeManager;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.plugin.PluginContext;

@Component
public class XmppBindPlugin extends BasePlugin {

    private final SchemeManager schemeManager;

    public XmppBindPlugin(PluginContext context, SchemeManager schemeManager) {
        super(context);
        this.schemeManager = schemeManager;
    }

    @Override
    public void start() {
        schemeManager.register(GameAccountBinding.class);
    }

    @Override
    public void stop() {
    }
}
