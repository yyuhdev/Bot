package blade.paper;

import blade.BuildConstants;
import blade.bot.IServerBot;
import blade.bot.KitBot;
import blade.bot.ServerBot;
import blade.bot.ServerBotSettings;
import blade.util.ItemUtil;
import blade.util.fake.FakePlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.ItemWrapper;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.CycleItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

import java.util.Arrays;
import java.util.function.Consumer;

import static blade.paper.MenuUtils.empty;
import static blade.paper.MenuUtils.wrap;

public class BotSettingGui {
    public static void show(ServerPlayer player, PaperPlatform platform, ServerBotSettings settings, @Nullable IServerBot bot) {
        Runnable show = () -> show(player, platform, settings, bot);
        Gui gui = Gui.empty(9, 6);
        gui.setBackground(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .setDisplayName(empty()));
        addEquipmentItems(gui, show, 1 , 1, EquipmentSlot.HEAD, settings);
        addEquipmentItems(gui, show, 2, 1, EquipmentSlot.CHEST, settings);
        addEquipmentItems(gui, show, 3, 1, EquipmentSlot.LEGS, settings);
        addEquipmentItems(gui, show, 4, 1, EquipmentSlot.FEET, settings);

        if (settings.shield) {
            gui.setItem(4, 4, new SimpleItem(new ItemBuilder(Material.SHIELD)
                    .addEnchantment(Enchantment.MENDING, 1, true)
                    .addItemFlags(ItemFlag.HIDE_ENCHANTS)
                    .setDisplayName(wrap("<#cdd6fa>Shield"))
                    .addLoreLines(empty())
                    .addLoreLines(wrap("<grey>Should your bot use it's shield?"))
                    .addLoreLines(empty())
                    .addLoreLines(wrap("<#cdd6fa>Enabled: <green><bold>ENABLED"))
                    .addLoreLines(empty())
                    .addLoreLines(wrap("<#cdd6fa>\uD83D\uDF8D\u25cf Click to disable!")), click -> {
                settings.shield = false;
                show.run();
            }));
        } else {
            gui.setItem(4, 4, new SimpleItem(new ItemBuilder(Material.SHIELD)
                    .addEnchantment(Enchantment.MENDING, 1, true)
                    .addItemFlags(ItemFlag.HIDE_ENCHANTS)
                    .setDisplayName(wrap("<#cdd6fa>Shield"))
                    .addLoreLines(empty())
                    .addLoreLines(wrap("<grey>Should your bot use it's shield?"))
                    .addLoreLines(empty())
                    .addLoreLines(wrap("<#cdd6fa>Enabled: <red><bold>DISABLED"))
                    .addLoreLines(empty())
                    .addLoreLines(wrap("<#cdd6fa>\uD83D\uDF8D\u25cf Click to enable!")), click -> {
                settings.shield = true;
                show.run();
            }));
        }

        setBoolItem(gui, show, 5, 4, settings.effects.contains(MobEffects.SLOW_FALLING),
                new ItemBuilder(Material.FEATHER)
                        .addEnchantment(Enchantment.MENDING, 1, true)
                        .addItemFlags(ItemFlag.HIDE_ENCHANTS)
                        .setDisplayName(wrap("<#cdd6fa>Feather Falling"))
                        .addLoreLines(empty())
                        .addLoreLines(wrap("<grey>Make your bot fall"))
                        .addLoreLines(wrap("<grey>slower by enabling this"))
                        .addLoreLines(empty())
                        .addLoreLines(wrap("<#cdd6fa>Enabled: <green><bold>ENABLED"))
                        .addLoreLines(empty())
                        .addLoreLines(wrap("<#cdd6fa>\uD83D\uDF8D\u25cf Click to toggle!"))
                        .get(),
                new ItemBuilder(Material.FEATHER)
                        .setDisplayName(wrap("<#cdd6fa>Feather Falling"))
                        .addLoreLines(empty())
                        .addLoreLines(wrap("<grey>Make your bot fall"))
                        .addLoreLines(wrap("<grey>slower by enabling this"))
                        .addLoreLines(empty())
                        .addLoreLines(wrap("<#cdd6fa>Enabled: <red><bold>DISABLED"))
                        .addLoreLines(empty())
                        .addLoreLines(wrap("<#cdd6fa>\uD83D\uDF8D\u25cf Click to toggle!"))
                        .get(),
                v -> {
                    if (v) settings.effects.add(MobEffects.SLOW_FALLING);
                    else settings.effects.remove(MobEffects.SLOW_FALLING);
                });
        if (bot == null) {
            gui.setItem(7, 2, new SimpleItem(new ItemBuilder(Material.BREEZE_SPAWN_EGG)
                    .setDisplayName(wrap("<#cdd6fa>Spawn Bot"))
                    .addLoreLines(empty())
                    .addLoreLines(wrap("<grey>Spawn a pvp bot to"))
                    .addLoreLines(wrap("<grey>practice D-Tapping and such"))
                    .addLoreLines(empty())
                    .addLoreLines(wrap("<#cdd6fa>\uD83D\uDF8D\u25cf Click to spawn!")), click -> {
                Location pos = click.getPlayer().getLocation();
                FakePlayer fakePlayer = new FakePlayer(platform, MinecraftServer.getServer(), CraftLocation.toVec3D(pos), pos.getYaw(), pos.getPitch(), ((CraftWorld) pos.getWorld()).getHandle(), IServerBot.getProfile());
                ServerBot spawningBot = new ServerBot(fakePlayer, platform, player, settings);
                platform.addBot(spawningBot);
                click.getPlayer().closeInventory();
            }));
        } else {
            gui.setItem(7, 1, new SimpleItem(new ItemBuilder(Material.ENDER_PEARL)
                    .setDisplayName(wrap("<#cdd6fa>Teleport to you"))
                    .addLoreLines(empty())
                    .addLoreLines(wrap("<gray>Teleport your bot back to you!"))
                    .addLoreLines(empty())
                    .addLoreLines(wrap("<#cdd6fa>\uD83D\uDF8D\u25cf Click to teleport")), click -> {
                Vec3 pos = player.position();
                bot.getVanillaPlayer().teleportTo(player.serverLevel(), pos.x, pos.y, pos.z, player.getYRot(), player.getXRot());
                click.getPlayer().closeInventory();
            }));
            gui.setItem(7, 2, new SimpleItem(new ItemBuilder(Material.TNT)
                    .setDisplayName(wrap("<#cdd6fa>Despawn"))
                    .addLoreLines(empty())
                    .addLoreLines(wrap("<gray>Despawn your current bot"))
                    .addLoreLines(empty())
                    .addLoreLines(wrap("<#cdd6fa>\uD83D\uDF8D\u25cf Click to despawn")), click -> {
                bot.destroy();
                click.getPlayer().closeInventory();
            }));
        }
        Window.single()
                .setGui(gui)
                .setTitle(wrap("Train"))
                .build(player.getBukkitEntity()).open();
    }

