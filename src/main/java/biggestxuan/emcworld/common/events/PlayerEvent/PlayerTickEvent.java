package biggestxuan.emcworld.common.events.PlayerEvent;

/**
 *  EMC WORLD MOD
 *  @Author Biggest_Xuan
 *  2022/08/04
 */

import biggestxuan.emcworld.EMCWorld;
import biggestxuan.emcworld.api.EMCWorldAPI;
import biggestxuan.emcworld.api.capability.IPlayerSkillCapability;
import biggestxuan.emcworld.api.capability.IUtilCapability;
import biggestxuan.emcworld.api.item.IEMCRepairableItem;
import biggestxuan.emcworld.api.item.INeedLevelItem;
import biggestxuan.emcworld.api.item.IPlayerDifficultyItem;
import biggestxuan.emcworld.api.item.ISecondEMCItem;
import biggestxuan.emcworld.api.item.base.BaseDifficultyItem;
import biggestxuan.emcworld.api.item.equipment.IAttackSpeedItem;
import biggestxuan.emcworld.api.item.equipment.armor.IEMCShieldArmor;
import biggestxuan.emcworld.api.item.equipment.armor.IReachArmor;
import biggestxuan.emcworld.api.item.equipment.armor.ISpeedArmor;
import biggestxuan.emcworld.api.item.equipment.armor.IUpgradeableArmor;
import biggestxuan.emcworld.common.capability.EMCWorldCapability;
import biggestxuan.emcworld.common.compact.CraftTweaker.CrTConfig;
import biggestxuan.emcworld.common.compact.Curios.PlayerCurios;
import biggestxuan.emcworld.common.compact.GameStage.GameStageManager;
import biggestxuan.emcworld.common.compact.Projecte.EMCHelper;
import biggestxuan.emcworld.common.compact.ScalingHealth.DifficultyHelper;
import biggestxuan.emcworld.common.config.ConfigManager;
import biggestxuan.emcworld.common.data.DifficultyData;
import biggestxuan.emcworld.common.items.Curios.NuclearBall;
import biggestxuan.emcworld.common.utils.MathUtils;
import biggestxuan.emcworld.common.utils.Message;
import biggestxuan.emcworld.common.utils.RaidUtils;
import dev.latvian.mods.projectex.Matter;
import divinerpg.capability.Arcana;
import divinerpg.capability.ArcanaCapability;
import mekanism.api.Coord4D;
import mekanism.api.MekanismAPI;
import mekanism.common.registries.MekanismDamageSource;
import mekanism.common.registries.MekanismItems;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.raid.Raid;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static biggestxuan.emcworld.common.utils.RaidUtils.getRaidAllPlayers;

