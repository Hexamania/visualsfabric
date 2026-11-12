package com.visualsfabric;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class VisualsFabricConfigScreen extends Screen {
    private static final String[] CATEGORIES = {
        "visuals", "sky", "spotify", "hitkill", "performance", "keybindings"
    };

    private final Screen parent;
    private int selectedCategory = 0;
    private boolean waitingForKeybind = false;
    private KeyBinding keybindToRebind = null;
    private final List<SliderWidget> sliders = new ArrayList<>();

    public VisualsFabricConfigScreen() {
        this(null);
    }

    public VisualsFabricConfigScreen(Screen parent) {
        super(Text.translatable("gui.visualsfabric.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        sliders.clear();
        super.init();
        rebuildWidgets();
    }

    private void rebuildWidgets() {
        int startY = 80;
        int centerX = width / 2;

        addCategoryTabs();

        switch (CATEGORIES[selectedCategory]) {
            case "visuals" -> addVisualsWidgets(startY, centerX);
            case "sky" -> addSkyWidgets(startY, centerX);
            case "spotify" -> addSpotifyWidgets(startY, centerX);
            case "hitkill" -> addHitKillWidgets(startY, centerX);
            case "performance" -> addPerformanceWidgets(startY, centerX);
            case "keybindings" -> addKeybindWidgets(startY, centerX);
        }

        int btnY = height - 40;
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.visualsfabric.reset_defaults"), btn -> resetDefaults())
            .dimensions(centerX - 100, btnY, 200, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.visualsfabric.done"), btn -> close())
            .dimensions(centerX - 100, btnY + 20, 200, 20).build());
    }

    private void addCategoryTabs() {
        int centerX = width / 2;
        int startY = 40;
        int categoryWidth = 140;
        int categoryHeight = 28;
        int spacing = 4;

        for (int i = 0; i < CATEGORIES.length; i++) {
            final int index = i;
            int x = centerX - (CATEGORIES.length * (categoryWidth + spacing)) / 2 + i * (categoryWidth + spacing);
            int y = startY;
            ButtonWidget button = ButtonWidget.builder(Text.translatable("category.visualsfabric." + CATEGORIES[i]), btn -> {
                selectedCategory = index;
                rebuildWidgets();
            }).dimensions(x, y, categoryWidth, categoryHeight).build();
            if (index == selectedCategory) button.active = false;
            addDrawableChild(button);
        }

        int badgeX = width - 100;
        int badgeY = 8;
        addDrawableChild(ButtonWidget.builder(Text.literal("§bAI-Assisted§r"), btn -> {})
            .dimensions(badgeX, badgeY, 90, 16).build());
    }

    private void addVisualsWidgets(int startY, int centerX) {
        addToggle("gui.visualsfabric.bloom", () -> VisualsFabricClient.bloomEnabled, v -> VisualsFabricClient.bloomEnabled = v);
        addSlider("gui.visualsfabric.intensity", () -> VisualsFabricClient.bloomIntensity, v -> VisualsFabricClient.bloomIntensity = v, 0.0f, 2.0f, 0.05f);
        addToggle("gui.visualsfabric.chromatic_aberration", () -> VisualsFabricClient.chromaticAberrationEnabled, v -> VisualsFabricClient.chromaticAberrationEnabled = v);
        addSlider("gui.visualsfabric.intensity", () -> VisualsFabricClient.chromaticAberrationIntensity, v -> VisualsFabricClient.chromaticAberrationIntensity = v, 0.0f, 0.1f, 0.005f);
        addToggle("gui.visualsfabric.film_grain", () -> VisualsFabricClient.filmGrainEnabled, v -> VisualsFabricClient.filmGrainEnabled = v);
        addSlider("gui.visualsfabric.intensity", () -> VisualsFabricClient.filmGrainIntensity, v -> VisualsFabricClient.filmGrainIntensity = v, 0.0f, 1.0f, 0.05f);
        addToggle("gui.visualsfabric.vignette", () -> VisualsFabricClient.vignetteEnabled, v -> VisualsFabricClient.vignetteEnabled = v);
        addSlider("gui.visualsfabric.intensity", () -> VisualsFabricClient.vignetteIntensity, v -> VisualsFabricClient.vignetteIntensity = v, 0.0f, 1.0f, 0.05f);
        addToggle("gui.visualsfabric.color_grading", () -> VisualsFabricClient.colorGradingEnabled, v -> VisualsFabricClient.colorGradingEnabled = v);
        addSlider("gui.visualsfabric.saturation", () -> VisualsFabricClient.saturation, v -> VisualsFabricClient.saturation = v, 0.0f, 2.0f, 0.05f);
        addSlider("gui.visualsfabric.contrast", () -> VisualsFabricClient.contrast, v -> VisualsFabricClient.contrast = v, 0.0f, 2.0f, 0.05f);
        addSlider("gui.visualsfabric.brightness", () -> VisualsFabricClient.brightness, v -> VisualsFabricClient.brightness = v, 0.0f, 2.0f, 0.05f);
        addSlider("gui.visualsfabric.gamma", () -> VisualsFabricClient.gamma, v -> VisualsFabricClient.gamma = v, 0.5f, 2.0f, 0.05f);
        addToggle("gui.visualsfabric.depth_of_field", () -> VisualsFabricClient.depthOfFieldEnabled, v -> VisualsFabricClient.depthOfFieldEnabled = v);
        addToggle("gui.visualsfabric.motion_blur", () -> VisualsFabricClient.motionBlurEnabled, v -> VisualsFabricClient.motionBlurEnabled = v);
        addToggle("gui.visualsfabric.enhanced_ao", () -> VisualsFabricClient.ambientOcclusionEnhanced, v -> VisualsFabricClient.ambientOcclusionEnhanced = v);
    }

    private void addSkyWidgets(int startY, int centerX) {
        addToggle("gui.visualsfabric.sky_effects", () -> VisualsFabricClient.skyEffectsEnabled, v -> VisualsFabricClient.skyEffectsEnabled = v);
        addToggle("gui.visualsfabric.enhanced_sun_moon", () -> VisualsFabricClient.enhancedSunMoon, v -> VisualsFabricClient.enhancedSunMoon = v);
        addSlider("gui.visualsfabric.sun_moon_scale", () -> VisualsFabricClient.sunMoonScale, v -> VisualsFabricClient.sunMoonScale = v, 0.5f, 3.0f, 0.1f);
        addToggle("gui.visualsfabric.star_field", () -> VisualsFabricClient.starFieldEnabled, v -> VisualsFabricClient.starFieldEnabled = v);
        addSlider("gui.visualsfabric.star_brightness", () -> VisualsFabricClient.starBrightness, v -> VisualsFabricClient.starBrightness = v, 0.0f, 2.0f, 0.05f);
        addSlider("gui.visualsfabric.star_density", () -> VisualsFabricClient.starDensity, v -> VisualsFabricClient.starDensity = v, 0.0f, 2.0f, 0.05f);
        addToggle("gui.visualsfabric.aurora", () -> VisualsFabricClient.auroraEnabled, v -> VisualsFabricClient.auroraEnabled = v);
        addSlider("gui.visualsfabric.aurora_intensity", () -> VisualsFabricClient.auroraIntensity, v -> VisualsFabricClient.auroraIntensity = v, 0.0f, 1.0f, 0.05f);
        addToggle("gui.visualsfabric.sky_color_grading", () -> VisualsFabricClient.skyColorGrading, v -> VisualsFabricClient.skyColorGrading = v);
        addSlider("gui.visualsfabric.sky_color_intensity", () -> VisualsFabricClient.skyColorIntensity, v -> VisualsFabricClient.skyColorIntensity = v, 0.0f, 1.0f, 0.05f);
    }

    private void addSpotifyWidgets(int startY, int centerX) {
        addToggle("gui.visualsfabric.spotify_hud", () -> VisualsFabricClient.spotifyHudEnabled, v -> VisualsFabricClient.spotifyHudEnabled = v);
        addSlider("gui.visualsfabric.spotify_hud_scale", () -> VisualsFabricClient.spotifyHudScale, v -> VisualsFabricClient.spotifyHudScale = v, 0.5f, 2.0f, 0.1f);
        addSlider("gui.visualsfabric.spotify_hud_x", () -> VisualsFabricClient.spotifyHudX, v -> VisualsFabricClient.spotifyHudX = v, 0.0f, 1.0f, 0.05f);
        addSlider("gui.visualsfabric.spotify_hud_y", () -> VisualsFabricClient.spotifyHudY, v -> VisualsFabricClient.spotifyHudY = v, 0.0f, 1.0f, 0.05f);
        addToggle("gui.visualsfabric.spotify_lyrics_in_sky", () -> VisualsFabricClient.spotifyLyricsInSky, v -> VisualsFabricClient.spotifyLyricsInSky = v);
    }

    private void addHitKillWidgets(int startY, int centerX) {
        addToggle("gui.visualsfabric.hit_effects", () -> VisualsFabricClient.hitEffectsEnabled, v -> VisualsFabricClient.hitEffectsEnabled = v);
        addToggle("gui.visualsfabric.hit_particles", () -> VisualsFabricClient.hitParticlesEnabled, v -> VisualsFabricClient.hitParticlesEnabled = v);
        addToggle("gui.visualsfabric.hit_screen_flash", () -> VisualsFabricClient.hitScreenFlashEnabled, v -> VisualsFabricClient.hitScreenFlashEnabled = v);
        addSlider("gui.visualsfabric.hit_intensity", () -> VisualsFabricClient.hitEffectIntensity, v -> VisualsFabricClient.hitEffectIntensity = v, 0.0f, 2.0f, 0.1f);
        addSlider("gui.visualsfabric.hit_screen_flash_intensity", () -> VisualsFabricClient.hitScreenFlashIntensity, v -> VisualsFabricClient.hitScreenFlashIntensity = v, 0.0f, 1.0f, 0.05f);
        addToggle("gui.visualsfabric.kill_effects", () -> VisualsFabricClient.killEffectsEnabled, v -> VisualsFabricClient.killEffectsEnabled = v);
        addToggle("gui.visualsfabric.kill_particles", () -> VisualsFabricClient.killParticlesEnabled, v -> VisualsFabricClient.killParticlesEnabled = v);
        addToggle("gui.visualsfabric.kill_screen_flash", () -> VisualsFabricClient.killScreenFlashEnabled, v -> VisualsFabricClient.killScreenFlashEnabled = v);
        addSlider("gui.visualsfabric.kill_intensity", () -> VisualsFabricClient.killEffectIntensity, v -> VisualsFabricClient.killEffectIntensity = v, 0.0f, 2.0f, 0.1f);
        addSlider("gui.visualsfabric.kill_screen_flash_intensity", () -> VisualsFabricClient.killScreenFlashIntensity, v -> VisualsFabricClient.killScreenFlashIntensity = v, 0.0f, 1.0f, 0.05f);
    }

    private void addPerformanceWidgets(int startY, int centerX) {
        addToggle("gui.visualsfabric.performance_mode", () -> VisualsFabricClient.performanceMode, v -> VisualsFabricClient.performanceMode = v);
        addToggle("gui.visualsfabric.low_quality_shaders", () -> VisualsFabricClient.lowQualityShaders, v -> VisualsFabricClient.lowQualityShaders = v);
        addToggle("gui.visualsfabric.reduced_particles", () -> VisualsFabricClient.reducedParticles, v -> VisualsFabricClient.reducedParticles = v);
    }

    private void addKeybindWidgets(int startY, int centerX) {
        addKeybind("key.visualsfabric.open_config", VisualsFabricClient.openConfigKey);
        addKeybind("key.visualsfabric.toggle_bloom", VisualsFabricClient.toggleBloomKey);
        addKeybind("key.visualsfabric.toggle_chromatic_aberration", VisualsFabricClient.toggleChromaticAberrationKey);
        addKeybind("key.visualsfabric.toggle_film_grain", VisualsFabricClient.toggleFilmGrainKey);
        addKeybind("key.visualsfabric.toggle_vignette", VisualsFabricClient.toggleVignetteKey);
        addKeybind("key.visualsfabric.toggle_color_grading", VisualsFabricClient.toggleColorGradingKey);
        addKeybind("key.visualsfabric.toggle_dof", VisualsFabricClient.toggleDepthOfFieldKey);
        addKeybind("key.visualsfabric.toggle_motion_blur", VisualsFabricClient.toggleMotionBlurKey);
        addKeybind("key.visualsfabric.toggle_ao", VisualsFabricClient.toggleAOKey);
        addKeybind("key.visualsfabric.toggle_sky_effects", VisualsFabricClient.toggleSkyEffectsKey);
        addKeybind("key.visualsfabric.toggle_enhanced_sun_moon", VisualsFabricClient.toggleEnhancedSunMoonKey);
        addKeybind("key.visualsfabric.toggle_star_field", VisualsFabricClient.toggleStarFieldKey);
        addKeybind("key.visualsfabric.toggle_aurora", VisualsFabricClient.toggleAuroraKey);
        addKeybind("key.visualsfabric.toggle_spotify_hud", VisualsFabricClient.toggleSpotifyHudKey);
        addKeybind("key.visualsfabric.toggle_spotify_lyrics", VisualsFabricClient.toggleSpotifyLyricsKey);
        addKeybind("key.visualsfabric.toggle_hit_effects", VisualsFabricClient.toggleHitEffectsKey);
        addKeybind("key.visualsfabric.toggle_kill_effects", VisualsFabricClient.toggleKillEffectsKey);
        addKeybind("key.visualsfabric.toggle_performance_mode", VisualsFabricClient.togglePerformanceModeKey);
    }

    private int nextY = 80;
    private void addToggle(String key, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        int y = nextY;
        nextY += 28;
        ButtonWidget button = ButtonWidget.builder(Text.translatable(key), btn -> {
            boolean newVal = !getter.get();
            setter.accept(newVal);
            btn.setMessage(Text.literal(newVal ? "§aON" : "§cOFF"));
        }).dimensions(width / 2 - 150, y, 210, 20).build();
        button.setMessage(Text.literal(getter.get() ? "§aON" : "§cOFF"));
        addDrawableChild(button);
    }

    private void addSlider(String key, Supplier<Float> getter, Consumer<Float> setter, float min, float max, float step) {
        int y = nextY;
        nextY += 28;
        SliderWidget slider = new SliderWidget(width / 2 - 150, y, 300, 20, Text.translatable(key), getter, setter, min, max, step);
        sliders.add(slider);
        addDrawableChild(slider);
    }

    private void addKeybind(String key, KeyBinding keyBinding) {
        int y = nextY;
        nextY += 28;
        ButtonWidget labelBtn = ButtonWidget.builder(Text.translatable(key), btn -> {}).dimensions(width / 2 - 150, y, 210, 20).build();
        labelBtn.active = false;
        addDrawableChild(labelBtn);

        ButtonWidget keyBtn = ButtonWidget.builder(Text.literal(keyBinding.getBoundKeyLocalizedText().getString()), btn -> {
            waitingForKeybind = true;
            keybindToRebind = keyBinding;
        }).dimensions(width / 2 + 70, y, 100, 20).build();
        addDrawableChild(keyBtn);
    }

    private void resetDefaults() {
        VisualsFabricClient.bloomEnabled = true;
        VisualsFabricClient.chromaticAberrationEnabled = true;
        VisualsFabricClient.filmGrainEnabled = true;
        VisualsFabricClient.vignetteEnabled = true;
        VisualsFabricClient.colorGradingEnabled = true;
        VisualsFabricClient.depthOfFieldEnabled = false;
        VisualsFabricClient.motionBlurEnabled = false;
        VisualsFabricClient.ambientOcclusionEnhanced = true;
        VisualsFabricClient.skyEffectsEnabled = true;
        VisualsFabricClient.enhancedSunMoon = true;
        VisualsFabricClient.starFieldEnabled = true;
        VisualsFabricClient.auroraEnabled = false;
        VisualsFabricClient.skyColorGrading = true;
        VisualsFabricClient.spotifyHudEnabled = false;
        VisualsFabricClient.spotifyLyricsInSky = false;
        VisualsFabricClient.hitEffectsEnabled = true;
        VisualsFabricClient.killEffectsEnabled = true;
        VisualsFabricClient.hitParticlesEnabled = true;
        VisualsFabricClient.killParticlesEnabled = true;
        VisualsFabricClient.hitScreenFlashEnabled = true;
        VisualsFabricClient.killScreenFlashEnabled = true;
        VisualsFabricClient.performanceMode = false;
        VisualsFabricClient.lowQualityShaders = false;
        VisualsFabricClient.reducedParticles = false;

        VisualsFabricClient.bloomIntensity = 0.6f;
        VisualsFabricClient.bloomThreshold = 0.8f;
        VisualsFabricClient.chromaticAberrationIntensity = 0.02f;
        VisualsFabricClient.filmGrainIntensity = 0.15f;
        VisualsFabricClient.vignetteIntensity = 0.4f;
        VisualsFabricClient.saturation = 1.1f;
        VisualsFabricClient.contrast = 1.05f;
        VisualsFabricClient.brightness = 1.0f;
        VisualsFabricClient.gamma = 1.0f;
        VisualsFabricClient.starBrightness = 1.0f;
        VisualsFabricClient.starDensity = 0.5f;
        VisualsFabricClient.sunMoonScale = 1.0f;
        VisualsFabricClient.auroraIntensity = 0.3f;
        VisualsFabricClient.skyColorIntensity = 0.5f;
        VisualsFabricClient.spotifyHudScale = 1.0f;
        VisualsFabricClient.spotifyHudX = 0.5f;
        VisualsFabricClient.spotifyHudY = 0.9f;
        VisualsFabricClient.hitEffectIntensity = 1.0f;
        VisualsFabricClient.killEffectIntensity = 1.0f;
        VisualsFabricClient.hitScreenFlashIntensity = 0.3f;
        VisualsFabricClient.killScreenFlashIntensity = 0.5f;

        nextY = 80;
        rebuildWidgets();
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().options.write();
        super.close();
        if (parent != null) MinecraftClient.getInstance().setScreen(parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int centerX = width / 2;
        context.drawCenteredTextWithShadow(textRenderer, title, centerX, 12, 0x00FFFF);
        if (waitingForKeybind) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("§c§lPRESS KEY TO BIND...§r"), centerX, height / 2 - 10, 0xFF0000);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("§7Press ESC to cancel§r"), centerX, height / 2 + 10, 0x808080);
        }
        for (SliderWidget slider : sliders) {
            slider.renderSlider(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (waitingForKeybind) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                waitingForKeybind = false;
                keybindToRebind = null;
                return true;
            }
            // Key rebinding - simplified for now
            waitingForKeybind = false;
            keybindToRebind = null;
            rebuildWidgets();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    static class SliderWidget extends ButtonWidget {
        private final Supplier<Float> getter;
        private final Consumer<Float> setter;
        private final float min, max, step;
        private boolean dragging = false;

        SliderWidget(int x, int y, int width, int height, Text label, Supplier<Float> getter, Consumer<Float> setter, float min, float max, float step) {
            super(x, y, width, height, label, btn -> {}, DEFAULT_NARRATION_SUPPLIER);
            this.getter = getter; this.setter = setter; this.min = min; this.max = max; this.step = step;
            active = true;
        }

        void renderSlider(DrawContext context, int mouseX, int mouseY, float delta) {
            float value = getter.get();
            int trackX = getX(); int trackY = getY() + 10; int trackWidth = getWidth(); int trackHeight = 4;
            context.fill(trackX, trackY, trackX + trackWidth, trackY + trackHeight, 0xFF333333);
            float percent = (value - min) / (max - min);
            int fillWidth = (int)(trackWidth * percent);
            context.fill(trackX, trackY, trackX + fillWidth, trackY + trackHeight, 0xFF00FFFF);
            int handleX = trackX + fillWidth - 6; int handleY = trackY - 4;
            context.fill(handleX, handleY, handleX + 12, handleY + 12, 0xFF00FFFF);
            String valueText = String.format("%.2f", value);
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, valueText, getX() + getWidth() + 10, getY() + 2, 0xAAAAAA);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isMouseOver(mouseX, mouseY)) {
                dragging = true; updateValue(mouseX); return true;
            }
            return false;
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            if (dragging) { updateValue(mouseX); return true; }
            return false;
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (dragging) { dragging = false; return true; }
            return false;
        }

        private void updateValue(double mouseX) {
            float percent = Math.max(0, Math.min(1, (float)((mouseX - getX()) / getWidth())));
            float newValue = min + percent * (max - min);
            newValue = Math.round(newValue / step) * step;
            setter.accept(newValue);
        }
    }
}