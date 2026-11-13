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
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
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

    // Core Visual Effects
    public static boolean bloomEnabled = true;
    public static boolean chromaticAberrationEnabled = true;
    public static boolean filmGrainEnabled = true;
    public static boolean vignetteEnabled = true;
    public static boolean colorGradingEnabled = true;
    public static boolean depthOfFieldEnabled = false;
    public static boolean motionBlurEnabled = false;
    public static boolean ambientOcclusionEnhanced = true;

    // Sky Effects
    public static boolean skyEffectsEnabled = true;
    public static boolean enhancedSunMoon = true;
    public static boolean starFieldEnabled = true;
    public static boolean skyColorGrading = true;
    public static boolean auroraEnabled = false;

    // Spotify HUD
    public static boolean spotifyHudEnabled = false;
    public static boolean spotifyLyricsInSky = false;

    // Hit & Kill Effects
    public static boolean hitEffectsEnabled = true;
    public static boolean killEffectsEnabled = true;
    public static boolean hitParticlesEnabled = true;
    public static boolean killParticlesEnabled = true;
    public static boolean hitScreenFlashEnabled = true;
    public static boolean killScreenFlashEnabled = true;

    // New: Dynamic FOV
    public static boolean dynamicFovEnabled = true;
    public static boolean sprintFovBoost = true;
    public static boolean elytraFovBoost = true;
    public static boolean speedFovEnabled = true;

    // New: Damage Indicators
    public static boolean damageIndicatorsEnabled = true;
    public static boolean damageNumbersEnabled = false;

    // New: Crosshair Customization
    public static boolean customCrosshairEnabled = true;
    public static int crosshairStyle = 0; // 0=Default, 1=Dot, 2=Cross, 3=Circle, 4=Dynamic
    public static float crosshairSize = 1.0f;
    public static float crosshairGap = 0f;
    public static float crosshairThickness = 1.0f;
    public static float crosshairColorR = 1f, crosshairColorG = 1f, crosshairColorB = 1f, crosshairColorA = 0.8f;
    public static boolean crosshairDynamic = false;

    // New: Weather Enhancements
    public static boolean weatherEnhanced = true;
    public static float rainIntensity = 1.0f;
    public static float snowIntensity = 1.0f;
    public static float fogDensity = 1.0f;
    public static boolean fogColorEnabled = false;

    // New: Water Caustics
    public static boolean waterCausticsEnabled = true;
    public static float causticsIntensity = 0.5f;
    public static float causticsSpeed = 1.0f;

    // New: Block Highlight/Outline
    public static boolean blockHighlightEnabled = true;
    public static float blockHighlightR = 1.0f, blockHighlightG = 1.0f, blockHighlightB = 0.0f, blockHighlightA = 0.6f;
    public static float blockOutlineWidth = 2.0f;
    public static boolean blockHighlightPulseEnabled = true;
    public static float blockHighlightPulseSpeed = 2.0f;
    public static float blockHighlightPulseMinAlpha = 0.3f;
    public static float blockHighlightPulseMaxAlpha = 0.8f;
    public static boolean blockHighlightOres = true;
    public static boolean blockHighlightChests = true;
    public static boolean blockHighlightMobs = false;
    public static boolean blockHighlightPlayers = false;
    public static float blockHighlightRange = 64.0f;
    public static int blockHighlightMode = 0; // 0=Outline, 1=Fill, 2=Both
    public static boolean blockHighlightThroughWalls = false;

    // New: Color Grading Presets
    public static int colorGradingPreset = 0; // 0=Custom, 1=Cinematic, 2=Vibrant, 3=Moody, 4=Retro, 5=Noir, 6=Sepia

    // New: Biome-specific Color Grading
    public static boolean biomeColorGradingEnabled = true;
    public static float biomeColorGradingIntensity = 0.5f;
    public static boolean biomeColorGradingSmoothTransitions = true;
    public static float biomeColorGradingTransitionSpeed = 2.0f;

    // New: Entity Outline/Glow
    public static boolean entityOutlineEnabled = true;
    public static boolean entityOutlineThroughWalls = true;
    public static float entityOutlineWidth = 2.0f;
    public static float entityOutlineHostileColorR = 1.0f, entityOutlineHostileColorG = 0.0f, entityOutlineHostileColorB = 0.0f, entityOutlineHostileColorA = 0.8f;
    public static float entityOutlinePassiveColorR = 0.0f, entityOutlinePassiveColorG = 1.0f, entityOutlinePassiveColorB = 0.0f, entityOutlinePassiveColorA = 0.6f;
    public static float entityOutlinePlayerColorR = 0.0f, entityOutlinePlayerColorG = 0.5f, entityOutlinePlayerColorB = 1.0f, entityOutlinePlayerColorA = 0.7f;

    // New: Speed Lines
    public static boolean speedLinesEnabled = true;
    public static float speedLinesThreshold = 0.3f;
    public static float speedLinesIntensity = 1.0f;

    // New: Screenshot/Photo Mode
    public static boolean photoModeEnabled = false;
    public static boolean photoModeHideHud = true;
    public static boolean photoModeFreezeTime = false;

    // New: Chat Message Effects
    public static boolean chatEffectsEnabled = true;
    public static boolean chatFadeInEnabled = true;
    public static float chatFadeInDuration = 0.3f;
    public static boolean chatFadeOutEnabled = true;
    public static float chatFadeOutDuration = 0.5f;
    public static boolean chatBackgroundBlurEnabled = true;
    public static float chatBackgroundBlurIntensity = 0.5f;
    public static boolean chatTimestampEnabled = true;
    public static int chatAnimationType = 0; // 0=Fade, 1=Slide, 2=Scale, 3=Typewriter, 4=Glitch
    public static float chatAnimationSpeed = 1.0f;
    public static float chatMessageSpacing = 1.2f;
    public static boolean chatShadowEnabled = true;
    public static float chatShadowOffset = 2.0f;
    public static float chatMaxMessages = 100;

    // New: Time Effects
    public static boolean timeEffectsEnabled = true;
    public static boolean sunriseEnabled = true;
    public static boolean sunsetEnabled = true;
    public static boolean nightVisionEnabled = false;
    public static boolean goldenHourEnabled = true;
    public static float sunriseIntensity = 1.0f;
    public static float sunsetIntensity = 1.0f;
    public static float nightVisionIntensity = 0.5f;
    public static float goldenHourIntensity = 0.8f;
    public static float timeTransitionSpeed = 1.0f;

    // New: Player Trails / Afterimages
    public static boolean playerTrailsEnabled = true;
    public static int trailLength = 20;
    public static float trailWidth = 0.5f;
    public static float trailColorR = 1.0f;
    public static float trailColorG = 0.8f;
    public static float trailColorB = 0.2f;
    public static float trailColorA = 0.6f;
    public static boolean trailFadeEnabled = true;
    public static boolean afterimageEnabled = true;
    public static int afterimageCount = 5;
    public static float afterimageSpacing = 3f;
    public static float afterimageAlpha = 0.3f;
    public static boolean afterimageScale = true;
    public static boolean playerTrailParticlesEnabled = false;

    // Performance
    public static boolean performanceMode = false;
    public static boolean lowQualityShaders = false;
    public static boolean reducedParticles = false;

    // New: Shader Quality Presets
    public enum ShaderPreset {
        ULTRA(0),       // All effects max quality
        HIGH(1),        // Most effects, reduced samples
        MEDIUM(2),      // Balanced quality/performance
        LOW(3),         // Minimal effects
        PERFORMANCE(4), // Maximum performance
        CUSTOM(5);      // User-defined settings

        private final int id;
        ShaderPreset(int id) { this.id = id; }
        public int getId() { return id; }
        public static ShaderPreset fromId(int id) {
            for (ShaderPreset preset : values()) {
                if (preset.id == id) return preset;
            }
            return CUSTOM;
        }
    }

    public static ShaderPreset shaderPreset = ShaderPreset.HIGH;
    public static boolean customSettings = false; // When true, uses individual effect settings instead of preset

    // Shader preset settings (auto-applied based on preset)
    public static int shaderBloomSamples = 8;

    // ========== NEW FEATURES: Atmosphere & Volumetric ==========
    // Volumetric Fog / Atmospheric Scattering
    public static boolean volumetricFogEnabled = true;
    public static int volumetricFogQuality = 2; // 0=Low, 1=Medium, 2=High, 3=Ultra
    public static float volumetricFogDensity = 0.015f;
    public static float volumetricFogHeightFalloff = 0.001f;
    public static float volumetricFogScattering = 0.9f;
    public static float volumetricFogExtinction = 0.1f;
    public static float volumetricFogPhase = 0.7f;
    public static float volumetricFogSunIntensity = 1.0f;
    public static float volumetricFogNoiseScale = 0.005f;
    public static float volumetricFogNoiseSpeed = 0.1f;
    public static int volumetricFogLayerCount = 32;
    public static float volumetricFogStartHeight = 0.0f;
    public static float volumetricFogEndHeight = 256.0f;

    // Heat Haze / Mirage Effect
    public static boolean heatHazeEnabled = true;
    public static float heatHazeIntensity = 0.3f;
    public static float heatHazeHeight = 50.0f;
    public static float heatHazeFrequency = 0.02f;
    public static float heatHazeSpeed = 0.5f;
    public static String heatHazeBiomes = "desert,badlands,nether"; // comma-separated biome IDs
    public static float heatHazeDistortionScale = 0.15f;

    // ========== NEW FEATURES: Rendering Pipeline ==========
    // Screen Space Reflections (SSR)
    public static boolean ssrEnabled = false; // Disabled by default - expensive
    public static int ssrQuality = 2; // 0=Low, 1=Medium, 2=High, 3=Ultra
    public static int ssrMaxSteps = 64;
    public static float ssrThickness = 0.1f;
    public static float ssrFadeDistance = 50.0f;
    public static float ssrRoughnessThreshold = 0.3f;
    public static boolean ssrBinarySearch = true;
    public static boolean ssrTemporalAccumulation = true;
    public static boolean ssrDenoiserEnabled = false;

    // Temporal Anti-Aliasing (TAA)
    public static boolean taaEnabled = false; // Disabled by default - complex integration
    public static float taaJitterSpread = 0.5f;
    public static float taaSharpening = 0.3f;
    public static float taaHistoryWeight = 0.9f;
    public static float taaVelocityWeight = 0.1f;
    public static boolean taaClippingEnabled = true;
    public static boolean taaDithering = true;

    // Dynamic Cubemap Reflections
    public static boolean cubemapReflectionsEnabled = false;
    public static int cubemapResolution = 256;
    public static int cubemapUpdateRate = 2; // frames between updates
    public static String cubemapReflectiveBlocks = "water,ice,polished_blackstone,amethyst_block"; // comma-separated
    public static float cubemapRoughnessThreshold = 0.2f;
    public static boolean cubemapInteriorEnabled = false;
    public static boolean cubemapSkyReflection = true;
    public static boolean cubemapEntityReflection = false;

    // Parallax Occlusion Mapping
    public static boolean parallaxEnabled = false;
    public static float parallaxHeightScale = 0.1f;
    public static int parallaxMinSteps = 8;
    public static int parallaxMaxSteps = 32;
    public static String parallaxBlockList = "stone,brick,wood,deepslate"; // comma-separated
    public static boolean parallaxSelfShadowing = true;
    public static boolean parallaxSilhouetteClipping = true;

    // ========== NEW FEATURES: Weather & Atmosphere ==========
    // Dynamic Weather Transitions
    public static boolean weatherTransitionsEnabled = true;
    public static float transitionDuration = 30.0f; // seconds
    public static float cloudBuildupSpeed = 0.5f;
    public static float rainFadeSpeed = 1.0f;
    public static boolean thunderBuildupEnabled = true;
    public static boolean fogTransitionEnabled = true;
    public static boolean weatherParticleTransition = true;

    // Cloud Shadows on Terrain
    public static boolean cloudShadowsEnabled = true;
    public static float cloudShadowOpacity = 0.3f;
    public static float cloudShadowSoftness = 0.5f;
    public static float cloudShadowSpeed = 1.0f;
    public static float cloudShadowScale = 1.0f;
    public static boolean cloudShadowBiomeBlend = true;
    public static boolean cloudShadowTimeOfDay = true;

    // ========== NEW FEATURES: Water Enhancement ==========
    // Underwater Distortion & Enhancement
    public static boolean underwaterDistortionEnabled = true;
    public static float underwaterRefractionStrength = 0.08f;
    public static boolean underwaterCausticsEnabled = true;
    public static float underwaterFogDensity = 0.08f;
    public static float underwaterColorAbsorption = 0.5f;
    public static boolean underwaterParticleSuspension = true;
    public static float underwaterFogColorR = 0.1f;
    public static float underwaterFogColorG = 0.4f;
    public static float underwaterFogColorB = 0.6f;
    public static boolean underwaterBlurEnabled = true;
    public static float underwaterBlurStrength = 0.5f;
    public static float underwaterDarkenDepth = 30.0f;

    // ========== NEW FEATURES: Lighting & Effects ==========
    // Enhanced Beacon Beams
    public static boolean beaconEnhancedEnabled = true;
    public static boolean beamVolumetric = true;
    public static int beamParticleCount = 50;
    public static boolean beamPulseEnabled = true;
    public static float beamPulseSpeed = 1.0f;
    public static boolean beamColorBlending = true;
    public static boolean beamHeightScaling = true;
    public static boolean beamLightScattering = true;

    // Portal Enhancement Effects
    public static boolean portalEnhancedEnabled = true;
    public static boolean portalDistortionEnabled = true;
    public static float portalDistortionStrength = 0.15f;
    public static boolean portalParticlesEnabled = true;
    public static String portalParticleTypes = "portal,enchant,end_rod"; // comma-separated
    public static boolean portalDimensionPreview = false;
    public static int previewSize = 200;
    public static int previewPosition = 0; // 0=top-left, 1=top-right, 2=bottom-left, 3=bottom-right
    public static boolean portalSoundVisualization = true;
    public static boolean portalRippleAnimation = true;

    // ========== NEW FEATURES: Entities & Accessibility ==========
    // Entity Glint/Outline Customization
    public static boolean entityGlintEnabled = true;
    public static float entityGlintSpeed = 1.0f;
    public static int entityGlintColors = 0; // 0=Default, 1=Rainbow, 2=Custom, 3=Rarity
    public static boolean spectralGlowEnabled = true;
    public static float spectralGlowColorR = 0.5f, spectralGlowColorG = 0.0f, spectralGlowColorB = 1.0f, spectralGlowColorA = 0.8f;
    public static boolean spectralGlowThroughWalls = true;
    public static boolean teamGlowEnabled = true;
    public static String teamGlowColors = "red,blue,green,yellow,aqua,purple"; // comma-separated
    public static int glintRenderMode = 0; // 0=Standard, 1=Pulse, 2=Wave, 3=Scanline

    // Subsurface Scattering for Entities
    public static boolean sssEnabled = false; // Disabled by default - expensive
    public static float sssRadius = 0.05f;
    public static float sssColorR = 1.0f, sssColorG = 0.7f, sssColorB = 0.5f;
    public static float sssIntensity = 0.5f;
    public static String sssEntityTypes = "player,cow,pig,sheep,zombie,creeper"; // comma-separated
    public static boolean sssThicknessMap = false;
    public static float sssTransmissionR = 1.0f, sssTransmissionG = 0.8f, sssTransmissionB = 0.6f;

    // Spectator Mode X-Ray Vision
    public static boolean spectatorXRayEnabled = false;
    public static boolean xRayOres = true;
    public static boolean xRayChests = true;
    public static boolean xRayEntities = true;
    public static boolean xRayStructures = false;
    public static float xRayRange = 64.0f;
    public static float xRayOutlineColorR = 1.0f, xRayOutlineColorG = 1.0f, xRayOutlineColorB = 0.0f, xRayOutlineColorA = 0.8f;
    public static float xRayOutlineWidth = 2.0f;
    public static int xRayMode = 0; // 0=Outline, 1=Fill, 2=Both, 3=Wireframe

    // Item Highlight/Glow Customization
    public static boolean itemHighlightEnabled = true;
    public static boolean highlightDroppedItems = true;
    public static boolean highlightHeldItems = false;
    public static boolean highlightItemFrames = true;
    public static boolean highlightByRarity = true;
    public static int highlightMode = 0; // 0=Glow, 1=Outline, 2=Pulse, 3=Both
    public static String rarityColors = "common:white,uncommon:yellow,rare:blue,epic:purple,legendary:orange"; // format: rarity:color
    public static String highlightTags = "minecraft:ores,minecraft:enchantable"; // comma-separated tags
    public static boolean highlightPulseEnabled = true;
    public static float highlightRange = 32.0f;
    public static boolean highlightThroughWalls = false;

    // ========== NEW FEATURES: UI & Accessibility ==========
    // Menu/Inventory Background Blur
    public static boolean menuBlurEnabled = true;
    public static int menuBlurRadius = 8;
    public static boolean menuBlurAnimation = true;
    public static float menuBlurFadeSpeed = 5.0f;
    public static boolean menuBlurOnlyInGame = true;
    public static float menuBlurOverlayColorR = 0.0f, menuBlurOverlayColorG = 0.0f, menuBlurOverlayColorB = 0.0f;
    public static float menuBlurOverlayAlpha = 0.3f;

    // Color Blind Accessibility Modes
    public static int colorBlindMode = 0; // 0=None, 1=Protanopia, 2=Deuteranopia, 3=Tritanopia, 4=Achromatopsia
    public static float colorBlindSeverity = 1.0f;
    public static boolean daltonizeEnabled = false;
    public static int daltonizeType = 0; // 0=Simulation, 1=Correction
    public static boolean highContrastMode = false;
    public static boolean uiColorBlindSafe = true;

    // HDR/Bloom Threshold Per-Biome
    public static boolean hdrEnabled = false; // Disabled by default - needs HDR framebuffer
    public static int hdrPreset = 0; // 0=Standard, 1=Filmic, 2=Photorealistic, 3=Custom
    public static float hdrExposure = 1.0f;
    public static boolean hdrAutoExposure = true;
    public static float hdrExposureSpeed = 2.0f;
    public static float hdrMinExposure = 0.1f;
    public static float hdrMaxExposure = 4.0f;
    public static boolean hdrHistogramEnabled = false;

    // ========== NEW FEATURES: Dimensions ==========
    // End/Nether Dimension Visual Overhauls
    public static boolean endOverhaulEnabled = true;
    public static boolean endVoidFogEnabled = true;
    public static float endVoidFogColorR = 0.0f, endVoidFogColorG = 0.0f, endVoidFogColorB = 0.05f;
    public static float endVoidFogDensity = 0.002f;
    public static boolean endStarfieldEnhanced = true;
    public static boolean endIslandGlow = true;
    public static boolean endParticleEffects = true;

    public static boolean netherOverhaulEnabled = true;
    public static boolean netherFogLayers = true;
    public static boolean netherAshParticles = true;
    public static boolean netherHeatHaze = true;
    public static boolean netherGlowFungus = true;
    public static boolean netherCeilingFog = true;
    public static int shaderChromaticSamples = 4;
    public static int shaderDofSamples = 16;
    public static int shaderMotionBlurSamples = 8;
    public static int shaderGodRaysSamples = 32;
    public static int shaderLensFlareGhosts = 5;
    public static int shaderStarFieldCount = 1000;
    public static int shaderParticleMaxCount = 500;
    public static float shaderRenderScale = 1.0f;
    public static boolean shaderEnableBloom = true;
    public static boolean shaderEnableChromaticAberration = true;
    public static boolean shaderEnableDepthOfField = false;
    public static boolean shaderEnableMotionBlur = false;
    public static boolean shaderEnableGodRays = true;
    public static boolean shaderEnableLensFlare = true;
    public static boolean shaderEnableStarField = true;
    public static boolean shaderEnableAurora = false;
    public static boolean shaderEnableWaterCaustics = true;
    public static boolean shaderEnableEntityOutline = true;
    public static boolean shaderEnableSpeedLines = true;
    public static boolean shaderEnableBiomeColorGrading = true;
    public static boolean shaderEnableParticleEnhancements = true;

    // New: Particle Enhancements
    public static boolean particleEnhancementsEnabled = true;
    public static boolean bloomParticlesEnabled = true;
    public static boolean trailParticlesEnabled = true;
    public static boolean customParticlesEnabled = true;
    public static boolean particleLightingEnabled = true;
    public static float particleBloomIntensity = 0.5f;
    public static float particleTrailLength = 10f;
    public static float particleTrailFadeSpeed = 2.0f;
    public static int particleMaxCustomParticles = 500;
    public static float particleLightRadius = 5.0f;
    public static float particleLightIntensity = 0.8f;

    // Core Intensities
    public static float bloomIntensity = 0.6f;
    public static float bloomThreshold = 0.8f;
    public static float chromaticAberrationIntensity = 0.02f;
    public static float filmGrainIntensity = 0.15f;
    public static float vignetteIntensity = 0.4f;
    public static float saturation = 1.1f;
    public static float contrast = 1.05f;
    public static float brightness = 1.0f;
    public static float gamma = 1.0f;

    // New: God Rays / Volumetric Lighting
    public static boolean godRaysEnabled = true;
    public static float godRaysIntensity = 0.5f;
    public static float godRaysDensity = 0.8f;
    public static float godRaysDecay = 0.95f;
    public static float godRaysWeight = 0.4f;
    public static float godRaysExposure = 0.3f;
    public static int godRaysNumSamples = 32;

    // New: Lens Flare
    public static boolean lensFlareEnabled = true;
    public static float lensFlareIntensity = 0.5f;
    public static float lensFlareThreshold = 0.9f;
    public static int lensFlareGhosts = 5;
    public static boolean lensFlareHaloEnabled = true;
    public static float lensFlareHaloIntensity = 0.3f;
    public static boolean lensFlareChromaticAberration = true;
    public static float lensFlareChromaticIntensity = 0.015f;
    public static int lensFlareGhostCountMin = 3;
    public static int lensFlareGhostCountMax = 8;
    public static float lensFlareRotationSpeed = 0.5f;
    public static boolean lensFlareAnamorphic = false;

    // Sky Intensities
    public static float starBrightness = 1.0f;
    public static float starDensity = 0.5f;
    public static float sunMoonScale = 1.0f;
    public static float auroraIntensity = 0.3f;
    public static float skyColorIntensity = 0.5f;

    // Time Effects Intensities
    public static float sunriseGlowIntensity = 0.6f;
    public static float sunsetGlowIntensity = 0.7f;
    public static float nightVisionNoiseIntensity = 0.15f;
    public static float goldenHourWarmthIntensity = 0.5f;

    // Entity Outline Intensities
    public static float entityOutlineIntensity = 1.0f;
    public static float entityOutlinePulseSpeed = 2.0f;

    // Spotify
    public static float spotifyHudScale = 1.0f;
    public static float spotifyHudX = 0.5f;
    public static float spotifyHudY = 0.9f;

    // Hit/Kill
    public static float hitEffectIntensity = 1.0f;
    public static float killEffectIntensity = 1.0f;
    public static float hitScreenFlashIntensity = 0.3f;
    public static float killScreenFlashIntensity = 0.5f;

    // Dynamic FOV
    public static float baseFov = 70f;
    public static float sprintFovAmount = 10f;
    public static float elytraFovAmount = 20f;
    public static float speedFovMultiplier = 1.5f;

    // Damage Indicators
    public static float damageIndicatorDuration = 2.0f;
    public static float damageIndicatorSize = 1.0f;

    // Camera screen shake
    public static boolean screenShakeEnabled = true;
    public static float screenShakeIntensity = 0.7f;
    public static float screenShakeDecay = 0.95f;
    public static float screenShakeTriggerStrength = 0.8f;
    private static float currentScreenShake = 0.0f;
    private static float effectTime = 0f;
    private static float hitFlashTimer = 0f;
    private static float killFlashTimer = 0f;
    private static Vec3d lastHitPos = null;
    private static Vec3d lastKillPos = null;

    // Damage indicator tracking
    private static final java.util.List<DamageIndicator> damageIndicators = new java.util.ArrayList<>();
    private static float lastHealth = 20f;
    private static java.util.UUID lastTrackedPlayer;

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
    // New keybinds
    public static KeyBinding toggleDynamicFovKey;
    public static KeyBinding toggleDamageIndicatorsKey;
    public static KeyBinding toggleCrosshairKey;
    public static KeyBinding toggleWeatherEnhancedKey;
    public static KeyBinding toggleWaterCausticsKey;
    public static KeyBinding toggleSpeedLinesKey;
    public static KeyBinding togglePhotoModeKey;
    public static KeyBinding cycleColorPresetKey;
    public static KeyBinding cycleCrosshairStyleKey;
    // New: Chat Message Effects
    public static KeyBinding toggleChatEffectsKey;
    public static KeyBinding toggleChatFadeInKey;
    public static KeyBinding toggleChatFadeOutKey;
    public static KeyBinding toggleChatBackgroundBlurKey;
    public static KeyBinding toggleChatTimestampKey;
    public static KeyBinding cycleChatAnimationKey;
    // New: Biome Color Grading
    public static KeyBinding toggleBiomeColorGradingKey;
    public static KeyBinding cycleBiomeColorIntensityKey;
    // New: Time Effects
    public static KeyBinding toggleTimeEffectsKey;
    public static KeyBinding toggleNightVisionKey;
    public static KeyBinding toggleGoldenHourKey;
    public static KeyBinding cycleSunriseIntensityKey;
    public static KeyBinding cycleSunsetIntensityKey;
    public static KeyBinding cycleNightVisionIntensityKey;
    public static KeyBinding cycleGoldenHourIntensityKey;
    // New: God Rays
    public static KeyBinding toggleGodRaysKey;
    public static KeyBinding cycleGodRaysQualityKey;
    // New: Player Trails / Afterimages
    public static KeyBinding togglePlayerTrailsKey;
    public static KeyBinding toggleAfterimageKey;
    public static KeyBinding cycleTrailLengthKey;
    public static KeyBinding cycleAfterimageCountKey;
    // New: Lens Flare
    public static KeyBinding toggleLensFlareKey;
    public static KeyBinding toggleLensFlareHaloKey;
    public static KeyBinding toggleLensFlareChromaticKey;
    public static KeyBinding cycleLensFlareGhostsKey;
    public static KeyBinding toggleLensFlareAnamorphicKey;
    // New: Entity Outline
    public static KeyBinding toggleEntityOutlineKey;
    // New: Particle Enhancements
    public static KeyBinding toggleParticleEnhancementsKey;
    public static KeyBinding toggleBloomParticlesKey;
    public static KeyBinding toggleTrailParticlesKey;
    public static KeyBinding toggleCustomParticlesKey;
    public static KeyBinding toggleParticleLightingKey;
    public static KeyBinding cycleParticleBloomIntensityKey;
    // New: Block Highlight/Outline
    public static KeyBinding toggleBlockHighlightKey;
    public static KeyBinding toggleBlockHighlightOresKey;
    public static KeyBinding toggleBlockHighlightChestsKey;
    public static KeyBinding toggleBlockHighlightModeKey;
    public static KeyBinding toggleBlockHighlightPulseKey;
    public static KeyBinding cycleBlockHighlightColorKey;
    public static KeyBinding increaseBlockOutlineWidthKey;
    public static KeyBinding decreaseBlockOutlineWidthKey;
    public static KeyBinding toggleBlockHighlightThroughWallsKey;

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

        // New keybinds
        toggleDynamicFovKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_dynamic_fov", GLFW.GLFW_KEY_F, "category.visualsfabric.camera"));
        toggleDamageIndicatorsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_damage_indicators", GLFW.GLFW_KEY_N, "category.visualsfabric.hitkill"));
        toggleCrosshairKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_crosshair", GLFW.GLFW_KEY_X, "category.visualsfabric.camera"));
        toggleWeatherEnhancedKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_weather", GLFW.GLFW_KEY_Y, "category.visualsfabric.world"));
        toggleWaterCausticsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_caustics", GLFW.GLFW_KEY_U, "category.visualsfabric.world"));
        toggleSpeedLinesKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_speed_lines", GLFW.GLFW_KEY_I, "category.visualsfabric.camera"));
        togglePhotoModeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_photo_mode", GLFW.GLFW_KEY_F8, "category.visualsfabric.general"));
        cycleColorPresetKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.cycle_color_preset", GLFW.GLFW_KEY_Z, "category.visualsfabric.color_grading"));
        cycleCrosshairStyleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.cycle_crosshair_style", GLFW.GLFW_KEY_COMMA, "category.visualsfabric.camera"));
        // New: Chat Message Effects
        toggleChatEffectsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_chat_effects", GLFW.GLFW_KEY_F1, "category.visualsfabric.ui"));
        toggleChatFadeInKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_chat_fade_in", GLFW.GLFW_KEY_F2, "category.visualsfabric.ui"));
        toggleChatFadeOutKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_chat_fade_out", GLFW.GLFW_KEY_F3, "category.visualsfabric.ui"));
        toggleChatBackgroundBlurKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_chat_background_blur", GLFW.GLFW_KEY_F4, "category.visualsfabric.ui"));
        toggleChatTimestampKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_chat_timestamp", GLFW.GLFW_KEY_F5, "category.visualsfabric.ui"));
        cycleChatAnimationKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.cycle_chat_animation", GLFW.GLFW_KEY_T, "category.visualsfabric.ui"));
        // New: Biome Color Grading
        toggleBiomeColorGradingKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_biome_color_grading", GLFW.GLFW_KEY_E, "category.visualsfabric.color_grading"));
        cycleBiomeColorIntensityKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.cycle_biome_color_intensity", GLFW.GLFW_KEY_Q, "category.visualsfabric.color_grading"));
        // New: Time Effects
        toggleTimeEffectsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_time_effects", GLFW.GLFW_KEY_F10, "category.visualsfabric.time"));
        toggleNightVisionKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_night_vision", GLFW.GLFW_KEY_F11, "category.visualsfabric.time"));
        toggleGoldenHourKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_golden_hour", GLFW.GLFW_KEY_F12, "category.visualsfabric.time"));
        cycleSunriseIntensityKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.cycle_sunrise_intensity", GLFW.GLFW_KEY_X, "category.visualsfabric.time"));
        cycleSunsetIntensityKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.cycle_sunset_intensity", GLFW.GLFW_KEY_C, "category.visualsfabric.time"));
        cycleNightVisionIntensityKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.cycle_night_vision_intensity", GLFW.GLFW_KEY_V, "category.visualsfabric.time"));
        cycleGoldenHourIntensityKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.cycle_golden_hour_intensity", GLFW.GLFW_KEY_B, "category.visualsfabric.time"));
        // New: God Rays
        toggleGodRaysKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_god_rays", GLFW.GLFW_KEY_1, "category.visualsfabric.lighting"));
        cycleGodRaysQualityKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.cycle_god_rays_quality", GLFW.GLFW_KEY_2, "category.visualsfabric.lighting"));
        // New: Player Trails / Afterimages
        togglePlayerTrailsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_player_trails", GLFW.GLFW_KEY_G, "category.visualsfabric.player"));
        toggleAfterimageKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_afterimage", GLFW.GLFW_KEY_H, "category.visualsfabric.player"));
        cycleTrailLengthKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.cycle_trail_length", GLFW.GLFW_KEY_J, "category.visualsfabric.player"));
        cycleAfterimageCountKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.cycle_afterimage_count", GLFW.GLFW_KEY_K, "category.visualsfabric.player"));
        // New: Lens Flare
        toggleLensFlareKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_lens_flare", GLFW.GLFW_KEY_Q, "category.visualsfabric.lighting"));
        toggleLensFlareHaloKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_lens_flare_halo", GLFW.GLFW_KEY_E, "category.visualsfabric.lighting"));
        toggleLensFlareChromaticKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_lens_flare_chromatic", GLFW.GLFW_KEY_T, "category.visualsfabric.lighting"));
        cycleLensFlareGhostsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.cycle_lens_flare_ghosts", GLFW.GLFW_KEY_MINUS, "category.visualsfabric.lighting"));
        toggleLensFlareAnamorphicKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_lens_flare_anamorphic", GLFW.GLFW_KEY_EQUAL, "category.visualsfabric.lighting"));
        // New: Entity Outline
        toggleEntityOutlineKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_entity_outline", GLFW.GLFW_KEY_F9, "category.visualsfabric.entities"));
        // New: Particle Enhancements
        toggleParticleEnhancementsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_particle_enhancements", GLFW.GLFW_KEY_T, "category.visualsfabric.particles"));
        toggleBloomParticlesKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_bloom_particles", GLFW.GLFW_KEY_W, "category.visualsfabric.particles"));
        toggleTrailParticlesKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_trail_particles", GLFW.GLFW_KEY_S, "category.visualsfabric.particles"));
        toggleCustomParticlesKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_custom_particles", GLFW.GLFW_KEY_F1, "category.visualsfabric.particles"));
        toggleParticleLightingKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_particle_lighting", GLFW.GLFW_KEY_F2, "category.visualsfabric.particles"));
        cycleParticleBloomIntensityKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.cycle_particle_bloom_intensity", GLFW.GLFW_KEY_F3, "category.visualsfabric.particles"));
        // New: Block Highlight/Outline
        toggleBlockHighlightKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_block_highlight", GLFW.GLFW_KEY_T, "category.visualsfabric.world"));
        toggleBlockHighlightOresKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_block_highlight_ores", GLFW.GLFW_KEY_PERIOD, "category.visualsfabric.world"));
        toggleBlockHighlightChestsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_block_highlight_chests", GLFW.GLFW_KEY_SLASH, "category.visualsfabric.world"));
        toggleBlockHighlightModeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.cycle_block_highlight_mode", GLFW.GLFW_KEY_LEFT_BRACKET, "category.visualsfabric.world"));
        toggleBlockHighlightPulseKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_block_highlight_pulse", GLFW.GLFW_KEY_RIGHT_BRACKET, "category.visualsfabric.world"));
        cycleBlockHighlightColorKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.cycle_block_highlight_color", GLFW.GLFW_KEY_BACKSLASH, "category.visualsfabric.world"));
        increaseBlockOutlineWidthKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.increase_block_outline_width", GLFW.GLFW_KEY_EQUAL, "category.visualsfabric.world"));
        decreaseBlockOutlineWidthKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.decrease_block_outline_width", GLFW.GLFW_KEY_MINUS, "category.visualsfabric.world"));
        toggleBlockHighlightThroughWallsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_block_highlight_through_walls", GLFW.GLFW_KEY_INSERT, "category.visualsfabric.world"));
        // New: Shader Quality Presets
        cycleShaderPresetKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.cycle_shader_preset", GLFW.GLFW_KEY_F4, "category.visualsfabric.performance"));
        toggleCustomSettingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_custom_shader_settings", GLFW.GLFW_KEY_F5, "category.visualsfabric.performance"));

        // ========== NEW KEYBINDINGS REGISTRATION ==========
        // Atmosphere
        toggleVolumetricFogKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_volumetric_fog", GLFW.GLFW_KEY_O, "category.visualsfabric.atmosphere"));
        cycleVolumetricFogQualityKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.cycle_volumetric_fog_quality", GLFW.GLFW_KEY_P, "category.visualsfabric.atmosphere"));
        toggleHeatHazeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_heat_haze", GLFW.GLFW_KEY_H, "category.visualsfabric.atmosphere"));

        // Rendering Pipeline
        toggleSSRKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_ssr", GLFW.GLFW_KEY_R, "category.visualsfabric.rendering"));
        cycleSSRQualityKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.cycle_ssr_quality", GLFW.GLFW_KEY_T, "category.visualsfabric.rendering"));
        toggleTAAKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_taa", GLFW.GLFW_KEY_Y, "category.visualsfabric.rendering"));
        toggleCubemapReflectionsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_cubemap_reflections", GLFW.GLFW_KEY_U, "category.visualsfabric.rendering"));
        toggleParallaxKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_parallax", GLFW.GLFW_KEY_I, "category.visualsfabric.rendering"));

        // Weather
        toggleWeatherTransitionsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_weather_transitions", GLFW.GLFW_KEY_N, "category.visualsfabric.weather"));
        toggleCloudShadowsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_cloud_shadows", GLFW.GLFW_KEY_M, "category.visualsfabric.weather"));

        // Water
        toggleUnderwaterDistortionKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_underwater_distortion", GLFW.GLFW_KEY_K, "category.visualsfabric.water"));
        toggleUnderwaterCausticsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_underwater_caustics", GLFW.GLFW_KEY_L, "category.visualsfabric.water"));

        // Lighting
        toggleBeaconEffectsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_beacon_effects", GLFW.GLFW_KEY_COMMA, "category.visualsfabric.lighting"));
        togglePortalEffectsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_portal_effects", GLFW.GLFW_KEY_PERIOD, "category.visualsfabric.lighting"));

        // Entities
        toggleEntityGlintKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_entity_glint", GLFW.GLFW_KEY_SLASH, "category.visualsfabric.entities"));
        cycleGlintModeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.cycle_glint_mode", GLFW.GLFW_KEY_SEMICOLON, "category.visualsfabric.entities"));
        toggleSSSKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_sss", GLFW.GLFW_KEY_APOSTROPHE, "category.visualsfabric.entities"));
        toggleSpectatorXRayKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_spectator_xray", GLFW.GLFW_KEY_LEFT_BRACKET, "category.visualsfabric.entities"));
        cycleXRayModeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.cycle_xray_mode", GLFW.GLFW_KEY_RIGHT_BRACKET, "category.visualsfabric.entities"));
        toggleItemHighlightKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_item_highlight", GLFW.GLFW_KEY_BACKSLASH, "category.visualsfabric.entities"));
        cycleHighlightModeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.cycle_highlight_mode", GLFW.GLFW_KEY_GRAVE_ACCENT, "category.visualsfabric.entities"));

        // UI & Accessibility
        toggleMenuBlurKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_menu_blur", GLFW.GLFW_KEY_F1, "category.visualsfabric.accessibility"));
        cycleColorBlindModeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.cycle_color_blind_mode", GLFW.GLFW_KEY_F2, "category.visualsfabric.accessibility"));
        toggleHDRKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_hdr", GLFW.GLFW_KEY_F3, "category.visualsfabric.accessibility"));
        cycleHDRPresetKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.cycle_hdr_preset", GLFW.GLFW_KEY_F4, "category.visualsfabric.accessibility"));

        // Dimensions
        toggleEndOverhaulKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_end_overhaul", GLFW.GLFW_KEY_F5, "category.visualsfabric.dimensions"));
        toggleNetherOverhaulKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_nether_overhaul", GLFW.GLFW_KEY_F6, "category.visualsfabric.dimensions"));
    }

    public static KeyBinding cycleShaderPresetKey;
    public static KeyBinding toggleCustomSettingsKey;

    // ========== NEW KEYBINDINGS ==========
    // Atmosphere
    public static KeyBinding toggleVolumetricFogKey;
    public static KeyBinding cycleVolumetricFogQualityKey;
    public static KeyBinding toggleHeatHazeKey;

    // Rendering Pipeline
    public static KeyBinding toggleSSRKey;
    public static KeyBinding cycleSSRQualityKey;
    public static KeyBinding toggleTAAKey;
    public static KeyBinding toggleCubemapReflectionsKey;
    public static KeyBinding toggleParallaxKey;

    // Weather
    public static KeyBinding toggleWeatherTransitionsKey;
    public static KeyBinding toggleCloudShadowsKey;

    // Water
    public static KeyBinding toggleUnderwaterDistortionKey;
    public static KeyBinding toggleUnderwaterCausticsKey;

    // Lighting
    public static KeyBinding toggleBeaconEffectsKey;
    public static KeyBinding togglePortalEffectsKey;

    // Entities
    public static KeyBinding toggleEntityGlintKey;
    public static KeyBinding cycleGlintModeKey;
    public static KeyBinding toggleSSSKey;
    public static KeyBinding toggleSpectatorXRayKey;
    public static KeyBinding cycleXRayModeKey;
    public static KeyBinding toggleItemHighlightKey;
    public static KeyBinding cycleHighlightModeKey;

    // UI & Accessibility
    public static KeyBinding toggleMenuBlurKey;
    public static KeyBinding cycleColorBlindModeKey;
    public static KeyBinding toggleHDRKey;
    public static KeyBinding cycleHDRPresetKey;

    // Dimensions
    public static KeyBinding toggleEndOverhaulKey;
    public static KeyBinding toggleNetherOverhaulKey;

    private void registerTickEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            effectTime += 0.016f;
            if (effectTime > 10000f) effectTime = 0f;
            if (hitFlashTimer > 0) hitFlashTimer -= 0.016f;
            if (killFlashTimer > 0) killFlashTimer -= 0.016f;
            if (!screenShakeEnabled) {
                currentScreenShake = 0.0f;
            } else if (currentScreenShake > 0.0001f) {
                currentScreenShake *= screenShakeDecay;
            } else {
                currentScreenShake = 0.0f;
            }

            // Update player trails and afterimages
            updatePlayerTrails(client);

            // Update damage indicators
            updateDamageIndicators(client);

            // Handle dynamic FOV
            if (dynamicFovEnabled && client.player != null) {
                applyDynamicFov(client);
            }

            // Handle photo mode
            if (photoModeEnabled && photoModeFreezeTime && client.world != null) {
                // Time freeze would go here
            }

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

            // New toggles
            while (toggleDynamicFovKey.wasPressed()) { dynamicFovEnabled = !dynamicFovEnabled; sendChatFeedback("Dynamic FOV", dynamicFovEnabled); }
            while (toggleDamageIndicatorsKey.wasPressed()) { damageIndicatorsEnabled = !damageIndicatorsEnabled; sendChatFeedback("Damage Indicators", damageIndicatorsEnabled); }
            while (toggleCrosshairKey.wasPressed()) { customCrosshairEnabled = !customCrosshairEnabled; sendChatFeedback("Custom Crosshair", customCrosshairEnabled); }
            while (toggleWeatherEnhancedKey.wasPressed()) { weatherEnhanced = !weatherEnhanced; sendChatFeedback("Weather Enhanced", weatherEnhanced); }
            while (toggleWaterCausticsKey.wasPressed()) { waterCausticsEnabled = !waterCausticsEnabled; sendChatFeedback("Water Caustics", waterCausticsEnabled); }
            while (toggleSpeedLinesKey.wasPressed()) { speedLinesEnabled = !speedLinesEnabled; sendChatFeedback("Speed Lines", speedLinesEnabled); }
            while (togglePhotoModeKey.wasPressed()) { photoModeEnabled = !photoModeEnabled; sendChatFeedback("Photo Mode", photoModeEnabled); }
            while (cycleColorPresetKey.wasPressed()) { colorGradingPreset = (colorGradingPreset + 1) % 7; applyColorPreset(); sendChatFeedback("Color Preset", getPresetName(colorGradingPreset)); }
            while (cycleCrosshairStyleKey.wasPressed()) { crosshairStyle = (crosshairStyle + 1) % 5; sendChatFeedback("Crosshair Style", getCrosshairStyleName(crosshairStyle)); }
            // New: Chat Message Effects
            while (toggleChatEffectsKey.wasPressed()) { chatEffectsEnabled = !chatEffectsEnabled; sendChatFeedback("Chat Effects", chatEffectsEnabled); }
            while (toggleChatFadeInKey.wasPressed()) { chatFadeInEnabled = !chatFadeInEnabled; sendChatFeedback("Chat Fade In", chatFadeInEnabled); }
            while (toggleChatFadeOutKey.wasPressed()) { chatFadeOutEnabled = !chatFadeOutEnabled; sendChatFeedback("Chat Fade Out", chatFadeOutEnabled); }
            while (toggleChatBackgroundBlurKey.wasPressed()) { chatBackgroundBlurEnabled = !chatBackgroundBlurEnabled; sendChatFeedback("Chat Background Blur", chatBackgroundBlurEnabled); }
            while (toggleChatTimestampKey.wasPressed()) { chatTimestampEnabled = !chatTimestampEnabled; sendChatFeedback("Chat Timestamps", chatTimestampEnabled); }
            while (cycleChatAnimationKey.wasPressed()) { chatAnimationType = (chatAnimationType + 1) % 5; sendChatFeedback("Chat Animation", getChatAnimationName(chatAnimationType)); }
            // New: Biome Color Grading
            while (toggleBiomeColorGradingKey.wasPressed()) { biomeColorGradingEnabled = !biomeColorGradingEnabled; sendChatFeedback("Biome Color Grading", biomeColorGradingEnabled); }
            while (cycleBiomeColorIntensityKey.wasPressed()) { biomeColorGradingIntensity = (biomeColorGradingIntensity + 0.25f) % 1.25f; if (biomeColorGradingIntensity < 0.25f) biomeColorGradingIntensity = 0.25f; sendChatFeedback("Biome Color Intensity", String.format("%.2f", biomeColorGradingIntensity)); }
            // New: Time Effects
            while (toggleTimeEffectsKey.wasPressed()) { timeEffectsEnabled = !timeEffectsEnabled; sendChatFeedback("Time Effects", timeEffectsEnabled); }
            while (toggleNightVisionKey.wasPressed()) { nightVisionEnabled = !nightVisionEnabled; sendChatFeedback("Night Vision", nightVisionEnabled); }
            while (toggleGoldenHourKey.wasPressed()) { goldenHourEnabled = !goldenHourEnabled; sendChatFeedback("Golden Hour", goldenHourEnabled); }
            while (cycleSunriseIntensityKey.wasPressed()) { sunriseIntensity = (sunriseIntensity + 0.25f) % 1.25f; if (sunriseIntensity < 0.25f) sunriseIntensity = 0.25f; sendChatFeedback("Sunrise Intensity", String.format("%.2f", sunriseIntensity)); }
            while (cycleSunsetIntensityKey.wasPressed()) { sunsetIntensity = (sunsetIntensity + 0.25f) % 1.25f; if (sunsetIntensity < 0.25f) sunsetIntensity = 0.25f; sendChatFeedback("Sunset Intensity", String.format("%.2f", sunsetIntensity)); }
            while (cycleNightVisionIntensityKey.wasPressed()) { nightVisionIntensity = (nightVisionIntensity + 0.25f) % 1.25f; if (nightVisionIntensity < 0.25f) nightVisionIntensity = 0.25f; sendChatFeedback("Night Vision Intensity", String.format("%.2f", nightVisionIntensity)); }
            while (cycleGoldenHourIntensityKey.wasPressed()) { goldenHourIntensity = (goldenHourIntensity + 0.25f) % 1.25f; if (goldenHourIntensity < 0.25f) goldenHourIntensity = 0.25f; sendChatFeedback("Golden Hour Intensity", String.format("%.2f", goldenHourIntensity)); }
            // New: God Rays
            while (toggleGodRaysKey.wasPressed()) { godRaysEnabled = !godRaysEnabled; sendChatFeedback("God Rays", godRaysEnabled); }
            while (cycleGodRaysQualityKey.wasPressed()) {
                int[] samples = {16, 32, 64, 128};
                int currentIndex = java.util.Arrays.asList(samples).indexOf(godRaysNumSamples);
                godRaysNumSamples = samples[(currentIndex + 1) % samples.length];
                sendChatFeedback("God Rays Samples", String.valueOf(godRaysNumSamples));
            }
            // New: Player Trails / Afterimages
            while (togglePlayerTrailsKey.wasPressed()) {
                playerTrailsEnabled = !playerTrailsEnabled;
                sendChatFeedback("Player Trails", playerTrailsEnabled);
            }
            while (toggleAfterimageKey.wasPressed()) {
                afterimageEnabled = !afterimageEnabled;
                sendChatFeedback("Afterimages", afterimageEnabled);
            }
            while (cycleTrailLengthKey.wasPressed()) {
                int[] lengths = {5, 10, 15, 20, 30, 40, 50};
                int currentIndex = 0;
                for (int i = 0; i < lengths.length; i++) {
                    if (lengths[i] == trailLength) { currentIndex = i; break; }
                }
                trailLength = lengths[(currentIndex + 1) % lengths.length];
                sendChatFeedback("Trail Length", String.valueOf(trailLength));
            }
            while (cycleAfterimageCountKey.wasPressed()) {
                int[] counts = {1, 2, 3, 4, 5, 6, 8, 10, 12, 15};
                int currentIndex = 0;
                for (int i = 0; i < counts.length; i++) {
                    if (counts[i] == afterimageCount) { currentIndex = i; break; }
                }
                afterimageCount = counts[(currentIndex + 1) % counts.length];
                sendChatFeedback("Afterimage Count", String.valueOf(afterimageCount));
            }
            // New: Lens Flare
            while (toggleLensFlareKey.wasPressed()) { lensFlareEnabled = !lensFlareEnabled; sendChatFeedback("Lens Flare", lensFlareEnabled); }
            while (toggleLensFlareHaloKey.wasPressed()) { lensFlareHaloEnabled = !lensFlareHaloEnabled; sendChatFeedback("Lens Flare Halo", lensFlareHaloEnabled); }
            while (toggleLensFlareChromaticKey.wasPressed()) { lensFlareChromaticAberration = !lensFlareChromaticAberration; sendChatFeedback("Lens Flare Chromatic Aberration", lensFlareChromaticAberration); }
            while (cycleLensFlareGhostsKey.wasPressed()) { lensFlareGhosts = (lensFlareGhosts >= 8) ? 3 : lensFlareGhosts + 1; sendChatFeedback("Lens Flare Ghosts", String.valueOf(lensFlareGhosts)); }
            while (toggleLensFlareAnamorphicKey.wasPressed()) { lensFlareAnamorphic = !lensFlareAnamorphic; sendChatFeedback("Lens Flare Anamorphic", lensFlareAnamorphic); }
            // New: Entity Outline
            while (toggleEntityOutlineKey.wasPressed()) { entityOutlineEnabled = !entityOutlineEnabled; sendChatFeedback("Entity Outline", entityOutlineEnabled); }
            // New: Particle Enhancements
            while (toggleParticleEnhancementsKey.wasPressed()) { particleEnhancementsEnabled = !particleEnhancementsEnabled; sendChatFeedback("Particle Enhancements", particleEnhancementsEnabled); }
            while (toggleBloomParticlesKey.wasPressed()) { bloomParticlesEnabled = !bloomParticlesEnabled; sendChatFeedback("Bloom Particles", bloomParticlesEnabled); }
            while (toggleTrailParticlesKey.wasPressed()) { trailParticlesEnabled = !trailParticlesEnabled; sendChatFeedback("Trail Particles", trailParticlesEnabled); }
            while (toggleCustomParticlesKey.wasPressed()) { customParticlesEnabled = !customParticlesEnabled; sendChatFeedback("Custom Particles", customParticlesEnabled); }
            while (toggleParticleLightingKey.wasPressed()) { particleLightingEnabled = !particleLightingEnabled; sendChatFeedback("Particle Lighting", particleLightingEnabled); }
            while (cycleParticleBloomIntensityKey.wasPressed()) { particleBloomIntensity = (particleBloomIntensity + 0.25f) % 1.25f; if (particleBloomIntensity < 0.25f) particleBloomIntensity = 0.25f; sendChatFeedback("Particle Bloom Intensity", String.format("%.2f", particleBloomIntensity)); }
            // New: Block Highlight/Outline
            while (toggleBlockHighlightKey.wasPressed()) { blockHighlightEnabled = !blockHighlightEnabled; sendChatFeedback("Block Highlight", blockHighlightEnabled); }
            while (toggleBlockHighlightOresKey.wasPressed()) { blockHighlightOres = !blockHighlightOres; sendChatFeedback("Block Highlight Ores", blockHighlightOres); }
            while (toggleBlockHighlightChestsKey.wasPressed()) { blockHighlightChests = !blockHighlightChests; sendChatFeedback("Block Highlight Chests", blockHighlightChests); }
            while (toggleBlockHighlightModeKey.wasPressed()) { blockHighlightMode = (blockHighlightMode + 1) % 3; sendChatFeedback("Block Highlight Mode", getBlockHighlightModeName(blockHighlightMode)); }
            while (toggleBlockHighlightPulseKey.wasPressed()) { blockHighlightPulseEnabled = !blockHighlightPulseEnabled; sendChatFeedback("Block Highlight Pulse", blockHighlightPulseEnabled); }
            while (cycleBlockHighlightColorKey.wasPressed()) { cycleBlockHighlightColor(); sendChatFeedback("Block Highlight Color", getBlockHighlightColorName()); }
            while (increaseBlockOutlineWidthKey.wasPressed()) { blockOutlineWidth = Math.min(5.0f, blockOutlineWidth + 0.5f); sendChatFeedback("Block Outline Width", String.format("%.1f", blockOutlineWidth)); }
            while (decreaseBlockOutlineWidthKey.wasPressed()) { blockOutlineWidth = Math.max(0.5f, blockOutlineWidth - 0.5f); sendChatFeedback("Block Outline Width", String.format("%.1f", blockOutlineWidth)); }
            while (toggleBlockHighlightThroughWallsKey.wasPressed()) { blockHighlightThroughWalls = !blockHighlightThroughWalls; sendChatFeedback("Block Highlight Through Walls", blockHighlightThroughWalls); }
            // New: Shader Quality Presets
            while (cycleShaderPresetKey.wasPressed()) {
                shaderPreset = ShaderPreset.fromId((shaderPreset.getId() + 1) % 6);
                customSettings = false;
                applyShaderPreset();
                sendChatFeedback("Shader Preset", getShaderPresetName(shaderPreset));
            }
            while (toggleCustomSettingsKey.wasPressed()) {
                customSettings = !customSettings;
                if (customSettings) shaderPreset = ShaderPreset.CUSTOM;
                sendChatFeedback("Custom Shader Settings", customSettings);
            }

            // ========== NEW KEYBINDINGS HANDLING ==========
            // Atmosphere
            while (toggleVolumetricFogKey.wasPressed()) { volumetricFogEnabled = !volumetricFogEnabled; sendChatFeedback("Volumetric Fog", volumetricFogEnabled); }
            while (cycleVolumetricFogQualityKey.wasPressed()) { volumetricFogQuality = (volumetricFogQuality + 1) % 4; sendChatFeedback("Volumetric Fog Quality", getVolumetricFogQualityName(volumetricFogQuality)); }
            while (toggleHeatHazeKey.wasPressed()) { heatHazeEnabled = !heatHazeEnabled; sendChatFeedback("Heat Haze", heatHazeEnabled); }

            // Rendering Pipeline
            while (toggleSSRKey.wasPressed()) { ssrEnabled = !ssrEnabled; sendChatFeedback("Screen Space Reflections", ssrEnabled); }
            while (cycleSSRQualityKey.wasPressed()) { ssrQuality = (ssrQuality + 1) % 4; sendChatFeedback("SSR Quality", getSSRQualityName(ssrQuality)); }
            while (toggleTAAKey.wasPressed()) { taaEnabled = !taaEnabled; sendChatFeedback("Temporal Anti-Aliasing", taaEnabled); }
            while (toggleCubemapReflectionsKey.wasPressed()) { cubemapReflectionsEnabled = !cubemapReflectionsEnabled; sendChatFeedback("Cubemap Reflections", cubemapReflectionsEnabled); }
            while (toggleParallaxKey.wasPressed()) { parallaxEnabled = !parallaxEnabled; sendChatFeedback("Parallax Occlusion Mapping", parallaxEnabled); }

            // Weather
            while (toggleWeatherTransitionsKey.wasPressed()) { weatherTransitionsEnabled = !weatherTransitionsEnabled; sendChatFeedback("Weather Transitions", weatherTransitionsEnabled); }
            while (toggleCloudShadowsKey.wasPressed()) { cloudShadowsEnabled = !cloudShadowsEnabled; sendChatFeedback("Cloud Shadows", cloudShadowsEnabled); }

            // Water
            while (toggleUnderwaterDistortionKey.wasPressed()) { underwaterDistortionEnabled = !underwaterDistortionEnabled; sendChatFeedback("Underwater Distortion", underwaterDistortionEnabled); }
            while (toggleUnderwaterCausticsKey.wasPressed()) { underwaterCausticsEnabled = !underwaterCausticsEnabled; sendChatFeedback("Underwater Caustics", underwaterCausticsEnabled); }

            // Lighting
            while (toggleBeaconEffectsKey.wasPressed()) { beaconEnhancedEnabled = !beaconEnhancedEnabled; sendChatFeedback("Beacon Effects", beaconEnhancedEnabled); }
            while (togglePortalEffectsKey.wasPressed()) { portalEnhancedEnabled = !portalEnhancedEnabled; sendChatFeedback("Portal Effects", portalEnhancedEnabled); }

            // Entities
            while (toggleEntityGlintKey.wasPressed()) { entityGlintEnabled = !entityGlintEnabled; sendChatFeedback("Entity Glint", entityGlintEnabled); }
            while (cycleGlintModeKey.wasPressed()) { glintRenderMode = (glintRenderMode + 1) % 4; sendChatFeedback("Glint Mode", getGlintModeName(glintRenderMode)); }
            while (toggleSSSKey.wasPressed()) { sssEnabled = !sssEnabled; sendChatFeedback("Subsurface Scattering", sssEnabled); }
            while (toggleSpectatorXRayKey.wasPressed()) { spectatorXRayEnabled = !spectatorXRayEnabled; sendChatFeedback("Spectator X-Ray", spectatorXRayEnabled); }
            while (cycleXRayModeKey.wasPressed()) { xRayMode = (xRayMode + 1) % 4; sendChatFeedback("X-Ray Mode", getXRayModeName(xRayMode)); }
            while (toggleItemHighlightKey.wasPressed()) { itemHighlightEnabled = !itemHighlightEnabled; sendChatFeedback("Item Highlight", itemHighlightEnabled); }
            while (cycleHighlightModeKey.wasPressed()) { highlightMode = (highlightMode + 1) % 4; sendChatFeedback("Highlight Mode", getHighlightModeName(highlightMode)); }

            // UI & Accessibility
            while (toggleMenuBlurKey.wasPressed()) { menuBlurEnabled = !menuBlurEnabled; sendChatFeedback("Menu Blur", menuBlurEnabled); }
            while (cycleColorBlindModeKey.wasPressed()) { colorBlindMode = (colorBlindMode + 1) % 5; sendChatFeedback("Color Blind Mode", getColorBlindModeName(colorBlindMode)); }
            while (toggleHDRKey.wasPressed()) { hdrEnabled = !hdrEnabled; sendChatFeedback("HDR", hdrEnabled); }
            while (cycleHDRPresetKey.wasPressed()) { hdrPreset = (hdrPreset + 1) % 4; sendChatFeedback("HDR Preset", getHDRPresetName(hdrPreset)); }

            // Dimensions
            while (toggleEndOverhaulKey.wasPressed()) { endOverhaulEnabled = !endOverhaulEnabled; sendChatFeedback("End Overhaul", endOverhaulEnabled); }
            while (toggleNetherOverhaulKey.wasPressed()) { netherOverhaulEnabled = !netherOverhaulEnabled; sendChatFeedback("Nether Overhaul", netherOverhaulEnabled); }
        });
    }

    private String getPresetName(int preset) {
        return switch (preset) {
            case 0 -> "Custom";
            case 1 -> "Cinematic";
            case 2 -> "Vibrant";
            case 3 -> "Moody";
            case 4 -> "Retro";
            case 5 -> "Noir";
            case 6 -> "Sepia";
            default -> "Custom";
        };
    }

    private String getCrosshairStyleName(int style) {
        return switch (style) {
            case 0 -> "Default";
            case 1 -> "Dot";
            case 2 -> "Cross";
            case 3 -> "Circle";
            case 4 -> "Dynamic";
            default -> "Default";
        };
    }

    private String getChatAnimationName(int type) {
        return switch (type) {
            case 0 -> "Fade";
            case 1 -> "Slide";
            case 2 -> "Scale";
            case 3 -> "Typewriter";
            case 4 -> "Glitch";
            default -> "Fade";
        };
    }

    private String getBlockHighlightModeName(int mode) {
        return switch (mode) {
            case 0 -> "Outline";
            case 1 -> "Fill";
            case 2 -> "Both";
            default -> "Outline";
        };
    }

    private void cycleBlockHighlightColor() {
        float[][] colors = {
            {1.0f, 1.0f, 0.0f}, // Yellow
            {1.0f, 0.0f, 0.0f}, // Red
            {0.0f, 1.0f, 0.0f}, // Green
            {0.0f, 0.0f, 1.0f}, // Blue
            {0.0f, 1.0f, 1.0f}, // Cyan
            {1.0f, 0.0f, 1.0f}, // Magenta
            {1.0f, 1.0f, 1.0f}, // White
            {1.0f, 0.5f, 0.0f}  // Orange
        };
        float[] current = {blockHighlightR, blockHighlightG, blockHighlightB};
        int currentIndex = 0;
        for (int i = 0; i < colors.length; i++) {
            if (Math.abs(colors[i][0] - current[0]) < 0.01f &&
                Math.abs(colors[i][1] - current[1]) < 0.01f &&
                Math.abs(colors[i][2] - current[2]) < 0.01f) {
                currentIndex = i;
                break;
            }
        }
        int nextIndex = (currentIndex + 1) % colors.length;
        blockHighlightR = colors[nextIndex][0];
        blockHighlightG = colors[nextIndex][1];
        blockHighlightB = colors[nextIndex][2];
    }

    private String getBlockHighlightColorName() {
        if (Math.abs(blockHighlightR - 1.0f) < 0.01f && Math.abs(blockHighlightG - 1.0f) < 0.01f && Math.abs(blockHighlightB - 0.0f) < 0.01f) return "Yellow";
        if (Math.abs(blockHighlightR - 1.0f) < 0.01f && Math.abs(blockHighlightG - 0.0f) < 0.01f && Math.abs(blockHighlightB - 0.0f) < 0.01f) return "Red";
        if (Math.abs(blockHighlightR - 0.0f) < 0.01f && Math.abs(blockHighlightG - 1.0f) < 0.01f && Math.abs(blockHighlightB - 0.0f) < 0.01f) return "Green";
        if (Math.abs(blockHighlightR - 0.0f) < 0.01f && Math.abs(blockHighlightG - 0.0f) < 0.01f && Math.abs(blockHighlightB - 1.0f) < 0.01f) return "Blue";
        if (Math.abs(blockHighlightR - 0.0f) < 0.01f && Math.abs(blockHighlightG - 1.0f) < 0.01f && Math.abs(blockHighlightB - 1.0f) < 0.01f) return "Cyan";
        if (Math.abs(blockHighlightR - 1.0f) < 0.01f && Math.abs(blockHighlightG - 0.0f) < 0.01f && Math.abs(blockHighlightB - 1.0f) < 0.01f) return "Magenta";
        if (Math.abs(blockHighlightR - 1.0f) < 0.01f && Math.abs(blockHighlightG - 1.0f) < 0.01f && Math.abs(blockHighlightB - 1.0f) < 0.01f) return "White";
        if (Math.abs(blockHighlightR - 1.0f) < 0.01f && Math.abs(blockHighlightG - 0.5f) < 0.01f && Math.abs(blockHighlightB - 0.0f) < 0.01f) return "Orange";
        return "Custom";
    }

    private void applyColorPreset() {
        switch (colorGradingPreset) {
            case 1 -> { // Cinematic
                saturation = 0.9f; contrast = 1.15f; brightness = 0.95f; gamma = 1.0f;
            }
            case 2 -> { // Vibrant
                saturation = 1.4f; contrast = 1.1f; brightness = 1.05f; gamma = 0.95f;
            }
            case 3 -> { // Moody
                saturation = 0.7f; contrast = 1.2f; brightness = 0.85f; gamma = 1.1f;
            }
            case 4 -> { // Retro
                saturation = 1.2f; contrast = 0.9f; brightness = 1.0f; gamma = 0.9f;
            }
            case 5 -> { // Noir
                saturation = 0.0f; contrast = 1.5f; brightness = 0.8f; gamma = 1.2f;
            }
            case 6 -> { // Sepia
                saturation = 0.5f; contrast = 1.1f; brightness = 1.0f; gamma = 0.95f;
            }
        }
    }

    private String getShaderPresetName(ShaderPreset preset) {
        return switch (preset) {
            case ULTRA -> "Ultra";
            case HIGH -> "High";
            case MEDIUM -> "Medium";
            case LOW -> "Low";
            case PERFORMANCE -> "Performance";
            case CUSTOM -> "Custom";
        };
    }

    // ========== NEW GETTER METHODS ==========
    private String getVolumetricFogQualityName(int quality) {
        return switch (quality) {
            case 0 -> "Low";
            case 1 -> "Medium";
            case 2 -> "High";
            case 3 -> "Ultra";
            default -> "Medium";
        };
    }

    private String getSSRQualityName(int quality) {
        return switch (quality) {
            case 0 -> "Low";
            case 1 -> "Medium";
            case 2 -> "High";
            case 3 -> "Ultra";
            default -> "Medium";
        };
    }

    private String getGlintModeName(int mode) {
        return switch (mode) {
            case 0 -> "Standard";
            case 1 -> "Pulse";
            case 2 -> "Wave";
            case 3 -> "Scanline";
            default -> "Standard";
        };
    }

    private String getXRayModeName(int mode) {
        return switch (mode) {
            case 0 -> "Outline";
            case 1 -> "Fill";
            case 2 -> "Both";
            case 3 -> "Wireframe";
            default -> "Outline";
        };
    }

    private String getHighlightModeName(int mode) {
        return switch (mode) {
            case 0 -> "Glow";
            case 1 -> "Outline";
            case 2 -> "Pulse";
            case 3 -> "Both";
            default -> "Glow";
        };
    }

    private String getColorBlindModeName(int mode) {
        return switch (mode) {
            case 0 -> "None";
            case 1 -> "Protanopia";
            case 2 -> "Deuteranopia";
            case 3 -> "Tritanopia";
            case 4 -> "Achromatopsia";
            default -> "None";
        };
    }

    private String getHDRPresetName(int preset) {
        return switch (preset) {
            case 0 -> "Standard";
            case 1 -> "Filmic";
            case 2 -> "Photorealistic";
            case 3 -> "Custom";
            default -> "Standard";
        };
    }

    private void applyShaderPreset() {
        if (customSettings) return; // Don't override custom settings

        switch (shaderPreset) {
            case ULTRA -> {
                // Maximum quality - all effects enabled at highest settings
                shaderBloomSamples = 16;
                shaderChromaticSamples = 8;
                shaderDofSamples = 32;
                shaderMotionBlurSamples = 16;
                shaderGodRaysSamples = 64;
                shaderLensFlareGhosts = 8;
                shaderStarFieldCount = 2000;
                shaderParticleMaxCount = 1000;
                shaderRenderScale = 1.0f;
                shaderEnableBloom = true;
                shaderEnableChromaticAberration = true;
                shaderEnableDepthOfField = true;
                shaderEnableMotionBlur = true;
                shaderEnableGodRays = true;
                shaderEnableLensFlare = true;
                shaderEnableStarField = true;
                shaderEnableAurora = true;
                shaderEnableWaterCaustics = true;
                shaderEnableEntityOutline = true;
                shaderEnableSpeedLines = true;
                shaderEnableBiomeColorGrading = true;
                shaderEnableParticleEnhancements = true;
                bloomEnabled = true;
                chromaticAberrationEnabled = true;
                depthOfFieldEnabled = true;
                motionBlurEnabled = true;
                godRaysEnabled = true;
                lensFlareEnabled = true;
                starFieldEnabled = true;
                auroraEnabled = true;
                waterCausticsEnabled = true;
                entityOutlineEnabled = true;
                speedLinesEnabled = true;
                biomeColorGradingEnabled = true;
                particleEnhancementsEnabled = true;
                lowQualityShaders = false;
                reducedParticles = false;
            }
            case HIGH -> {
                // High quality - most effects enabled, reduced samples
                shaderBloomSamples = 8;
                shaderChromaticSamples = 4;
                shaderDofSamples = 16;
                shaderMotionBlurSamples = 8;
                shaderGodRaysSamples = 32;
                shaderLensFlareGhosts = 5;
                shaderStarFieldCount = 1500;
                shaderParticleMaxCount = 750;
                shaderRenderScale = 1.0f;
                shaderEnableBloom = true;
                shaderEnableChromaticAberration = true;
                shaderEnableDepthOfField = true;
                shaderEnableMotionBlur = true;
                shaderEnableGodRays = true;
                shaderEnableLensFlare = true;
                shaderEnableStarField = true;
                shaderEnableAurora = true;
                shaderEnableWaterCaustics = true;
                shaderEnableEntityOutline = true;
                shaderEnableSpeedLines = true;
                shaderEnableBiomeColorGrading = true;
                shaderEnableParticleEnhancements = true;
                bloomEnabled = true;
                chromaticAberrationEnabled = true;
                depthOfFieldEnabled = true;
                motionBlurEnabled = true;
                godRaysEnabled = true;
                lensFlareEnabled = true;
                starFieldEnabled = true;
                auroraEnabled = true;
                waterCausticsEnabled = true;
                entityOutlineEnabled = true;
                speedLinesEnabled = true;
                biomeColorGradingEnabled = true;
                particleEnhancementsEnabled = true;
                lowQualityShaders = false;
                reducedParticles = false;
            }
            case MEDIUM -> {
                // Balanced - core effects enabled, some advanced effects disabled
                shaderBloomSamples = 6;
                shaderChromaticSamples = 3;
                shaderDofSamples = 8;
                shaderMotionBlurSamples = 6;
                shaderGodRaysSamples = 16;
                shaderLensFlareGhosts = 4;
                shaderStarFieldCount = 1000;
                shaderParticleMaxCount = 500;
                shaderRenderScale = 1.0f;
                shaderEnableBloom = true;
                shaderEnableChromaticAberration = true;
                shaderEnableDepthOfField = false;
                shaderEnableMotionBlur = false;
                shaderEnableGodRays = true;
                shaderEnableLensFlare = true;
                shaderEnableStarField = true;
                shaderEnableAurora = false;
                shaderEnableWaterCaustics = true;
                shaderEnableEntityOutline = true;
                shaderEnableSpeedLines = true;
                shaderEnableBiomeColorGrading = true;
                shaderEnableParticleEnhancements = true;
                bloomEnabled = true;
                chromaticAberrationEnabled = true;
                depthOfFieldEnabled = false;
                motionBlurEnabled = false;
                godRaysEnabled = true;
                lensFlareEnabled = true;
                starFieldEnabled = true;
                auroraEnabled = false;
                waterCausticsEnabled = true;
                entityOutlineEnabled = true;
                speedLinesEnabled = true;
                biomeColorGradingEnabled = true;
                particleEnhancementsEnabled = true;
                lowQualityShaders = false;
                reducedParticles = false;
            }
            case LOW -> {
                // Low quality - minimal effects
                shaderBloomSamples = 4;
                shaderChromaticSamples = 2;
                shaderDofSamples = 4;
                shaderMotionBlurSamples = 4;
                shaderGodRaysSamples = 8;
                shaderLensFlareGhosts = 2;
                shaderStarFieldCount = 500;
                shaderParticleMaxCount = 250;
                shaderRenderScale = 0.9f;
                shaderEnableBloom = true;
                shaderEnableChromaticAberration = false;
                shaderEnableDepthOfField = false;
                shaderEnableMotionBlur = false;
                shaderEnableGodRays = false;
                shaderEnableLensFlare = false;
                shaderEnableStarField = true;
                shaderEnableAurora = false;
                shaderEnableWaterCaustics = false;
                shaderEnableEntityOutline = true;
                shaderEnableSpeedLines = false;
                shaderEnableBiomeColorGrading = false;
                shaderEnableParticleEnhancements = true;
                bloomEnabled = true;
                chromaticAberrationEnabled = false;
                depthOfFieldEnabled = false;
                motionBlurEnabled = false;
                godRaysEnabled = false;
                lensFlareEnabled = false;
                starFieldEnabled = true;
                auroraEnabled = false;
                waterCausticsEnabled = false;
                entityOutlineEnabled = true;
                speedLinesEnabled = false;
                biomeColorGradingEnabled = false;
                particleEnhancementsEnabled = true;
                lowQualityShaders = true;
                reducedParticles = true;
            }
            case PERFORMANCE -> {
                // Maximum performance - only essential effects
                shaderBloomSamples = 2;
                shaderChromaticSamples = 1;
                shaderDofSamples = 2;
                shaderMotionBlurSamples = 2;
                shaderGodRaysSamples = 4;
                shaderLensFlareGhosts = 1;
                shaderStarFieldCount = 200;
                shaderParticleMaxCount = 100;
                shaderRenderScale = 0.75f;
                shaderEnableBloom = false;
                shaderEnableChromaticAberration = false;
                shaderEnableDepthOfField = false;
                shaderEnableMotionBlur = false;
                shaderEnableGodRays = false;
                shaderEnableLensFlare = false;
                shaderEnableStarField = false;
                shaderEnableAurora = false;
                shaderEnableWaterCaustics = false;
                shaderEnableEntityOutline = false;
                shaderEnableSpeedLines = false;
                shaderEnableBiomeColorGrading = false;
                shaderEnableParticleEnhancements = false;
                bloomEnabled = false;
                chromaticAberrationEnabled = false;
                depthOfFieldEnabled = false;
                motionBlurEnabled = false;
                godRaysEnabled = false;
                lensFlareEnabled = false;
                starFieldEnabled = false;
                auroraEnabled = false;
                waterCausticsEnabled = false;
                entityOutlineEnabled = false;
                speedLinesEnabled = false;
                biomeColorGradingEnabled = false;
                particleEnhancementsEnabled = false;
                lowQualityShaders = true;
                reducedParticles = true;
                performanceMode = true;
            }
            case CUSTOM -> {
                // Custom settings - don't override, user controls individual settings
            }
        }
    }

    private void sendChatFeedback(String effect, boolean enabled) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && client.inGameHud != null) {
            client.inGameHud.getChatHud().addMessage(Text.literal("§b[VisualsFabric] §r" + effect + ": " + (enabled ? "§aON" : "§cOFF")));
        }
    }

    private void sendChatFeedback(String effect, String value) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && client.inGameHud != null) {
            client.inGameHud.getChatHud().addMessage(Text.literal("§b[VisualsFabric] §r" + effect + ": §e" + value));
        }
    }

    private void registerRenderEvents() {
        WorldRenderEvents.AFTER_ENTITIES.register(this::renderPostProcessing);
        WorldRenderEvents.LAST.register(this::renderOverlayEffects);
        WorldRenderEvents.BEFORE_ENTITIES.register(this::renderSkyEffects);
        WorldRenderEvents.BEFORE_ENTITIES.register(this::renderBlockHighlight);
        WorldRenderEvents.BEFORE_ENTITIES.register(this::renderTimeEffects);
        WorldRenderEvents.BEFORE_ENTITIES.register(this::renderGodRays);
    }

    private boolean shouldApplyPostProcessing() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.world != null && client.player != null &&
            (shaderEnableBloom && bloomEnabled ||
             shaderEnableChromaticAberration && chromaticAberrationEnabled ||
             shaderEnableDepthOfField && depthOfFieldEnabled ||
             shaderEnableMotionBlur && motionBlurEnabled ||
             colorGradingEnabled);
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
        int width = (int)(client.getWindow().getFramebufferWidth() * shaderRenderScale);
        int height = (int)(client.getWindow().getFramebufferHeight() * shaderRenderScale);
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
        if (shaderEnableBloom && bloomEnabled && (!performanceMode || !lowQualityShaders)) applyBloomEffect();
        if (shaderEnableChromaticAberration && chromaticAberrationEnabled && (!performanceMode || !lowQualityShaders)) applyChromaticAberration();
        if (shaderEnableDepthOfField && depthOfFieldEnabled && !performanceMode) applyDepthOfField(tickDelta);
        if (shaderEnableMotionBlur && motionBlurEnabled && !performanceMode) applyMotionBlur();
        if (colorGradingEnabled) applyColorGrading();
        if (shaderEnableBiomeColorGrading && biomeColorGradingEnabled && (!performanceMode || !lowQualityShaders)) applyBiomeColorGrading(tickDelta);
        if (shaderEnableLensFlare && lensFlareEnabled && (!performanceMode || !lowQualityShaders)) applyLensFlareEffect(tickDelta);
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
    private void applyBiomeColorGrading(float tickDelta) {
        if (performanceMode && lowQualityShaders) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        // Use effectTime for smooth transitions between biomes
        float transitionFactor = biomeColorGradingSmoothTransitions ?
            (float)Math.sin(effectTime * biomeColorGradingTransitionSpeed) * 0.5f + 0.5f : 1.0f;

        // Simplified biome detection using dimension and basic checks
        // Avoid RegistryEntry/Biome API issues in 1.21.4
        float r = 1.0f, g = 1.0f, b = 1.0f;
        float saturationMod = 1.0f;
        float contrastMod = 1.0f;
        float brightnessMod = 1.0f;

        // Check dimension for Nether/End
        var dimension = client.world.getRegistryKey();
        boolean isNether = dimension != null && dimension.getValue().toString().contains("nether");
        boolean isEnd = dimension != null && dimension.getValue().toString().contains("end");

        if (isNether) {
            // Nether: warm, hellish tones
            r = 1.3f; g = 0.7f; b = 0.4f;
            saturationMod = 1.2f;
            contrastMod = 1.15f;
            brightnessMod = 0.9f;
        } else if (isEnd) {
            // End: cold, ethereal tones
            r = 0.7f; g = 0.9f; b = 1.2f;
            saturationMod = 0.8f;
            contrastMod = 1.2f;
            brightnessMod = 1.1f;
        } else {
            // Overworld - use a simple temperature approximation based on Y level
            // Higher Y = colder (mountains), Lower Y = warmer (caves/desert-like)
            int y = client.player.getBlockPos().getY();
            float tempFactor = Math.max(0, Math.min(1, (y - 64) / 128f));

            if (tempFactor < 0.3f) {
                // Cold biomes (snowy, frozen)
                r = 0.85f; g = 0.95f; b = 1.15f;
                saturationMod = 0.7f;
                contrastMod = 1.1f;
                brightnessMod = 1.05f;
            } else if (tempFactor > 0.7f) {
                // Hot biomes (desert, savanna) - approximated
                r = 1.2f; g = 1.0f; b = 0.7f;
                saturationMod = 1.15f;
                contrastMod = 1.1f;
                brightnessMod = 1.1f;
            } else {
                // Temperate biomes (plains, forest, etc.)
                r = 1.0f; g = 1.0f; b = 1.0f;
                saturationMod = 1.0f;
                contrastMod = 1.0f;
                brightnessMod = 1.0f;
            }
        }

        // Apply intensity and transition
        float intensity = biomeColorGradingIntensity * transitionFactor;
        r = 1.0f + (r - 1.0f) * intensity;
        g = 1.0f + (g - 1.0f) * intensity;
        b = 1.0f + (b - 1.0f) * intensity;
        saturationMod = 1.0f + (saturationMod - 1.0f) * intensity;
        contrastMod = 1.0f + (contrastMod - 1.0f) * intensity;
        brightnessMod = 1.0f + (brightnessMod - 1.0f) * intensity;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(r, g, b, intensity * 0.3f);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }

    // New: Lens Flare render methods
    private void applyLensFlareEffect(float tickDelta) {
        if (performanceMode && lowQualityShaders) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        // Get sun position for lens flare source
        float sunAngle = client.world.getSkyAngle(tickDelta) * ((float)Math.PI * 2) - (float)Math.PI / 2;
        float sunHeight = (float)Math.sin(sunAngle);

        // Only render when sun is above horizon (or bright light sources)
        if (sunHeight < 0.1f && !hasBrightLightSources(client)) return;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE); // Additive blending for lens flare

        float baseAlpha = lensFlareIntensity * 0.6f;
        float rotation = effectTime * lensFlareRotationSpeed;

        // Calculate screen position of sun
        Vec3d sunPos = calculateSunScreenPosition(client, tickDelta);
        if (sunPos == null) {
            RenderSystem.disableBlend();
            return;
        }

        int centerX = (int)sunPos.x;
        int centerY = (int)sunPos.y;
        int width = client.getWindow().getFramebufferWidth();
        int height = client.getWindow().getFramebufferHeight();

        // Vector from center of screen to light source
        float dx = centerX - width / 2f;
        float dy = centerY - height / 2f;
        float distFromCenter = (float)Math.sqrt(dx * dx + dy * dy);
        float maxDist = (float)Math.sqrt(width * width + height * height) / 2f;
        float normalizedDist = distFromCenter / maxDist;

        // Only show flare when light source is visible on screen
        if (centerX < -100 || centerX > width + 100 || centerY < -100 || centerY > height + 100) {
            RenderSystem.disableBlend();
            return;
        }

        Tessellator tessellator = Tessellator.getInstance();

        // Render ghosts (internal reflections)
        int ghostCount = Math.min(lensFlareGhosts, lensFlareGhostCountMax);
        ghostCount = Math.max(ghostCount, lensFlareGhostCountMin);

        for (int i = 0; i < ghostCount; i++) {
            float ghostProgress = (i + 1) / (float)(ghostCount + 1);
            float ghostDist = normalizedDist * ghostProgress * (lensFlareAnamorphic ? 1.5f : 1.0f);
            float ghostSize = 30f * (1f - ghostProgress * 0.5f) * lensFlareIntensity;
            float ghostAlpha = baseAlpha * (1f - ghostProgress) * (0.5f + 0.5f * (float)Math.sin(effectTime * 3 + i));

            int ghostX = width / 2 + (int)(dx * ghostProgress);
            int ghostY = height / 2 + (int)(dy * ghostProgress);

            RenderSystem.setShaderColor(1f, 0.95f, 0.8f, ghostAlpha);

            BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
            float halfSize = ghostSize / 2;
            buffer.vertex(ghostX - halfSize, ghostY + halfSize, 0);
            buffer.vertex(ghostX + halfSize, ghostY + halfSize, 0);
            buffer.vertex(ghostX + halfSize, ghostY - halfSize, 0);
            buffer.vertex(ghostX - halfSize, ghostY - halfSize, 0);
            BufferRenderer.drawWithGlobalProgram(buffer.end());

            // Chromatic aberration on ghosts
            if (lensFlareChromaticAberration) {
                float caOffset = lensFlareChromaticIntensity * ghostSize * (i + 1) * 0.5f;

                // Red channel
                RenderSystem.setShaderColor(1f, 0f, 0f, ghostAlpha * 0.3f);
                buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
                buffer.vertex(ghostX - halfSize + caOffset, ghostY + halfSize, 0);
                buffer.vertex(ghostX + halfSize + caOffset, ghostY + halfSize, 0);
                buffer.vertex(ghostX + halfSize + caOffset, ghostY - halfSize, 0);
                buffer.vertex(ghostX - halfSize + caOffset, ghostY - halfSize, 0);
                BufferRenderer.drawWithGlobalProgram(buffer.end());

                // Blue channel
                RenderSystem.setShaderColor(0f, 0f, 1f, ghostAlpha * 0.3f);
                buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
                buffer.vertex(ghostX - halfSize - caOffset, ghostY + halfSize, 0);
                buffer.vertex(ghostX + halfSize - caOffset, ghostY + halfSize, 0);
                buffer.vertex(ghostX + halfSize - caOffset, ghostY - halfSize, 0);
                buffer.vertex(ghostX - halfSize - caOffset, ghostY - halfSize, 0);
                BufferRenderer.drawWithGlobalProgram(buffer.end());
            }
        }

        // Render halo around main light source
        if (lensFlareHaloEnabled) {
            float haloSize = 80f * lensFlareIntensity * (1f + 0.3f * (float)Math.sin(effectTime * 2));
            float haloAlpha = lensFlareHaloIntensity * baseAlpha * (0.7f + 0.3f * (float)Math.sin(effectTime * 1.5));

            RenderSystem.setShaderColor(1f, 0.9f, 0.7f, haloAlpha);

            // Outer glow (multiple passes for bloom-like effect)
            for (int pass = 0; pass < 3; pass++) {
                float passSize = haloSize * (1.5f - pass * 0.3f);
                float passAlpha = haloAlpha * (1f - pass * 0.25f);
                RenderSystem.setShaderColor(1f, 0.9f, 0.7f, passAlpha);

                BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
                float halfSize = passSize / 2;
                buffer.vertex(centerX - halfSize, centerY + halfSize, 0);
                buffer.vertex(centerX + halfSize, centerY + halfSize, 0);
                buffer.vertex(centerX + halfSize, centerY - halfSize, 0);
                buffer.vertex(centerX - halfSize, centerY - halfSize, 0);
                BufferRenderer.drawWithGlobalProgram(buffer.end());
            }

            // Anamorphic horizontal streak
            if (lensFlareAnamorphic) {
                float streakLength = haloSize * 4;
                float streakWidth = haloSize * 0.15f;
                float streakAlpha = haloAlpha * 0.6f;
                float streakAngle = rotation;

                RenderSystem.setShaderColor(1f, 0.85f, 0.6f, streakAlpha);
                BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);

                float cos = (float)Math.cos(streakAngle);
                float sin = (float)Math.sin(streakAngle);
                float hx = cos * streakLength / 2;
                float hy = sin * streakLength / 2;
                float wx = -sin * streakWidth / 2;
                float wy = cos * streakWidth / 2;

                buffer.vertex(centerX - hx - wx, centerY - hy - wy, 0);
                buffer.vertex(centerX + hx - wx, centerY + hy - wy, 0);
                buffer.vertex(centerX + hx + wx, centerY + hy + wy, 0);
                buffer.vertex(centerX - hx + wx, centerY - hy + wy, 0);
                BufferRenderer.drawWithGlobalProgram(buffer.end());
            }
        }

        // Render main flare core
        float coreSize = 15f * lensFlareIntensity;
        float coreAlpha = baseAlpha * 1.5f;
        RenderSystem.setShaderColor(1f, 1f, 0.9f, coreAlpha);

        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        float halfSize = coreSize / 2;
        buffer.vertex(centerX - halfSize, centerY + halfSize, 0);
        buffer.vertex(centerX + halfSize, centerY + halfSize, 0);
        buffer.vertex(centerX + halfSize, centerY - halfSize, 0);
        buffer.vertex(centerX - halfSize, centerY - halfSize, 0);
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        // Chromatic aberration on core
        if (lensFlareChromaticAberration) {
            float caOffset = lensFlareChromaticIntensity * coreSize * 2;

            RenderSystem.setShaderColor(1f, 0.2f, 0.1f, coreAlpha * 0.4f);
            buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
            buffer.vertex(centerX - halfSize + caOffset, centerY + halfSize, 0);
            buffer.vertex(centerX + halfSize + caOffset, centerY + halfSize, 0);
            buffer.vertex(centerX + halfSize + caOffset, centerY - halfSize, 0);
            buffer.vertex(centerX - halfSize + caOffset, centerY - halfSize, 0);
            BufferRenderer.drawWithGlobalProgram(buffer.end());

            RenderSystem.setShaderColor(0.1f, 0.2f, 1f, coreAlpha * 0.4f);
            buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
            buffer.vertex(centerX - halfSize - caOffset, centerY + halfSize, 0);
            buffer.vertex(centerX + halfSize - caOffset, centerY + halfSize, 0);
            buffer.vertex(centerX + halfSize - caOffset, centerY - halfSize, 0);
            buffer.vertex(centerX - halfSize - caOffset, centerY - halfSize, 0);
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        }

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }

    private Vec3d calculateSunScreenPosition(MinecraftClient client, float tickDelta) {
        // Project sun position to screen coordinates
        float sunAngle = client.world.getSkyAngle(tickDelta) * ((float)Math.PI * 2) - (float)Math.PI / 2;
        float sunHeight = (float)Math.sin(sunAngle);
        float sunAzimuth = (float)Math.cos(sunAngle);

        if (sunHeight < -0.2f) return null; // Sun below horizon

        // Project to screen (simplified - using viewport)
        int width = client.getWindow().getFramebufferWidth();
        int height = client.getWindow().getFramebufferHeight();

        // Convert to NDC then to screen
        double fov = Math.toRadians(client.options.getFov().getValue());
        double aspect = (double)width / height;

        float ndcX = (float)(sunAzimuth / (Math.tan(fov / 2) * aspect));
        float ndcY = (float)(sunHeight / (Math.tan(fov / 2)));

        int screenX = (int)((ndcX + 1) * width / 2);
        int screenY = (int)((1 - ndcY) * height / 2);

        return new Vec3d(screenX, screenY, 0);
    }

    private boolean hasBrightLightSources(MinecraftClient client) {
        // Simplified check - always return false to avoid API issues
        // Could be expanded with proper block entity iteration if needed
        return false;
    }

    private void renderSkyEffects(WorldRenderContext context) {
        if (!skyEffectsEnabled) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;
        MatrixStack matrices = context.matrixStack();
        if (enhancedSunMoon) renderEnhancedSunMoon(matrices, context.tickCounter().getTickDelta(false));
        if (shaderEnableStarField && starFieldEnabled) renderStarField(matrices, context.tickCounter().getTickDelta(false));
        if (shaderEnableAurora && auroraEnabled && !performanceMode) renderAurora(matrices, context.tickCounter().getTickDelta(false));
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

    // New: Block Highlight/Outline
    private void renderBlockHighlight(WorldRenderContext context) {
        if (!blockHighlightEnabled) return;
        if (performanceMode && reducedParticles) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        MatrixStack matrices = context.matrixStack();
        float tickDelta = context.tickCounter().getTickDelta(false);

        // Calculate pulse alpha if enabled (uses effectTime for animation)
        float pulseAlpha = blockHighlightA;
        if (blockHighlightPulseEnabled) {
            float pulse = (float)Math.sin(effectTime * blockHighlightPulseSpeed) * 0.5f + 0.5f;
            pulseAlpha = blockHighlightPulseMinAlpha + (blockHighlightPulseMaxAlpha - blockHighlightPulseMinAlpha) * pulse;
        }

        // Get camera position for distance culling
        Camera camera = client.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();

        // Collect blocks to highlight
        java.util.List<BlockPos> highlightBlocks = new java.util.ArrayList<>();
        int range = (int)blockHighlightRange;

        // Check blocks in range around player
        BlockPos playerPos = client.player.getBlockPos();
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    if (pos.getSquaredDistance(playerPos) > range * range) continue;

                    BlockState blockState = client.world.getBlockState(pos);
                    if (blockState.isAir()) continue;

                    boolean shouldHighlight = false;

                    // Check for ores
                    if (blockHighlightOres && isOreBlock(blockState)) {
                        shouldHighlight = true;
                    }
                    // Check for chests
                    else if (blockHighlightChests && isChestBlock(blockState)) {
                        shouldHighlight = true;
                    }

                    if (shouldHighlight) {
                        // Distance culling if not through walls
                        if (!blockHighlightThroughWalls) {
                            if (!hasLineOfSight(client.world, cameraPos, pos)) continue;
                        }
                        highlightBlocks.add(pos);
                    }
                }
            }
        }

        if (highlightBlocks.isEmpty()) return;

        // Render highlights
        matrices.push();

        // Translate to camera-relative coordinates
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableCull();
        RenderSystem.lineWidth(blockOutlineWidth);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer;

        for (BlockPos pos : highlightBlocks) {
            BlockState blockState = client.world.getBlockState(pos);
            Box box = blockState.getOutlineShape(client.world, pos).getBoundingBox()
                .expand(0.002, 0.002, 0.002) // Slightly larger than block
                .offset(pos.getX(), pos.getY(), pos.getZ());

            // Render based on mode
            if (blockHighlightMode == 0 || blockHighlightMode == 2) { // Outline or Both
                RenderSystem.setShaderColor(blockHighlightR, blockHighlightG, blockHighlightB, pulseAlpha);

                buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION);
                // 12 edges of a cube - Bottom face
                buffer.vertex((float)box.minX, (float)box.minY, (float)box.minZ);
                buffer.vertex((float)box.maxX, (float)box.minY, (float)box.minZ);
                buffer.vertex((float)box.maxX, (float)box.minY, (float)box.minZ);
                buffer.vertex((float)box.maxX, (float)box.minY, (float)box.maxZ);
                buffer.vertex((float)box.maxX, (float)box.minY, (float)box.maxZ);
                buffer.vertex((float)box.minX, (float)box.minY, (float)box.maxZ);
                buffer.vertex((float)box.minX, (float)box.minY, (float)box.maxZ);
                buffer.vertex((float)box.minX, (float)box.minY, (float)box.minZ);
                // Top face
                buffer.vertex((float)box.minX, (float)box.maxY, (float)box.minZ);
                buffer.vertex((float)box.maxX, (float)box.maxY, (float)box.minZ);
                buffer.vertex((float)box.maxX, (float)box.maxY, (float)box.minZ);
                buffer.vertex((float)box.maxX, (float)box.maxY, (float)box.maxZ);
                buffer.vertex((float)box.maxX, (float)box.maxY, (float)box.maxZ);
                buffer.vertex((float)box.minX, (float)box.maxY, (float)box.maxZ);
                buffer.vertex((float)box.minX, (float)box.maxY, (float)box.maxZ);
                buffer.vertex((float)box.minX, (float)box.maxY, (float)box.minZ);
                // Vertical edges
                buffer.vertex((float)box.minX, (float)box.minY, (float)box.minZ);
                buffer.vertex((float)box.minX, (float)box.maxY, (float)box.minZ);
                buffer.vertex((float)box.maxX, (float)box.minY, (float)box.minZ);
                buffer.vertex((float)box.maxX, (float)box.maxY, (float)box.minZ);
                buffer.vertex((float)box.maxX, (float)box.minY, (float)box.maxZ);
                buffer.vertex((float)box.maxX, (float)box.maxY, (float)box.maxZ);
                buffer.vertex((float)box.minX, (float)box.minY, (float)box.maxZ);
                buffer.vertex((float)box.minX, (float)box.maxY, (float)box.maxZ);

                BufferRenderer.drawWithGlobalProgram(buffer.end());
            }

            if (blockHighlightMode == 1 || blockHighlightMode == 2) { // Fill or Both
                RenderSystem.setShaderColor(blockHighlightR, blockHighlightG, blockHighlightB, pulseAlpha * 0.15f);

                buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
                // Bottom
                buffer.vertex((float)box.minX, (float)box.minY, (float)box.minZ);
                buffer.vertex((float)box.maxX, (float)box.minY, (float)box.minZ);
                buffer.vertex((float)box.maxX, (float)box.minY, (float)box.maxZ);
                buffer.vertex((float)box.minX, (float)box.minY, (float)box.maxZ);
                // Top
                buffer.vertex((float)box.minX, (float)box.maxY, (float)box.minZ);
                buffer.vertex((float)box.minX, (float)box.maxY, (float)box.maxZ);
                buffer.vertex((float)box.maxX, (float)box.maxY, (float)box.maxZ);
                buffer.vertex((float)box.maxX, (float)box.maxY, (float)box.minZ);
                // North
                buffer.vertex((float)box.minX, (float)box.minY, (float)box.minZ);
                buffer.vertex((float)box.minX, (float)box.maxY, (float)box.minZ);
                buffer.vertex((float)box.maxX, (float)box.maxY, (float)box.minZ);
                buffer.vertex((float)box.maxX, (float)box.minY, (float)box.minZ);
                // South
                buffer.vertex((float)box.maxX, (float)box.minY, (float)box.maxZ);
                buffer.vertex((float)box.maxX, (float)box.maxY, (float)box.maxZ);
                buffer.vertex((float)box.minX, (float)box.maxY, (float)box.maxZ);
                buffer.vertex((float)box.minX, (float)box.minY, (float)box.maxZ);
                // West
                buffer.vertex((float)box.minX, (float)box.minY, (float)box.maxZ);
                buffer.vertex((float)box.minX, (float)box.maxY, (float)box.maxZ);
                buffer.vertex((float)box.minX, (float)box.maxY, (float)box.minZ);
                buffer.vertex((float)box.minX, (float)box.minY, (float)box.minZ);
                // East
                buffer.vertex((float)box.maxX, (float)box.minY, (float)box.minZ);
                buffer.vertex((float)box.maxX, (float)box.maxY, (float)box.minZ);
                buffer.vertex((float)box.maxX, (float)box.maxY, (float)box.maxZ);
                buffer.vertex((float)box.maxX, (float)box.minY, (float)box.maxZ);

                BufferRenderer.drawWithGlobalProgram(buffer.end());
            }
        }

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableCull();
        RenderSystem.lineWidth(1.0f);
        RenderSystem.disableBlend();

        matrices.pop();
    }

    // Helper methods for block detection
    private boolean isOreBlock(BlockState blockState) {
        Block block = blockState.getBlock();
        return block == Blocks.DIAMOND_ORE ||
               block == Blocks.DEEPSLATE_DIAMOND_ORE ||
               block == Blocks.EMERALD_ORE ||
               block == Blocks.DEEPSLATE_EMERALD_ORE ||
               block == Blocks.GOLD_ORE ||
               block == Blocks.DEEPSLATE_GOLD_ORE ||
               block == Blocks.IRON_ORE ||
               block == Blocks.DEEPSLATE_IRON_ORE ||
               block == Blocks.COPPER_ORE ||
               block == Blocks.DEEPSLATE_COPPER_ORE ||
               block == Blocks.COAL_ORE ||
               block == Blocks.DEEPSLATE_COAL_ORE ||
               block == Blocks.REDSTONE_ORE ||
               block == Blocks.DEEPSLATE_REDSTONE_ORE ||
               block == Blocks.LAPIS_ORE ||
               block == Blocks.DEEPSLATE_LAPIS_ORE ||
               block == Blocks.NETHER_GOLD_ORE ||
               block == Blocks.NETHER_QUARTZ_ORE ||
               block == Blocks.ANCIENT_DEBRIS ||
               blockState.getBlock().getTranslationKey().contains("ore");
    }

    private boolean isChestBlock(BlockState blockState) {
        Block block = blockState.getBlock();
        return block instanceof net.minecraft.block.ChestBlock ||
               block instanceof net.minecraft.block.EnderChestBlock ||
               block instanceof net.minecraft.block.TrappedChestBlock ||
               block == Blocks.CHEST ||
               block == Blocks.ENDER_CHEST ||
               block == Blocks.TRAPPED_CHEST ||
               block == Blocks.BARREL ||
               block == Blocks.SHULKER_BOX ||
               block == Blocks.WHITE_SHULKER_BOX ||
               block == Blocks.ORANGE_SHULKER_BOX ||
               block == Blocks.MAGENTA_SHULKER_BOX ||
               block == Blocks.LIGHT_BLUE_SHULKER_BOX ||
               block == Blocks.YELLOW_SHULKER_BOX ||
               block == Blocks.LIME_SHULKER_BOX ||
               block == Blocks.PINK_SHULKER_BOX ||
               block == Blocks.GRAY_SHULKER_BOX ||
               block == Blocks.LIGHT_GRAY_SHULKER_BOX ||
               block == Blocks.CYAN_SHULKER_BOX ||
               block == Blocks.PURPLE_SHULKER_BOX ||
               block == Blocks.BLUE_SHULKER_BOX ||
               block == Blocks.BROWN_SHULKER_BOX ||
               block == Blocks.GREEN_SHULKER_BOX ||
               block == Blocks.RED_SHULKER_BOX ||
               block == Blocks.BLACK_SHULKER_BOX;
    }

    private boolean hasLineOfSight(net.minecraft.world.World world, Vec3d from, BlockPos to) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (world == null || client.player == null) return true;
        Vec3d toVec = new Vec3d(to.getX() + 0.5, to.getY() + 0.5, to.getZ() + 0.5);
        HitResult result = world.raycast(new RaycastContext(from, toVec,
            RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, client.player));
        return result.getType() == HitResult.Type.MISS ||
               (result.getType() == HitResult.Type.BLOCK &&
                ((BlockHitResult)result).getBlockPos().equals(to));
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
        // New overlay effects
        if (customCrosshairEnabled && !photoModeEnabled) renderCustomCrosshair(matrices, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
        if (damageIndicatorsEnabled) renderDamageIndicators(matrices, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
        if (shaderEnableSpeedLines && speedLinesEnabled) renderSpeedLines(matrices, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight(), context.tickCounter().getTickDelta(false));
        if (shaderEnableWaterCaustics && waterCausticsEnabled && client.player != null && client.player.isSubmergedInWater()) renderWaterCaustics(matrices, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
        if (playerTrailsEnabled) renderPlayerTrails(matrices, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight(), context.tickCounter().getTickDelta(false));
        if (afterimageEnabled) renderPlayerAfterimages(matrices, context.tickCounter().getTickDelta(false));
        if (shaderEnableEntityOutline && entityOutlineEnabled && (!performanceMode || !lowQualityShaders)) renderEntityOutline(matrices, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight(), context.tickCounter().getTickDelta(false));
        // New: Particle Enhancements
        if (shaderEnableParticleEnhancements && particleEnhancementsEnabled && (!performanceMode || !reducedParticles)) renderParticleEnhancements(matrices, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight(), context.tickCounter().getTickDelta(false));
        // New: Chat Message Effects
        if (chatEffectsEnabled && (!performanceMode || !reducedParticles)) renderChatEffects(context);
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

    // New: Custom Crosshair
    private void renderCustomCrosshair(MatrixStack matrices, int width, int height) {
        if (crosshairStyle == 0) return; // Default vanilla crosshair

        int centerX = width / 2;
        int centerY = height / 2;
        float size = 10f * crosshairSize;
        float gap = 4f * crosshairSize + crosshairGap;
        float thickness = crosshairThickness;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(crosshairColorR, crosshairColorG, crosshairColorB, crosshairColorA);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer;

        switch (crosshairStyle) {
            case 1 -> { // Dot
                buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
                buffer.vertex(centerX - size/2, centerY + size/2, 0);
                buffer.vertex(centerX + size/2, centerY + size/2, 0);
                buffer.vertex(centerX + size/2, centerY - size/2, 0);
                buffer.vertex(centerX - size/2, centerY - size/2, 0);
                BufferRenderer.drawWithGlobalProgram(buffer.end());
            }
            case 2 -> { // Cross
                // Horizontal
                buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
                buffer.vertex(centerX - size - gap, centerY + thickness/2, 0);
                buffer.vertex(centerX - gap, centerY + thickness/2, 0);
                buffer.vertex(centerX - gap, centerY - thickness/2, 0);
                buffer.vertex(centerX - size - gap, centerY - thickness/2, 0);
                BufferRenderer.drawWithGlobalProgram(buffer.end());

                buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
                buffer.vertex(centerX + gap, centerY + thickness/2, 0);
                buffer.vertex(centerX + size + gap, centerY + thickness/2, 0);
                buffer.vertex(centerX + size + gap, centerY - thickness/2, 0);
                buffer.vertex(centerX + gap, centerY - thickness/2, 0);
                BufferRenderer.drawWithGlobalProgram(buffer.end());

                // Vertical
                buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
                buffer.vertex(centerX - thickness/2, centerY - gap, 0);
                buffer.vertex(centerX + thickness/2, centerY - gap, 0);
                buffer.vertex(centerX + thickness/2, centerY - size - gap, 0);
                buffer.vertex(centerX - thickness/2, centerY - size - gap, 0);
                BufferRenderer.drawWithGlobalProgram(buffer.end());

                buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
                buffer.vertex(centerX - thickness/2, centerY + gap, 0);
                buffer.vertex(centerX + thickness/2, centerY + gap, 0);
                buffer.vertex(centerX + thickness/2, centerY + size + gap, 0);
                buffer.vertex(centerX - thickness/2, centerY + size + gap, 0);
                BufferRenderer.drawWithGlobalProgram(buffer.end());
            }
            case 3 -> { // Circle
                RenderSystem.setShaderColor(crosshairColorR, crosshairColorG, crosshairColorB, crosshairColorA * 0.5f);
                int segments = 32;
                buffer = tessellator.begin(VertexFormat.DrawMode.LINE_STRIP, VertexFormats.POSITION);
                for (int i = 0; i <= segments; i++) {
                    double angle = (i / (double)segments) * Math.PI * 2;
                    buffer.vertex(centerX + (float)Math.cos(angle) * size, centerY + (float)Math.sin(angle) * size, 0);
                }
                BufferRenderer.drawWithGlobalProgram(buffer.end());
            }
            case 4 -> { // Dynamic - expands when moving
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player != null) {
                    double speed = Math.sqrt(client.player.getVelocity().x * client.player.getVelocity().x +
                                            client.player.getVelocity().z * client.player.getVelocity().z);
                    float dynamicSize = size * (1 + (float)speed * 5);
                    float dynamicGap = gap * (1 + (float)speed * 3);

                    // Draw expanding crosshair
                    buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
                    buffer.vertex(centerX - dynamicSize - dynamicGap, centerY + thickness/2, 0);
                    buffer.vertex(centerX - dynamicGap, centerY + thickness/2, 0);
                    buffer.vertex(centerX - dynamicGap, centerY - thickness/2, 0);
                    buffer.vertex(centerX - dynamicSize - dynamicGap, centerY - thickness/2, 0);
                    BufferRenderer.drawWithGlobalProgram(buffer.end());

                    buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
                    buffer.vertex(centerX + dynamicGap, centerY + thickness/2, 0);
                    buffer.vertex(centerX + dynamicSize + dynamicGap, centerY + thickness/2, 0);
                    buffer.vertex(centerX + dynamicSize + dynamicGap, centerY - thickness/2, 0);
                    buffer.vertex(centerX + dynamicGap, centerY - thickness/2, 0);
                    BufferRenderer.drawWithGlobalProgram(buffer.end());

                    buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
                    buffer.vertex(centerX - thickness/2, centerY - dynamicGap, 0);
                    buffer.vertex(centerX + thickness/2, centerY - dynamicGap, 0);
                    buffer.vertex(centerX + thickness/2, centerY - dynamicSize - dynamicGap, 0);
                    buffer.vertex(centerX - thickness/2, centerY - dynamicSize - dynamicGap, 0);
                    BufferRenderer.drawWithGlobalProgram(buffer.end());

                    buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
                    buffer.vertex(centerX - thickness/2, centerY + dynamicGap, 0);
                    buffer.vertex(centerX + thickness/2, centerY + dynamicGap, 0);
                    buffer.vertex(centerX + thickness/2, centerY + dynamicSize + dynamicGap, 0);
                    buffer.vertex(centerX - thickness/2, centerY + dynamicSize + dynamicGap, 0);
                    BufferRenderer.drawWithGlobalProgram(buffer.end());
                }
            }
        }

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }

    // New: Damage Indicators
    private void updateDamageIndicators(MinecraftClient client) {
        if (client.player == null) {
            lastTrackedPlayer = null;
            damageIndicators.clear();
            return;
        }

        java.util.UUID playerId = client.player.getUuid();
        if (!playerId.equals(lastTrackedPlayer)) {
            lastTrackedPlayer = playerId;
            lastHealth = client.player.getHealth();
            damageIndicators.clear();
            return;
        }

        float currentHealth = client.player.getHealth();
        if (currentHealth < lastHealth) {
            float damage = lastHealth - currentHealth;
            triggerScreenShake(Math.min(damage / 5.0f, 1.0f));
            if (damageIndicatorsEnabled) {
                double angle = Math.random() * Math.PI * 2;
                damageIndicators.add(new DamageIndicator(angle, damage, damageIndicatorDuration));
            }
        }
        lastHealth = currentHealth;

        if (damageIndicatorsEnabled) {
            damageIndicators.removeIf(indicator -> {
                indicator.life -= 0.016f;
                return indicator.life <= 0;
            });
        } else {
            damageIndicators.clear();
        }
    }

    private void renderDamageIndicators(MatrixStack matrices, int width, int height) {
        if (damageIndicators.isEmpty()) return;

        int centerX = width / 2;
        int centerY = height / 2;
        int radius = Math.min(width, height) / 3;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        Tessellator tessellator = Tessellator.getInstance();

        for (DamageIndicator indicator : damageIndicators) {
            float alpha = Math.min(1.0f, indicator.life / 0.5f) * 0.8f;
            RenderSystem.setShaderColor(1, 0.2f, 0.2f, alpha);

            int indicatorX = centerX + (int)(Math.cos(indicator.angle) * radius);
            int indicatorY = centerY + (int)(Math.sin(indicator.angle) * radius);
            float size = 20f * damageIndicatorSize * indicator.life / damageIndicatorDuration;

            // Draw arrow pointing to damage source
            BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
            buffer.vertex(indicatorX - size/2, indicatorY + size/2, 0);
            buffer.vertex(indicatorX + size/2, indicatorY + size/2, 0);
            buffer.vertex(indicatorX + size/2, indicatorY - size/2, 0);
            buffer.vertex(indicatorX - size/2, indicatorY - size/2, 0);
            BufferRenderer.drawWithGlobalProgram(buffer.end());

            if (damageNumbersEnabled) {
                // Draw damage number would go here
            }
        }

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }

    // New: Player Trails Update
    private void updatePlayerTrails(MinecraftClient client) {
        if (!playerTrailsEnabled && !afterimageEnabled) {
            playerTrail.clear();
            playerAfterimages.clear();
            return;
        }

        if (client.player == null) return;

        PlayerEntity player = client.player;
        Vec3d currentPos = player.getPos();
        float currentYaw = player.getYaw();
        float currentPitch = player.getPitch();

        // Update trail points - add new point every few ticks
        trailTickCounter++;
        if (trailTickCounter >= 2) { // Add point every 2 ticks
            trailTickCounter = 0;
            if (playerTrailsEnabled) {
                playerTrail.add(0, new TrailPoint(currentPos, currentYaw, currentPitch));

                // Limit trail length
                while (playerTrail.size() > trailLength) {
                    playerTrail.remove(playerTrail.size() - 1);
                }
            }

            // Add afterimage at intervals
            if (afterimageEnabled && trailTickCounter % (int)afterimageSpacing == 0) {
                playerAfterimages.add(0, new Afterimage(
                    currentPos, currentYaw, currentPitch
                ));

                // Limit afterimage count
                while (playerAfterimages.size() > afterimageCount) {
                    playerAfterimages.remove(playerAfterimages.size() - 1);
                }
            }
        }

        // Update trail point ages and alphas
        float ageIncrement = 1.0f / trailLength;
        for (int i = 0; i < playerTrail.size(); i++) {
            TrailPoint point = playerTrail.get(i);
            point.age = i * ageIncrement;
            if (trailFadeEnabled) {
                point.alpha = trailColorA * (1.0f - point.age * 0.8f);
            } else {
                point.alpha = trailColorA;
            }

            // Emit trail particles
            if (playerTrailParticlesEnabled && !performanceMode && client.world != null && client.particleManager != null && Math.random() < 0.3) {
                client.particleManager.addParticle(
                    ParticleTypes.ENCHANT,
                    point.position.x, point.position.y + 0.5, point.position.z,
                    0, 0, 0
                );
            }
        }

        // Update afterimage ages, alphas, and scales
        float afterimageAgeIncrement = 1.0f / afterimageCount;
        for (int i = 0; i < playerAfterimages.size(); i++) {
            Afterimage img = playerAfterimages.get(i);
            img.age = i * afterimageAgeIncrement;
            img.alpha = afterimageAlpha * (1.0f - img.age * 0.7f);
            if (afterimageScale) {
                img.scale = 1.0f - img.age * 0.3f;
            }
        }

        // Clean up old entries
        playerTrail.removeIf(p -> p.age >= 1.0f || p.alpha <= 0.01f);
        playerAfterimages.removeIf(a -> a.age >= 1.0f || a.alpha <= 0.01f);
    }

    static class DamageIndicator {
        double angle;
        float damage;
        float life;

        DamageIndicator(double angle, float damage, float life) {
            this.angle = angle;
            this.damage = damage;
            this.life = life;
        }
    }

    // New: Player Trail Data
    private static final java.util.List<TrailPoint> playerTrail = new java.util.ArrayList<>();
    private static final java.util.List<Afterimage> playerAfterimages = new java.util.ArrayList<>();
    private static int trailTickCounter = 0;

    static class TrailPoint {
        Vec3d position;
        float yaw;
        float pitch;
        float age;
        float alpha;

        TrailPoint(Vec3d position, float yaw, float pitch) {
            this.position = position;
            this.yaw = yaw;
            this.pitch = pitch;
            this.age = 0f;
            this.alpha = trailColorA;
        }
    }

    static class Afterimage {
        Vec3d position;
        float yaw;
        float pitch;
        float age;
        float alpha;
        float scale;

        Afterimage(Vec3d position, float yaw, float pitch) {
            this.position = position;
            this.yaw = yaw;
            this.pitch = pitch;
            this.age = 0f;
            this.alpha = afterimageAlpha;
            this.scale = 1.0f;
        }
    }

    // New: Speed Lines
    private void renderSpeedLines(MatrixStack matrices, int width, int height, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        double speed = Math.sqrt(client.player.getVelocity().x * client.player.getVelocity().x +
                                client.player.getVelocity().z * client.player.getVelocity().z);

        if (speed < speedLinesThreshold) return;

        float intensity = Math.min(1.0f, (float)((speed - speedLinesThreshold) / 0.5)) * speedLinesIntensity;
        if (intensity <= 0) return;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        RenderSystem.setShaderColor(1, 1, 1, intensity * 0.3f);

        Tessellator tessellator = Tessellator.getInstance();
        int centerX = width / 2;
        int centerY = height / 2;

        // Draw radial speed lines
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION);
        for (int i = 0; i < 16; i++) {
            double angle = (i / 16.0) * Math.PI * 2 + effectTime * 2;
            double length = 50 + speed * 200 * intensity;
            double startRadius = 100;

            buffer.vertex((float)(centerX + Math.cos(angle) * startRadius), (float)(centerY + Math.sin(angle) * startRadius), 0);
            buffer.vertex((float)(centerX + Math.cos(angle) * (startRadius + length)), (float)(centerY + Math.sin(angle) * (startRadius + length)), 0);
        }
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }

    // New: Water Caustics
    private void renderWaterCaustics(MatrixStack matrices, int width, int height) {
        if (performanceMode && reducedParticles) return;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        RenderSystem.setShaderColor(1, 1, 1, causticsIntensity * 0.4f);

        // Simple animated caustic pattern
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);

        float time = effectTime * causticsSpeed;
        int tileSize = 64;

        for (int x = -tileSize; x < width + tileSize; x += tileSize) {
            for (int y = -tileSize; y < height + tileSize; y += tileSize) {
                // Animated offset
                float offsetX = (float)Math.sin(time + x * 0.01) * 10;
                float offsetY = (float)Math.cos(time + y * 0.01) * 10;
                float alpha = (float)(Math.sin(time * 2 + x * 0.05 + y * 0.05) * 0.5 + 0.5);

                if (alpha > 0.3) {
                    RenderSystem.setShaderColor(1, 1, 1, causticsIntensity * 0.3f * alpha);
                    buffer.vertex(x + offsetX, y + offsetY + tileSize, 0);
                    buffer.vertex(x + offsetX + tileSize, y + offsetY + tileSize, 0);
                    buffer.vertex(x + offsetX + tileSize, y + offsetY, 0);
                    buffer.vertex(x + offsetX, y + offsetY, 0);
                }
            }
        }
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }

    // New: Player Trails Rendering
    private void renderPlayerTrails(MatrixStack matrices, int width, int height, float tickDelta) {
        if (performanceMode && lowQualityShaders) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.gameRenderer == null || client.world == null) return;
        if (playerTrail.isEmpty()) return;

        Camera camera = client.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);

        Tessellator tessellator = Tessellator.getInstance();

        // Render trail as connected quads (ribbon)
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        for (int i = 0; i < playerTrail.size() - 1; i++) {
            TrailPoint p1 = playerTrail.get(i);
            TrailPoint p2 = playerTrail.get(i + 1);

            float alpha1 = p1.alpha;
            float alpha2 = p2.alpha;

            if (alpha1 <= 0.01f && alpha2 <= 0.01f) continue;

            // Width tapers based on age
            float w1 = trailWidth * (1.0f - p1.age * 0.5f) * 2.0f;
            float w2 = trailWidth * (1.0f - p2.age * 0.5f) * 2.0f;

            // Perpendicular offset for ribbon
            float yawRad1 = (float)Math.toRadians(p1.yaw);
            float yawRad2 = (float)Math.toRadians(p2.yaw);

            double px1 = p1.position.x - cameraPos.x;
            double py1 = p1.position.y - cameraPos.y + 0.5;
            double pz1 = p1.position.z - cameraPos.z;

            double px2 = p2.position.x - cameraPos.x;
            double py2 = p2.position.y - cameraPos.y + 0.5;
            double pz2 = p2.position.z - cameraPos.z;

            // Simple perpendicular vectors
            double perpX1 = -Math.sin(yawRad1) * w1;
            double perpZ1 = Math.cos(yawRad1) * w1;
            double perpX2 = -Math.sin(yawRad2) * w2;
            double perpZ2 = Math.cos(yawRad2) * w2;

            // Add quad vertices
            buffer.vertex((float)(px1 + perpX1), (float)py1, (float)(pz1 + perpZ1))
                  .color(trailColorR, trailColorG, trailColorB, alpha1);
            buffer.vertex((float)(px1 - perpX1), (float)py1, (float)(pz1 - perpZ1))
                  .color(trailColorR, trailColorG, trailColorB, alpha1);
            buffer.vertex((float)(px2 - perpX2), (float)py2, (float)(pz2 - perpZ2))
                  .color(trailColorR, trailColorG, trailColorB, alpha2);
            buffer.vertex((float)(px2 + perpX2), (float)py2, (float)(pz2 + perpZ2))
                  .color(trailColorR, trailColorG, trailColorB, alpha2);
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }

    // New: Player Afterimages Rendering
    private void renderPlayerAfterimages(MatrixStack matrices, float tickDelta) {
        if (performanceMode && lowQualityShaders) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        if (playerAfterimages.isEmpty()) return;

        // Render afterimages using the entity renderer with modified alpha/scale
        // This is a simplified version - full implementation would use EntityRenderDispatcher
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableCull();

        // Note: Full afterimage rendering requires accessing EntityRenderDispatcher
        // and rendering the player entity multiple times with different transforms
        // This is a placeholder showing the pattern

        // For now, render simple ghost quads at afterimage positions
        Camera camera = client.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        for (Afterimage img : playerAfterimages) {
            if (img.alpha <= 0.01f) continue;

            double px = img.position.x - cameraPos.x;
            double py = img.position.y - cameraPos.y;
            double pz = img.position.z - cameraPos.z;

            float size = 0.6f * img.scale;
            float halfSize = size / 2;

            // Simple billboard quad facing camera
            buffer.vertex((float)(px - halfSize), (float)(py + size), (float)(pz - halfSize))
                  .color(1, 1, 1, img.alpha);
            buffer.vertex((float)(px + halfSize), (float)(py + size), (float)(pz - halfSize))
                  .color(1, 1, 1, img.alpha);
            buffer.vertex((float)(px + halfSize), (float)(py - halfSize), (float)(pz - halfSize))
                  .color(1, 1, 1, img.alpha);
            buffer.vertex((float)(px - halfSize), (float)(py - halfSize), (float)(pz - halfSize))
                  .color(1, 1, 1, img.alpha);
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.enableCull();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }

    // New: Particle Enhancements
    private void renderParticleEnhancements(MatrixStack matrices, int width, int height, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        float time = effectTime;
        Tessellator tessellator = Tessellator.getInstance();

        // 1. Bloom Particles - glowing particles around bright areas
        if (bloomParticlesEnabled) {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            RenderSystem.setShaderColor(1, 1, 1, particleBloomIntensity * 0.6f);

            BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
            int numBloomParticles = performanceMode ? 20 : 50;

            for (int i = 0; i < numBloomParticles; i++) {
                float angle = (i / (float)numBloomParticles) * (float)Math.PI * 2 + time * 0.5f;
                float radius = 100 + (float)Math.sin(time + i * 0.5f) * 50;
                float x = width / 2 + (float)Math.cos(angle) * radius;
                float y = height / 2 + (float)Math.sin(angle) * radius;
                float size = 8 + (float)Math.sin(time * 3 + i) * 4;
                float alpha = (float)(Math.sin(time * 2 + i) * 0.3 + 0.5);

                RenderSystem.setShaderColor(1, 0.9f, 0.7f, particleBloomIntensity * alpha * 0.4f);
                buffer.vertex(x - size, y + size, 0);
                buffer.vertex(x + size, y + size, 0);
                buffer.vertex(x + size, y - size, 0);
                buffer.vertex(x - size, y - size, 0);
            }
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.disableBlend();
        }

        // 2. Trail Particles - motion trails behind player movement
        if (trailParticlesEnabled && client.player != null) {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            double speed = Math.sqrt(client.player.getVelocity().x * client.player.getVelocity().x +
                                    client.player.getVelocity().z * client.player.getVelocity().z);

            if (speed > 0.05) {
                float trailAlpha = Math.min(1.0f, (float)(speed * particleTrailFadeSpeed)) * 0.5f;
                RenderSystem.setShaderColor(0.5f, 0.7f, 1.0f, trailAlpha);

                BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION);
                int trailSegments = performanceMode ? 5 : (int)Math.min(particleTrailLength, 20);

                for (int i = 0; i < trailSegments; i++) {
                    float progress = i / (float)trailSegments;
                    float trailX = width / 2 + (float)Math.sin(time * 2 + progress * 5) * 50 * progress;
                    float trailY = height / 2 + (float)Math.cos(time * 2 + progress * 5) * 50 * progress;
                    float nextTrailX = width / 2 + (float)Math.sin(time * 2 + (progress + 1.0f/trailSegments) * 5) * 50 * (progress + 1.0f/trailSegments);
                    float nextTrailY = height / 2 + (float)Math.cos(time * 2 + (progress + 1.0f/trailSegments) * 5) * 50 * (progress + 1.0f/trailSegments);

                    buffer.vertex(trailX, trailY, 0);
                    buffer.vertex(nextTrailX, nextTrailY, 0);
                }
                BufferRenderer.drawWithGlobalProgram(buffer.end());
            }
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.disableBlend();
        }

        // 3. Custom Particles - ambient decorative particles
        if (customParticlesEnabled) {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            int maxParticles = performanceMode ? particleMaxCustomParticles / 4 : particleMaxCustomParticles;
            maxParticles = Math.min(maxParticles, 200);

            BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);

            for (int i = 0; i < maxParticles; i++) {
                // Use deterministic pseudo-random positions based on effectTime
                float seed = i * 17.3f;
                float x = (float)(Math.sin(time * 0.3f + seed) * 0.5 + 0.5) * width;
                float y = (float)(Math.cos(time * 0.2f + seed * 1.3f) * 0.5 + 0.5) * height;
                float size = 2 + (float)(Math.sin(time * 1.5f + seed * 2.1f) * 1.5f + 1.5f);
                float alpha = (float)(Math.sin(time + seed * 0.7f) * 0.2 + 0.3);
                float r = (float)(Math.sin(seed * 0.5f) * 0.3 + 0.7);
                float g = (float)(Math.cos(seed * 0.5f) * 0.3 + 0.7);
                float b = (float)(Math.sin(seed * 0.7f) * 0.3 + 0.7);

                RenderSystem.setShaderColor(r, g, b, alpha * 0.4f);
                buffer.vertex(x - size, y + size, 0);
                buffer.vertex(x + size, y + size, 0);
                buffer.vertex(x + size, y - size, 0);
                buffer.vertex(x - size, y - size, 0);
            }
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.disableBlend();
        }

        // 4. Particle Lighting - dynamic light sources from particles
        if (particleLightingEnabled) {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

            BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
            int numLights = performanceMode ? 3 : 8;

            for (int i = 0; i < numLights; i++) {
                float angle = (i / (float)numLights) * (float)Math.PI * 2 + time * 0.3f;
                float radius = particleLightRadius * 50;
                float x = width / 2 + (float)Math.cos(angle) * radius;
                float y = height / 2 + (float)Math.sin(angle) * radius;
                float lightSize = particleLightRadius * 20;
                float pulse = (float)(Math.sin(time * 2 + i) * 0.3 + 0.7);
                float alpha = particleLightIntensity * pulse * 0.2f;

                RenderSystem.setShaderColor(1.0f, 0.9f, 0.6f, alpha);
                buffer.vertex(x - lightSize, y + lightSize, 0);
                buffer.vertex(x + lightSize, y + lightSize, 0);
                buffer.vertex(x + lightSize, y - lightSize, 0);
                buffer.vertex(x - lightSize, y - lightSize, 0);
            }
            BufferRenderer.drawWithGlobalProgram(buffer.end());

            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.disableBlend();
        }
    }

    // New: Chat Message Effects
    private void renderChatEffects(WorldRenderContext context) {
        if (!chatEffectsEnabled || performanceMode) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.inGameHud == null || client.inGameHud.getChatHud() == null) return;

        MatrixStack matrices = context.matrixStack();
        matrices.push();
        matrices.translate(0, 0, 950); // Above other overlays

        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();

        // Get chat messages from ChatHud
        // Note: ChatHud fields are private, so we use reflection or render our own
        // For now, this is a placeholder for the render logic

        // Render background blur for chat area
        if (chatBackgroundBlurEnabled && !performanceMode) {
            renderChatBackgroundBlur(matrices, width, height);
        }

        // Render individual message effects (fade in/out, animations)
        renderChatMessages(matrices, width, height, context.tickCounter().getTickDelta(false));

        matrices.pop();
    }

    private void renderChatBackgroundBlur(MatrixStack matrices, int width, int height) {
        if (performanceMode && lowQualityShaders) return;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Chat area typically at bottom-left
        int chatX = 10;
        int chatY = height - 150;
        int chatWidth = 400;
        int chatHeight = 140;

        // Draw semi-transparent blurred background
        float alpha = chatBackgroundBlurIntensity * 0.6f;
        RenderSystem.setShaderColor(0, 0, 0, alpha);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        buffer.vertex(chatX, chatY + chatHeight, 0);
        buffer.vertex(chatX + chatWidth, chatY + chatHeight, 0);
        buffer.vertex(chatX + chatWidth, chatY, 0);
        buffer.vertex(chatX, chatY, 0);
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }

    private void renderChatMessages(MatrixStack matrices, int width, int height, float tickDelta) {
        // This would integrate with ChatHud's message rendering
        // Uses effectTime for animation timing
        float animTime = effectTime * chatAnimationSpeed;

        // Example animation logic based on chatAnimationType:
        // 0 = Fade: alpha = 1 - (age / fadeOutDuration)
        // 1 = Slide: xOffset = easeOut(age / fadeInDuration) * slideDistance
        // 2 = Scale: scale = easeOut(age / fadeInDuration)
        // 3 = Typewriter: character reveal based on age * charsPerSecond
        // 4 = Glitch: random offset/jitter based on sin(animTime * 10)

        // Integration point: Mixin or custom ChatHud renderer would call this
    }

    // New: Chat Message Effect Data
    static class ChatMessageEffect {
        String message;
        long timestamp;
        float age;
        float fadeAlpha;
        float slideOffset;
        float scale;
        int animationType;

        ChatMessageEffect(String message, int animationType) {
            this.message = message;
            this.timestamp = System.currentTimeMillis();
            this.age = 0f;
            this.fadeAlpha = 0f;
            this.slideOffset = 0f;
            this.scale = 0.5f;
            this.animationType = animationType;
        }

        void update(float deltaTime) {
            age += deltaTime;

            switch (animationType) {
                case 0: // Fade
                    if (age < chatFadeInDuration) {
                        fadeAlpha = age / chatFadeInDuration;
                    } else if (age > chatFadeOutDuration) {
                        fadeAlpha = Math.max(0, 1 - (age - chatFadeOutDuration) / chatFadeOutDuration);
                    } else {
                        fadeAlpha = 1f;
                    }
                    break;
                case 1: // Slide
                    if (age < chatFadeInDuration) {
                        float progress = age / chatFadeInDuration;
                        slideOffset = (float)Math.sin(progress * Math.PI / 2) * 50; // Ease out
                    }
                    fadeAlpha = Math.min(1f, age / chatFadeInDuration);
                    break;
                case 2: // Scale
                    if (age < chatFadeInDuration) {
                        float progress = age / chatFadeInDuration;
                        scale = 0.5f + 0.5f * (float)Math.sin(progress * Math.PI / 2);
                    } else {
                        scale = 1f;
                    }
                    fadeAlpha = Math.min(1f, age / chatFadeInDuration);
                    break;
                case 3: // Typewriter
                    fadeAlpha = Math.min(1f, age / chatFadeInDuration);
                    break;
                case 4: // Glitch
                    fadeAlpha = Math.min(1f, age / chatFadeInDuration);
                    // Glitch effect applied in render via effectTime
                    break;
            }
        }
    }

    // New: Entity Outline/Glow (simplified screen-space glow)
    private void renderEntityOutline(MatrixStack matrices, int width, int height, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        // Use effectTime for pulsing animation
        float pulse = (float)(Math.sin(effectTime * entityOutlinePulseSpeed) * 0.5 + 0.5);
        float intensity = entityOutlineIntensity * (0.7f + pulse * 0.3f);

        // Simplified screen-space entity glow - render glow quads at entity screen positions
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);

        Tessellator tessellator = Tessellator.getInstance();
        Camera camera = client.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();

        for (net.minecraft.entity.Entity entity : client.world.getEntities()) {
            if (entity == client.player && !client.options.getPerspective().isFirstPerson()) continue;
            if (!entityOutlineThroughWalls && !client.player.canSee(entity)) continue;

            boolean isHostile = entity instanceof net.minecraft.entity.mob.HostileEntity;
            boolean isPassive = entity instanceof net.minecraft.entity.passive.PassiveEntity && !(entity instanceof PlayerEntity);
            boolean isPlayer = entity instanceof PlayerEntity;

            if (!isHostile && !isPassive && !isPlayer) continue;

            float r, g, b, a;
            if (isHostile) {
                r = entityOutlineHostileColorR; g = entityOutlineHostileColorG; b = entityOutlineHostileColorB; a = entityOutlineHostileColorA;
            } else if (isPassive) {
                r = entityOutlinePassiveColorR; g = entityOutlinePassiveColorG; b = entityOutlinePassiveColorB; a = entityOutlinePassiveColorA;
            } else { // Player
                r = entityOutlinePlayerColorR; g = entityOutlinePlayerColorG; b = entityOutlinePlayerColorB; a = entityOutlinePlayerColorA;
            }

            // Project entity position to screen
            Vec3d entityPos = entity.getPos();
            double px = entityPos.x - cameraPos.x;
            double py = entityPos.y - cameraPos.y + entity.getHeight() * 0.5;
            double pz = entityPos.z - cameraPos.z;

            // Simple screen projection (very approximate)
            // In a real implementation, you'd use the projection matrix
            float scale = 0.02f * intensity * a;
            float size = (float)(entity.getWidth() * 50 * scale);

            RenderSystem.setShaderColor(r, g, b, a * intensity * 0.5f);

            BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
            buffer.vertex((float)(px - size), (float)(py + size), (float)pz);
            buffer.vertex((float)(px + size), (float)(py + size), (float)pz);
            buffer.vertex((float)(px + size), (float)(py - size), (float)pz);
            buffer.vertex((float)(px - size), (float)(py - size), (float)pz);
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        }

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    // New: Dynamic FOV
    private void applyDynamicFov(MinecraftClient client) {
        if (client.player == null) return;

        float targetFov = baseFov;
        boolean sprinting = client.player.isSprinting() && !client.player.isUsingItem();
        boolean elytra = client.player.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST).isOf(net.minecraft.item.Items.ELYTRA) && client.player.isGliding();

        if (sprintFovBoost && sprinting) {
            targetFov += sprintFovAmount;
        }
        if (elytraFovBoost && elytra) {
            targetFov += elytraFovAmount;
        }
        if (speedFovEnabled) {
            double speed = Math.sqrt(client.player.getVelocity().x * client.player.getVelocity().x +
                                    client.player.getVelocity().z * client.player.getVelocity().z);
            targetFov += (float)speed * speedFovMultiplier * 10;
        }

        // Smoothly interpolate FOV - use SimpleOption.setValue()
        int currentFov = client.options.getFov().getValue();
        int newFov = (int)(currentFov * 0.9 + targetFov * 0.1);
        client.options.getFov().setValue(newFov);
    }

    public static void triggerScreenShake(float strength) {
        if (!screenShakeEnabled) return;
        currentScreenShake = Math.max(currentScreenShake, Math.max(0.0f, strength) * screenShakeTriggerStrength);
    }

    public static Vec3d getScreenShakeOffset() {
        if (!screenShakeEnabled || currentScreenShake <= 0.0001f) {
            return Vec3d.ZERO;
        }

        double amplitude = currentScreenShake * screenShakeIntensity * 0.08;
        return new Vec3d(
            Math.sin(effectTime * 43.0f) * amplitude,
            Math.sin(effectTime * 57.0f) * amplitude * 0.6,
            Math.sin(effectTime * 37.0f) * amplitude * 0.4
        );
    }

    public static void onHitEntity(Vec3d hitPos) {
        if (hitEffectsEnabled) { hitFlashTimer = 0.2f; lastHitPos = hitPos; if (hitParticlesEnabled && !performanceMode) spawnHitParticles(hitPos); }
        triggerScreenShake(1.0f);
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
    // New getters
    public static boolean isDynamicFovEnabled() { return dynamicFovEnabled; }
    public static boolean isDamageIndicatorsEnabled() { return damageIndicatorsEnabled; }
    public static boolean isCustomCrosshairEnabled() { return customCrosshairEnabled; }
    public static boolean isWeatherEnhancedEnabled() { return weatherEnhanced; }
    public static boolean isWaterCausticsEnabled() { return waterCausticsEnabled; }
    public static boolean isSpeedLinesEnabled() { return speedLinesEnabled; }
    public static boolean isPhotoModeEnabled() { return photoModeEnabled; }
    public static int getColorGradingPreset() { return colorGradingPreset; }
    public static int getCrosshairStyle() { return crosshairStyle; }
    // New: Biome Color Grading getters
    public static boolean isBiomeColorGradingEnabled() { return biomeColorGradingEnabled; }
    public static float getBiomeColorGradingIntensity() { return biomeColorGradingIntensity; }
    public static boolean isBiomeColorGradingSmoothTransitionsEnabled() { return biomeColorGradingSmoothTransitions; }
    public static float getBiomeColorGradingTransitionSpeed() { return biomeColorGradingTransitionSpeed; }

    // New: Time Effects getters
    public static boolean isTimeEffectsEnabled() { return timeEffectsEnabled; }
    public static boolean isSunriseEnabled() { return sunriseEnabled; }
    public static boolean isSunsetEnabled() { return sunsetEnabled; }
    public static boolean isNightVisionEnabled() { return nightVisionEnabled; }
    public static boolean isGoldenHourEnabled() { return goldenHourEnabled; }
    public static float getSunriseIntensity() { return sunriseIntensity; }
    public static float getSunsetIntensity() { return sunsetIntensity; }
    public static float getNightVisionIntensity() { return nightVisionIntensity; }
    public static float getGoldenHourIntensity() { return goldenHourIntensity; }
    public static float getTimeTransitionSpeed() { return timeTransitionSpeed; }
    public static float getSunriseGlowIntensity() { return sunriseGlowIntensity; }
    public static float getSunsetGlowIntensity() { return sunsetGlowIntensity; }
    public static float getNightVisionNoiseIntensity() { return nightVisionNoiseIntensity; }
    public static float getGoldenHourWarmthIntensity() { return goldenHourWarmthIntensity; }

    // New: Player Trails / Afterimages getters
    public static boolean isPlayerTrailsEnabled() { return playerTrailsEnabled; }
    public static int getTrailLength() { return trailLength; }
    public static float getTrailWidth() { return trailWidth; }
    public static float getTrailColorR() { return trailColorR; }
    public static float getTrailColorG() { return trailColorG; }
    public static float getTrailColorB() { return trailColorB; }
    public static float getTrailColorA() { return trailColorA; }
    public static boolean isTrailFadeEnabled() { return trailFadeEnabled; }
    public static boolean isAfterimageEnabled() { return afterimageEnabled; }
    public static int getAfterimageCount() { return afterimageCount; }
    public static float getAfterimageSpacing() { return afterimageSpacing; }
    public static float getAfterimageAlpha() { return afterimageAlpha; }
    public static boolean isAfterimageScaleEnabled() { return afterimageScale; }
    public static boolean isPlayerTrailParticlesEnabled() { return playerTrailParticlesEnabled; }

    // New: Block Highlight/Outline getters
    public static boolean isBlockHighlightEnabled() { return blockHighlightEnabled; }
    public static float getBlockHighlightR() { return blockHighlightR; }
    public static float getBlockHighlightG() { return blockHighlightG; }
    public static float getBlockHighlightB() { return blockHighlightB; }
    public static float getBlockHighlightA() { return blockHighlightA; }
    public static float getBlockOutlineWidth() { return blockOutlineWidth; }
    public static boolean isBlockHighlightPulseEnabled() { return blockHighlightPulseEnabled; }
    public static float getBlockHighlightPulseSpeed() { return blockHighlightPulseSpeed; }
    public static float getBlockHighlightPulseMinAlpha() { return blockHighlightPulseMinAlpha; }
    public static float getBlockHighlightPulseMaxAlpha() { return blockHighlightPulseMaxAlpha; }
    public static boolean isBlockHighlightOresEnabled() { return blockHighlightOres; }
    public static boolean isBlockHighlightChestsEnabled() { return blockHighlightChests; }
    public static boolean isBlockHighlightMobsEnabled() { return blockHighlightMobs; }
    public static boolean isBlockHighlightPlayersEnabled() { return blockHighlightPlayers; }
    public static float getBlockHighlightRange() { return blockHighlightRange; }
    public static int getBlockHighlightMode() { return blockHighlightMode; }
    public static boolean isBlockHighlightThroughWallsEnabled() { return blockHighlightThroughWalls; }

    // New: Entity Outline getters
    public static boolean isEntityOutlineEnabled() { return entityOutlineEnabled; }
    public static boolean isEntityOutlineThroughWallsEnabled() { return entityOutlineThroughWalls; }
    public static float getEntityOutlineWidth() { return entityOutlineWidth; }
    public static float getEntityOutlineHostileColorR() { return entityOutlineHostileColorR; }
    public static float getEntityOutlineHostileColorG() { return entityOutlineHostileColorG; }
    public static float getEntityOutlineHostileColorB() { return entityOutlineHostileColorB; }
    public static float getEntityOutlineHostileColorA() { return entityOutlineHostileColorA; }
    public static float getEntityOutlinePassiveColorR() { return entityOutlinePassiveColorR; }
    public static float getEntityOutlinePassiveColorG() { return entityOutlinePassiveColorG; }
    public static float getEntityOutlinePassiveColorB() { return entityOutlinePassiveColorB; }
    public static float getEntityOutlinePassiveColorA() { return entityOutlinePassiveColorA; }
    public static float getEntityOutlinePlayerColorR() { return entityOutlinePlayerColorR; }
    public static float getEntityOutlinePlayerColorG() { return entityOutlinePlayerColorG; }
    public static float getEntityOutlinePlayerColorB() { return entityOutlinePlayerColorB; }
    public static float getEntityOutlinePlayerColorA() { return entityOutlinePlayerColorA; }
    public static float getEntityOutlineIntensity() { return entityOutlineIntensity; }
    public static float getEntityOutlinePulseSpeed() { return entityOutlinePulseSpeed; }

    // New: Particle Enhancements getters
    public static boolean isParticleEnhancementsEnabled() { return particleEnhancementsEnabled; }
    public static boolean isBloomParticlesEnabled() { return bloomParticlesEnabled; }
    public static boolean isTrailParticlesEnabled() { return trailParticlesEnabled; }
    public static boolean isCustomParticlesEnabled() { return customParticlesEnabled; }
    public static boolean isParticleLightingEnabled() { return particleLightingEnabled; }
    public static float getParticleBloomIntensity() { return particleBloomIntensity; }
    public static float getParticleTrailLength() { return particleTrailLength; }
    public static float getParticleTrailFadeSpeed() { return particleTrailFadeSpeed; }
    public static int getParticleMaxCustomParticles() { return particleMaxCustomParticles; }
    public static float getParticleLightRadius() { return particleLightRadius; }
    public static float getParticleLightIntensity() { return particleLightIntensity; }

    // New: Shader Quality Presets getters
    public static ShaderPreset getShaderPreset() { return shaderPreset; }
    public static boolean isCustomShaderSettingsEnabled() { return customSettings; }
    public static int getShaderBloomSamples() { return shaderBloomSamples; }
    public static int getShaderChromaticSamples() { return shaderChromaticSamples; }
    public static int getShaderDofSamples() { return shaderDofSamples; }
    public static int getShaderMotionBlurSamples() { return shaderMotionBlurSamples; }
    public static int getShaderGodRaysSamples() { return shaderGodRaysSamples; }
    public static int getShaderLensFlareGhosts() { return shaderLensFlareGhosts; }
    public static int getShaderStarFieldCount() { return shaderStarFieldCount; }
    public static int getShaderParticleMaxCount() { return shaderParticleMaxCount; }
    public static float getShaderRenderScale() { return shaderRenderScale; }
    public static boolean isShaderBloomEnabled() { return shaderEnableBloom; }
    public static boolean isShaderChromaticAberrationEnabled() { return shaderEnableChromaticAberration; }
    public static boolean isShaderDepthOfFieldEnabled() { return shaderEnableDepthOfField; }
    public static boolean isShaderMotionBlurEnabled() { return shaderEnableMotionBlur; }
    public static boolean isShaderGodRaysEnabled() { return shaderEnableGodRays; }
    public static boolean isShaderLensFlareEnabled() { return shaderEnableLensFlare; }
    public static boolean isShaderStarFieldEnabled() { return shaderEnableStarField; }
    public static boolean isShaderAuroraEnabled() { return shaderEnableAurora; }
    public static boolean isShaderWaterCausticsEnabled() { return shaderEnableWaterCaustics; }
    public static boolean isShaderEntityOutlineEnabled() { return shaderEnableEntityOutline; }
    public static boolean isShaderSpeedLinesEnabled() { return shaderEnableSpeedLines; }
    public static boolean isShaderBiomeColorGradingEnabled() { return shaderEnableBiomeColorGrading; }
    public static boolean isShaderParticleEnhancementsEnabled() { return shaderEnableParticleEnhancements; }

    // New: God Rays getters
    public static boolean isGodRaysEnabled() { return godRaysEnabled; }
    public static float getGodRaysIntensity() { return godRaysIntensity; }
    public static float getGodRaysDensity() { return godRaysDensity; }
    public static float getGodRaysDecay() { return godRaysDecay; }
    public static float getGodRaysWeight() { return godRaysWeight; }
    public static float getGodRaysExposure() { return godRaysExposure; }
    public static int getGodRaysNumSamples() { return godRaysNumSamples; }

    // New: Lens Flare getters
    public static boolean isLensFlareEnabled() { return lensFlareEnabled; }
    public static float getLensFlareIntensity() { return lensFlareIntensity; }
    public static float getLensFlareThreshold() { return lensFlareThreshold; }
    public static int getLensFlareGhosts() { return lensFlareGhosts; }
    public static boolean isLensFlareHaloEnabled() { return lensFlareHaloEnabled; }
    public static float getLensFlareHaloIntensity() { return lensFlareHaloIntensity; }
    public static boolean isLensFlareChromaticAberrationEnabled() { return lensFlareChromaticAberration; }
    public static float getLensFlareChromaticIntensity() { return lensFlareChromaticIntensity; }
    public static int getLensFlareGhostCountMin() { return lensFlareGhostCountMin; }
    public static int getLensFlareGhostCountMax() { return lensFlareGhostCountMax; }
    public static float getLensFlareRotationSpeed() { return lensFlareRotationSpeed; }
    public static boolean isLensFlareAnamorphic() { return lensFlareAnamorphic; }

    // New: Chat Message Effects getters
    public static boolean isChatEffectsEnabled() { return chatEffectsEnabled; }
    public static boolean isChatFadeInEnabled() { return chatFadeInEnabled; }
    public static float getChatFadeInDuration() { return chatFadeInDuration; }
    public static boolean isChatFadeOutEnabled() { return chatFadeOutEnabled; }
    public static float getChatFadeOutDuration() { return chatFadeOutDuration; }
    public static boolean isChatBackgroundBlurEnabled() { return chatBackgroundBlurEnabled; }
    public static float getChatBackgroundBlurIntensity() { return chatBackgroundBlurIntensity; }
    public static boolean isChatTimestampEnabled() { return chatTimestampEnabled; }
    public static int getChatAnimationType() { return chatAnimationType; }
    public static float getChatAnimationSpeed() { return chatAnimationSpeed; }
    public static float getChatMessageSpacing() { return chatMessageSpacing; }
    public static boolean isChatShadowEnabled() { return chatShadowEnabled; }
    public static float getChatShadowOffset() { return chatShadowOffset; }
    public static float getChatMaxMessages() { return chatMaxMessages; }

    // New: Time Effects render methods
    private void renderTimeEffects(WorldRenderContext context) {
        if (!timeEffectsEnabled || performanceMode) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        float timeOfDay = getTimeOfDay(client.world);
        MatrixStack matrices = context.matrixStack();

        // Determine which time effect to apply based on time of day
        if (isSunriseTime(timeOfDay) && sunriseEnabled) {
            renderSunriseEffect(matrices, context.tickCounter().getTickDelta(false), timeOfDay);
        } else if (isSunsetTime(timeOfDay) && sunsetEnabled) {
            renderSunsetEffect(matrices, context.tickCounter().getTickDelta(false), timeOfDay);
        } else if (isNightTime(timeOfDay) && nightVisionEnabled) {
            renderNightVisionEffect(matrices, context.tickCounter().getTickDelta(false));
        } else if (isGoldenHourTime(timeOfDay) && goldenHourEnabled) {
            renderGoldenHourEffect(matrices, context.tickCounter().getTickDelta(false), timeOfDay);
        }
    }

    private void renderSunriseEffect(MatrixStack matrices, float tickDelta, float timeOfDay) {
        if (performanceMode && lowQualityShaders) return;

        float progress = getSunriseProgress(timeOfDay);
        float intensity = sunriseIntensity * sunriseGlowIntensity * progress;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        // Warm sunrise colors - orange/pink gradient
        float r = 1.0f;
        float g = 0.4f + 0.3f * (float)Math.sin(effectTime * 0.5f + timeOfDay * 10);
        float b = 0.1f + 0.2f * (float)Math.cos(effectTime * 0.3f);

        RenderSystem.setShaderColor(r, g, b, intensity * 0.15f);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        MinecraftClient client = MinecraftClient.getInstance();
        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();

        buffer.vertex(0, height, 0);
        buffer.vertex(width, height, 0);
        buffer.vertex(width, 0, 0);
        buffer.vertex(0, 0, 0);
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        // Sunrise glow particles at horizon
        if (!performanceMode || !reducedParticles) {
            renderSunriseParticles(matrices, tickDelta, progress);
        }

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }

    private void renderSunsetEffect(MatrixStack matrices, float tickDelta, float timeOfDay) {
        if (performanceMode && lowQualityShaders) return;

        float progress = getSunsetProgress(timeOfDay);
        float intensity = sunsetIntensity * sunsetGlowIntensity * progress;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        // Deep sunset colors - red/orange/purple
        float r = 1.0f;
        float g = 0.2f + 0.2f * (float)Math.sin(effectTime * 0.4f + timeOfDay * 8);
        float b = 0.3f + 0.2f * (float)Math.cos(effectTime * 0.2f);

        RenderSystem.setShaderColor(r, g, b, intensity * 0.18f);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        MinecraftClient client = MinecraftClient.getInstance();
        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();

        buffer.vertex(0, height, 0);
        buffer.vertex(width, height, 0);
        buffer.vertex(width, 0, 0);
        buffer.vertex(0, 0, 0);
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        // Sunset particles
        if (!performanceMode || !reducedParticles) {
            renderSunsetParticles(matrices, tickDelta, progress);
        }

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }

    private void renderNightVisionEffect(MatrixStack matrices, float tickDelta) {
        if (performanceMode) return;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Green night vision tint with scanlines
        float intensity = nightVisionIntensity;
        RenderSystem.setShaderColor(0.1f, 1.0f, 0.1f, intensity * 0.12f);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        MinecraftClient client = MinecraftClient.getInstance();
        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();

        buffer.vertex(0, height, 0);
        buffer.vertex(width, height, 0);
        buffer.vertex(width, 0, 0);
        buffer.vertex(0, 0, 0);
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        // Scanline effect
        if (!performanceMode || !reducedParticles) {
            renderNightVisionScanlines(matrices, width, height, tickDelta);
        }

        // Noise/grain overlay
        if (nightVisionNoiseIntensity > 0) {
            renderNightVisionNoise(matrices, width, height, tickDelta);
        }

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }

    private void renderGoldenHourEffect(MatrixStack matrices, float tickDelta, float timeOfDay) {
        if (performanceMode && lowQualityShaders) return;

        float progress = getGoldenHourProgress(timeOfDay);
        float intensity = goldenHourIntensity * goldenHourWarmthIntensity * progress;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Warm golden tones
        float r = 1.0f;
        float g = 0.7f + 0.2f * (float)Math.sin(effectTime * 0.3f + timeOfDay * 5);
        float b = 0.2f + 0.1f * (float)Math.cos(effectTime * 0.2f);

        RenderSystem.setShaderColor(r, g, b, intensity * 0.1f);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        MinecraftClient client = MinecraftClient.getInstance();
        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();

        buffer.vertex(0, height, 0);
        buffer.vertex(width, height, 0);
        buffer.vertex(width, 0, 0);
        buffer.vertex(0, 0, 0);
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        // Golden hour lens flare / bloom
        if (!performanceMode || !reducedParticles) {
            renderGoldenHourParticles(matrices, tickDelta, progress);
        }

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }

    // Helper methods for time calculations
    private float getTimeOfDay(net.minecraft.world.World world) {
        long time = world.getTimeOfDay() % 24000;
        return time / 24000.0f; // 0.0 = midnight, 0.25 = sunrise, 0.5 = noon, 0.75 = sunset
    }

    private boolean isSunriseTime(float timeOfDay) {
        // Sunrise: ~23000-24000 (0.958-1.0) and 0-1000 (0.0-0.042)
        return timeOfDay >= 0.958f || timeOfDay <= 0.042f;
    }

    private boolean isSunsetTime(float timeOfDay) {
        // Sunset: ~12000-13000 (0.5-0.542)
        return timeOfDay >= 0.5f && timeOfDay <= 0.542f;
    }

    private boolean isNightTime(float timeOfDay) {
        // Night: ~13000-23000 (0.542-0.958)
        return timeOfDay > 0.542f && timeOfDay < 0.958f;
    }

    private boolean isGoldenHourTime(float timeOfDay) {
        // Golden hour: ~1000-3000 (0.042-0.125) and ~11000-12000 (0.458-0.5)
        return (timeOfDay >= 0.042f && timeOfDay <= 0.125f) || (timeOfDay >= 0.458f && timeOfDay <= 0.5f);
    }

    private float getSunriseProgress(float timeOfDay) {
        if (timeOfDay >= 0.958f) {
            return (timeOfDay - 0.958f) / 0.042f;
        } else {
            return 1.0f - timeOfDay / 0.042f;
        }
    }

    private float getSunsetProgress(float timeOfDay) {
        return 1.0f - (timeOfDay - 0.5f) / 0.042f;
    }

    private float getGoldenHourProgress(float timeOfDay) {
        if (timeOfDay <= 0.125f) {
            return 1.0f - (timeOfDay - 0.042f) / 0.083f;
        } else {
            return 1.0f - (timeOfDay - 0.458f) / 0.042f;
        }
    }

    private void renderSunriseParticles(MatrixStack matrices, float tickDelta, float progress) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        RenderSystem.setShaderColor(1, 0.5f, 0.2f, progress * 0.3f);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);

        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();
        int horizonY = height / 3;

        for (int i = 0; i < 20; i++) {
            float x = (float)Math.sin(effectTime * 0.5f + i * 0.3f) * width * 0.8f + width / 2f;
            float y = horizonY + (float)Math.cos(effectTime * 0.3f + i * 0.5f) * 50 * progress;
            float size = 5f + (float)Math.sin(effectTime + i) * 3f;

            buffer.vertex(x - size, y + size, 0);
            buffer.vertex(x + size, y + size, 0);
            buffer.vertex(x + size, y - size, 0);
            buffer.vertex(x - size, y - size, 0);
        }
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }

    private void renderSunsetParticles(MatrixStack matrices, float tickDelta, float progress) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        RenderSystem.setShaderColor(1, 0.3f, 0.4f, progress * 0.35f);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);

        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();
        int horizonY = height / 3;

        for (int i = 0; i < 25; i++) {
            float x = (float)Math.sin(effectTime * 0.4f + i * 0.25f) * width * 0.9f + width / 2f;
            float y = horizonY + (float)Math.cos(effectTime * 0.2f + i * 0.4f) * 60 * progress;
            float size = 4f + (float)Math.sin(effectTime * 1.2f + i) * 2f;

            buffer.vertex(x - size, y + size, 0);
            buffer.vertex(x + size, y + size, 0);
            buffer.vertex(x + size, y - size, 0);
            buffer.vertex(x - size, y - size, 0);
        }
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }

    private void renderNightVisionScanlines(MatrixStack matrices, int width, int height, float tickDelta) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(0, 0.8f, 0, nightVisionIntensity * 0.05f);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION);

        float scanlineOffset = (effectTime * 50) % 4;
        for (int y = (int)scanlineOffset; y < height; y += 4) {
            buffer.vertex(0, y, 0);
            buffer.vertex(width, y, 0);
        }
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }

    private void renderNightVisionNoise(MatrixStack matrices, int width, int height, float tickDelta) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(0.1f, 0.5f, 0.1f, nightVisionNoiseIntensity * 0.08f);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);

        int tileSize = 8;
        for (int x = 0; x < width; x += tileSize) {
            for (int y = 0; y < height; y += tileSize) {
                if (Math.random() > 0.7) {
                    buffer.vertex(x, y + tileSize, 0);
                    buffer.vertex(x + tileSize, y + tileSize, 0);
                    buffer.vertex(x + tileSize, y, 0);
                    buffer.vertex(x, y, 0);
                }
            }
        }
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }

    private void renderGoldenHourParticles(MatrixStack matrices, float tickDelta, float progress) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        RenderSystem.setShaderColor(1, 0.8f, 0.3f, progress * 0.25f);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);

        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();

        for (int i = 0; i < 15; i++) {
            float angle = (i / 15.0f) * (float)Math.PI * 2 + effectTime * 0.3f;
            float radius = 100 + (float)Math.sin(effectTime + i) * 50;
            float x = width / 2f + (float)Math.cos(angle) * radius;
            float y = height / 2f + (float)Math.sin(angle) * radius;
            float size = 8f * progress;

            buffer.vertex(x - size, y + size, 0);
            buffer.vertex(x + size, y + size, 0);
            buffer.vertex(x + size, y - size, 0);
            buffer.vertex(x - size, y - size, 0);
        }
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }

    // New: God Rays render methods
    private void applyGodRays(float tickDelta) {
        if (!godRaysEnabled) return;
        if (performanceMode && godRaysNumSamples > 32) return; // Reduce quality in performance mode

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        // Get sun position for god rays origin
        float sunAngle = client.world.getSkyAngle(tickDelta);
        Vec3d sunPos = new Vec3d(
            Math.cos(sunAngle * Math.PI * 2) * 1000,
            256,
            Math.sin(sunAngle * Math.PI * 2) * 1000
        );

        // Simple god rays implementation using radial blur from sun position
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        // Use effectTime for animation
        float time = effectTime * 0.5f;
        float animatedDensity = godRaysDensity + (float)Math.sin(time) * 0.05f;

        RenderSystem.setShaderColor(1, 0.95f, 0.85f, godRaysIntensity * 0.6f);

        // Radial blur from sun screen position
        Tessellator tessellator = Tessellator.getInstance();
        int width = client.getWindow().getFramebufferWidth();
        int height = client.getWindow().getFramebufferHeight();
        int centerX = width / 2;
        int centerY = height / 2;

        // Project sun to screen (simplified - just use center for now)
        // In a full implementation, you'd project the sun position to screen coordinates

        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

        // Draw multiple layers for volumetric effect
        int samples = performanceMode ? Math.min(godRaysNumSamples, 32) : godRaysNumSamples;
        for (int i = 0; i < samples; i++) {
            float progress = i / (float)samples;
            float scale = 1.0f + progress * godRaysWeight * 3.0f;
            float alpha = godRaysIntensity * (1.0f - progress) * godRaysExposure * (float)Math.pow(godRaysDecay, i);
            alpha *= animatedDensity;

            if (alpha < 0.01f) continue;

            RenderSystem.setShaderColor(1, 0.95f, 0.85f, alpha);

            float sizeX = width * scale;
            float sizeY = height * scale;
            float offsetX = centerX - sizeX / 2;
            float offsetY = centerY - sizeY / 2;

            buffer.vertex(offsetX, offsetY + sizeY, 0).texture(0, 1);
            buffer.vertex(offsetX + sizeX, offsetY + sizeY, 0).texture(1, 1);
            buffer.vertex(offsetX + sizeX, offsetY, 0).texture(1, 0);
            buffer.vertex(offsetX, offsetY, 0).texture(0, 0);
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }

    private void renderGodRays(WorldRenderContext context) {
        if (!godRaysEnabled) return;
        if (performanceMode) return; // Heavy effect, disable in performance mode

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        MatrixStack matrices = context.matrixStack();
        float tickDelta = context.tickCounter().getTickDelta(false);

        // Get sun position
        float sunAngle = client.world.getSkyAngle(tickDelta);
        float sunX = (float)Math.cos(sunAngle * Math.PI * 2);
        float sunY = (float)Math.sin(sunAngle * Math.PI * 2);

        // Only render when sun is above horizon
        if (sunY < 0) return;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        RenderSystem.setShaderColor(1, 0.9f, 0.7f, godRaysIntensity * 0.4f * sunY);

        // Draw volumetric light shafts
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);

        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();
        int centerX = width / 2;
        int centerY = height / 2;

        // Screen-space sun position
        int sunScreenX = centerX + (int)(sunX * width * 0.5f);
        int sunScreenY = centerY - (int)(sunY * height * 0.5f);

        float time = effectTime * 0.3f;

        // Render light shafts radiating from sun
        for (int i = 0; i < godRaysNumSamples; i++) {
            float progress = i / (float)godRaysNumSamples;
            float rayLength = 300 * progress * godRaysWeight;
            float rayWidth = 50 * (1.0f - progress * 0.5f);
            float alpha = godRaysIntensity * (1.0f - progress) * godRaysExposure * (float)Math.pow(godRaysDecay, i);

            // Animate with density
            alpha *= godRaysDensity + (float)Math.sin(time + i * 0.5f) * 0.1f;
            if (alpha < 0.01f) continue;

            RenderSystem.setShaderColor(1, 0.95f, 0.85f, alpha);

            // Draw quad from sun outward
            double angle = Math.atan2(centerY - sunScreenY, centerX - sunScreenX);
            double perpAngle = angle + Math.PI / 2;

            float dx1 = (float)Math.cos(perpAngle) * rayWidth;
            float dy1 = (float)Math.sin(perpAngle) * rayWidth;
            float dx2 = (float)Math.cos(angle) * rayLength;
            float dy2 = (float)Math.sin(angle) * rayLength;

            buffer.vertex(sunScreenX - dx1, sunScreenY - dy1, 0);
            buffer.vertex(sunScreenX + dx1, sunScreenY + dy1, 0);
            buffer.vertex(sunScreenX + dx1 + dx2, sunScreenY + dy1 + dy2, 0);
            buffer.vertex(sunScreenX - dx1 + dx2, sunScreenY - dy1 + dy2, 0);
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }
}

class VisualsFabricResourceReloadListener implements IdentifiableResourceReloadListener {
    private static final Identifier ID = Identifier.of(VisualsFabricClient.MOD_ID, "resource_reload_listener");
    @Override public Identifier getFabricId() { return ID; }
    @Override public Collection<Identifier> getFabricDependencies() { return Collections.emptyList(); }
    @Override public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager resourceManager, Executor prepareExecutor, Executor applyExecutor) {
        return CompletableFuture.supplyAsync(() -> { VisualsFabricClient.LOGGER.info("Reloading VisualsFabric resources"); return null; }, prepareExecutor);
    }
}