package ru.brightlight.tabbychataddon.server;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.config.Configuration;
import ru.brightlight.tabbychataddon.server.network.MessageChatSticker;
import ru.brightlight.tabbychataddon.server.network.MessageGetStickerDataPart;
import ru.brightlight.tabbychataddon.server.network.MessageGetStickerMeta;
import ru.brightlight.tabbychataddon.server.network.MessageSendSticker;
import ru.brightlight.tabbychataddon.server.network.MessageStickerDataPart;
import ru.brightlight.tabbychataddon.server.network.MessageStickerMeta;
import ru.brightlight.tabbychataddon.server.network.MessageStickersAvailable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

@Mod(
        name = "TabbyChatStickersAddon",
        modid = "tabbychatstickersaddon",
        version = "1.0.0",
        acceptableRemoteVersions = "*"
)
public class StickersAddon {
    static Configuration config;

    public static int cdGlobal;
    public static int cdPlayerGlobal;
    public static int cdPlayerLocal;
    public static String msgNoPerm;
    public static String msgNotReadyGlobal;
    public static String msgNotReadyLocal;
    public static String globalPrefix;
    public static String localPrefix;
    public static int localRadius;
    public static HashMap<Integer, StickerData> handles;
    public static long gcd = 0L;
    public static Map<Player, Long> global_cd;
    public static Map<Player, Long> local_cd;
    public static SimpleNetworkWrapper network;
    public static Map<Integer, Stickerpack> stickerpacks;

    @SubscribeEvent
    public void onPlayerLoggedOutEvent(PlayerLoggedOutEvent e) {
        global_cd.remove(Bukkit.getPlayer(e.player.getUniqueID()));
        local_cd.remove(Bukkit.getPlayer(e.player.getUniqueID()));
    }

    @SubscribeEvent
    public void onPlayerLoggedInEvent(PlayerLoggedInEvent e) {
        global_cd.put(Bukkit.getPlayer(e.player.getUniqueID()), 0L);
        local_cd.put(Bukkit.getPlayer(e.player.getUniqueID()), 0L);
        network.sendTo(new MessageStickersAvailable(stickerpacks, Bukkit.getServer().getPlayer(e.player.getUniqueID())), (EntityPlayerMP) e.player);
    }

    @EventHandler
    public void load(FMLInitializationEvent e) {
        config = new Configuration(new File("config/Stickers.cfg"));
        config.load();
        cdGlobal = config.getInt("global_cd", "cd", 0, 0, Integer.MAX_VALUE, "Global cooldown");
        cdPlayerGlobal = config.getInt("player_cd_global", "cd", 30, 0, Integer.MAX_VALUE, "Player cooldown GLOBAL");
        cdPlayerLocal = config.getInt("player_cd_local", "cd", 15, 0, Integer.MAX_VALUE, "Player cooldown LOCAL");
        msgNoPerm = config.getString("accessdenied", "messages", "У вас нет доступа.", "No permission message");
        msgNotReadyGlobal = config.getString("notready_global", "messages", "§4Глобальная перезарядка стикера... Подождите... \\(^o^)/", "Global cooldown message");
        msgNotReadyLocal = config.getString("notready_local", "messages", "§4Перезарядка стикера... Подождите... \\(^o^)/", "Local cooldown message");
        globalPrefix = config.getString("prefix_global", "chat", "§8[§6G§8] §r", "Global chat prefix");
        localPrefix = config.getString("prefix_local", "chat", "§8[§fL§8] §r", "Local chat prefix");
        localRadius = config.getInt("local_radius", "chat", 100, 0, Integer.MAX_VALUE, "Local chat radius");
        config.save();

        File stickerpacksDirs = new File("stickers");
        stickerpacksDirs.mkdir();
        int i = 0;
        System.out.println("Stickers scanning starts here");

        for (File stickerpackDir : stickerpacksDirs.listFiles()) {
            Stickerpack s = new Stickerpack();
            s.permission = new Permission("stickers." + stickerpackDir.getName());
            int j = 0;
            File[] files = stickerpackDir.listFiles();
            Arrays.sort(files);

            for (File sticker : files) {
                s.put(j++, sticker);
            }

            stickerpacks.put(i++, s);
            System.out.println("Loaded stickerpack \'" + stickerpackDir.getName() + "\' id=" + i + " size=" + j);
        }

        network = NetworkRegistry.INSTANCE.newSimpleChannel("TabbyChatStickers");
        byte var15 = 0;
        int var16 = var15 + 1;
        network.registerMessage(MessageStickersAvailable.Handler.class, MessageStickersAvailable.class, var15, Side.CLIENT);
        network.registerMessage(MessageGetStickerMeta.Handler.class, MessageGetStickerMeta.class, var16++, Side.SERVER);
        network.registerMessage(MessageStickerMeta.Handler.class, MessageStickerMeta.class, var16++, Side.CLIENT);
        network.registerMessage(MessageGetStickerDataPart.Handler.class, MessageGetStickerDataPart.class, var16++, Side.SERVER);
        network.registerMessage(MessageStickerDataPart.Handler.class, MessageStickerDataPart.class, var16++, Side.CLIENT);
        network.registerMessage(MessageSendSticker.Handler.class, MessageSendSticker.class, var16++, Side.SERVER);
        network.registerMessage(MessageChatSticker.Handler.class, MessageChatSticker.class, var16++, Side.CLIENT);
        FMLCommonHandler.instance().bus().register(this);
    }

    public static class Stickerpack extends HashMap<Integer, File> {
        public Permission permission;
    }

    public static class StickerData {
        public int offset = 0;
        public int size;
        public RandomAccessFile stream;
    }
}