    private static void addEquipmentItems(Gui gui, Runnable show, int x, int y, EquipmentSlot slot, ServerBotSettings settings) {
        ServerBotSettings.ArmorPiece armor = settings.armor.get(slot);
        setBoolItem(gui, show, x, y + 1, armor.blastProtection(),
                new ItemBuilder(Material.LIME_DYE)
                        .addEnchantment(Enchantment.MENDING,1, true)
                        .addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
                        .setDisplayName(wrap("<#cdd6fa>Blast Protection"))
                        .addLoreLines(empty())
                        .addLoreLines(wrap("<gray>Toggle the Blast Protection"))
                        .addLoreLines(wrap(String.format("<gray>Enchant for the %s", ItemUtil.getSlotName(slot))))
                        .addLoreLines(empty())
                        .addLoreLines(wrap("<#cdd6fa>Enabled: <green><bold>ENABLED"))
                        .addLoreLines(empty())
                        .addLoreLines(wrap("<#cdd6fa>\uD83D\uDF8D\u25cf Click to toggle"))
                        .get(),
                new ItemBuilder(Material.RED_DYE)
                        .setDisplayName(wrap("<#cdd6fa>Blast Protection"))
                        .addLoreLines(empty())
                        .addLoreLines(wrap("<gray>Toggle the Blast Protection"))
                        .addLoreLines(wrap(String.format("<gray>Enchant for the %s", ItemUtil.getSlotName(slot))))
                        .addLoreLines(empty())
                        .addLoreLines(wrap("<#cdd6fa>Enabled: <red><bold>DISABLED"))
                        .addLoreLines(empty())
                        .addLoreLines(wrap("<#cdd6fa>\uD83D\uDF8D\u25cf Click to toggle"))
                        .get(),
                v -> settings.armor.put(slot, armor.withBlastProtection(v)));
        setCycleItem(gui, show, x, y, armor.type(), new ServerBotSettings.ArmorType[] { ServerBotSettings.ArmorType.NETHERITE, ServerBotSettings.ArmorType.DIAMOND, ServerBotSettings.ArmorType.IRON, ServerBotSettings.ArmorType.GOLD, ServerBotSettings.ArmorType.CHAIN },
                new ItemStack[] {
                        new ItemBuilder(CraftItemStack.asNewCraftStack(armor.type().slotToItem.get(slot)))
                                .addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ITEM_SPECIFICS)
                                .addEnchantment(Enchantment.MENDING, 1, true)
                                .addItemFlags(ItemFlag.HIDE_ENCHANTS)
                                .setDisplayName(wrap("<#cdd6fa>Armor Type"))
                                .addLoreLines(empty())
                                .addLoreLines(wrap("<gray>Click to change the armor"))
                                .addLoreLines(wrap(String.format("<gray>type for the %s", ItemUtil.getSlotName(slot))))
                                .addLoreLines(empty())
                                .addLoreLines(wrap("<#cdd6fa>Enabled: <#cdd6fa><bold>NETHERITE"))
                                .addLoreLines(empty())
                                .addLoreLines(wrap("<#cdd6fa>\uD83D\uDF8D\u25cf Click to toggle"))
                                .get(),
                        new ItemBuilder(CraftItemStack.asNewCraftStack(armor.type().slotToItem.get(slot)))
                                .addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ITEM_SPECIFICS)
                                .addEnchantment(Enchantment.MENDING, 1, true)
                                .addItemFlags(ItemFlag.HIDE_ENCHANTS)
                                .setDisplayName(wrap("<#cdd6fa>Armor Type"))
                                .addLoreLines(empty())
                                .addLoreLines(wrap("<gray>Click to change the armor"))
                                .addLoreLines(wrap(String.format("<gray>type for the %s", ItemUtil.getSlotName(slot))))
                                .addLoreLines(empty())
                                .addLoreLines(wrap("<#cdd6fa>Enabled: <aqua><bold>DIAMOND"))
                                .addLoreLines(empty())
                                .addLoreLines(wrap("<#cdd6fa>\uD83D\uDF8D\u25cf Click to toggle"))
                                .get(),
                        new ItemBuilder(CraftItemStack.asNewCraftStack(armor.type().slotToItem.get(slot)))
                                .addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ITEM_SPECIFICS)
                                .addEnchantment(Enchantment.MENDING, 1, true)
                                .addItemFlags(ItemFlag.HIDE_ENCHANTS)
                                .setDisplayName(wrap("<#cdd6fa>Armor Type"))
                                .addLoreLines(empty())
                                .addLoreLines(wrap("<gray>Click to change the armor"))
                                .addLoreLines(wrap(String.format("<gray>type for the %s", ItemUtil.getSlotName(slot))))
                                .addLoreLines(empty())
                                .addLoreLines(wrap("<#cdd6fa>Enabled: <white><bold>IRON"))
                                .addLoreLines(empty())
                                .addLoreLines(wrap("<#cdd6fa>\uD83D\uDF8D\u25cf Click to toggle"))
                                .get(),
                        new ItemBuilder(CraftItemStack.asNewCraftStack(armor.type().slotToItem.get(slot)))
                                .addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ITEM_SPECIFICS)
                                .addEnchantment(Enchantment.MENDING, 1, true)
                                .addItemFlags(ItemFlag.HIDE_ENCHANTS)
                                .setDisplayName(wrap("<#cdd6fa>Armor Type"))
                                .addLoreLines(empty())
                                .addLoreLines(wrap("<gray>Click to change the armor"))
                                .addLoreLines(wrap(String.format("<gray>type for the %s", ItemUtil.getSlotName(slot))))
                                .addLoreLines(empty())
                                .addLoreLines(wrap("<#cdd6fa>Enabled: <yellow><bold>GOLD"))
                                .addLoreLines(empty())
                                .addLoreLines(wrap("<#cdd6fa>\uD83D\uDF8D\u25cf Click to toggle"))
                                .get(),
                        new ItemBuilder(CraftItemStack.asNewCraftStack(armor.type().slotToItem.get(slot)))
                                .addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ITEM_SPECIFICS)
                                .addEnchantment(Enchantment.MENDING, 1, true)
                                .addItemFlags(ItemFlag.HIDE_ENCHANTS)
                                .setDisplayName(wrap("<#cdd6fa>Armor Type"))
                                .addLoreLines(empty())
                                .addLoreLines(wrap("<gray>Click to change the armor"))
                                .addLoreLines(wrap(String.format("<gray>type for the %s", ItemUtil.getSlotName(slot))))
                                .addLoreLines(empty())
                                .addLoreLines(wrap("<#cdd6fa>Enabled: <white><bold>CHAINMAIL"))
                                .addLoreLines(empty())
                                .addLoreLines(wrap("<#cdd6fa>\uD83D\uDF8D\u25cf Click to toggle"))
                                .get(),
                }, v -> settings.armor.put(slot, armor.withType(v)));
    }

    private static <O> void setCycleItem(Gui gui, Runnable show, int x, int y, O currently, O[] options, ItemStack[] optionItems, Consumer<O> setter) {
        ItemProvider[] items = new ItemProvider[options.length];
        for (int i = 0; i < options.length; i++) {
            items[i] = new ItemWrapper(optionItems[i]);
        }

        int pos = Arrays.binarySearch(options, currently);
        gui.setItem(x, y, CycleItem.withStateChangeHandler((player, index) -> {
            setter.accept(options[index]);
            show.run();
        }, pos == -1 ? 0 : pos, items));
    }

    private static void setBoolItem(Gui gui, Runnable show, int x, int y, boolean currently, ItemStack trueItem, ItemStack falseItem, Consumer<Boolean> setter) {
        gui.setItem(x, y, new SimpleItem(currently ? trueItem : falseItem, click -> {
            setter.accept(!currently);
            show.run();
        }));
    }
    
    private static ItemStack withType(ItemStack stack, Material type) {
        ItemStack itemStack = new ItemStack(type, stack.getAmount());
        if (stack.hasItemMeta()) {
            itemStack.setItemMeta(stack.getItemMeta());
        }

        return itemStack;
    }
}
