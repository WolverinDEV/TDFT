package dev.wolveringer.tdft.h4;

import dev.wolveringer.tdft.plugin.Plugin;

public class PluginH4 extends Plugin {
    @Override
    public String getName() {
        return "Test H4";
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public void onEnable() {
        this.registerTestUnit(Matrix.class);
    }
}
