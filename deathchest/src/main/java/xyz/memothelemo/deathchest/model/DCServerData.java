package xyz.memothelemo.deathchest.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import xyz.memothelemo.deathchest.interfaces.DcServerPlayer;
import xyz.memothelemo.edenmc.api.model.organization.Member;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static xyz.memothelemo.deathchest.DeathChestMod.LOGGER;

// TODO: Find very efficient ways to store death chest data
public class DCServerData extends SavedData {
    private ConcurrentHashMap<@NonNull BlockPos, @NonNull DeathChestDataEntry> chestsPerPosition
        = new ConcurrentHashMap<>();

    // key: player's discord snowflake
    private ConcurrentHashMap<@NonNull String, @NonNull HashSet<BlockPos>> chestsPerPlayer
        = new ConcurrentHashMap<>();

    public boolean onNewDeathChest(
        @NonNull ServerPlayer player,
        @NonNull Member member,
        @NonNull BlockPos position,
        @NonNull DeathChestDataEntry data
    ) {
        if (chestsPerPosition.containsKey(position)) {
            return false;
        }

        LOGGER.debug("[server_data]: chests_per_position <- {}", position);
        LOGGER.debug("[server_data]: chests_per_player <- {}", member.getDiscordId());

        chestsPerPosition.put(position, data);
        getChestsForPlayer(member).add(position);
        setDirty();
        return true;
    }

    public void onDeathChestRemoved(@NonNull Member member, @NonNull BlockPos position) {
        LOGGER.debug("[server_data]: chests_per_position -/-> {}", position);
        LOGGER.debug("[server_data]: chests_per_player -/-> {}", member.getDiscordId());

        chestsPerPosition.remove(position);
        getChestsForPlayer(member).remove(position);
        setDirty();
    }

    public @Nullable DeathChestDataEntry getDataForChest(@NonNull BlockPos position) {
        return this.chestsPerPosition.get(position);
    }

    public @Nullable HashSet<BlockPos> getChestsForPlayer(@NonNull ServerPlayer player) {
        Member member = ((DcServerPlayer) player).dc$asEdenMember();
        return member != null ? getChestsForPlayer(member) : null;
    }

    public @NonNull HashSet<BlockPos> getChestsForPlayer(@NonNull Member member) {
        return this.chestsPerPlayer.computeIfAbsent(member.getDiscordId(), $ -> new HashSet<>());
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    public static DCServerData empty() {
        return new DCServerData();
    }

    public static DCServerData fromServer(MinecraftServer server) {
        ServerLevel level = server.getLevel(Level.OVERWORLD);
        if (level == null) {
            LOGGER.warn("Cannot find overworld level! Any changes to the server data will not be saved!");
            return DCServerData.empty();
        }

        DCServerData data = level.getDataStorage().computeIfAbsent(TYPE);
        data.setDirty();
        return data;
    }

    private static final Codec<BlockPos> BLOCK_POS_KEY_CODEC = Codec.STRING.xmap(
        str -> {
            // Decode: "x,y,z" → BlockPos
            String[] parts = str.split(",", 3);
            return new BlockPos(
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2])
            );
        },
        pos -> pos.getX() + "," + pos.getY() + "," + pos.getZ()
    );

    private static final Codec<HashSet<BlockPos>> BLOCK_POS_SET_CODEC
        = BlockPos.CODEC.listOf().xmap(HashSet::new, ArrayList::new);

    private static final Codec<DCServerData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.unboundedMap(BLOCK_POS_KEY_CODEC, DeathChestDataEntry.CODEC)
                .fieldOf("chests_per_position")
                .forGetter((v) -> v.chestsPerPosition),
            Codec.unboundedMap(ExtraCodecs.NON_EMPTY_STRING, BLOCK_POS_SET_CODEC)
                .fieldOf("chests_per_player")
                .forGetter((v) -> v.chestsPerPlayer)
        ).apply(instance, (arg0, arg1) -> {
            DCServerData data = new DCServerData();
            data.chestsPerPosition = new ConcurrentHashMap<>(arg0);
            data.chestsPerPlayer = new ConcurrentHashMap<>(arg1);

            LOGGER.debug("Loaded {} chests_per_position entries", data.chestsPerPosition.size());
            LOGGER.debug("Loaded {} chests_per_player entries", data.chestsPerPlayer.size());
            return data;
        })
    );

    private static final SavedDataType<DCServerData> TYPE = new SavedDataType<>(
        Objects.requireNonNull(Identifier.tryBuild("death_chests", "server_data")),
        DCServerData::empty,
        CODEC,
        (DataFixTypes) null
    );

    private DCServerData() {}
}
