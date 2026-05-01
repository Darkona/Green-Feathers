package com.darkona.feathers.api.registry;

import net.minecraft.resources.ResourceLocation;

public final class FeathersIds {

    public static final String MOD_ID = "greenfeathers";

    private FeathersIds() {}

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
