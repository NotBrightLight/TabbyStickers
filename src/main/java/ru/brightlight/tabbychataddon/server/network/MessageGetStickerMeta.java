package ru.brightlight.tabbychataddon.server.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Random;

import ru.brightlight.tabbychataddon.server.StickersAddon;
import org.apache.commons.codec.digest.DigestUtils;

public class MessageGetStickerMeta implements IMessage {
    public boolean isLogo;
    public int stickerpackId;
    public int stickerId;
    public byte[] md5;
    static Random random = new Random();

    public void fromBytes(ByteBuf buf) {
        this.stickerpackId = buf.readInt();
        this.isLogo = buf.readBoolean();
        if (!this.isLogo) {
            this.stickerId = buf.readInt();
        }

        if (buf.readBoolean()) {
            this.md5 = new byte[16];
            buf.readBytes(this.md5, 0, 16);
        }

    }

    public void toBytes(ByteBuf buf) {
    }

    public static class Handler implements IMessageHandler<MessageGetStickerMeta, IMessage> {
        public IMessage onMessage(MessageGetStickerMeta message, MessageContext ctx) {
            StickersAddon.Stickerpack sp = StickersAddon.stickerpacks.get(message.stickerpackId);
            if (sp == null) {
                return null;
            } else {
                File s = sp.get(message.isLogo ? 0 : message.stickerId + 1);
                if (s == null) {
                    return null;
                } else {
                    if (message.md5 != null) {
                        try {
                            if (Arrays.equals(message.md5, DigestUtils.md5(new FileInputStream(s)))) {
                                return new MessageStickerMeta(message.stickerpackId, message.isLogo, message.stickerId, 0, 0);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    int size = (int) s.length();
                    int handle;
                    synchronized (StickersAddon.handles) {
                        do {
                            do {
                                handle = MessageGetStickerMeta.random.nextInt();
                            } while (handle == 0);
                        } while (StickersAddon.handles.containsKey(handle));

                        StickersAddon.StickerData sd = new StickersAddon.StickerData();
                        sd.size = size;

                        try {
                            sd.stream = new RandomAccessFile(s, "r");
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        StickersAddon.handles.put(handle, sd);
                    }

                    return new MessageStickerMeta(message.stickerpackId, message.isLogo, message.stickerId, handle, size);
                }
            }
        }
    }
}
