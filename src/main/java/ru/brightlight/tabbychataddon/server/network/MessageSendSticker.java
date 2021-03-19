package ru.brightlight.tabbychataddon.server.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import ru.brightlight.tabbychataddon.server.StickersAddon;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class MessageSendSticker implements IMessage {
    public int stickerpackId;
    public int stickerId;
    public boolean isGlobal;

    public void fromBytes(ByteBuf buf) {
        this.stickerpackId = buf.readInt();
        this.stickerId = buf.readInt();
        this.isGlobal = buf.readBoolean();
    }

    public void toBytes(ByteBuf buf) {
    }

    public static class Handler implements IMessageHandler<MessageSendSticker, IMessage> {
        public IMessage onMessage(MessageSendSticker message, MessageContext ctx) {
            StickersAddon.Stickerpack stickerpack = StickersAddon.stickerpacks.get(message.stickerpackId);
            if (stickerpack == null) {
                return null;
            } else if (!stickerpack.containsKey(message.stickerId)) {
                return null;
            } else {
                EntityPlayerMP player = ctx.getServerHandler().playerEntity;
                Player playerb = Bukkit.getServer().getPlayer(player.getUniqueID());
                if (playerb.hasPermission(stickerpack.permission)) {
                    ClassLoader bcl = Bukkit.getServer().getPluginManager().getPlugin("BrightPlugins").getClass().getClassLoader();
                    Plugin mb = Bukkit.getServer().getPluginManager().getPlugin("MaxBans");
                    // Проверяем через плагин на баны есть ли у игрока мут
                    // А также через чат менеджер получаем формат игрока в чате
                    try {
                        Object e = Class.forName("org.maxgamer.maxbans.MaxBans", true, bcl).getDeclaredMethod("getBanManager", null).invoke(mb, null);
                        Object mute = Class.forName("org.maxgamer.maxbans.banmanager.BanManager", true, bcl).getDeclaredMethod("getMute", String.class).invoke(e, player.getDisplayName().toLowerCase());
                        if (mute == null && System.currentTimeMillis() / 1000L >= StickersAddon.gcd) {
                            String format = message.isGlobal ? StickersAddon.globalPrefix : StickersAddon.localPrefix;
                            Class chatHandlerClass = Class.forName("ru.brightlight.plugins.listeners.ListenerChatMessages", true, bcl);
                            format = format + chatHandlerClass.getDeclaredMethod("getPlayerInChat", String.class).invoke(null, playerb.getName());
                            if (message.isGlobal) {
                                if (System.currentTimeMillis() / 1000L < StickersAddon.global_cd.get(playerb)) {
                                    if (StickersAddon.msgNotReadyGlobal.length() > 0) {
                                        player.addChatMessage(new ChatComponentText(StickersAddon.msgNotReadyGlobal));
                                    }
                                    return null;
                                }

                                StickersAddon.gcd = System.currentTimeMillis() / 1000L + (long) StickersAddon.cdGlobal;
                                StickersAddon.global_cd.replace(playerb, System.currentTimeMillis() / 1000L + (long) StickersAddon.cdPlayerGlobal);
                                StickersAddon.network.sendToAll(new MessageChatSticker(format, message.stickerpackId, message.stickerId));
                            } else {
                                if (System.currentTimeMillis() / 1000L < StickersAddon.local_cd.get(playerb)) {
                                    if (StickersAddon.msgNotReadyLocal.length() > 0) {
                                        player.addChatMessage(new ChatComponentText(StickersAddon.msgNotReadyLocal));
                                    }
                                    return null;
                                }

                                StickersAddon.local_cd.replace(playerb, System.currentTimeMillis() / 1000L + (long) StickersAddon.cdPlayerLocal);
                                String[] usernames = MinecraftServer.getServer().getAllUsernames();

                                for (String username : usernames) {
                                    Player p = Bukkit.getPlayer(username);
                                    if (p.getWorld() == playerb.getWorld() && playerb.getLocation().distance(p.getLocation()) <= (double) StickersAddon.localRadius) {
                                        StickersAddon.network.sendTo(new MessageChatSticker(format, message.stickerpackId, message.stickerId), MinecraftServer.getServer().getConfigurationManager().func_152612_a(username));
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    player.addChatMessage(new ChatComponentText(StickersAddon.msgNoPerm));
                }
                return null;
            }
        }
    }
}
