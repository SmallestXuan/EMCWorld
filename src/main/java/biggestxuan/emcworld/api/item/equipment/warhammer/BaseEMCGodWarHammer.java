package biggestxuan.emcworld.api.item.equipment.warhammer;

/*
 *  EMC WORLD MOD
 *  @Author Biggest_Xuan
 *  2022/12/05
 */

import biggestxuan.emcworld.EMCWorld;
import biggestxuan.emcworld.api.EMCWorldAPI;
import biggestxuan.emcworld.api.item.*;
import biggestxuan.emcworld.api.item.equipment.IAttackSpeedItem;
import biggestxuan.emcworld.api.item.equipment.IEMCGodWeaponLevel;
import biggestxuan.emcworld.api.item.equipment.weapon.BaseEMCGodWeapon;
import biggestxuan.emcworld.api.item.equipment.weapon.IAdditionsDamageWeapon;
import biggestxuan.emcworld.api.item.equipment.weapon.ICriticalWeapon;
import biggestxuan.emcworld.common.items.Equipment.Weapon.WarHammer.WarHammerItem;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.Rarity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class BaseEMCGodWarHammer extends WarHammerItem implements IEMCRepairableItem, IUpgradeableItem, IUpgradeableMaterial, ISecondEMCItem, IEMCInfuserItem, ICriticalWeapon, IAdditionsDamageWeapon, IAttackSpeedItem, IEMCGodWeaponLevel {
    public BaseEMCGodWarHammer(){
        super(EMCWorldAPI.getInstance().getWarHammerTier("god"));
    }

    protected abstract double getAttackCostRate(ItemStack stack);

    protected abstract long modifyEMCSecond(ItemStack stack);

    protected abstract double criticalChance(ItemStack stack);

    protected abstract double criticalRate(ItemStack stack);

    protected abstract float damage(ItemStack stack);

    protected abstract double attackRange(ItemStack stack);

    @Override
    public abstract double getAttackSpeed(ItemStack stack);

    @Override
    public double getAttackRange(ItemStack stack) {
        double b = attackRange(stack);
        long costEMC = getCostEMC(stack);
        if(costEMC >= 1){
            b *= (1 + Math.log(costEMC)/85);
        }
        return (float) b;
    }

    @Override
    public double getCriticalChance(ItemStack stack) {
        double b = criticalChance(stack);
        long costEMC = getCostEMC(stack);
        if(costEMC >= 1){
            b += Math.log(costEMC) / 100 * 1.02;
        }
        return (float) b;
    }

    @Override
    public double getCriticalRate(ItemStack stack) {
        double b = criticalRate(stack);
        long costEMC = getCostEMC(stack);
        if(costEMC >= 1){
            b += Math.log(costEMC) / 100 * 1.03;
        }
        return (float) b;
    }

    @Override
    public double costEMCWhenAttack(ItemStack stack) {
        return getAttackCostRate(stack);
    }

    @Override
    public long getMaxInfuser(ItemStack stack) {
        return (long) (Math.pow(1.4,getLevel(stack)) * 500000);
    }

    @Override
    public long getTickCost(ItemStack stack) {
        return 40L * getLevel(stack);
    }

    @Override
    public long EMCModifySecond(ItemStack stack) {
        return modifyEMCSecond(stack);
    }

    @Override
    public int getMaxLevel() {
        return BaseEMCGodWeapon.getWeaponMaxLevel();
    }

    @Override
    public float getAdditionsDamage(ItemStack stack){
        double b = damage(stack);
        long costEMC = getCostEMC(stack);
        if(costEMC >= 1){
            b *= (1 + Math.log(costEMC)/85);
        }
        return (float) b;
    }

    @Nonnull
    @Override
    public ITextComponent getName(@Nonnull ItemStack p_200295_1_) {
        int level = getLevel(p_200295_1_);
        String name = this.toString();
        return EMCWorld.tc("item.emcworld."+name).append(" (+"+level+")");
    }

    @Nonnull
    @Override
    public Rarity getRarity(@Nonnull ItemStack p_77613_1_){
        int level = getLevel(p_77613_1_);
        if(level <= 8) return Rarity.COMMON;
        if(level <= 14) return Rarity.UNCOMMON;
        if(level <= 20) return Rarity.RARE;
        return Rarity.EPIC;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@Nonnull ItemStack p_77624_1_, @Nullable World p_77624_2_, List<ITextComponent> p_77624_3_, @Nonnull ITooltipFlag p_77624_4_) {
        p_77624_3_.add(EMCWorld.tc("tooltip.emcworld.weapon_god"));
    }
}
