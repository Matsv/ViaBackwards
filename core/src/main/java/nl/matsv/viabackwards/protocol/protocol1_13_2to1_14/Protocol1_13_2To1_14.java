package nl.matsv.viabackwards.protocol.protocol1_13_2to1_14;

import lombok.Getter;
import nl.matsv.viabackwards.ViaBackwards;
import nl.matsv.viabackwards.api.BackwardsProtocol;
import nl.matsv.viabackwards.api.entities.storage.EntityTracker;
import nl.matsv.viabackwards.protocol.protocol1_13_2to1_14.data.BackwardsMappings;
import nl.matsv.viabackwards.protocol.protocol1_13_2to1_14.data.SoundMapping;
import nl.matsv.viabackwards.protocol.protocol1_13_2to1_14.packets.BlockItemPackets1_14;
import nl.matsv.viabackwards.protocol.protocol1_13_2to1_14.packets.EntityPackets1_14;
import nl.matsv.viabackwards.protocol.protocol1_13_2to1_14.packets.PlayerPackets1_14;
import nl.matsv.viabackwards.protocol.protocol1_13_2to1_14.packets.SoundPackets1_14;
import nl.matsv.viabackwards.protocol.protocol1_13_2to1_14.storage.ChunkLightStorage;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;

@Getter
public class Protocol1_13_2To1_14 extends BackwardsProtocol {
    private BlockItemPackets1_14 blockItemPackets;
    private EntityPackets1_14 entityPackets;

    static {
        BackwardsMappings.init();
        SoundMapping.init();
    }

