package ru.brightlight.tabbychataddon.server.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class MessageChatSticker implements IMessage {
    public int stickerpackId;
    public int stickerId;
    String player;

    public MessageChatSticker(String player, int stickerpackId, int stickerId) {
        this.player = player;
        this.stickerpackId = stickerpackId;
        this.stickerId = stickerId;
    }

    public void fromBytes(ByteBuf buf) {
    }

    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.stickerpackId);
        buf.writeInt(this.stickerId);
        ByteBufUtils.writeUTF8String(buf, this.player);
    }

    public static class Handler implements IMessageHandler<MessageChatSticker, IMessage> {
        public IMessage onMessage(MessageChatSticker message, MessageContext ctx) {
            return null;
        }
    }
}
