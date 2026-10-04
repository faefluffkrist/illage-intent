package com.faefluffkrist.illagerhostilities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class HostilitiesConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("illager-hostilities.json");
    public boolean enabled = true;
    public boolean friendsAndFoes = true;
    public boolean takesAPillage = true;
    public boolean undead = true, creepers = true, spiders = true, silverfish = true;
    public Map<String, Boolean> attackers = defaults();
    public static HostilitiesConfig editable = new HostilitiesConfig();
    public static HostilitiesConfig active = new HostilitiesConfig();
    private static Map<String, Boolean> defaults() {
        Map<String, Boolean> m = new LinkedHashMap<>();
        for (String id : new String[]{"minecraft:pillager", "minecraft:vindicator", "minecraft:evoker", "minecraft:illusioner", "minecraft:vex", "minecraft:witch", "minecraft:ravager", "friendsandfoes:iceologer", "friendsandfoes:illusioner", "takesapillage:archer", "takesapillage:legioner", "takesapillage:skirmisher"}) m.put(id, true);
        return m;
    }
    public static void load() {
        try {
            if (Files.exists(FILE)) {
                HostilitiesConfig c = GSON.fromJson(Files.readString(FILE), HostilitiesConfig.class);
                if (c == null) throw new IOException("Empty configuration");
                if (c.attackers == null) c.attackers = defaults();
                defaults().forEach(c.attackers::putIfAbsent);
                editable = c;
            }
        } catch (Exception e) {
            IllagerHostilities.LOGGER.error("Cannot read {}; using defaults and preserving original file", FILE, e);
            editable = new HostilitiesConfig();
            activate();
            return;
        }
        save(); activate();
    }
    public static HostilitiesConfig copy(HostilitiesConfig source) { return GSON.fromJson(GSON.toJson(source), HostilitiesConfig.class); }
    public static boolean same(HostilitiesConfig a, HostilitiesConfig b) { return GSON.toJsonTree(a).equals(GSON.toJsonTree(b)); }
    public static void activate() { active = GSON.fromJson(GSON.toJson(editable), HostilitiesConfig.class); }
    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Path temp = FILE.resolveSibling(FILE.getFileName()+".tmp");
            Files.writeString(temp, GSON.toJson(editable));
            Files.move(temp, FILE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) { throw new IllegalStateException("Cannot save " + FILE, e); }
    }
}
