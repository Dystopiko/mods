package xyz.memothelemo.dystopia.deathchest.model;

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
import xyz.memothelemo.dystopia.deathchest.interfaces.DcServerPlayer;
import xyz.memothelemo.edenmc.api.model.organization.Member;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static xyz.memothelemo.dystopia.deathchest.DeathChestMod.LOGGER;

/**
 * Persistent world saved data tracking death chest positions and player ownership mappings across server restarts.
 */
public class DCServerData extends SavedData {
    /**
     * Map of block positions to their corresponding death chest data entries.
     */
    private ConcurrentHashMap<@NonNull BlockPos, @NonNull DeathChestDataEntry> chestsPerPosition
        = new ConcurrentHashMap<>();

    /**
     * Map of player Discord IDs to their set of active death chest block positions.
     */
    private ConcurrentHashMap<@NonNull String, @NonNull HashSet<BlockPos>> chestsPerPlayer
        = new ConcurrentHashMap<>();

    /**
     * Registers a new death chest in the server data store.
     *
     * @param player the player creating the death chest
     * @param member the Eden member profile of the player
     * @param position the world position of the death chest
     * @param data the data entry containing inventory and metadata
     * @return {@code true} if successfully registered, {@code false} if a chest already exists at the position
     */
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

    /**
     * Removes a death chest from the server data store upon claim or destruction.
     *
     * @param member the Eden member owning the death chest
     * @param position the position of the death chest
     */
    public void onDeathChestRemoved(@NonNull Member member, @NonNull BlockPos position) {
        LOGGER.debug("[server_data]: chests_per_position -/-> {}", position);
        LOGGER.debug("[server_data]: chests_per_player -/-> {}", member.getDiscordId());

        chestsPerPosition.remove(position);
        getChestsForPlayer(member).remove(position);
        setDirty();
    }

    /**
     * Retrieves the death chest data entry for a specific block position.
     *
     * @param position the position to query
     * @return the {@link DeathChestDataEntry} if present, or {@code null}
     */
    public @Nullable DeathChestDataEntry getDataForChest(@NonNull BlockPos position) {
        return this.chestsPerPosition.get(position);
    }

    /**
     * Retrieves the set of active death chest positions owned by a server player.
     *
     * @param player the server player
     * @return the set of block positions, or {@code null} if no Eden member profile is bound
     */
    public @Nullable HashSet<BlockPos> getChestsForPlayer(@NonNull ServerPlayer player) {
        Member member = ((DcServerPlayer) player).dc$asEdenMember();
        return member != null ? getChestsForPlayer(member) : null;
    }

    /**
     * Retrieves the set of active death chest positions owned by an Eden member.
     *
     * @param member the Eden member
     * @return the non-null set of block positions
     */
    public @NonNull HashSet<BlockPos> getChestsForPlayer(@NonNull Member member) {
        return this.chestsPerPlayer.computeIfAbsent(member.getDiscordId(), $ -> new HashSet<>());
    }

    /**
     * Creates an empty server data instance.
     *
     * @return a new empty {@link DCServerData}
     */
    public static DCServerData empty() {
        return new DCServerData();
    }

    /**
     * Obtains or initializes the persistent death chest data for a Minecraft server.
     *
     * @param server the active server instance
     * @return the loaded or newly created {@link DCServerData}
     */
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
