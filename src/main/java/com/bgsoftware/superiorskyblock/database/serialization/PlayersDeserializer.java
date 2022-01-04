package com.bgsoftware.superiorskyblock.database.serialization;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.database.DatabaseResult;
import com.bgsoftware.superiorskyblock.database.cache.CachedPlayerInfo;
import com.bgsoftware.superiorskyblock.database.cache.DatabaseCache;
import com.bgsoftware.superiorskyblock.lang.PlayerLocales;

import java.util.Optional;
import java.util.UUID;

public final class PlayersDeserializer {

    static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private PlayersDeserializer() {

    }

    public static void deserializeMissions(DatabaseBridge databaseBridge, DatabaseCache<CachedPlayerInfo> databaseCache) {
        databaseBridge.loadAllObjects("players_missions", missionsRow -> {
            DatabaseResult missions = new DatabaseResult(missionsRow);

            Optional<String> player = missions.getString("player");

            if (!player.isPresent()) {
                SuperiorSkyblockPlugin.log("&cCannot load player mission of null player, skipping...");
                return;
            }

            UUID uuid = UUID.fromString(player.get());
            CachedPlayerInfo cachedPlayerInfo = databaseCache.addCachedInfo(uuid, new CachedPlayerInfo());

            Optional<String> name = missions.getString("name");

            if (!name.isPresent()) {
                SuperiorSkyblockPlugin.log("&cCannot load player mission of null mission, skipping...");
                return;
            }

            Optional<Integer> finishCount = missions.getInt("finish_count");

            if (!finishCount.isPresent()) {
                SuperiorSkyblockPlugin.log("&cCannot load player mission of invalid finish count, skipping...");
                return;
            }

            Mission<?> mission = plugin.getMissions().getMission(name.get());

            if (mission != null)
                cachedPlayerInfo.completedMissions.put(mission, finishCount.get());
        });
    }

    public static void deserializePlayerSettings(DatabaseBridge databaseBridge, DatabaseCache<CachedPlayerInfo> databaseCache) {
        databaseBridge.loadAllObjects("players_settings", playerSettingsRow -> {
            DatabaseResult playerSettings = new DatabaseResult(playerSettingsRow);

            Optional<String> player = playerSettings.getString("player");

            if (!player.isPresent()) {
                SuperiorSkyblockPlugin.log("&cCannot load player settings of null player, skipping...");
                return;
            }

            UUID uuid = UUID.fromString(player.get());
            CachedPlayerInfo cachedPlayerInfo = databaseCache.addCachedInfo(uuid, new CachedPlayerInfo());

            playerSettings.getBoolean("toggled_panel").ifPresent(toggledPanel ->
                    cachedPlayerInfo.toggledPanel = toggledPanel);
            playerSettings.getBoolean("island_fly").ifPresent(islandFly ->
                    cachedPlayerInfo.islandFly = islandFly);
            playerSettings.getString("border_color").ifPresent(borderColor ->
                    BorderColor.safeValue(borderColor, BorderColor.BLUE));
            playerSettings.getString("language").ifPresent(userLocale ->
                    cachedPlayerInfo.userLocale = PlayerLocales.getLocale(userLocale));
            playerSettings.getBoolean("toggled_border").ifPresent(worldBorderEnabled ->
                    cachedPlayerInfo.worldBorderEnabled = worldBorderEnabled);
        });
    }

}
