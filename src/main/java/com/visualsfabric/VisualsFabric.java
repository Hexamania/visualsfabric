package com.visualsfabric;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VisualsFabric implements ModInitializer {
    public static final String MOD_ID = "visualsfabric";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("VisualsFabric initialized - Pure visual enhancements for Fabric 26.3/1.21.4");
    }
}