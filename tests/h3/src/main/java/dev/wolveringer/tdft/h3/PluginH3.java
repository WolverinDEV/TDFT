package dev.wolveringer.tdft.h3;

import dev.wolveringer.tdft.plugin.Plugin;

public class PluginH3 extends Plugin {
    @Override
    public String getName() {
        return "Test H3";
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public void onEnable() {
        this.registerTestUnit(PascalsTriangle.class);
        this.registerTestUnit(Dogs.class);
    }
}
