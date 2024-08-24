package me.redcarlos.higtools.system;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.WindowTabScreen;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import net.minecraft.client.gui.screen.Screen;

public class HIGTab extends Tab {
    public HIGTab() {
        super("HIG Tools");
    }

    @Override
    public TabScreen createScreen(GuiTheme theme) {
        return new HIGScreen(theme, this);
    }

    @Override
    public boolean isScreen(Screen screen) {
        return screen instanceof HIGScreen;
    }

    private static class HIGScreen extends WindowTabScreen {
        private final Settings settings;

        public HIGScreen(GuiTheme theme, Tab tab) {
            super(theme, tab);
            settings = HIGSystem.get().settings;
            settings.onActivated();
        }

        @Override
        public void initWidgets() {
            add(theme.settings(settings)).expandX();
        }

        @Override
        public void tick() {
            super.tick();
            settings.tick(window, theme);
        }

        @Override
        public boolean toClipboard() {
            return NbtUtils.toClipboard(HIGSystem.get());
        }

        @Override
        public boolean fromClipboard() {
            return NbtUtils.fromClipboard(HIGSystem.get());
        }
    }
}
