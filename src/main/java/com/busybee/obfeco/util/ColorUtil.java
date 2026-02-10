package com.busybee.obfeco.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ColorUtil {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();
    
    public static Component colorize(String text) {
        return MINI_MESSAGE.deserialize(text);
    }
    
    public static String colorizeToLegacy(String text) {
        Component component = MINI_MESSAGE.deserialize(text);
        return LEGACY_SERIALIZER.serialize(component);
    }
    
    public static String strip(String text) {
        return MiniMessage.miniMessage().stripTags(text);
    }
}