@Mod.EventBusSubscriber(
        modid = EMCWorld.MODID,
        bus=Mod.EventBusSubscriber.Bus.FORGE
)
public class PlayerTickEvent {
    private static final UUID EMCWORLD_MODIFY_HEALTH_ID = UUID.fromString("a80fcb74-82f3-4e06-8df7-7d8031e8976e");
    private static final UUID EMCWORLD_MODIFY_SPEED_ID = UUID.fromString("ec77a354-5dab-4ec4-8bde-496ba2b72860");
    private static final UUID EMCWORLD_REACH_DISTANCE_ID = UUID.fromString("4f6e18a2-d5ed-41bb-9e8e-ee816bd4d059");
    private static final UUID EMCWORLD_ATTACK_SPEED_ID = UUID.fromString("64acff0d-09a2-4e4b-a397-e47b378e1d7f");
    private static final String EMCWORLD_MODIFY_HEALTH_NAME = "EMCWorld.HealthModifier";
    private static final String EMCWORLD_MODIFY_SPEED_NAME = "EMCWorld.SpeedModifier";
    private static final String EMCWORLD_REACH_DISTANCE_NAME = "EMCWorld.ReachDistanceModifier";
    private static final String EMCWORLD_ATTACK_SPEED_NAME = "EMCWorld.AttackSpeedModifier";

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void playerTickEvent(TickEvent.PlayerTickEvent event) {
        final Item[] radiationItem = new Item[]{
                MekanismItems.PLUTONIUM_PELLET.getItem(),MekanismItems.ANTIMATTER_PELLET.getItem(),MekanismItems.POLONIUM_PELLET.getItem()
        };
        PlayerEntity player = event.player;
        MinecraftServer server = player.getCommandSenderWorld().getServer();
        if(!GameStageManager.hasStage(player,"fly") && !player.isCreative() && !player.isSpectator()){
            player.abilities.flying=false;
            player.abilities.mayfly=false;
        }
        if(player.level.isClientSide() || event.side.isClient()) return;
        if(event.phase == TickEvent.Phase.END) return;
        LazyOptional<IUtilCapability> cap = player.getCapability(EMCWorldCapability.UTIL);
        cap.ifPresent((c)->{
                long l = c.getCoolDown();
                if(l>0){
                    c.setCoolDown(l-1);
                }
                if(c.getTimer() >0){
                    c.setTimer(c.getTimer()-1);
                }
        });
        IPlayerSkillCapability c = EMCWorldAPI.getInstance().getPlayerSkillCapability(player);
        IUtilCapability util = EMCWorldAPI.getInstance().getUtilCapability(player);
        /*World world1 = server.overworld();
        ShareEMCData emcData = ShareEMCData.getInstance(world1);
        if(ConfigManager.SHARE_EMC.get()){
            if(!util.share()){
                emcData.addEMC(EMCHelper.getPlayerEMC(player));
                util.setShare(true);
            }
        }else{
            if(util.share()){
                EMCHelper.clearPlayerEMC(player);
                emcData.setEMC(0);
                util.setShare(false);
            }
        }
        emcData.setEMC(EMCHelper.getPlayerEMC(player));
        EMCHelper.setPlayerEMC(player,emcData.getEMC());*/
        if(c.getSkills()[7] >0){
            c.setSkills(7,c.getSkills()[7]-1);
        }
        if(c.getSkills()[19] >0){
            c.setSkills(19,c.getSkills()[19]-1);
        }
        if(c.getSkills()[42] >0){
            c.setSkills(42,c.getSkills()[42]-1);
        }
        if(c.getSkills()[43] >0){
            c.setSkills(43,c.getSkills()[43]-1);
        }
        if(!player.isDeadOrDying()){
            Arcana arcana = player.getCapability(ArcanaCapability.CAPABILITY_ARCANA).orElseThrow(NullPointerException::new);
            util.setMaxArcana(arcana.getMaxArcana());
            util.setArcana(arcana.getArcana());
        }
        util.setSHDifficulty(DifficultyHelper.getPlayerDifficulty(player));
        float extraSpeed = Math.min(util.getSpeed(),getPlayerMaxSpeed(player));
        ModifiableAttributeInstance speed_instance = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if(speed_instance != null){
            AttributeModifier modifier = speed_instance.getModifier(EMCWORLD_MODIFY_SPEED_ID);
            if(modifier != null){
                speed_instance.removeModifier(EMCWORLD_MODIFY_SPEED_ID);
            }
            speed_instance.addPermanentModifier(new AttributeModifier(EMCWORLD_MODIFY_SPEED_ID, EMCWORLD_MODIFY_SPEED_NAME,extraSpeed,AttributeModifier.Operation.ADDITION));
        }
        double attackSpeed = 1;
        if(c.getProfession() == 3 && c.getSkills()[36] != 0 && c.getModify() == 1){
            attackSpeed += c.getSkills()[36] /10000f;
        }
        if(player.getMainHandItem().getItem() instanceof IAttackSpeedItem){
            IAttackSpeedItem si = (IAttackSpeedItem) player.getMainHandItem().getItem();
            attackSpeed *= si.getAttackSpeed(player.getMainHandItem());
        }
        ModifiableAttributeInstance attack_speed_instance = player.getAttribute(Attributes.ATTACK_SPEED);
        if(attack_speed_instance != null){
            AttributeModifier modifier = attack_speed_instance.getModifier(EMCWORLD_ATTACK_SPEED_ID);
            if(modifier != null){
                attack_speed_instance.removeModifier(EMCWORLD_ATTACK_SPEED_ID);
            }
            double d = attackSpeed < 1 ? 0.8 * attackSpeed - 0.8 : 4 * attackSpeed - 4;
            attack_speed_instance.addPermanentModifier(new AttributeModifier(EMCWORLD_ATTACK_SPEED_ID, EMCWORLD_ATTACK_SPEED_NAME,d,AttributeModifier.Operation.ADDITION));
        }
        double extraReachDistance = 0;
        for(ItemStack stack:player.inventory.armor){
            if(stack.getItem() instanceof IReachArmor){
                IReachArmor armor = (IReachArmor) stack.getItem();
                extraReachDistance += armor.getReachDistance(stack);
            }
        }
        ModifiableAttributeInstance reach_instance = player.getAttribute(ForgeMod.REACH_DISTANCE.get());
        if(reach_instance != null){
            AttributeModifier modifier = reach_instance.getModifier(EMCWORLD_REACH_DISTANCE_ID);
            if(modifier != null){
                reach_instance.removeModifier(EMCWORLD_REACH_DISTANCE_ID);
            }
            reach_instance.addPermanentModifier(new AttributeModifier(EMCWORLD_REACH_DISTANCE_ID,EMCWORLD_REACH_DISTANCE_NAME,extraReachDistance, AttributeModifier.Operation.ADDITION));
        }
        ItemStack[] handItem = new ItemStack[]{
                player.getMainHandItem(),player.getOffhandItem()
        };
        float health = 0;
        for(ItemStack stack:getPlayerAllArmor(player)){
            if(stack.getItem() instanceof IUpgradeableArmor){
                IUpgradeableArmor armor = (IUpgradeableArmor) stack.getItem();
                health += armor.extraHealth(stack);
            }

        }
        ModifiableAttributeInstance health_instance = player.getAttribute(Attributes.MAX_HEALTH);
        if(health_instance != null){
            AttributeModifier modifier = health_instance.getModifier(EMCWORLD_MODIFY_HEALTH_ID);
            if(modifier != null){
                health_instance.removeModifier(EMCWORLD_MODIFY_HEALTH_ID);
            }
            health_instance.addPermanentModifier(new AttributeModifier(EMCWORLD_MODIFY_HEALTH_ID,EMCWORLD_MODIFY_HEALTH_NAME,health,AttributeModifier.Operation.ADDITION));
        }
        for(ItemStack item:handItem){
            if(item.getItem() instanceof INeedLevelItem){
                INeedLevelItem levelItem = (INeedLevelItem) item.getItem();
                int needLevel = levelItem.getUseLevel(item);
                int playerLevel = c.getLevel();
                if(playerLevel < needLevel && !player.isCreative()){
                    for (int i = 0; i < player.inventory.getContainerSize(); i++) {
                        if(player.inventory.getItem(i).equals(item)){
                            player.drop(item,false);
                            player.inventory.setItem(i,ItemStack.EMPTY);
                        }
                    }
                }
            }
        }
        assert server != null;
        ServerWorld world = server.overworld();
        BlockPos playerPos = new BlockPos(player.position());
        if(world.dayTime() % 100 == 0){
            if(!player.level.dimension().equals(World.OVERWORLD)){
                if(world.isRaided(playerPos)){
                    for(PlayerEntity player1 : PlayerDeathEvent.getNearPlayer(world.getRaidAt(playerPos),world)){
                        Message.sendMessage(player1, EMCWorld.tc("message.raid.dim_cancel"));
                    }
                    world.getRaidAt(playerPos).stop();
                }
            }
        }
        if(world.isRaided(playerPos)){
            Raid raid = world.getRaidAt(new BlockPos(player.position()));
            if(raid != null){
                util.setRaid(true);
                if(raid.isLoss()){
                    util.setState(3);
                }else if(raid.isVictory()){
                    util.setState(2);
                    if(!util.hasBeenDisplayDamage()){
                        List<? extends RaidInfo> players = getRaidAllPlayers(raid);
                        int count = players.size();
                        float allDamage = 0f;
                        for(RaidInfo r : players){
                            allDamage += r.damage;
                        }
                        Message.sendMessage(player,EMCWorld.tc("message.raid.successful_title"));
                        for (int i = 0; i < count; i++) {
                            RaidInfo info = players.get(i);
                            Message.sendMessage(player,EMCWorld.tc("message.raid.successful_info",i+1,info.getPlayer().getScoreboardName(),String.format("%.2f",info.damage),String.format("%.2f",(info.damage/allDamage)*100)+"%"));
                        }
                    }
                    util.setDisplayDamage(true);
                }else if(raid.isBetweenWaves()){
                    util.setState(4);
                }else util.setState(1);
                float rate = 1f;
                if(getVillagerAmount(raid) <= 10){
                    rate = (float) (1f + ((-0.2 * getVillagerAmount(raid) + 2) / (float) MathUtils.difficultyLoss()));
                }
                util.setPillager(raid.getTotalRaidersAlive());
                util.setVillager(getVillagerAmount(raid));
                util.setWave(raid.getGroupsSpawned());
                util.setRaidRate(rate);
                util.setMaxWave(RaidUtils.getRaidWave());
                if(c.getModify() == 0 || c.getLevel() < 80){
                    for(PlayerEntity player1 : PlayerDeathEvent.getNearPlayer(raid)){
                        Message.sendMessage(player1, EMCWorld.tc("message.raid.cancel"));
                    }
                    raid.stop();
                }
            }
        }
        else{
            util.setRaid(false);
            util.setState(1);
            util.setPillager(0);
            util.setVillager(0);
            util.setDisplayDamage(false);
            util.setRaidDamage(0f);
        }
        DifficultyData data = DifficultyData.getInstance(server.overworld());
        data.putDifficulty(Math.min(data.getDifficulty(),ConfigManager.DIFFICULTY.get()));
        util.setDifficulty(data.getDifficulty());
        double radiation = MekanismAPI.getRadiationManager().getRadiationLevel(new Coord4D(playerPos,player.level));
        if(radiation > 50000 && !player.isCreative()){
            ItemStack stack = PlayerCurios.getPlayerNuclearBall(player);
            float hurt = player.getMaxHealth() / 20f;
            if(!stack.equals(ItemStack.EMPTY)){
                NuclearBall ball = (NuclearBall) stack.getItem();
                if(ball.getLevel() == 3){
                    stack.setDamageValue(stack.getDamageValue()+1);
                    return;
                }
                if(radiation > ball.getMax_radiation() && world.getDayTime()% 40 == 0){
                    player.hurt(MekanismDamageSource.RADIATION,hurt);
                }else{
                    stack.setDamageValue(stack.getDamageValue()+1);
                }
            }
            else player.hurt(MekanismDamageSource.RADIATION,hurt*2f);
        }
        if(world.getGameTime() % 20 == 0){
            long amount = 0;
            for(ItemStack stack:getPlayerAllItem(player)){
                if(stack.getItem() instanceof ISecondEMCItem){
                    ISecondEMCItem item = (ISecondEMCItem) stack.getItem();
                    if(stack.getItem() instanceof INeedLevelItem){
                        INeedLevelItem levelItem = (INeedLevelItem) stack.getItem();
                        if(c.getLevel() < levelItem.getUseLevel(stack) && !player.isCreative()){
                            continue;
                        }
                    }
                    amount += item.EMCModifySecond(stack);
                }
            }
            EMCHelper.modifyPlayerEMC(player,amount,false);
        }
        float max_total = 0f;
        float total = 0f;
        for(ItemStack stack:getPlayerAllItem(player)){
            if(stack.getItem() instanceof IEMCRepairableItem){
                IEMCRepairableItem item = (IEMCRepairableItem) stack.getItem();
                if(EMCHelper.getPlayerEMC(player) >= item.getTickCost(stack) && stack.getDamageValue() != 0){
                    EMCHelper.modifyPlayerEMC(player,Math.negateExact(item.getTickCost(stack)),false);
                    stack.setDamageValue(stack.getDamageValue()-1);
                }
            }
            if(stack.getItem() instanceof IPlayerDifficultyItem){
                IPlayerDifficultyItem item1 = (IPlayerDifficultyItem) stack.getItem();
                if(util.getDifficulty() < item1.requireDifficulty()){
                    stack.setCount(0);
                }
            }
            if(stack.hasTag()){
                if(stack.getOrCreateTag().getString("patchouli:book").equals("the_afterlight:afterlight_tome") && !GameStageManager.hasStage(player,"four")){
                    stack.shrink(1);
                }
            }
            if(stack.getItem() instanceof IEMCShieldArmor){
                IEMCShieldArmor armor = (IEMCShieldArmor) stack.getItem();
                armor.setMaxShield(stack,armor.maxShield(stack));
                armor.modifyShield(stack,0);
                armor.setShieldSpeed(stack,armor.shieldSpeed(stack));
            }
            if(stack.getItem() instanceof BaseDifficultyItem){
                BaseDifficultyItem item = (BaseDifficultyItem) stack.getItem();
                double diff = CrTConfig.getWorldDifficulty();
                if(item.getDifficulty() > diff){
                    stack.shrink(stack.getCount());
                }
            }
            for(Item item:radiationItem){
                if(stack.getItem().equals(item)){
                    MekanismAPI.getRadiationManager().radiate(new Coord4D(player),0.2d);
                }
            }
            if(ConfigManager.FREE_MODE.get()){
                for(MAP m : MAP.values()){
                    if((stack.getItem().getRegistryName().equals(m.getItem()[0]) || stack.getItem().getRegistryName().equals(m.getItem()[1])) && !GameStageManager.hasStage(player,m.stage)){
                        GameStageManager.addStage(player, m.getStage());
                    }
                }
            }
        }
        for(ItemStack stack:player.inventory.armor){
            if(stack.getItem() instanceof IEMCShieldArmor){
                IEMCShieldArmor armor = (IEMCShieldArmor) stack.getItem();
                long cost = (long) (-100000 * armor.getShieldSpeed(stack) / 100);
                if(armor.getShield(stack) < armor.getMaxShield(stack) && armor.getInfuser(stack) >= cost){
                    armor.addInfuser(stack, cost);
                    armor.heal(stack);
                }
                total += armor.getShield(stack);
                max_total += armor.getMaxShield(stack);
            }
        }
        util.setShield(total);
        util.setMaxShield(max_total);
    }

