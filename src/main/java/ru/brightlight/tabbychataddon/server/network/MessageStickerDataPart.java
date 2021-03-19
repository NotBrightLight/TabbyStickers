package ru.brightlight.tabbychataddon.server.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class MessageStickerDataPart implements IMessage {
    public int handle;
    public byte[] data;
    int size;

    public MessageStickerDataPart(int handle, byte[] data, int size) {
        this.handle = handle;
        this.data = data;
        this.size = size;
    }

    public void fromBytes(ByteBuf buf) {
    }

    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.handle);
        buf.writeInt(this.size);

        for (int i = 0; i < this.size; ++i) {
            buf.writeByte(this.data[i]);
        }

    }

    public static class Handler implements IMessageHandler<MessageStickerDataPart, IMessage> {
        public IMessage onMessage(MessageStickerDataPart message, MessageContext ctx) {
            return null;
        }
    }
}
