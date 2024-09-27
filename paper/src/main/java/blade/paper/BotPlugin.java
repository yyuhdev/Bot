package blade.paper;

import blade.BladeMachine;
import blade.Bot;
import blade.bot.IServerBot;
import blade.bot.ServerBot;
import blade.bot.ServerBotSettings;
import blade.debug.BladeDebug;
import blade.impl.goal.KillTargetGoal;
import blade.util.fake.FakePlayer;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.*;
import dev.jorel.commandapi.executors.CommandArguments;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.bstats.bukkit.Metrics;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.xenondevs.invui.item.builder.ItemBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class BotPlugin extends JavaPlugin {
    public static final TextColor PRIMARY = TextColor.color(0xCDD6FA);
    public static PaperPlatform platform;

    @Override
    public void onLoad() {
        super.onLoad();
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this));
    }

    @Override
    public void onEnable() {
        CommandAPI.onEnable();
        new Metrics(this, 22946);

        platform = new PaperPlatform();
        platform.register(this);

        new CommandTree("bot")
                .withPermission("bot.use")
                .then(new LiteralArgument("totem")
                        .withPermission("bot.totem")
                        .executesPlayer((sender, args) -> {
                            Location pos = sender.getLocation();
                            List<Bot> bots = new ArrayList<>(PaperPlatform.BOTS);
                            for (Bot bot : bots) {
                                if (bot instanceof IServerBot sBot && sBot.getSpawner().getUUID().equals(sender.getUniqueId())) {
                                    bot.destroy();
                                    sender.sendMessage(Component.text("Removed your simple bot!", PRIMARY));
                                    return;
                                }
                            }

                            FakePlayer fakePlayer = new FakePlayer(platform, MinecraftServer.getServer(), CraftLocation.toVec3D(pos), pos.getYaw(), pos.getPitch(), ((CraftWorld) pos.getWorld()).getHandle(), IServerBot.getProfile());
                            ServerBot bot = new ServerBot(fakePlayer, platform, ((CraftPlayer) sender).getHandle(), ServerBotSettings.TOTEM.clone());
                            platform.addBot(bot);
                            sender.sendMessage(Component.text("Spawned a totem bot!", PRIMARY));
                        }))
                .then(new LiteralArgument("shield")
                        .withPermission("bot.shield")
                        .executesPlayer((sender, args) -> {
                            Location pos = sender.getLocation();
                            List<Bot> bots = new ArrayList<>(PaperPlatform.BOTS);
                            for (Bot bot : bots) {
                                if (bot instanceof IServerBot sBot && sBot.getSpawner().getUUID().equals(sender.getUniqueId())) {
                                    bot.destroy();
                                    sender.sendMessage(Component.text("Removed your simple bot!", PRIMARY));
                                    return;
                                }
                            }

                            FakePlayer fakePlayer = new FakePlayer(platform, MinecraftServer.getServer(), CraftLocation.toVec3D(pos), pos.getYaw(), pos.getPitch(), ((CraftWorld) pos.getWorld()).getHandle(), IServerBot.getProfile());
                            ServerBot bot = new ServerBot(fakePlayer, platform, ((CraftPlayer) sender).getHandle(), ServerBotSettings.SHIELD.clone());
                            platform.addBot(bot);
                            sender.sendMessage(Component.text("Spawned a shield bot!", PRIMARY));
                        }))
                .executesPlayer((sender, args) -> {
                    for (Bot b : PaperPlatform.BOTS) {
                        if (b instanceof IServerBot bot && bot.getSpawner().getUUID().equals(sender.getUniqueId())) {
                            BotSettingGui.show(((CraftPlayer) sender).getHandle(), platform, bot.getSettings(), bot);
                            return;
                        }
                    }

                    BotSettingGui.show(((CraftPlayer) sender).getHandle(), platform, new ServerBotSettings(), null);
                })
                .register();
    }

    @Override
    public void onDisable() {
        CommandAPI.onDisable();
        platform.removeAll();
    }

    public Argument<?> createPossibleBots(Argument<?> tree, Function<CommandArguments, ServerPlayer> playerGetter) {
        Function<CommandArguments, Bot> botGetter = args -> {
            Bot bot = new Bot(playerGetter.apply(args), platform);
            platform.addBot(bot);
            return bot;
        };
        BiFunction<CommandSender, CommandArguments, Bot> kill = (sender, args) -> {
            Entity target = (Entity) args.get("target");
            if (!(target instanceof LivingEntity)) return null;
            Bot bot = botGetter.apply(args);
            BladeMachine blade = bot.getBlade();
            blade.setGoal(new KillTargetGoal(() -> {
                Entity entity = (Entity) args.get("target");
                if (entity instanceof LivingEntity) return ((CraftLivingEntity) entity).getHandle();
                bot.destroy();
                return null;
            }));
            return bot;
        };

        return tree
                .then(new LiteralArgument("kill")
                        .then(new EntitySelectorArgument.OneEntity("target")
                                .executes((sender, args) -> {
                                    Bot bot = kill.apply(sender, args);
                                    if (bot == null) return;
                                    sender.sendMessage(Component.text("Spawned a killing bot."));
                                })))
                .then(new LiteralArgument("cart")
                        .then(new EntitySelectorArgument.OneEntity("target")
                                .executes((sender, args) -> {
                                    Bot bot = kill.apply(sender, args);
                                    if (bot == null) return;

                                    PlayerInventory inv = bot.getVanillaPlayer().getBukkitEntity().getInventory();
                                    inv.addItem(new ItemBuilder(Material.BOW)
                                            .addEnchantment(Enchantment.FLAME, 1, true).get());
                                    for (int i = 0; i < 5; i++) {
                                        inv.addItem(new ItemStack(Material.TNT_MINECART));
                                    }
                                    inv.addItem(new ItemStack(Material.RAIL, 64));
                                    inv.addItem(new ItemStack(Material.ARROW, 64));
                                    sender.sendMessage(Component.text("Spawned a cart bot."));
                                })))
                .then(new LiteralArgument("crystal")
                        .then(new EntitySelectorArgument.OneEntity("target")
                                .executes((sender, args) -> {
                                    Bot bot = kill.apply(sender, args);
                                    if (bot == null) return;

                                    PlayerInventory inv = bot.getVanillaPlayer().getBukkitEntity().getInventory();
                                    inv.addItem(new ItemBuilder(Material.NETHERITE_SWORD)
                                            .addEnchantment(Enchantment.KNOCKBACK, 1, true).get());
                                    inv.addItem(new ItemStack(Material.END_CRYSTAL, 64));
                                    inv.addItem(new ItemStack(Material.OBSIDIAN, 64));
                                    inv.addItem(new ItemStack(Material.ENDER_PEARL, 16));
                                    inv.setHelmet(new ItemBuilder(Material.NETHERITE_HELMET)
                                            .addEnchantment(Enchantment.PROTECTION, 4, true).get());
                                    inv.setChestplate(new ItemBuilder(Material.NETHERITE_CHESTPLATE)
                                            .addEnchantment(Enchantment.PROTECTION, 4, true).get());
                                    inv.setLeggings(new ItemBuilder(Material.NETHERITE_LEGGINGS)
                                            .addEnchantment(Enchantment.BLAST_PROTECTION, 4, true).get());
                                    inv.setBoots(new ItemBuilder(Material.NETHERITE_BOOTS)
                                            .addEnchantment(Enchantment.PROTECTION, 4, true)
                                            .addEnchantment(Enchantment.FEATHER_FALLING, 4, true).get());
                                    for (int i = 0; i < 12; i++) {
                                        inv.addItem(new ItemStack(Material.TOTEM_OF_UNDYING));
                                    }
                                    inv.setItemInOffHand(new ItemStack(Material.TOTEM_OF_UNDYING));
                                    sender.sendMessage(Component.text("Spawned a crystal bot."));
                                })))
                .then(new LiteralArgument("sword")
                        .then(new EntitySelectorArgument.OneEntity("target")
                                .executes((sender, args) -> {
                                    Bot bot = kill.apply(sender, args);
                                    if (bot == null) return;

                                    PlayerInventory inv = bot.getVanillaPlayer().getBukkitEntity().getInventory();
                                    inv.addItem(new ItemBuilder(Material.DIAMOND_SWORD)
                                            .addEnchantment(Enchantment.SHARPNESS, 5, true).get());
                                    inv.setHelmet(new ItemBuilder(Material.DIAMOND_HELMET)
                                            .addEnchantment(Enchantment.PROTECTION, 4, true).get());
                                    inv.setChestplate(new ItemBuilder(Material.DIAMOND_CHESTPLATE)
                                            .addEnchantment(Enchantment.PROTECTION, 4, true).get());
                                    inv.setLeggings(new ItemBuilder(Material.DIAMOND_LEGGINGS)
                                            .addEnchantment(Enchantment.PROTECTION, 4, true).get());
                                    inv.setBoots(new ItemBuilder(Material.DIAMOND_BOOTS)
                                            .addEnchantment(Enchantment.PROTECTION, 4, true)
                                            .addEnchantment(Enchantment.FEATHER_FALLING, 4, true).get());
                                    inv.setItemInOffHand(new ItemStack(Material.GOLDEN_APPLE, 64));
                                    sender.sendMessage(Component.text("Spawned a sword bot."));
                                })))
                .then(new LiteralArgument("test")
                        .then(new LiteralArgument("move_forward")
                                .executes((sender, args) -> {
                                    Bot bot = botGetter.apply(args);
                                    bot.setMoveForward(true);
                                }))
                        .then(new LiteralArgument("move_right")
                                .executes((sender, args) -> {
                                    Bot bot = botGetter.apply(args);
                                    bot.setMoveRight(true);
                                }))
                        .then(new LiteralArgument("attack")
                                .executes((sender, args) -> {
                                    Bot bot = botGetter.apply(args);
                                    bot.attack();
                                }))
                        .then(new LiteralArgument("bow")
                                .executes((sender, args) -> {
                                    Bot bot = botGetter.apply(args);
                                    PlayerInventory inv = bot.getVanillaPlayer().getBukkitEntity().getInventory();
                                    inv.addItem(new ItemStack(Material.BOW));
                                    inv.addItem(new ItemStack(Material.ARROW, 64));
                                    platform.addBot(bot);
                                    bot.interact(true);
                                    bot.getVanillaPlayer().getBukkitEntity().getScheduler().runDelayed(BotPlugin.this, task -> {
                                        bot.interact(false);
                                    }, null, 40L);
                                })));
    }
}