    @Override
    protected void registerPackets() {
        blockItemPackets = new BlockItemPackets1_14();
        blockItemPackets.register(this);
        entityPackets = new EntityPackets1_14();
        entityPackets.register(this);
        new PlayerPackets1_14().register(this);
        new SoundPackets1_14().register(this);

        registerOutgoing(State.PLAY, 0x15, 0x16);

        registerOutgoing(State.PLAY, 0x17, 0x18);

        registerOutgoing(State.PLAY, 0x18, 0x19);

        registerOutgoing(State.PLAY, 0x19, 0x1A);
        registerOutgoing(State.PLAY, 0x1A, 0x1B);
        registerOutgoing(State.PLAY, 0x1B, 0x1C);
        registerOutgoing(State.PLAY, 0x54, 0x1D);
        registerOutgoing(State.PLAY, 0x1E, 0x20);
        registerOutgoing(State.PLAY, 0x20, 0x21);

        registerOutgoing(State.PLAY, 0x2B, 0x27);

        registerOutgoing(State.PLAY, 0x2C, 0x2B);

        registerOutgoing(State.PLAY, 0x30, 0x2D);
        registerOutgoing(State.PLAY, 0x31, 0x2E);
        registerOutgoing(State.PLAY, 0x32, 0x2F);
        registerOutgoing(State.PLAY, 0x33, 0x30);
        registerOutgoing(State.PLAY, 0x34, 0x31);
        // Position and look
        registerOutgoing(State.PLAY, 0x35, 0x32);

        registerOutgoing(State.PLAY, 0x36, 0x34);

        registerOutgoing(State.PLAY, 0x38, 0x36);
        registerOutgoing(State.PLAY, 0x39, 0x37);

        registerOutgoing(State.PLAY, 0x3B, 0x39);
        registerOutgoing(State.PLAY, 0x3C, 0x3A);
        registerOutgoing(State.PLAY, 0x3D, 0x3B);
        registerOutgoing(State.PLAY, 0x3E, 0x3C);
        registerOutgoing(State.PLAY, 0x3F, 0x3D);
        registerOutgoing(State.PLAY, 0x42, 0x3E);

        registerOutgoing(State.PLAY, 0x44, 0x40);
        registerOutgoing(State.PLAY, 0x45, 0x41);

        registerOutgoing(State.PLAY, 0x47, 0x43);
        registerOutgoing(State.PLAY, 0x48, 0x44);
        registerOutgoing(State.PLAY, 0x49, 0x45);
        registerOutgoing(State.PLAY, 0x4A, 0x46);
        registerOutgoing(State.PLAY, 0x4B, 0x47);
        registerOutgoing(State.PLAY, 0x4C, 0x48);

        registerOutgoing(State.PLAY, 0x4E, 0x4A);
        registerOutgoing(State.PLAY, 0x4F, 0x4B);
        registerOutgoing(State.PLAY, 0x52, 0x4C);

        registerOutgoing(State.PLAY, 0x53, 0x4E); // c
        registerOutgoing(State.PLAY, 0x55, 0x4F); // c
        registerOutgoing(State.PLAY, 0x56, 0x50); // c

        //Update View Position
        registerOutgoing(State.PLAY, 0x40, -1, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper packetWrapper) throws Exception {
                        packetWrapper.cancel();
                    }
                });
            }
        });
        //Update View Distance
        registerOutgoing(State.PLAY, 0x41, -1, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper packetWrapper) throws Exception {
                        packetWrapper.cancel();
                    }
                });
            }
        });

        registerOutgoing(State.PLAY, 0x57, 0x51, new PacketRemapper() { // c
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        wrapper.passthrough(Type.BOOLEAN); // Reset/clear
                        int size = wrapper.passthrough(Type.VAR_INT); // Mapping size

                        for (int i = 0; i < size; i++) {
                            wrapper.passthrough(Type.STRING); // Identifier

                            // Parent
                            if (wrapper.passthrough(Type.BOOLEAN))
                                wrapper.passthrough(Type.STRING);

                            // Display data
                            if (wrapper.passthrough(Type.BOOLEAN)) {
                                wrapper.passthrough(Type.STRING); // Title
                                wrapper.passthrough(Type.STRING); // Description
                                blockItemPackets.handleItemToClient(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)); // Icon
                                wrapper.passthrough(Type.VAR_INT); // Frame type
                                int flags = wrapper.passthrough(Type.INT); // Flags
                                if ((flags & 1) != 0)
                                    wrapper.passthrough(Type.STRING); // Background texture
                                wrapper.passthrough(Type.FLOAT); // X
                                wrapper.passthrough(Type.FLOAT); // Y
                            }

                            wrapper.passthrough(Type.STRING_ARRAY); // Criteria

                            int arrayLength = wrapper.passthrough(Type.VAR_INT);
                            for (int array = 0; array < arrayLength; array++) {
                                wrapper.passthrough(Type.STRING_ARRAY); // String array
                            }
                        }
                    }
                });
            }
        });

        registerOutgoing(State.PLAY, 0x58, 0x52); // c
        registerOutgoing(State.PLAY, 0x59, 0x53); // c

        // tags
        registerOutgoing(State.PLAY, 0x5B, 0x55, new PacketRemapper() {
            @Override
            public void registerMap() { // c
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int blockTagsSize = wrapper.read(Type.VAR_INT);
                        wrapper.write(Type.VAR_INT, blockTagsSize); // block tags
                        for (int i = 0; i < blockTagsSize; i++) {
                            wrapper.passthrough(Type.STRING);
                            Integer[] blockIds = wrapper.passthrough(Type.VAR_INT_ARRAY);
                            for (int j = 0; j < blockIds.length; j++) {
                                blockIds[j] = getNewBlockStateId(blockIds[j]);
                            }
                        }
                        int itemTagsSize = wrapper.read(Type.VAR_INT);
                        wrapper.write(Type.VAR_INT, itemTagsSize); // item tags
                        for (int i = 0; i < itemTagsSize; i++) {
                            wrapper.passthrough(Type.STRING);
                            Integer[] itemIds = wrapper.passthrough(Type.VAR_INT_ARRAY);
                            for (int j = 0; j < itemIds.length; j++) {
                                itemIds[j] = /*BlockItemPackets1_14.getNewItemId TODO BLOCK IDS*/(itemIds[j]);
                            }
                        }
                        int fluidTagsSize = wrapper.passthrough(Type.VAR_INT); // fluid tags
                        for (int i = 0; i < fluidTagsSize; i++) {
                            wrapper.passthrough(Type.STRING);
                            wrapper.passthrough(Type.VAR_INT_ARRAY);
                        }
                        // Eat entity tags
                        int entityTagsSize = wrapper.read(Type.VAR_INT);
                        for (int i = 0; i < entityTagsSize; i++) {
                            wrapper.read(Type.STRING);
                            wrapper.read(Type.VAR_INT_ARRAY);
                        }
                    }
                });
            }
        });


        // Light update
        out(State.PLAY, 0x24, -1, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int x = wrapper.read(Type.VAR_INT);
                        int z = wrapper.read(Type.VAR_INT);
                        int skyLightMask = wrapper.read(Type.VAR_INT);
                        int blockLightMask = wrapper.read(Type.VAR_INT);
                        int emptySkyLightMask = wrapper.read(Type.VAR_INT);
                        int emptyBlockLightMask = wrapper.read(Type.VAR_INT);

                        byte[][] skyLight = new byte[16][];
                        // we don't need void and +256 light
                        if (isSet(skyLightMask, 0)) {
                            wrapper.read(Type.BYTE_ARRAY);
                        }
                        for (int i = 0; i < 16; i++) {
                            if (isSet(skyLightMask, i + 1)) {
                                Byte[] array = wrapper.read(Type.BYTE_ARRAY);
                                skyLight[i] = new byte[array.length];
                                for (int j = 0; j < array.length; j++) {
                                    skyLight[i][j] = array[j];
                                }
                            } else if (isSet(emptySkyLightMask, i + 1)) {
                                skyLight[i] = ChunkLightStorage.EMPTY_LIGHT;
                            }
                        }
                        if (isSet(skyLightMask, 17)) {
                            wrapper.read(Type.BYTE_ARRAY);
                        }

                        byte[][] blockLight = new byte[16][];
                        if (isSet(blockLightMask, 0)) {
                            wrapper.read(Type.BYTE_ARRAY);
                        }
                        for (int i = 0; i < 16; i++) {
                            if (isSet(blockLightMask, i + 1)) {
                                Byte[] array = wrapper.read(Type.BYTE_ARRAY);
                                blockLight[i] = new byte[array.length];
                                for (int j = 0; j < array.length; j++) {
                                    blockLight[i][j] = array[j];
                                }
                            } else if (isSet(emptyBlockLightMask, i + 1)) {
                                blockLight[i] = ChunkLightStorage.EMPTY_LIGHT;
                            }
                        }
                        if (isSet(blockLightMask, 17)) {
                            wrapper.read(Type.BYTE_ARRAY);
                        }

                        wrapper.user().get(ChunkLightStorage.class).setStoredLight(skyLight, blockLight, x, z);
                        wrapper.cancel();
                    }

                    private boolean isSet(int mask, int i) {
                        return (mask & (1 << i)) != 0;
                    }
                });
            }
        });


        //Incomming

        //Unknown packet added in 19w11a - 0x02
        registerIncoming(State.PLAY, 0x03, 0x02); // r
        registerIncoming(State.PLAY, 0x04, 0x03); // r
        registerIncoming(State.PLAY, 0x05, 0x04); // r
        registerIncoming(State.PLAY, 0x06, 0x05); // r
        registerIncoming(State.PLAY, 0x07, 0x06); // r
        registerIncoming(State.PLAY, 0x08, 0x07); // r

        registerIncoming(State.PLAY, 0x0A, 0x09); // r
        registerIncoming(State.PLAY, 0x0B, 0x0A); // r

        registerIncoming(State.PLAY, 0x0D, 0x0C); // r
        registerIncoming(State.PLAY, 0x0E, 0x0D); // r
        //Unknown packet added in 19w11a - 0x0F
        registerIncoming(State.PLAY, 0x0F, 0x0E); // r
        registerIncoming(State.PLAY, 0x11, 0x10); // r
        registerIncoming(State.PLAY, 0x12, 0x11); // r
        registerIncoming(State.PLAY, 0x13, 0x12); // r
        registerIncoming(State.PLAY, 0x14, 0x0F); // r
        registerIncoming(State.PLAY, 0x15, 0x13); // r
        registerIncoming(State.PLAY, 0x16, 0x14); // r
        registerIncoming(State.PLAY, 0x17, 0x15); // r
        registerIncoming(State.PLAY, 0x18, 0x16); // r
        registerIncoming(State.PLAY, 0x19, 0x17); // r

        registerIncoming(State.PLAY, 0x1B, 0x19); // r
        registerIncoming(State.PLAY, 0x1C, 0x1A); // r

        registerIncoming(State.PLAY, 0x1E, 0x1C); // r
        registerIncoming(State.PLAY, 0x1F, 0x1D); // r
        registerIncoming(State.PLAY, 0x20, 0x1E); // r
        registerIncoming(State.PLAY, 0x21, 0x1F); // r
        registerIncoming(State.PLAY, 0x22, 0x20); // r
        registerIncoming(State.PLAY, 0x23, 0x21); // r

        registerIncoming(State.PLAY, 0x25, 0x23); // r

