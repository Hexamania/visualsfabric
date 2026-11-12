package com.visualsfabric;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class VisualsFabricConfigScreen extends Screen {
    private static final Identifier BACKGROUND_TEXTURE = Identifier.of("visualsfabric", "textures/gui/config_background.png");
    private final Screen parent;
    private List<ConfigEntry> entries = new ArrayList<>();
    private int contentStartY = 40;
    private int entryHeight = 24;
    private int spacing = 4;

    public VisualsFabricConfigScreen() {
        this(null);
    }

    public VisualsFabricConfigScreen(Screen parent) {
        super(Text.translatable("gui.visualsfabric.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        entries.clear();

        // Visual Effects Category
        entries.add(new CategoryEntry(Text.translatable("category.visualsfabric.visuals")));

        entries.add(new ToggleEntry(
            Text.translatable("gui.visualsfabric.bloom"),
            VisualsFabricClient::isBloomEnabled,
            val -> VisualsFabricClient.bloomEnabled = val,
            () -> VisualsFabricClient.bloomIntensity,
            val -> VisualsFabricClient.bloomIntensity = val,
            0.0, 2.0, "%.2f"
        ));

        entries.add(new ToggleEntry(
            Text.translatable("gui.visualsfabric.chromatic_aberration"),
            VisualsFabricClient::isChromaticAberrationEnabled,
            val -> VisualsFabricClient.chromaticAberrationEnabled = val,
            () -> VisualsFabricClient.chromaticAberrationIntensity,
            val -> VisualsFabricClient.chromaticAberrationIntensity = val,
            0.0, 0.1, "%.3f"
        ));

        entries.add(new ToggleEntry(
            Text.translatable("gui.visualsfabric.film_grain"),
            VisualsFabricClient::isFilmGrainEnabled,
            val -> VisualsFabricClient.filmGrainEnabled = val,
            () -> VisualsFabricClient.filmGrainIntensity,
            val -> VisualsFabricClient.filmGrainIntensity = val,
            0.0, 1.0, "%.2f"
        ));

        entries.add(new ToggleEntry(
            Text.translatable("gui.visualsfabric.vignette"),
            VisualsFabricClient::isVignetteEnabled,
            val -> VisualsFabricClient.vignetteEnabled = val,
            () -> VisualsFabricClient.vignetteIntensity,
            val -> VisualsFabricClient.vignetteIntensity = val,
            0.0, 1.0, "%.2f"
        ));

        entries.add(new ToggleEntry(
            Text.translatable("gui.visualsfabric.color_grading"),
            VisualsFabricClient::isColorGradingEnabled,
            val -> VisualsFabricClient.colorGradingEnabled = val,
            null, null, 0, 0, null
        ));

        entries.add(new ToggleEntry(
            Text.translatable("gui.visualsfabric.depth_of_field"),
            VisualsFabricClient::isDepthOfFieldEnabled,
            val -> VisualsFabricClient.depthOfFieldEnabled = val,
            null, null, 0, 0, null
        ));

        entries.add(new ToggleEntry(
            Text.translatable("gui.visualsfabric.motion_blur"),
            VisualsFabricClient::isMotionBlurEnabled,
            val -> VisualsFabricClient.motionBlurEnabled = val,
            null, null, 0, 0, null
        ));

        entries.add(new ToggleEntry(
            Text.translatable("gui.visualsfabric.enhanced_ao"),
            VisualsFabricClient::isAmbientOcclusionEnhanced,
            val -> VisualsFabricClient.ambientOcclusionEnhanced = val,
            null, null, 0, 0, null
        ));

        // Color Grading Category
        entries.add(new CategoryEntry(Text.translatable("category.visualsfabric.color_grading")));

        entries.add(new SliderOnlyEntry(
            Text.translatable("gui.visualsfabric.saturation"),
            () -> VisualsFabricClient.saturation,
            val -> VisualsFabricClient.saturation = val,
            0.0, 2.0, "%.2f"
        ));

        entries.add(new SliderOnlyEntry(
            Text.translatable("gui.visualsfabric.contrast"),
            () -> VisualsFabricClient.contrast,
            val -> VisualsFabricClient.contrast = val,
            0.0, 2.0, "%.2f"
        ));

        entries.add(new SliderOnlyEntry(
            Text.translatable("gui.visualsfabric.brightness"),
            () -> VisualsFabricClient.brightness,
            val -> VisualsFabricClient.brightness = val,
            0.0, 2.0, "%.2f"
        ));

        entries.add(new SliderOnlyEntry(
            Text.translatable("gui.visualsfabric.gamma"),
            () -> VisualsFabricClient.gamma,
            val -> VisualsFabricClient.gamma = val,
            0.0, 2.0, "%.2f"
        ));

        // Key Bindings Category
        entries.add(new CategoryEntry(Text.translatable("category.visualsfabric.keybindings")));

        entries.add(new KeybindEntry(
            Text.translatable("gui.visualsfabric.open_config"),
            VisualsFabricClient.openConfigKey
        ));

        entries.add(new KeybindEntry(
            Text.translatable("gui.visualsfabric.toggle_bloom"),
            VisualsFabricClient.toggleBloomKey
        ));

        entries.add(new KeybindEntry(
            Text.translatable("gui.visualsfabric.toggle_chromatic_aberration"),
            VisualsFabricClient.toggleChromaticAberrationKey
        ));

        entries.add(new KeybindEntry(
            Text.translatable("gui.visualsfabric.toggle_film_grain"),
            VisualsFabricClient.toggleFilmGrainKey
        ));

        entries.add(new KeybindEntry(
            Text.translatable("gui.visualsfabric.toggle_vignette"),
            VisualsFabricClient.toggleVignetteKey
        ));

        entries.add(new KeybindEntry(
            Text.translatable("gui.visualsfabric.toggle_color_grading"),
            VisualsFabricClient.toggleColorGradingKey
        ));

        entries.add(new KeybindEntry(
            Text.translatable("gui.visualsfabric.toggle_dof"),
            VisualsFabricClient.toggleDepthOfFieldKey
        ));

        entries.add(new KeybindEntry(
            Text.translatable("gui.visualsfabric.toggle_motion_blur"),
            VisualsFabricClient.toggleMotionBlurKey
        ));

        entries.add(new KeybindEntry(
            Text.translatable("gui.visualsfabric.toggle_ao"),
            VisualsFabricClient.toggleAOKey
        ));

        // Buttons
        int buttonY = contentStartY + entries.size() * (entryHeight + spacing) + 10;
        int buttonWidth = 100;
        int centerX = width / 2;

        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.visualsfabric.reset_defaults"), btn -> resetToDefaults())
            .dimensions(centerX - buttonWidth - 5, buttonY, buttonWidth, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.visualsfabric.done"), btn -> close())
            .dimensions(centerX + 5, buttonY, buttonWidth, 20).build());
    }

    private void resetToDefaults() {
        VisualsFabricClient.bloomEnabled = true;
        VisualsFabricClient.chromaticAberrationEnabled = true;
        VisualsFabricClient.filmGrainEnabled = true;
        VisualsFabricClient.vignetteEnabled = true;
        VisualsFabricClient.colorGradingEnabled = true;
        VisualsFabricClient.depthOfFieldEnabled = false;
        VisualsFabricClient.motionBlurEnabled = false;
        VisualsFabricClient.ambientOcclusionEnhanced = true;

        VisualsFabricClient.bloomIntensity = 0.6f;
        VisualsFabricClient.bloomThreshold = 0.8f;
        VisualsFabricClient.chromaticAberrationIntensity = 0.02f;
        VisualsFabricClient.filmGrainIntensity = 0.15f;
        VisualsFabricClient.vignetteIntensity = 0.4f;
        VisualsFabricClient.saturation = 1.1f;
        VisualsFabricClient.contrast = 1.05f;
        VisualsFabricClient.brightness = 1.0f;
        VisualsFabricClient.gamma = 1.0f;

        // Recreate widgets with new values
        init();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render dark semi-transparent background
        context.fill(0, 0, width, height, 0xB0000000);

        // Render panel background
        int panelWidth = 380;
        int panelHeight = Math.min(height - 40, contentStartY + entries.size() * (entryHeight + spacing) + 60);
        int panelX = (width - panelWidth) / 2;
        int panelY = (height - panelHeight) / 2;

        // Draw panel background with rounded corners effect
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xFF1A1A2E);
        context.fill(panelX + 1, panelY + 1, panelX + panelWidth - 1, panelY + panelHeight - 1, 0xFF16213E);
        context.fill(panelX + 2, panelY + 2, panelX + panelWidth - 2, panelY + panelHeight - 2, 0xFF0F0F23);

        // Draw accent line at top
        context.fill(panelX + 2, panelY + 2, panelX + panelWidth - 2, panelY + 3, 0xFF00D4FF);

        // Draw title
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, panelY + 12, 0xFF00D4FF);

        // Draw version
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("§7v1.0.0 §8| §7AI-Assisted"), width / 2, panelY + 24, 0xFF888888);

        // Render entries
        int y = panelY + contentStartY;
        for (ConfigEntry entry : entries) {
            if (y + entryHeight > panelY + panelHeight - 40) break;
            entry.render(context, panelX + 10, y, panelWidth - 20, entryHeight, mouseX, mouseY);
            y += entryHeight + spacing;
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        if (parent != null) {
            client.setScreen(parent);
        } else {
            client.setScreen(null);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (ConfigEntry entry : entries) {
            if (entry instanceof KeybindEntry keybindEntry) {
                if (keybindEntry.keyPressed(keyCode, scanCode, modifiers)) {
                    return true;
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // Config Entry Types
    abstract static class ConfigEntry {
        abstract void render(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY);
    }

    static class CategoryEntry extends ConfigEntry {
        final Text text;
        CategoryEntry(Text text) { this.text = text; }
        @Override
        void render(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY) {
            context.fill(x, y + height - 1, x + width, y + height, 0xFF00D4FF);
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, text, x, y + 2, 0xFF00D4FF);
        }
    }

    static class ToggleEntry extends ConfigEntry {
        final Text label;
        final java.util.function.BooleanSupplier getter;
        final java.util.function.Consumer<Boolean> setter;
        final Supplier<Float> intensityGetter;
        final java.util.function.Consumer<Float> intensitySetter;
        final double min, max;
        final String format;
        private ButtonWidget toggleButton;
        private SliderWidget slider;

        ToggleEntry(Text label, java.util.function.BooleanSupplier getter, java.util.function.Consumer<Boolean> setter,
                    Supplier<Float> intensityGetter, java.util.function.Consumer<Float> intensitySetter,
                    double min, double max, String format) {
            this.label = label;
            this.getter = getter;
            this.setter = setter;
            this.intensityGetter = intensityGetter;
            this.intensitySetter = intensitySetter;
            this.min = min;
            this.max = max;
            this.format = format;
        }

        @Override
        void render(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY) {
            if (toggleButton == null) {
                toggleButton = ButtonWidget.builder(Text.literal(""), btn -> {
                    boolean newVal = !getter.getAsBoolean();
                    setter.accept(newVal);
                    btn.setMessage(Text.literal(newVal ? "§aON" : "§cOFF"));
                }).dimensions(x + width - 50, y, 44, height).build();
                toggleButton.setMessage(Text.literal(getter.getAsBoolean() ? "§aON" : "§cOFF"));
            } else {
                toggleButton.setX(x + width - 50);
                toggleButton.setY(y);
            }
            toggleButton.render(context, mouseX, mouseY, 0);

            if (intensityGetter != null && intensitySetter != null && getter.getAsBoolean()) {
                // SliderWidget constructor: (int x, int y, int width, int height, Text label, double value)
                slider = new SliderWidget(
                    x, y + height + 2, width, height,
                    Text.translatable("gui.visualsfabric.intensity"),
                    intensityGetter.get()
                ) {
                    @Override
                    protected void applyValue() {
                        intensitySetter.accept((float)this.value);
                    }
                    @Override
                    protected void updateMessage() {
                        this.setMessage(Text.literal(String.format(format, this.value)));
                    }
                };
                slider.setMessage(Text.literal(String.format(format, intensityGetter.get())));
                slider.render(context, mouseX, mouseY, 0);
            }

            // Label
            int color = getter.getAsBoolean() ? 0xFFFFFFFF : 0xFFAAAAAA;
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, label, x + 4, y + (height - MinecraftClient.getInstance().textRenderer.fontHeight) / 2, color);
        }
    }

    static class SliderOnlyEntry extends ConfigEntry {
        final Text label;
        final Supplier<Float> getter;
        final java.util.function.Consumer<Float> setter;
        final double min, max;
        final String format;
        private SliderWidget slider;

        SliderOnlyEntry(Text label, Supplier<Float> getter, java.util.function.Consumer<Float> setter,
                        double min, double max, String format) {
            this.label = label;
            this.getter = getter;
            this.setter = setter;
            this.min = min;
            this.max = max;
            this.format = format;
        }

        @Override
        void render(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY) {
            // SliderWidget constructor: (int x, int y, int width, int height, Text label, double value)
            slider = new SliderWidget(
                x, y, width, height,
                Text.literal(""),
                getter.get()
            ) {
                @Override
                protected void applyValue() {
                    setter.accept((float)this.value);
                }
                @Override
                protected void updateMessage() {
                    this.setMessage(Text.literal(label.getString() + ": " + String.format(format, this.value)));
                }
            };
            slider.setMessage(Text.literal(label.getString() + ": " + String.format(format, getter.get())));
            slider.render(context, mouseX, mouseY, 0);
        }
    }

    static class KeybindEntry extends ConfigEntry {
        final Text label;
        final KeyBinding keyBinding;
        private ButtonWidget button;
        private boolean waitingForKey = false;

        KeybindEntry(Text label, KeyBinding keyBinding) {
            this.label = label;
            this.keyBinding = keyBinding;
        }

        @Override
        void render(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY) {
            if (button == null) {
                button = ButtonWidget.builder(Text.literal(""), btn -> {
                    waitingForKey = true;
                    btn.setMessage(Text.literal("§e> PRESS KEY <"));
                }).dimensions(x + width - 100, y, 94, height).build();
            } else {
                button.setX(x + width - 100);
                button.setY(y);
            }

            if (waitingForKey) {
                button.setMessage(Text.literal("§e> PRESS KEY <"));
            } else {
                String keyName = keyBinding.getBoundKeyLocalizedText().getString();
                button.setMessage(Text.literal(keyName.isEmpty() ? "§7NONE" : "§f" + keyName));
            }
            button.render(context, mouseX, mouseY, 0);

            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, label, x + 4, y + (height - MinecraftClient.getInstance().textRenderer.fontHeight) / 2, 0xFFFFFFFF);
        }

        // Handle key input - key remapping not fully implemented in this version
        boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (waitingForKey && keyCode != GLFW.GLFW_KEY_ESCAPE) {
                // Key remapping would go here - requires Key object conversion
                waitingForKey = false;
                return true;
            } else if (waitingForKey && keyCode == GLFW.GLFW_KEY_ESCAPE) {
                waitingForKey = false;
                return true;
            }
            return false;
        }
    }
}