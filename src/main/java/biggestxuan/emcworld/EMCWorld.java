package biggestxuan.emcworld;

/**
 *  EMC WORLD MOD
 *  @Author Biggest_Xuan
 *  2022/07/24
 */

import biggestxuan.emcworld.client.event.ClientTickEvent;
import biggestxuan.emcworld.client.key.Admin;
import biggestxuan.emcworld.client.key.ArcanaDisplay;
import biggestxuan.emcworld.client.key.SpeedControl;
import biggestxuan.emcworld.common.blocks.AdvancedUpdateBlock.AdvancedUpdateGUI;
import biggestxuan.emcworld.common.blocks.GemstoneBlock.GemstoneGUI;
import biggestxuan.emcworld.common.blocks.InfuserBlock.InfuserGUI;
import biggestxuan.emcworld.common.blocks.SteelFurnace.SteelFurnaceGUI;
import biggestxuan.emcworld.common.blocks.WeaponUpgradeBlock.WeaponUpgradeGUI;
import biggestxuan.emcworld.common.config.ConfigManager;
import biggestxuan.emcworld.common.events.commandEvent;
import biggestxuan.emcworld.common.network.PacketHandler;
import biggestxuan.emcworld.common.network.SkillPacket.SkillNetworking;
import biggestxuan.emcworld.common.network.UtilPacket.UtilNetworking;
import biggestxuan.emcworld.common.registry.*;
import biggestxuan.emcworld.common.utils.RaidUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.resources.FilePack;
import net.minecraft.resources.IPackNameDecorator;
import net.minecraft.resources.ResourcePackInfo;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(EMCWorld.MODID)
public class EMCWorld {
    public static final Logger LOGGER = LogManager.getLogger("EMCWorld");
    public static final String MODID = "emcworld";
    public static final int ModPackVersion = 3;
    public static final String PackVersion = "0.3.0";
    public static final String TITLE = "EMCWorld " + PackVersion;
    public static final String PREFIX = "[EMCWorld] ";
    public static final long MAX_EMC = 1_000_000_000_000_000L;
    public static boolean isOffline = false;
    public static final File RP = new File(FMLPaths.GAMEDIR.get().toFile(),"resources/EMCWorld Language.zip");

    public EMCWorld(){
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::enqueueIMC);
        bus.addListener(this::processIMC);
        bus.addListener(this::doClientStuff);
        bus.addListener(this::doStuff);

        EWItems.ITEMS.register(bus);
        EWEffects.EFFECTS.register(bus);
        EWTileEntityTypes.TILE_ENTITIES.register(bus);
        EWBlocks.BLOCKS.register(bus);
        EWBlocks.B.register(bus);
        EWSlurries.SLURRY.register(bus);
        EWGases.GASES.register(bus);
        EWFluids.FLUIDS.register(bus);
        EWInfuseTypes.INFUSE_TYPES.register(bus);
        EWPigments.PIGMENTS.register(bus);
        EWContainerTypes.CONTAINERS.register(bus);
        EWRecipeTypes.RECIPES.register(bus);
        EWSounds.SOUND.register(bus);
        EWVillagers.POI.register(bus);
        EWVillagers.PROFESSION.register(bus);

        bus.addGenericListener(Block.class,EWBlocks::botaniaInit);

        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigManager.COMMON_CONFIG);
        if(FMLEnvironment.dist == Dist.CLIENT){
            Minecraft.getInstance().getResourcePackRepository().addPackFinder((consumer, factory) -> {
                ResourcePackInfo info = ResourcePackInfo.create(MODID,true,() -> new FilePack(RP),factory, ResourcePackInfo.Priority.TOP, IPackNameDecorator.BUILT_IN);
                if(info != null){
                    consumer.accept(info);
                }
            });
            Minecraft.getInstance().getResourcePackRepository().reload();
        }
    }

    private void doStuff(FMLCommonSetupEvent event){
        event.enqueueWork(UtilNetworking::registerMessage);
        event.enqueueWork(SkillNetworking::registerMessage);
        event.enqueueWork(RaidUtils::init);
        event.enqueueWork(PacketHandler::init);
        MinecraftForge.EVENT_BUS.register(new commandEvent());
    }

    private void doClientStuff(FMLClientSetupEvent event) {
        event.enqueueWork(()->{
            ScreenManager.register(EWContainerTypes.advancedUpdateContainer.get(), AdvancedUpdateGUI::new);
            ScreenManager.register(EWContainerTypes.infuserContainer.get(), InfuserGUI::new);
            ScreenManager.register(EWContainerTypes.weaponUpgradeContainer.get(), WeaponUpgradeGUI::new);
            ScreenManager.register(EWContainerTypes.gemstoneContainer.get(), GemstoneGUI::new);
            ScreenManager.register(EWContainerTypes.steelFurnaceContainer.get(), SteelFurnaceGUI::new);
            ClientRegistry.registerKeyBinding(Admin.ADMIN_KEY);
            ClientRegistry.registerKeyBinding(SpeedControl.SPEED_KEY);
            ClientRegistry.registerKeyBinding(ArcanaDisplay.ArcanaKey);
            LOGGER.info(ClientTickEvent.isCrash); //DEBUG
        });
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
    }
    private void processIMC(final InterModProcessEvent event) {}

    public static ResourceLocation rl(String id){
        return new ResourceLocation(MODID,id);
    }

    public static TranslationTextComponent tc(String id){
        return new TranslationTextComponent(id);
    }

    public static <T> TranslationTextComponent tc(T id){
        return tc(String.valueOf(id));
    }

    public static TranslationTextComponent tc(String id,Object... name){
        return new TranslationTextComponent(id,name);
    }
}