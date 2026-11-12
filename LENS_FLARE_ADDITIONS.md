// ============================================================
// LENS FLARE FEATURE - CODE BLOCKS TO ADD TO VisualsFabricClient.java
// ============================================================

// ------------------------------------------------------------
// 1. STATIC FIELDS (Add after line ~119, before Weather Enhancements section)
// ------------------------------------------------------------

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


// ------------------------------------------------------------
// 2. GETTER METHODS (Add after line ~891, before the closing brace of the class)
// ------------------------------------------------------------

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


// ------------------------------------------------------------
// 3. KEY BINDINGS (Add in registerKeyBindings() after line ~254)
// ------------------------------------------------------------

        toggleLensFlareKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_lens_flare", GLFW.GLFW_KEY_Q, "category.visualsfabric.lighting"));
        toggleLensFlareHaloKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_lens_flare_halo", GLFW.GLFW_KEY_E, "category.visualsfabric.lighting"));
        toggleLensFlareChromaticKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_lens_flare_chromatic", GLFW.GLFW_KEY_T, "category.visualsfabric.lighting"));
        cycleLensFlareGhostsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.cycle_lens_flare_ghosts", GLFW.GLFW_KEY_MINUS, "category.visualsfabric.lighting"));
        toggleLensFlareAnamorphicKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visualsfabric.toggle_lens_flare_anamorphic", GLFW.GLFW_KEY_EQUAL, "category.visualsfabric.lighting"));

// And declare the key binding fields (add after line ~213):
    public static KeyBinding toggleLensFlareKey;
    public static KeyBinding toggleLensFlareHaloKey;
    public static KeyBinding toggleLensFlareChromaticKey;
    public static KeyBinding cycleLensFlareGhostsKey;
    public static KeyBinding toggleLensFlareAnamorphicKey;


// ------------------------------------------------------------
// 4. KEY HANDLING (Add in registerTickEvents() after line ~306)
// ------------------------------------------------------------

            while (toggleLensFlareKey.wasPressed()) { lensFlareEnabled = !lensFlareEnabled; sendChatFeedback("Lens Flare", lensFlareEnabled); }
            while (toggleLensFlareHaloKey.wasPressed()) { lensFlareHaloEnabled = !lensFlareHaloEnabled; sendChatFeedback("Lens Flare Halo", lensFlareHaloEnabled); }
            while (toggleLensFlareChromaticKey.wasPressed()) { lensFlareChromaticAberration = !lensFlareChromaticAberration; sendChatFeedback("Lens Flare Chromatic Aberration", lensFlareChromaticAberration); }
            while (cycleLensFlareGhostsKey.wasPressed()) { lensFlareGhosts = (lensFlareGhosts >= 8) ? 3 : lensFlareGhosts + 1; sendChatFeedback("Lens Flare Ghosts", String.valueOf(lensFlareGhosts)); }
            while (toggleLensFlareAnamorphicKey.wasPressed()) { lensFlareAnamorphic = !lensFlareAnamorphic; sendChatFeedback("Lens Flare Anamorphic", lensFlareAnamorphic); }


// ------------------------------------------------------------
// 5. RENDER METHOD - POST PROCESSING (Add in applyPostProcessingShaders() after line ~429)
// ------------------------------------------------------------

        if (lensFlareEnabled && (!performanceMode || !lowQualityShaders)) applyLensFlareEffect(tickDelta);


// ------------------------------------------------------------
// 6. LENS FLARE RENDER METHOD (Add after applyColorGrading() around line ~440)
// ------------------------------------------------------------

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
        MatrixStack matrices = new MatrixStack();
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
        
        // Get view matrix and project
        MatrixStack matrices = new MatrixStack();
        matrices.multiply(client.gameRenderer.getCamera().getViewMatrix(tickDelta));
        
        // Sun direction vector
        Vec3d sunDir = new Vec3d(sunAzimuth, sunHeight, 0).normalize();
        
        // Project to screen (simplified - using viewport)
        int width = client.getWindow().getFramebufferWidth();
        int height = client.getWindow().getFramebufferHeight();
        
        // Convert to NDC then to screen
        double fov = Math.toRadians(client.options.getFov().getValue());
        double aspect = (double)width / height;
        
        float ndcX = (float)(sunDir.x / (sunDir.z * Math.tan(fov / 2) * aspect));
        float ndcY = (float)(sunDir.y / (sunDir.z * Math.tan(fov / 2)));
        
        int screenX = (int)((ndcX + 1) * width / 2);
        int screenY = (int)((1 - ndcY) * height / 2);
        
        return new Vec3d(screenX, screenY, 0);
    }
    
    private boolean hasBrightLightSources(MinecraftClient client) {
        // Check for other bright light sources (beacons, end crystals, etc.)
        if (client.world == null || client.player == null) return false;
        
        // Simple check for nearby bright block entities
        return client.world.getBlockEntities().stream()
            .anyMatch(be -> be instanceof net.minecraft.block.entity.BeaconBlockEntity 
                || be instanceof net.minecraft.block.entity.EndGatewayBlockEntity
                || (be instanceof net.minecraft.block.entity.BlockEntity && be.getPos().isWithinDistance(client.player.getBlockPos(), 20)));
    }


