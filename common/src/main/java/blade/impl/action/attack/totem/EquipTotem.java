package blade.impl.action.attack.totem;

import blade.impl.ConfigKeys;
import blade.impl.StateKeys;
import blade.impl.util.AttackUtil;
import blade.inventory.BotInventory;
import blade.inventory.Slot;
import blade.inventory.SlotFlag;
import blade.planner.score.ScoreState;
import blade.util.blade.BladeAction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

public class EquipTotem extends BladeAction implements Totem {
    @Override
    public void onTick() {
        BotInventory inv = bot.getInventory();
        Slot inventorySlot = inv.findFirst(stack -> stack.is(Items.TOTEM_OF_UNDYING), SlotFlag.MAIN, SlotFlag.ARMOR);
        if (inventorySlot == null) return;
        if (tick == 0) {
            inv.openInventory();
        }
        if (tick >= ConfigKeys.getDifficultyReversedCubic(bot) * 0.8) {
            inv.move(inventorySlot, Slot.fromOffHand(), true);
        }
    }

    @Override
    public boolean isSatisfied() {
        return true;
    }

    @Override
    public void getResult(ScoreState result) {
        result.setValue(StateKeys.OFF_HAND_TOTEM, 1);
        result.setValue(StateKeys.DOING_PVP, 1.0);
    }

    @Override
    public double getScore() {
        BotInventory inv = bot.getInventory();
        Player player = bot.getVanillaPlayer();
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        double deltaY = player.getDeltaMovement().y;
        double health = player.getHealth() / player.getMaxHealth();
        double healthReversed = 0 - health + 1;
        Slot hotBarSlot = inv.findFirst(stack -> stack.is(Items.TOTEM_OF_UNDYING), SlotFlag.HOT_BAR);
        return getTotemScore(bot) +
                (inv.getOffHand().is(Items.TOTEM_OF_UNDYING) ? -12 : 0) +
                (hotBarSlot != null && inv.getSelectedSlot() == hotBarSlot.getHotBarIndex() ? 0.4 : 0) +
                Math.min(Math.max(deltaY * 2, 0), 1.0) +
                (healthReversed * healthReversed) +
                AttackUtil.isAttacking(target, bot.getVanillaPlayer()) +
                (Math.min(tick / 0.7, 5));
    }

    @Override
    public void onRelease(BladeAction next) {
        super.onRelease(next);
        bot.getInventory().closeInventory();
    }
}