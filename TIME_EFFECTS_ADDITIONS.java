// ============================================================================
// TIME-BASED EFFECTS - Code blocks to add to VisualsFabricClient.java
// ============================================================================

// --- 1. ADD STATIC FIELDS (after line ~131, after Photo Mode fields) ---
// Time-based Effects
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

// --- 2. ADD INTENSITY FIELDS (after line ~153, after skyColorIntensity) ---
// Time Effects Intensities
public static float sunriseGlowIntensity = 0.6f;
public static float sunsetGlowIntensity = 0.7f;
public static float nightVisionNoiseIntensity = 0.15f;
public static float goldenHourWarmthIntensity = 0.5f;

// --- 3. ADD KEY BINDINGS (after line ~222, after cycleBiomeColorIntensityKey) ---
// Time Effects Keybindings
public static KeyBinding toggleTimeEffectsKey;
public static KeyBinding toggleNightVisionKey;
public static KeyBinding toggleGoldenHourKey;
public static KeyBinding cycleSunriseIntensityKey;
public static KeyBinding cycleSunsetIntensityKey;
public static KeyBinding cycleNightVisionIntensityKey;
public static KeyBinding cycleGoldenHourIntensityKey;

// --- 4. REGISTER KEY BINDINGS (in registerKeyBindings(), after line ~266) ---
// Time Effects keybindings
toggleTimeEffectsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_time_effects", GLFW.GLFW_KEY_T, "category.visualsfabric.time"));
toggleNightVisionKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_night_vision", GLFW.GLFW_KEY_NUMPAD_1, "category.visualsfabric.time"));
toggleGoldenHourKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_golden_hour", GLFW.GLFW_KEY_NUMPAD_2, "category.visualsfabric.time"));
cycleSunriseIntensityKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.cycle_sunrise_intensity", GLFW.GLFW_KEY_S, "category.visualsfabric.time"));
cycleSunsetIntensityKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.cycle_sunset_intensity", GLFW.GLFW_KEY_W, "category.visualsfabric.time"));
cycleNightVisionIntensityKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.cycle_night_vision_intensity", GLFW.GLFW_KEY_NUMPAD_3, "category.visualsfabric.time"));
cycleGoldenHourIntensityKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.cycle_golden_hour_intensity", GLFW.GLFW_KEY_NUMPAD_4, "category.visualsfabric.time"));

// --- 5. ADD KEY HANDLING (in registerTickEvents(), after line ~321) ---
// Time Effects toggles
while (toggleTimeEffectsKey.wasPressed()) { timeEffectsEnabled = !timeEffectsEnabled; sendChatFeedback("Time Effects", timeEffectsEnabled); }
while (toggleNightVisionKey.wasPressed()) { nightVisionEnabled = !nightVisionEnabled; sendChatFeedback("Night Vision", nightVisionEnabled); }
while (toggleGoldenHourKey.wasPressed()) { goldenHourEnabled = !goldenHourEnabled; sendChatFeedback("Golden Hour", goldenHourEnabled); }
while (cycleSunriseIntensityKey.wasPressed()) { sunriseIntensity = (sunriseIntensity + 0.25f) % 1.25f; if (sunriseIntensity < 0.25f) sunriseIntensity = 0.25f; sendChatFeedback("Sunrise Intensity", String.format("%.2f", sunriseIntensity)); }
while (cycleSunsetIntensityKey.wasPressed()) { sunsetIntensity = (sunsetIntensity + 0.25f) % 1.25f; if (sunsetIntensity < 0.25f) sunsetIntensity = 0.25f; sendChatFeedback("Sunset Intensity", String.format("%.2f", sunsetIntensity)); }
while (cycleNightVisionIntensityKey.wasPressed()) { nightVisionIntensity = (nightVisionIntensity + 0.25f) % 1.25f; if (nightVisionIntensity < 0.25f) nightVisionIntensity = 0.25f; sendChatFeedback("Night Vision Intensity", String.format("%.2f", nightVisionIntensity)); }
while (cycleGoldenHourIntensityKey.wasPressed()) { goldenHourIntensity = (goldenHourIntensity + 0.25f) % 1.25f; if (goldenHourIntensity < 0.25f) goldenHourIntensity = 0.25f; sendChatFeedback("Golden Hour Intensity", String.format("%.2f", goldenHourIntensity)); }

// --- 6. ADD GETTER METHODS (after line ~912, after biome color grading getters) ---
// Time Effects getters
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

// --- 7. ADD RENDER METHODS (add new private methods before the closing brace) ---

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

// --- 8. REGISTER RENDER EVENT (in registerRenderEvents(), after line ~389) ---
// WorldRenderEvents.BEFORE_ENTITIES.register(this::renderTimeEffects);

// Or if you prefer overlay rendering (after entities):
// WorldRenderEvents.LAST.register(this::renderTimeEffectsOverlay);

private void renderTimeEffectsOverlay(WorldRenderContext context) {
    if (!timeEffectsEnabled) return;
    MinecraftClient client = MinecraftClient.getInstance();
    if (client.world == null || client.player == null) return;

    float timeOfDay = getTimeOfDay(client.world);
    MatrixStack matrices = context.matrixStack();
    matrices.push();
    matrices.translate(0, 0, 900);

    if (isSunriseTime(timeOfDay) && sunriseEnabled) {
        renderSunriseEffect(matrices, context.tickCounter().getTickDelta(false), timeOfDay);
    } else if (isSunsetTime(timeOfDay) && sunsetEnabled) {
        renderSunsetEffect(matrices, context.tickCounter().getTickDelta(false), timeOfDay);
    } else if (isNightTime(timeOfDay) && nightVisionEnabled) {
        renderNightVisionEffect(matrices, context.tickCounter().getTickDelta(false));
    } else if (isGoldenHourTime(timeOfDay) && goldenHourEnabled) {
        renderGoldenHourEffect(matrices, context.tickCounter().getTickDelta(false), timeOfDay);
    }

    matrices.pop();
}