// ------------------------------------------------------------
// 7. ALTERNATIVE: SIMPLER LENS FLARE FOR OVERLAY RENDERING
// Add in renderOverlayEffects() after line ~486 if you prefer overlay rendering
// ------------------------------------------------------------

        // Alternative: Render lens flare as overlay (for HUD-space rendering)
        if (lensFlareEnabled && (!performanceMode || !lowQualityShaders)) {
            renderLensFlareOverlay(matrices, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight(), context.tickCounter().getTickDelta(false));
        }

// Add this method:
    private void renderLensFlareOverlay(MatrixStack matrices, int width, int height, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;
        
        float sunAngle = client.world.getSkyAngle(tickDelta) * ((float)Math.PI * 2) - (float)Math.PI / 2;
        float sunHeight = (float)Math.sin(sunAngle);
        
        if (sunHeight < 0.1f) return;
        
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        
        float rotation = effectTime * lensFlareRotationSpeed;
        float baseAlpha = lensFlareIntensity * 0.4f;
        
        // Sun position in overlay space (scaled)
        int scaledWidth = client.getWindow().getScaledWidth();
        int scaledHeight = client.getWindow().getScaledHeight();
        
        double fov = Math.toRadians(client.options.getFov().getValue());
        double aspect = (double)scaledWidth / scaledHeight;
        
        float sunAzimuth = (float)Math.cos(sunAngle);
        float ndcX = (float)(sunAzimuth / (Math.tan(fov / 2) * aspect));
        float ndcY = (float)(sunHeight / (Math.tan(fov / 2)));
        
        int centerX = (int)((ndcX + 1) * scaledWidth / 2);
        int centerY = (int)((1 - ndcY) * scaledHeight / 2);
        
        // Only render if on screen
        if (centerX < -50 || centerX > scaledWidth + 50 || centerY < -50 || centerY > scaledHeight + 50) {
            RenderSystem.disableBlend();
            return;
        }
        
        // Vector from screen center to sun
        float dx = centerX - scaledWidth / 2f;
        float dy = centerY - scaledHeight / 2f;
        
        Tessellator tessellator = Tessellator.getInstance();
        
        // Render ghosts
        int ghostCount = Math.min(lensFlareGhosts, lensFlareGhostCountMax);
        
        for (int i = 0; i < ghostCount; i++) {
            float progress = (i + 1) / (float)(ghostCount + 1);
            float ghostX = scaledWidth / 2f + dx * progress;
            float ghostY = scaledHeight / 2f + dy * progress;
            float size = 20f * (1f - progress * 0.4f) * lensFlareIntensity;
            float alpha = baseAlpha * (1f - progress) * (0.6f + 0.4f * (float)Math.sin(effectTime * 2.5 + i));
            
            RenderSystem.setShaderColor(1f, 0.9f, 0.7f, alpha);
            
            BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
            float hs = size / 2;
            buffer.vertex(ghostX - hs, ghostY + hs, 0);
            buffer.vertex(ghostX + hs, ghostY + hs, 0);
            buffer.vertex(ghostX + hs, ghostY - hs, 0);
            buffer.vertex(ghostX - hs, ghostY - hs, 0);
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        }
        
        // Halo
        if (lensFlareHaloEnabled) {
            float haloSize = 60f * lensFlareIntensity * (1f + 0.2f * (float)Math.sin(effectTime * 1.8));
            float haloAlpha = lensFlareHaloIntensity * baseAlpha * 0.8f;
            
            RenderSystem.setShaderColor(1f, 0.85f, 0.6f, haloAlpha);
            BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
            float hs = haloSize / 2;
            buffer.vertex(centerX - hs, centerY + hs, 0);
            buffer.vertex(centerX + hs, centerY + hs, 0);
            buffer.vertex(centerX + hs, centerY - hs, 0);
            buffer.vertex(centerX - hs, centerY - hs, 0);
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        }
        
        // Core
        float coreSize = 12f * lensFlareIntensity;
        RenderSystem.setShaderColor(1f, 1f, 0.95f, baseAlpha * 1.2f);
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        float hs = coreSize / 2;
        buffer.vertex(centerX - hs, centerY + hs, 0);
        buffer.vertex(centerX + hs, centerY + hs, 0);
        buffer.vertex(centerX + hs, centerY - hs, 0);
        buffer.vertex(centerX - hs, centerY - hs, 0);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }


// ------------------------------------------------------------
// 8. PERFORMANCE MODE CHECK (Already handled in applyPostProcessingShaders)
// The lens flare is disabled when performanceMode && lowQualityShaders
// ------------------------------------------------------------

// In applyPostProcessingShaders(), the check:
// if (lensFlareEnabled && (!performanceMode || !lowQualityShaders)) applyLensFlareEffect(tickDelta);
// Already handles performance mode correctly.


// ------------------------------------------------------------
// 9. EFFECT TIME USAGE
// The effectTime variable (line 170) is already updated in registerTickEvents()
// and is used in the lens flare for:
// - Ghost pulsing: effectTime * 3 + i
// - Halo breathing: effectTime * 2, effectTime * 1.5
// - Rotation: effectTime * lensFlareRotationSpeed
// - Anamorphic streak rotation: rotation (based on effectTime)
// ------------------------------------------------------------