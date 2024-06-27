package blade.util.fake;

import blade.platform.ServerPlatform;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.stats.Stat;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.EnumSet;

public class FakePlayer extends ServerPlayer {
    public static Field ServerPlayer$spawnInvulnerableTime;
    static {
        try {
            ServerPlayer$spawnInvulnerableTime = ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");
            ServerPlayer$spawnInvulnerableTime.setAccessible(true);
        } catch (NoSuchFieldException ignored) {
        }
    }

    private final ClientboundPlayerInfoUpdatePacket.Entry fakePlayerEntry;
    private final ServerPlatform platform;

    /**
     * When attacked, a player receives a ClientboundSetEntityMotionPacket for themselves and the server sets
     * the server side velocity to zero (or the velocity before the attack). For the shield, it sets the delta movement
     * on the server, without telling the client. If you stun (shield break and attack at the same time, double click)
     * then the velocity of the shield is stored and sent due to the attack.
     */
    public Vec3 serverSideDelta = Vec3.ZERO;
    public boolean forceBothDelta = false;
    public boolean preventMove = false;

    public FakePlayer(ServerPlatform platform, MinecraftServer server, Vec3 pos, float yaw, float pitch, ServerLevel world, GameProfile profile) {
        super(server, world, profile, new ClientInformation("en_us", 2, ChatVisiblity.HIDDEN, true, 0x7F, HumanoidArm.RIGHT, false, false));
        this.platform = platform;
        // Please save yourself the trouble and don't use setPosRaw. The bounding box is wrong with that.
        setPos(pos.x, pos.y, pos.z);
        setRot(yaw, pitch);
        platform.declareFakePlayer(this);
        fakePlayerEntry = new ClientboundPlayerInfoUpdatePacket.Entry(getUUID(), getGameProfile(), false, 0,
                GameType.SURVIVAL, getDisplayName(), null);
        connection = new ServerGamePacketListenerImpl(server, new FakeConnection(this), this, CommonListenerCookie.createInitial(profile, false));
        updateAll();

        // Bukkit.getPluginManager().registerEvents(this, PaperPlatform.PLUGIN);
        world.addNewPlayer(this);
        invulnerableTime = 0;
        try {
            ServerPlayer$spawnInvulnerableTime.set(this, 0);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void tick() {
        super.tick();
        forceBothDelta = true;
        this.doTick();
        forceBothDelta = false;
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        disconnect();
    }

    @Override
    public @NotNull String getScoreboardName() {
        return "BOT:" + getUUID();
    }

    public void update(ServerPlayer player) {
        player.connection.send(platform.buildPlayerInfoPacket(EnumSet.of(Action.ADD_PLAYER, Action.UPDATE_LISTED), fakePlayerEntry));
    }

    public void updateAll() {
        PlayerList playerList = server.getPlayerList();
        for (ServerPlayer player : playerList.getPlayers()) {
            update(player);
        }
    }

    @Override
    public void setDeltaMovement(Vec3 vec3) {
        setDeltaMovement(vec3, true);
    }

    public void setDeltaMovement(Vec3 vec3, boolean serverSide) {
        if (forceBothDelta) {
            serverSideDelta = vec3;
            super.setDeltaMovement(vec3);
            return;
        }
        if (serverSide) {
            serverSideDelta = vec3;
        } else {
            super.setDeltaMovement(vec3);
        }
    }

    @Override
    public @NotNull Vec3 getDeltaMovement() {
        return getDeltaMovement(true);
    }

    public Vec3 getDeltaMovement(boolean serverSide) {
        return serverSide && !forceBothDelta ? serverSideDelta : super.getDeltaMovement();
    }

    /**
     * performance
     */
    @Override
    public void awardStat(Stat<?> stat, int i) {
    }

    @Override
    public @NotNull String getIpAddress() {
        return "127.0.0.1";
    }

    public void sentMotionPacket() {
        setDeltaMovement(serverSideDelta, false);
        setDeltaMovement(Vec3.ZERO, true);
    }

    @Override
    public boolean isControlledByLocalInstance() {
        return true;
    }

    @Override
    public boolean isEffectiveAi() {
        return true;
    }

    @Override
    public void setPos(double d, double e, double f) {
        if (preventMove) {
            return;
        }
        super.setPos(d, e, f);
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState blockState, BlockPos blockPos) {
        doCheckFallDamage(0.0, y, 0.0, onGround);
    }
}