package blade.bot;

import blade.util.ItemUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ServerBotSettings implements Cloneable {
    private static final Map<EquipmentSlot, ArmorPiece> NETHERITE_ARMOR = new HashMap<>() {{
        put(EquipmentSlot.HEAD, new ArmorPiece(ArmorType.NETHERITE, false));
        put(EquipmentSlot.CHEST, new ArmorPiece(ArmorType.NETHERITE, false));
        put(EquipmentSlot.LEGS, new ArmorPiece(ArmorType.NETHERITE, true));
        put(EquipmentSlot.FEET, new ArmorPiece(ArmorType.NETHERITE, false));
    }};

    public static final ServerBotSettings TOTEM = new ServerBotSettings();

    public static final ServerBotSettings SHIELD = new ServerBotSettings() {{
        shield = true;
        // autoHit = true;
    }};

    public boolean autoHealing = true;
    public boolean moveTowardsSpawner = false;

    public Map<EquipmentSlot, ArmorPiece> armor = new HashMap<>(NETHERITE_ARMOR);

    public boolean shield = false;
    public boolean autoHit = false;

    public float reach = 3.0f;

    public Set<Holder<MobEffect>> effects = new HashSet<>();

    @Override
    public ServerBotSettings clone() {
        try {
            ServerBotSettings clone = (ServerBotSettings) super.clone();
            clone.armor = new HashMap<>(clone.armor);
            clone.effects = new HashSet<>(clone.effects);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public record ArmorPiece(ArmorType type, boolean blastProtection) {
        public ArmorPiece withBlastProtection(boolean v) {
            return new ArmorPiece(type, v);
        }

        public ArmorPiece withType(ArmorType v) {
            return new ArmorPiece(v, blastProtection);
        }

        public ItemStack buildStack(EquipmentSlot slot, Level world) {
            ItemStack stack = new ItemStack(type.slotToItem.get(slot));
            stack.enchant(ItemUtil.getEnchantment(blastProtection ? Enchantments.BLAST_PROTECTION : Enchantments.PROTECTION, world), 4);
            stack.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
            return stack;
        }
    }

    public enum ArmorType {
        NETHERITE(Map.of(
                EquipmentSlot.HEAD, Items.NETHERITE_HELMET,
                EquipmentSlot.CHEST, Items.NETHERITE_CHESTPLATE,
                EquipmentSlot.LEGS, Items.NETHERITE_LEGGINGS,
                EquipmentSlot.FEET, Items.NETHERITE_BOOTS
        )),
        DIAMOND(Map.of(
                EquipmentSlot.HEAD, Items.DIAMOND_HELMET,
                EquipmentSlot.CHEST, Items.DIAMOND_CHESTPLATE,
                EquipmentSlot.LEGS, Items.DIAMOND_LEGGINGS,
                EquipmentSlot.FEET, Items.DIAMOND_BOOTS
        )),
        IRON(Map.of(
                EquipmentSlot.HEAD, Items.IRON_HELMET,
                EquipmentSlot.CHEST, Items.IRON_CHESTPLATE,
                EquipmentSlot.LEGS, Items.IRON_LEGGINGS,
                EquipmentSlot.FEET, Items.IRON_BOOTS
        )),
        GOLD(Map.of(
                EquipmentSlot.HEAD, Items.GOLDEN_HELMET,
                EquipmentSlot.CHEST, Items.GOLDEN_CHESTPLATE,
                EquipmentSlot.LEGS, Items.GOLDEN_LEGGINGS,
                EquipmentSlot.FEET, Items.GOLDEN_BOOTS
        )),
        CHAIN(Map.of(
                EquipmentSlot.HEAD, Items.CHAINMAIL_HELMET,
                EquipmentSlot.CHEST, Items.CHAINMAIL_CHESTPLATE,
                EquipmentSlot.LEGS, Items.CHAINMAIL_LEGGINGS,
                EquipmentSlot.FEET, Items.CHAINMAIL_BOOTS
        ));

        public final Map<EquipmentSlot, Item> slotToItem;

        ArmorType(Map<EquipmentSlot, Item> slotToItem) {
            this.slotToItem = slotToItem;
        }
    }
}