    public static class RaidInfo{
        private final PlayerEntity player;
        private final float damage;

        public RaidInfo(PlayerEntity player){
            this.damage = player == null ? 0 : player.getCapability(EMCWorldCapability.UTIL).orElseThrow(NullPointerException::new).getRaidDamage();
            this.player = player;
        }

        public PlayerEntity getPlayer(){
            return player;
        }

        public Float getDamage(){
            return damage;
        }

    }

    public static List<ItemStack> getPlayerAllItem(PlayerEntity player){
        List<ItemStack> list = new ArrayList<>();
        list.addAll(player.inventory.items);
        list.addAll(player.inventory.armor);
        return list;
    }

    public static List<ItemStack> getPlayerAllArmor(PlayerEntity player){
        return player.inventory.armor;
    }

    private static int getVillagerAmount(Raid raid){
        int size = 0;
        for(VillagerEntity villager:new RaidUtils(raid).getVillager()){
            if(Objects.requireNonNull(villager.getServer()).overworld().isRaided(new BlockPos(villager.position()))){
                if(Objects.requireNonNull(villager.getServer().overworld().getRaidAt(new BlockPos(villager.position()))).getId() == raid.getId()){
                    size++;
                }
            }
        }
        return size;
    }

    public static float getPlayerMaxSpeed(PlayerEntity player){
        float max = 0f;
        for(ItemStack stack:player.inventory.armor){
            if(stack.getItem() instanceof ISpeedArmor){
                ISpeedArmor armor = (ISpeedArmor) stack.getItem();
                max += armor.getSpeed(stack);
            }
        }
        return max;
    }

    enum MAP{
        ONE(Matter.BASIC,"one"),
        TWO(Matter.DARK, "two"),
        THREE(Matter.RED, "three"),
        FOUR(Matter.PINK, "four"),
        FIVE(Matter.VIOLET, "five"),
        SIX(Matter.BLUE, "six"),
        SEVEN(Matter.GREEN, "seven"),
        EIGHT(Matter.CYAN, "eight")
        ;

        final Matter item;
        final String stage;

        MAP(Matter item,String stage){
            this.item = item;
            this.stage = stage;
        }

        public ResourceLocation[] getItem() {
            return new ResourceLocation[]{
                new ResourceLocation("projectex",item.name+"_collector"),
                    new ResourceLocation("projectex",item.name+"_relay"),
            };
        }

        public String getStage() {
            return stage;
        }
    }
}
