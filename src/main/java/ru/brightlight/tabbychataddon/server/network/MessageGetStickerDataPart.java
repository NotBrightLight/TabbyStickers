package ru.brightlight.tabbychataddon.server.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

import java.io.IOException;

import ru.brightlight.tabbychataddon.server.StickersAddon;

public class MessageGetStickerDataPart implements IMessage {
    public int handle;
    public static int bufferSize = 16384;

    public void fromBytes(ByteBuf buf) {
        this.handle = buf.readInt();
    }

    public void toBytes(ByteBuf buf) {
    }

    public static class Handler implements IMessageHandler<MessageGetStickerDataPart, IMessage> {
        public IMessage onMessage(MessageGetStickerDataPart message, MessageContext ctx) {
            StickersAddon.StickerData sd = StickersAddon.handles.get(message.handle);
            if (sd == null) {
                return null;
            } else {
                int size = Math.min(sd.size - sd.offset, MessageGetStickerDataPart.bufferSize);
                byte[] data = new byte[size];

                try {
                    sd.stream.seek(sd.offset);
                    sd.stream.read(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                sd.offset += size;
                if (sd.offset >= sd.size) {
                    StickersAddon.handles.remove(message.handle);
                }

                return new MessageStickerDataPart(message.handle, data, size);
            }
        }
    }
}
