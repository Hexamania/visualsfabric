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
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
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

    private static Framebuffer postProcessFramebuffer;
    private static Framebuffer pingPongFramebuffer;

    public static boolean bloomEnabled = true;
    public static boolean chromaticAberrationEnabled = true;
    public static boolean filmGrainEnabled = true;
    public static boolean vignetteEnabled = true;
    public static boolean colorGradingEnabled = true;
    public static boolean depthOfFieldEnabled = false;
    public static boolean motionBlurEnabled = false;
    public static boolean ambientOcclusionEnhanced = true;

    public static boolean skyEffectsEnabled = true;
    public static boolean enhancedSunMoon = true;
    public static boolean starFieldEnabled = true;
    public static boolean skyColorGrading = true;
    public static boolean auroraEnabled = false;

    public static boolean spotifyHudEnabled = false;
    public static boolean spotifyLyricsInSky = false;

    public static boolean hitEffectsEnabled = true;
    public static boolean killEffectsEnabled = true;
    public static boolean hitParticlesEnabled = true;
    public static boolean killParticlesEnabled = true;
    public static boolean hitScreenFlashEnabled = true;
    public static boolean killScreenFlashEnabled = true;

    public static boolean performanceMode = false;
    public static boolean lowQualityShaders = false;
    public static boolean reducedParticles = false;

    public static float bloomIntensity = 0.6f;
    public static float bloomThreshold = 0.8f;
    public static float chromaticAberrationIntensity = 0.02f;
    public static float filmGrainIntensity = 0.15f;
    public static float vignetteIntensity = 0.4f;
    public static float saturation = 1.1f;
    public static float contrast = 1.05f;
    public static float brightness = 1.0f;
    public static float gamma = 1.0f;

    public static float starBrightness = 1.0f;
    public static float starDensity = 0.5f;
    public static float sunMoonScale = 1.0f;
    public static float auroraIntensity = 0.3f;
    public static float skyColorIntensity = 0.5f;

    public static float spotifyHudScale = 1.0f;
    public static float spotifyHudX = 0.5f;
    public static float spotifyHudY = 0.9f;

    public static float hitEffectIntensity = 1.0f;
    public static float killEffectIntensity = 1.0f;
    public static float hitScreenFlashIntensity = 0.3f;
    public static float killScreenFlashIntensity = 0.5f;

    private static float effectTime = 0f;
    private static float hitFlashTimer = 0f;
    private static float killFlashTimer = 0f;
    private static Vec3d lastHitPos = null;
    private static Vec3d lastKillPos = null;

    public static String currentTrackName = "Unknown Track";
    public static String currentArtistName = "Unknown Artist";
    public static String currentLyrics = "";
    public static float trackProgress = 0f;
    public static float trackDuration = 0f;

    public static KeyBinding toggleBloomKey;
    public static KeyBinding toggleChromaticAberrationKey;
    public static KeyBinding toggleFilmGrainKey;
    public static KeyBinding toggleVignetteKey;
    public static KeyBinding toggleColorGradingKey;
    public static KeyBinding toggleDepthOfFieldKey;
    public static KeyBinding toggleMotionBlurKey;
    public static KeyBinding toggleAOKey;
    public static KeyBinding openConfigKey;
    public static KeyBinding toggleSkyEffectsKey;
    public static KeyBinding toggleEnhancedSunMoonKey;
    public static KeyBinding toggleStarFieldKey;
    public static KeyBinding toggleAuroraKey;
    public static KeyBinding toggleSpotifyHudKey;
    public static KeyBinding toggleSpotifyLyricsKey;
    public static KeyBinding toggleHitEffectsKey;
    public static KeyBinding toggleKillEffectsKey;
    public static KeyBinding togglePerformanceModeKey;

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
        toggleBloomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_bloom", GLFW.GLFW_KEY_B, "category.visualsfabric.visuals"));
        toggleChromaticAberrationKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_chromatic_aberration", GLFW.GLFW_KEY_C, "category.visualsfabric.visuals"));
        toggleFilmGrainKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_film_grain", GLFW.GLFW_KEY_G, "category.visualsfabric.visuals"));
        toggleVignetteKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_vignette", GLFW.GLFW_KEY_V, "category.visualsfabric.visuals"));
        toggleColorGradingKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_color_grading", GLFW.GLFW_KEY_R, "category.visualsfabric.visuals"));
        toggleDepthOfFieldKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_dof", GLFW.GLFW_KEY_D, "category.visualsfabric.visuals"));
        toggleMotionBlurKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_motion_blur", GLFW.GLFW_KEY_M, "category.visualsfabric.visuals"));
        toggleAOKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_ao", GLFW.GLFW_KEY_A, "category.visualsfabric.visuals"));
        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.open_config", GLFW.GLFW_KEY_F6, "category.visualsfabric.general"));
        toggleSkyEffectsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_sky_effects", GLFW.GLFW_KEY_K, "category.visualsfabric.sky"));
        toggleEnhancedSunMoonKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_enhanced_sun_moon", GLFW.GLFW_KEY_L, "category.visualsfabric.sky"));
        toggleStarFieldKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_star_field", GLFW.GLFW_KEY_SEMICOLON, "category.visualsfabric.sky"));
        toggleAuroraKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_aurora", GLFW.GLFW_KEY_APOSTROPHE, "category.visualsfabric.sky"));
        toggleSpotifyHudKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_spotify_hud", GLFW.GLFW_KEY_P, "category.visualsfabric.spotify"));
        toggleSpotifyLyricsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_spotify_lyrics", GLFW.GLFW_KEY_O, "category.visualsfabric.spotify"));
        toggleHitEffectsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_hit_effects", GLFW.GLFW_KEY_H, "category.visualsfabric.hitkill"));
        toggleKillEffectsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_kill_effects", GLFW.GLFW_KEY_J, "category.visualsfabric.hitkill"));
        togglePerformanceModeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_performance_mode", GLFW.GLFW_KEY_F7, "category.visualsfabric.performance"));
    }

    private void registerTickEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            effectTime += 0.016f;
            if (effectTime > 10000f) effectTime = 0f;
            if (hitFlashTimer > 0) hitFlashTimer -= 0.016f;
            if (killFlashTimer > 0) killFlashTimer -= 0.016f;

            while (toggleBloomKey.wasPressed()) { bloomEnabled = !bloomEnabled; sendChatFeedback("Bloom", bloomEnabled); }
            while (toggleChromaticAberrationKey.wasPressed()) { chromaticAberrationEnabled = !chromaticAberrationEnabled; sendChatFeedback("Chromatic Aberration", chromaticAberrationEnabled); }
            while (toggleFilmGrainKey.wasPressed()) { filmGrainEnabled = !filmGrainEnabled; sendChatFeedback("Film Grain", filmGrainEnabled); }
            while (toggleVignetteKey.wasPressed()) { vignetteEnabled = !vignetteEnabled; sendChatFeedback("Vignette", vignetteEnabled); }
            while (toggleColorGradingKey.wasPressed()) { colorGradingEnabled = !colorGradingEnabled; sendChatFeedback("Color Grading", colorGradingEnabled); }
            while (toggleDepthOfFieldKey.wasPressed()) { depthOfFieldEnabled = !depthOfFieldEnabled; sendChatFeedback("Depth of Field", depthOfFieldEnabled); }
            while (toggleMotionBlurKey.wasPressed()) { motionBlurEnabled = !motionBlurEnabled; sendChatFeedback("Motion Blur", motionBlurEnabled); }
            while (toggleAOKey.wasPressed()) { ambientOcclusionEnhanced = !ambientOcclusionEnhanced; sendChatFeedback("Enhanced AO", ambientOcclusionEnhanced); }
            while (openConfigKey.wasPressed()) { if (client.currentScreen == null) client.setScreen(new VisualsFabricConfigScreen()); }

            while (toggleSkyEffectsKey.wasPressed()) { skyEffectsEnabled = !skyEffectsEnabled; sendChatFeedback("Sky Effects", skyEffectsEnabled); }
            while (toggleEnhancedSunMoonKey.wasPressed()) { enhancedSunMoon = !enhancedSunMoon; sendChatFeedback("Enhanced Sun/Moon", enhancedSunMoon); }
            while (toggleStarFieldKey.wasPressed()) { starFieldEnabled = !starFieldEnabled; sendChatFeedback("Star Field", starFieldEnabled); }
            while (toggleAuroraKey.wasPressed()) { auroraEnabled = !auroraEnabled; sendChatFeedback("Aurora", auroraEnabled); }
            while (toggleSpotifyHudKey.wasPressed()) { spotifyHudEnabled = !spotifyHudEnabled; sendChatFeedback("Spotify HUD", spotifyHudEnabled); }
            while (toggleSpotifyLyricsKey.wasPressed()) { spotifyLyricsInSky = !spotifyLyricsInSky; sendChatFeedback("Spotify Lyrics in Sky", spotifyLyricsInSky); }
            while (toggleHitEffectsKey.wasPressed()) { hitEffectsEnabled = !hitEffectsEnabled; sendChatFeedback("Hit Effects", hitEffectsEnabled); }
            while (toggleKillEffectsKey.wasPressed()) { killEffectsEnabled = !killEffectsEnabled; sendChatFeedback("Kill Effects", killEffectsEnabled); }
            while (togglePerformanceModeKey.wasPressed()) { performanceMode = !performanceMode; sendChatFeedback("Performance Mode", performanceMode); }
        });
    }

    private void sendChatFeedback(String effect, boolean enabled) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && client.inGameHud != null) {
            client.inGameHud.getChatHud().addMessage(Text.literal("§b[VisualsFabric] §r" + effect + ": " + (enabled ? "§aON" : "§cOFF")));
        }
    }

    private void registerRenderEvents() {
        WorldRenderEvents.AFTER_ENTITIES.register(this::renderPostProcessing);
        WorldRenderEvents.LAST.register(this::renderOverlayEffects);
        WorldRenderEvents.BEFORE_ENTITIES.register(this::renderSkyEffects);
    }

    private boolean shouldApplyPostProcessing() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.world != null && client.player != null && (bloomEnabled || chromaticAberrationEnabled || depthOfFieldEnabled || motionBlurEnabled || colorGradingEnabled);
    }

    private void renderPostProcessing(WorldRenderContext context) {
        if (!shouldApplyPostProcessing()) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (postProcessFramebuffer == null || postProcessFramebuffer.textureWidth != client.getWindow().getFramebufferWidth() || postProcessFramebuffer.textureHeight != client.getWindow().getFramebufferHeight()) {
            createPostProcessFramebuffers(client);
        }
        client.getFramebuffer().beginRead();
        postProcessFramebuffer.beginWrite(true);
        blitFramebuffer(client.getFramebuffer(), postProcessFramebuffer);
        postProcessFramebuffer.endWrite();
        client.getFramebuffer().endRead();
        applyPostProcessingShaders(context.tickCounter().getTickDelta(false));
    }

    private void createPostProcessFramebuffers(MinecraftClient client) {
        if (postProcessFramebuffer != null) postProcessFramebuffer.delete();
        if (pingPongFramebuffer != null) pingPongFramebuffer.delete();
        int width = client.getWindow().getFramebufferWidth();
        int height = client.getWindow().getFramebufferHeight();
        if (performanceMode && lowQualityShaders) { width /= 2; height /= 2; }
        postProcessFramebuffer = new SimpleFramebuffer(width, height, true);
        postProcessFramebuffer.setClearColor(0, 0, 0, 0);
        pingPongFramebuffer = new SimpleFramebuffer(width, height, true);
        pingPongFramebuffer.setClearColor(0, 0, 0, 0);
    }

    private void blitFramebuffer(Framebuffer src, Framebuffer dst) {
        src.beginRead();
        RenderSystem.viewport(0, 0, dst.textureWidth, dst.textureHeight);
        RenderSystem.setShaderTexture(0, src.getColorAttachment());
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
        postProcessFramebuffer.beginRead();
        if (bloomEnabled && (!performanceMode || !lowQualityShaders)) applyBloomEffect();
        if (chromaticAberrationEnabled && (!performanceMode || !lowQualityShaders)) applyChromaticAberration();
        if (depthOfFieldEnabled && !performanceMode) applyDepthOfField(tickDelta);
        if (motionBlurEnabled && !performanceMode) applyMotionBlur();
        if (colorGradingEnabled) applyColorGrading();
        postProcessFramebuffer.endRead();
        client.getFramebuffer().beginWrite(true);
        blitFramebuffer(postProcessFramebuffer, client.getFramebuffer());
        client.getFramebuffer().endWrite();
    }

    private void applyBloomEffect() { RenderSystem.enableBlend(); RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE); RenderSystem.disableBlend(); }
    private void applyChromaticAberration() {}
    private void applyDepthOfField(float tickDelta) {}
    private void applyMotionBlur() {}
    private void applyColorGrading() { RenderSystem.enableBlend(); RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); RenderSystem.disableBlend(); }

    private void renderSkyEffects(WorldRenderContext context) {
        if (!skyEffectsEnabled) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;
        MatrixStack matrices = context.matrixStack();
        if (enhancedSunMoon) renderEnhancedSunMoon(matrices, context.tickCounter().getTickDelta(false));
        if (starFieldEnabled) renderStarField(matrices, context.tickCounter().getTickDelta(false));
        if (auroraEnabled && !performanceMode) renderAurora(matrices, context.tickCounter().getTickDelta(false));
        if (skyColorGrading) renderSkyColorGrading(matrices);
    }

    private void renderEnhancedSunMoon(MatrixStack matrices, float tickDelta) {}
    private void renderStarField(MatrixStack matrices, float tickDelta) {
        if (performanceMode && reducedParticles) return;
        RenderSystem.enableBlend(); RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        RenderSystem.setShaderColor(1, 1, 1, starBrightness * 0.8f);
        RenderSystem.setShaderColor(1, 1, 1, 1); RenderSystem.disableBlend();
    }
    private void renderAurora(MatrixStack matrices, float tickDelta) {
        if (performanceMode) return;
        RenderSystem.enableBlend(); RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        RenderSystem.setShaderColor(0.2f, 0.8f, 0.3f, auroraIntensity * 0.5f);
        RenderSystem.setShaderColor(1, 1, 1, 1); RenderSystem.disableBlend();
    }
    private void renderSkyColorGrading(MatrixStack matrices) {
        RenderSystem.enableBlend(); RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1, 1, 1, skyColorIntensity * 0.3f);
        RenderSystem.setShaderColor(1, 1, 1, 1); RenderSystem.disableBlend();
    }

    private void renderOverlayEffects(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        MatrixStack matrices = context.matrixStack();
        matrices.push(); matrices.translate(0, 0, 900);
        if (filmGrainEnabled) renderFilmGrain(matrices, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
        if (vignetteEnabled) renderVignette(matrices, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
        if (hitEffectsEnabled && hitScreenFlashEnabled && hitFlashTimer > 0) renderHitScreenFlash(matrices, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
        if (killEffectsEnabled && killScreenFlashEnabled && killFlashTimer > 0) renderKillScreenFlash(matrices, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
        if (spotifyHudEnabled) renderSpotifyHUD(matrices, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
        if (spotifyLyricsInSky && !currentLyrics.isEmpty()) renderSpotifyLyricsInSky(matrices, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
        matrices.pop();
    }

    private void renderFilmGrain(MatrixStack matrices, int width, int height) {
        if (performanceMode && reducedParticles) return;
        RenderSystem.enableBlend(); RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1, 1, 1, filmGrainIntensity * 0.5f);
        RenderSystem.setShaderColor(1, 1, 1, 1); RenderSystem.disableBlend();
    }
    private void renderVignette(MatrixStack matrices, int width, int height) {
        RenderSystem.enableBlend(); RenderSystem.blendFunc(GL11.GL_ZERO, GL11.GL_SRC_COLOR);
        RenderSystem.setShaderColor(1, 1, 1, 1); RenderSystem.setShaderColor(1, 1, 1, 1); RenderSystem.disableBlend();
    }
    private void renderHitScreenFlash(MatrixStack matrices, int width, int height) {
        float alpha = hitScreenFlashIntensity * (hitFlashTimer / 0.2f);
        RenderSystem.enableBlend(); RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        RenderSystem.setShaderColor(1, 0.2f, 0.2f, alpha);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        buffer.vertex(0, height, 0); buffer.vertex(width, height, 0); buffer.vertex(width, 0, 0); buffer.vertex(0, 0, 0);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.setShaderColor(1, 1, 1, 1); RenderSystem.disableBlend();
    }
    private void renderKillScreenFlash(MatrixStack matrices, int width, int height) {
        float alpha = killScreenFlashIntensity * (killFlashTimer / 0.3f);
        RenderSystem.enableBlend(); RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        RenderSystem.setShaderColor(1, 0.8f, 0.2f, alpha);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        buffer.vertex(0, height, 0); buffer.vertex(width, height, 0); buffer.vertex(width, 0, 0); buffer.vertex(0, 0, 0);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.setShaderColor(1, 1, 1, 1); RenderSystem.disableBlend();
    }
    private void renderSpotifyHUD(MatrixStack matrices, int width, int height) {
        if (performanceMode && reducedParticles) return;
        RenderSystem.enableBlend(); RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        int hudX = (int)(width * spotifyHudX); int hudY = (int)(height * spotifyHudY); float scale = spotifyHudScale;
        RenderSystem.setShaderColor(0, 0, 0, 0.7f);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        int hudWidth = (int)(300 * scale); int hudHeight = (int)(80 * scale);
        buffer.vertex(hudX - hudWidth/2, hudY + hudHeight, 0); buffer.vertex(hudX + hudWidth/2, hudY + hudHeight, 0);
        buffer.vertex(hudX + hudWidth/2, hudY, 0); buffer.vertex(hudX - hudWidth/2, hudY, 0);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.setShaderColor(1, 1, 1, 1); RenderSystem.disableBlend();
    }
    private void renderSpotifyLyricsInSky(MatrixStack matrices, int width, int height) {
        int centerX = width / 2; int skyY = height / 8;
        RenderSystem.enableBlend(); RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1, 1, 1, 0.9f);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        buffer.vertex(centerX - 200, skyY + 40, 0); buffer.vertex(centerX + 200, skyY + 40, 0);
        buffer.vertex(centerX + 200, skyY - 40, 0); buffer.vertex(centerX - 200, skyY - 40, 0);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.setShaderColor(1, 1, 1, 1); RenderSystem.disableBlend();
    }

    public static void onHitEntity(Vec3d hitPos) {
        if (hitEffectsEnabled) { hitFlashTimer = 0.2f; lastHitPos = hitPos; if (hitParticlesEnabled && !performanceMode) spawnHitParticles(hitPos); }
    }
    public static void onKillEntity(Vec3d killPos) {
        if (killEffectsEnabled) { killFlashTimer = 0.3f; lastKillPos = killPos; if (killParticlesEnabled && !performanceMode) spawnKillParticles(killPos); }
    }
    private static void spawnHitParticles(Vec3d pos) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null && client.particleManager != null) {
            for (int i = 0; i < (performanceMode || reducedParticles ? 3 : 8); i++) {
                double vx = (Math.random() - 0.5) * 0.2; double vy = Math.random() * 0.3; double vz = (Math.random() - 0.5) * 0.2;
                client.particleManager.addParticle(ParticleTypes.DAMAGE_INDICATOR, pos.x, pos.y, pos.z, vx, vy, vz);
            }
        }
    }
    private static void spawnKillParticles(Vec3d pos) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null && client.particleManager != null) {
            for (int i = 0; i < (performanceMode || reducedParticles ? 5 : 15); i++) {
                double vx = (Math.random() - 0.5) * 0.5; double vy = Math.random() * 0.5; double vz = (Math.random() - 0.5) * 0.5;
                client.particleManager.addParticle(ParticleTypes.EXPLOSION, pos.x, pos.y, pos.z, vx, vy, vz);
                client.particleManager.addParticle(ParticleTypes.SOUL_FIRE_FLAME, pos.x, pos.y, pos.z, vx * 0.5, vy * 0.5, vz * 0.5);
            }
        }
    }
    public static void setSpotifyTrack(String track, String artist, String lyrics, float progress, float duration) {
        currentTrackName = track; currentArtistName = artist; currentLyrics = lyrics; trackProgress = progress; trackDuration = duration;
    }

    private void registerResourcePack() { ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new VisualsFabricResourceReloadListener()); }
    private void loadShaderPrograms() { LOGGER.info("Loading VisualsFabric shader programs..."); }

    public static float getEffectTime() { return effectTime; }
    public static boolean isBloomEnabled() { return bloomEnabled; }
    public static boolean isChromaticAberrationEnabled() { return chromaticAberrationEnabled; }
    public static boolean isFilmGrainEnabled() { return filmGrainEnabled; }
    public static boolean isVignetteEnabled() { return vignetteEnabled; }
    public static boolean isColorGradingEnabled() { return colorGradingEnabled; }
    public static boolean isDepthOfFieldEnabled() { return depthOfFieldEnabled; }
    public static boolean isMotionBlurEnabled() { return motionBlurEnabled; }
    public static boolean isAmbientOcclusionEnhanced() { return ambientOcclusionEnhanced; }
    public static boolean isSkyEffectsEnabled() { return skyEffectsEnabled; }
    public static boolean isEnhancedSunMoonEnabled() { return enhancedSunMoon; }
    public static boolean isStarFieldEnabled() { return starFieldEnabled; }
    public static boolean isAuroraEnabled() { return auroraEnabled; }
    public static boolean isSkyColorGradingEnabled() { return skyColorGrading; }
    public static boolean isSpotifyHudEnabled() { return spotifyHudEnabled; }
    public static boolean isSpotifyLyricsInSkyEnabled() { return spotifyLyricsInSky; }
    public static boolean isHitEffectsEnabled() { return hitEffectsEnabled; }
    public static boolean isKillEffectsEnabled() { return killEffectsEnabled; }
    public static boolean isHitParticlesEnabled() { return hitParticlesEnabled; }
    public static boolean isKillParticlesEnabled() { return killParticlesEnabled; }
    public static boolean isHitScreenFlashEnabled() { return hitScreenFlashEnabled; }
    public static boolean isKillScreenFlashEnabled() { return killScreenFlashEnabled; }
    public static boolean isPerformanceModeEnabled() { return performanceMode; }
    public static boolean isLowQualityShadersEnabled() { return lowQualityShaders; }
    public static boolean isReducedParticlesEnabled() { return reducedParticles; }
}

class VisualsFabricResourceReloadListener implements IdentifiableResourceReloadListener {
    private static final Identifier ID = Identifier.of(VisualsFabricClient.MOD_ID, "resource_reload_listener");
    @Override public Identifier getFabricId() { return ID; }
    @Override public Collection<Identifier> getFabricDependencies() { return Collections.emptyList(); }
    @Override public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager resourceManager, Executor prepareExecutor, Executor applyExecutor) {
        return CompletableFuture.supplyAsync(() -> { VisualsFabricClient.LOGGER.info("Reloading VisualsFabric resources"); return null; }, prepareExecutor);
    }
}