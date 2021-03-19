package ru.brightlight.tabbychataddon.server.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class MessageStickerMeta implements IMessage {
    public boolean isLogo;
    public int stickerpackId;
    public int stickerId;
    public int handle;
    public int size;

    public MessageStickerMeta(int stickerpackId, boolean isLogo, int stickerId, int handle, int size) {
        this.stickerpackId = stickerpackId;
        this.isLogo = isLogo;
        this.stickerId = stickerId;
        this.handle = handle;
        this.size = size;
    }

    public void fromBytes(ByteBuf buf) {
    }

    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.stickerpackId);
        buf.writeBoolean(this.isLogo);
        if (!this.isLogo) {
            buf.writeInt(this.stickerId);
        }

        buf.writeInt(this.handle);
        buf.writeInt(this.size);
    }

    public static class Handler implements IMessageHandler<MessageStickerMeta, IMessage> {
        public IMessage onMessage(MessageStickerMeta message, MessageContext ctx) {
            return null;
        }
    }
}
