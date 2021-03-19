package ru.brightlight.tabbychataddon.server.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import ru.brightlight.tabbychataddon.server.StickersAddon;
import org.bukkit.entity.Player;

import java.util.Map;

public class MessageStickersAvailable implements IMessage {
    public Map<Integer, StickersAddon.Stickerpack> stickerpacks;
    public Player player;

    public MessageStickersAvailable(Map<Integer, StickersAddon.Stickerpack> stickerpacks, Player player) {
        this.stickerpacks = stickerpacks;
        this.player = player;
    }

    public void fromBytes(ByteBuf buf) {
    }

    public void toBytes(ByteBuf buf) {
        int spl = this.stickerpacks.size();
        buf.writeInt(spl);

        for (Map.Entry<Integer, StickersAddon.Stickerpack> integerStickerpackEntry : this.stickerpacks.entrySet()) {
            buf.writeInt(integerStickerpackEntry.getKey());
            buf.writeBoolean(this.player.hasPermission(integerStickerpackEntry.getValue().permission));
            buf.writeInt(integerStickerpackEntry.getValue().size() - 1);
        }

    }

    public static class Handler implements IMessageHandler<MessageStickersAvailable, IMessage> {
        public IMessage onMessage(MessageStickersAvailable message, MessageContext ctx) {
            return null;
        }
    }
}
