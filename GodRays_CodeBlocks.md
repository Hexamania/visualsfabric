// ============================================================================
// 1. STATIC FIELDS - Add to the "Lighting" section (after line ~146)
// ============================================================================

// Lighting Effects
public static boolean godRaysEnabled = true;
public static float godRaysIntensity = 0.5f;
public static float godRaysDensity = 0.8f;
public static float godRaysDecay = 0.95f;
public static float godRaysWeight = 0.4f;
public static float godRaysExposure = 0.3f;
public static int godRaysNumSamples = 32;

// ============================================================================
// 2. GETTER METHODS - Add to the getter section (after line ~890)
// ============================================================================

public static boolean isGodRaysEnabled() { return godRaysEnabled; }
public static float getGodRaysIntensity() { return godRaysIntensity; }
public static float getGodRaysDensity() { return godRaysDensity; }
public static float getGodRaysDecay() { return godRaysDecay; }
public static float getGodRaysWeight() { return godRaysWeight; }
public static float getGodRaysExposure() { return godRaysExposure; }
public static int getGodRaysNumSamples() { return godRaysNumSamples; }

// ============================================================================
// 3. KEY BINDINGS - Add to registerKeyBindings() method (after line ~254)
// ============================================================================

// God Rays keybinds
toggleGodRaysKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_god_rays", GLFW.GLFW_KEY_1, "category.visualsfabric.lighting"));
cycleGodRaysQualityKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.cycle_god_rays_quality", GLFW.GLFW_KEY_2, "category.visualsfabric.lighting"));

// ============================================================================
// 4. KEY BINDING FIELDS - Add to key binding fields section (after line ~214)
// ============================================================================

public static KeyBinding toggleGodRaysKey;
public static KeyBinding cycleGodRaysQualityKey;

// ============================================================================
// 5. KEY HANDLING - Add to registerTickEvents() key handling (after line ~306)
// ============================================================================

while (toggleGodRaysKey.wasPressed()) { godRaysEnabled = !godRaysEnabled; sendChatFeedback("God Rays", godRaysEnabled); }
while (cycleGodRaysQualityKey.wasPressed()) { 
    int[] samples = {16, 32, 64, 128};
    int currentIndex = java.util.Arrays.asList(samples).indexOf(godRaysNumSamples);
    godRaysNumSamples = samples[(currentIndex + 1) % samples.length];
    sendChatFeedback("God Rays Samples", String.valueOf(godRaysNumSamples)); 
}

// ============================================================================
// 6. RENDER METHOD - Add to renderPostProcessing() or as separate method
// ============================================================================

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

// ============================================================================
// 7. INTEGRATION - Add call to applyGodRays in applyPostProcessingShaders()
// ============================================================================

// In applyPostProcessingShaders(float tickDelta), add after line ~429:
// if (godRaysEnabled && (!performanceMode || godRaysNumSamples <= 32)) applyGodRays(tickDelta);

// ============================================================================
// 8. ALTERNATIVE: Add to renderSkyEffects() for BEFORE_ENTITIES rendering
// ============================================================================

// In renderSkyEffects(WorldRenderContext context), add after line ~449:
// if (godRaysEnabled && !performanceMode) renderGodRays(matrices, context.tickCounter().getTickDelta(false));

private void renderGodRays(MatrixStack matrices, float tickDelta) {
    if (!godRaysEnabled) return;
    if (performanceMode) return; // Heavy effect, disable in performance mode
    
    MinecraftClient client = MinecraftClient.getInstance();
    if (client.world == null) return;
    
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