//        registerIncoming(State.PLAY, 0x29, 0x27); // r
        registerIncoming(State.PLAY, 0x2A, 0x27); // r
        registerIncoming(State.PLAY, 0x2B, 0x28); // r
        registerIncoming(State.PLAY, 0x2D, 0x2A); // r
    }

    public static int getNewBlockStateId(int id) {
        int newId = BackwardsMappings.blockStateMappings.getNewBlock(id);
        if (newId == -1) {
            ViaBackwards.getPlatform().getLogger().warning("Missing 1.14 blockstate id for 1.13.2 block " + id);
            return 0;
        }
        return newId;
    }


    public static int getNewBlockId(int id) {
        int newId = BackwardsMappings.blockMappings.getNewBlock(id);
        if (newId == -1) {
            ViaBackwards.getPlatform().getLogger().warning("Missing 1.14 block id for 1.13.2 block " + id);
            return id;
        }
        return newId;
    }


    @Override
    public void init(UserConnection user) {
        // Register ClientWorld
        if (!user.has(ClientWorld.class))
            user.put(new ClientWorld(user));

        // Register EntityTracker if it doesn't exist yet.
        if (!user.has(EntityTracker.class))
            user.put(new EntityTracker(user));

        // Init protocol in EntityTracker
        user.get(EntityTracker.class).initProtocol(this);

        if (!user.has(ChunkLightStorage.class))
            user.put(new ChunkLightStorage(user));
    }
}
