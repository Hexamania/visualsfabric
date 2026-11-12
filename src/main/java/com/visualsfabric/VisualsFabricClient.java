package com.visualsfabric;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.apache.commons.io.IOUtils;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class VisualsFabricClient implements ClientModInitializer {
    public static final String MOD_ID = "visualsfabric";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Post-processing framebuffer for custom shaders
    private static Framebuffer postProcessFramebuffer;
    private static Framebuffer pingPongFramebuffer;

    // Visual effect toggles
    public static boolean bloomEnabled = true;
    public static boolean chromaticAberrationEnabled = true;
    public static boolean filmGrainEnabled = true;
    public static boolean vignetteEnabled = true;
    public static boolean colorGradingEnabled = true;
    public static boolean depthOfFieldEnabled = false;
    public static boolean motionBlurEnabled = false;
    public static boolean ambientOcclusionEnhanced = true;

    // Effect intensity settings
    public static float bloomIntensity = 0.6f;
    public static float bloomThreshold = 0.8f;
    public static float chromaticAberrationIntensity = 0.02f;
    public static float filmGrainIntensity = 0.15f;
    public static float vignetteIntensity = 0.4f;
    public static float saturation = 1.1f;
    public static float contrast = 1.05f;
    public static float brightness = 1.0f;
    public static float gamma = 1.0f;

    // Time for animated effects
    private static float effectTime = 0f;

    // Key bindings
    public static KeyBinding toggleBloomKey;
    public static KeyBinding toggleChromaticAberrationKey;
    public static KeyBinding toggleFilmGrainKey;
    public static KeyBinding toggleVignetteKey;
    public static KeyBinding toggleColorGradingKey;
    public static KeyBinding toggleDepthOfFieldKey;
    public static KeyBinding toggleMotionBlurKey;
    public static KeyBinding toggleAOKey;
    public static KeyBinding openConfigKey;

    @Override
    public void onInitializeClient() {
        LOGGER.info("VisualsFabric Client initialized - Setting up visual pipeline");

        registerKeyBindings();
        registerTickEvents();
        registerRenderEvents();
        registerResourcePack();
        loadShaderPrograms();
    }

    private void registerKeyBindings() {
        toggleBloomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.visualsfabric.toggle_bloom", GLFW.GLFW_KEY_B, "category.visualsfabric.visuals"
        ));
        toggleChromaticAberrationKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.visualsfabric.toggle_chromatic_aberration", GLFW.GLFW_KEY_C, "category.visualsfabric.visuals"
        ));
        toggleFilmGrainKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.visualsfabric.toggle_film_grain", GLFW.GLFW_KEY_G, "category.visualsfabric.visuals"
        ));
        toggleVignetteKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.visualsfabric.toggle_vignette", GLFW.GLFW_KEY_V, "category.visualsfabric.visuals"
        ));
        toggleColorGradingKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.visualsfabric.toggle_color_grading", GLFW.GLFW_KEY_R, "category.visualsfabric.visuals"
        ));
        toggleDepthOfFieldKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.visualsfabric.toggle_dof", GLFW.GLFW_KEY_D, "category.visualsfabric.visuals"
        ));
        toggleMotionBlurKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.visualsfabric.toggle_motion_blur", GLFW.GLFW_KEY_M, "category.visualsfabric.visuals"
        ));
        toggleAOKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.visualsfabric.toggle_ao", GLFW.GLFW_KEY_A, "category.visualsfabric.visuals"
        ));
        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.visualsfabric.open_config", GLFW.GLFW_KEY_F6, "category.visualsfabric.general"
        ));
    }

    private void registerTickEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Update effect time for animations
            effectTime += 0.016f; // ~60 FPS
            if (effectTime > 10000f) effectTime = 0f;

            // Handle key presses
            while (toggleBloomKey.wasPressed()) {
                bloomEnabled = !bloomEnabled;
                sendChatFeedback("Bloom", bloomEnabled);
            }
            while (toggleChromaticAberrationKey.wasPressed()) {
                chromaticAberrationEnabled = !chromaticAberrationEnabled;
                sendChatFeedback("Chromatic Aberration", chromaticAberrationEnabled);
            }
            while (toggleFilmGrainKey.wasPressed()) {
                filmGrainEnabled = !filmGrainEnabled;
                sendChatFeedback("Film Grain", filmGrainEnabled);
            }
            while (toggleVignetteKey.wasPressed()) {
                vignetteEnabled = !vignetteEnabled;
                sendChatFeedback("Vignette", vignetteEnabled);
            }
            while (toggleColorGradingKey.wasPressed()) {
                colorGradingEnabled = !colorGradingEnabled;
                sendChatFeedback("Color Grading", colorGradingEnabled);
            }
            while (toggleDepthOfFieldKey.wasPressed()) {
                depthOfFieldEnabled = !depthOfFieldEnabled;
                sendChatFeedback("Depth of Field", depthOfFieldEnabled);
            }
            while (toggleMotionBlurKey.wasPressed()) {
                motionBlurEnabled = !motionBlurEnabled;
                sendChatFeedback("Motion Blur", motionBlurEnabled);
            }
            while (toggleAOKey.wasPressed()) {
                ambientOcclusionEnhanced = !ambientOcclusionEnhanced;
                sendChatFeedback("Enhanced AO", ambientOcclusionEnhanced);
            }
            while (openConfigKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new VisualsFabricConfigScreen());
                }
            }
        });
    }

    private void sendChatFeedback(String effect, boolean enabled) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && client.inGameHud != null) {
            client.inGameHud.getChatHud().addMessage(
                Text.literal("§b[VisualsFabric] §r" + effect + ": " + (enabled ? "§aON" : "§cOFF"))
            );
        }
    }

    private void registerRenderEvents() {
        WorldRenderEvents.AFTER_ENTITIES.register(this::renderPostProcessing);
        WorldRenderEvents.LAST.register(this::renderOverlayEffects);
    }

    private boolean shouldApplyPostProcessing() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.world != null && client.player != null &&
               (bloomEnabled || chromaticAberrationEnabled || depthOfFieldEnabled || motionBlurEnabled || colorGradingEnabled);
    }

    private void renderPostProcessing(WorldRenderContext context) {
        if (!shouldApplyPostProcessing()) return;

        MinecraftClient client = MinecraftClient.getInstance();

        // Initialize framebuffers if needed
        if (postProcessFramebuffer == null ||
            postProcessFramebuffer.textureWidth != client.getWindow().getFramebufferWidth() ||
            postProcessFramebuffer.textureHeight != client.getWindow().getFramebufferHeight()) {
            createPostProcessFramebuffers(client);
        }

        // Copy main framebuffer to post-process framebuffer
        client.getFramebuffer().beginRead();
        postProcessFramebuffer.beginWrite(true);
        blitFramebuffer(client.getFramebuffer(), postProcessFramebuffer);
        postProcessFramebuffer.endWrite();
        client.getFramebuffer().endRead();

        // Apply post-processing shaders
        applyPostProcessingShaders(context.tickCounter().getTickDelta(false));
    }

    private void createPostProcessFramebuffers(MinecraftClient client) {
        if (postProcessFramebuffer != null) {
            postProcessFramebuffer.delete();
        }
        if (pingPongFramebuffer != null) {
            pingPongFramebuffer.delete();
        }
        int width = client.getWindow().getFramebufferWidth();
        int height = client.getWindow().getFramebufferHeight();
        postProcessFramebuffer = new SimpleFramebuffer(width, height, true);
        postProcessFramebuffer.setClearColor(0, 0, 0, 0);
        pingPongFramebuffer = new SimpleFramebuffer(width, height, true);
        pingPongFramebuffer.setClearColor(0, 0, 0, 0);
    }

    private void blitFramebuffer(Framebuffer src, Framebuffer dst) {
        src.beginRead();
        RenderSystem.viewport(0, 0, dst.textureWidth, dst.textureHeight);
        RenderSystem.setShaderTexture(0, src.getColorAttachment());
        // Draw quad using immediate mode with default shader
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(0, dst.textureHeight, 0).texture(0, 1);
        buffer.vertex(dst.textureWidth, dst.textureHeight, 0).texture(1, 1);
        buffer.vertex(dst.textureWidth, 0, 0).texture(1, 0);
        buffer.vertex(0, 0, 0).texture(0, 0);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        src.endRead();
    }

    private void applyPostProcessingShaders(float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();

        // Bind the post-process framebuffer for reading
        postProcessFramebuffer.beginRead();

        // Apply bloom if enabled
        if (bloomEnabled) {
            applyBloomEffect();
        }

        // Apply chromatic aberration if enabled
        if (chromaticAberrationEnabled) {
            applyChromaticAberration();
        }

        // Apply depth of field if enabled
        if (depthOfFieldEnabled) {
            applyDepthOfField(tickDelta);
        }

        // Apply motion blur if enabled
        if (motionBlurEnabled) {
            applyMotionBlur();
        }

        // Apply color grading if enabled
        if (colorGradingEnabled) {
            applyColorGrading();
        }

        postProcessFramebuffer.endRead();

        // Blit result back to main framebuffer
        client.getFramebuffer().beginWrite(true);
        blitFramebuffer(postProcessFramebuffer, client.getFramebuffer());
        client.getFramebuffer().endWrite();
    }

    private void applyBloomEffect() {
        // Bloom implementation using separable Gaussian blur
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        RenderSystem.disableBlend();
    }

    private void applyChromaticAberration() {
        // Chromatic aberration - offset RGB channels slightly
    }

    private void applyDepthOfField(float tickDelta) {
        // Depth of field using depth buffer
    }

    private void applyMotionBlur() {
        // Motion blur using velocity buffer or frame accumulation
    }

    private void applyColorGrading() {
        // Color grading: saturation, contrast, brightness, gamma
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableBlend();
    }

    private void renderOverlayEffects(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        MatrixStack matrices = context.matrixStack();

        matrices.push();
        matrices.translate(0, 0, 900); // Render on top

        // Film grain overlay
        if (filmGrainEnabled) {
            renderFilmGrain(matrices, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
        }

        // Vignette overlay
        if (vignetteEnabled) {
            renderVignette(matrices, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
        }

        matrices.pop();
    }

    private void renderFilmGrain(MatrixStack matrices, int width, int height) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1, 1, 1, filmGrainIntensity * 0.5f);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }

    private void renderVignette(MatrixStack matrices, int width, int height) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_ZERO, GL11.GL_SRC_COLOR);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }

    private void registerResourcePack() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new VisualsFabricResourceReloadListener());
    }

    private void loadShaderPrograms() {
        LOGGER.info("Loading VisualsFabric shader programs...");
    }

    // Getters for config screen
    public static float getEffectTime() { return effectTime; }
    public static boolean isBloomEnabled() { return bloomEnabled; }
    public static boolean isChromaticAberrationEnabled() { return chromaticAberrationEnabled; }
    public static boolean isFilmGrainEnabled() { return filmGrainEnabled; }
    public static boolean isVignetteEnabled() { return vignetteEnabled; }
    public static boolean isColorGradingEnabled() { return colorGradingEnabled; }
    public static boolean isDepthOfFieldEnabled() { return depthOfFieldEnabled; }
    public static boolean isMotionBlurEnabled() { return motionBlurEnabled; }
    public static boolean isAmbientOcclusionEnhanced() { return ambientOcclusionEnhanced; }
}

class VisualsFabricResourceReloadListener implements IdentifiableResourceReloadListener {
    private static final Identifier ID = Identifier.of(VisualsFabricClient.MOD_ID, "resource_reload_listener");

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public Collection<Identifier> getFabricDependencies() {
        return Collections.emptyList();
    }

    @Override
    public CompletableFuture<Void> reload(Synchronizer synchronizer,
                                           ResourceManager resourceManager,
                                           Executor prepareExecutor,
                                           Executor applyExecutor) {
        return CompletableFuture.supplyAsync(() -> {
            VisualsFabricClient.LOGGER.info("Reloading VisualsFabric resources");
            return null;
        }, prepareExecutor);
    }
}