package nl.matsv.viabackwards.protocol.protocol1_13_2to1_14.packets;

import nl.matsv.viabackwards.ViaBackwards;
import nl.matsv.viabackwards.api.entities.meta.MetaHandler;
import nl.matsv.viabackwards.api.entities.storage.EntityData;
import nl.matsv.viabackwards.api.entities.storage.MetaStorage;
import nl.matsv.viabackwards.api.entities.types.AbstractEntityType;
import nl.matsv.viabackwards.api.entities.types.EntityType1_13;
import nl.matsv.viabackwards.api.entities.types.EntityType1_14;
import nl.matsv.viabackwards.api.exceptions.RemovedValueException;
import nl.matsv.viabackwards.api.rewriters.EntityRewriter;
import nl.matsv.viabackwards.protocol.protocol1_12_2to1_13.packets.BlockItemPackets1_13;
import nl.matsv.viabackwards.protocol.protocol1_13_2to1_14.Protocol1_13_2To1_14;
import nl.matsv.viabackwards.protocol.protocol1_13_2to1_14.data.EntityTypeMapping;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.api.minecraft.VillagerData;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.minecraft.metadata.MetaType;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_13_2;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.version.Types1_13_2;
import us.myles.ViaVersion.api.type.types.version.Types1_14;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.Particle;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;

import java.util.Optional;

public class EntityPackets1_14 extends EntityRewriter<Protocol1_13_2To1_14> {
    @Override
    protected void registerPackets(Protocol1_13_2To1_14 protocol) {
        // Spawn Object
        protocol.registerOutgoing(State.PLAY, 0x0, 0x0, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity id
                map(Type.UUID); // 1 - UUID
                map(Type.VAR_INT, Type.BYTE); // 2 - Type
                map(Type.DOUBLE); // 3 - X
                map(Type.DOUBLE); // 4 - Y
                map(Type.DOUBLE); // 5 - Z
                map(Type.BYTE); // 6 - Pitch
                map(Type.BYTE); // 7 - Yaw
                map(Type.INT); // 8 - Data

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        byte type = wrapper.get(Type.BYTE, 0);
                        EntityType1_14.EntityType entityType = EntityType1_14.getTypeFromId(type);
                        if (entityType == null) {
                            ViaBackwards.getPlatform().getLogger().warning("Could not find 1.14 entity type " + type);
                            return;
                        }
                        addTrackedEntity(
                                wrapper.user(),
                                wrapper.get(Type.VAR_INT, 0),
                                entityType
                        );
                    }
                });
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int id = wrapper.get(Type.BYTE, 0);
                        EntityType1_13.EntityType entityType = EntityType1_13.getTypeFromId(EntityTypeMapping.getOldId(id).orElse(id), false);
                        Optional<EntityType1_13.ObjectType> type;
                        if (entityType.isOrHasParent(EntityType1_13.EntityType.MINECART_ABSTRACT)) {
                            type = Optional.of(EntityType1_13.ObjectType.MINECART);
                            int data = 0;
                            switch (entityType) {
                                case CHEST_MINECART:
                                    data = 1;
                                    break;
                                case FURNACE_MINECART:
                                    data = 2;
                                    break;
                                case TNT_MINECART:
                                    data = 3;
                                    break;
                                case SPAWNER_MINECART:
                                    data = 4;
                                    break;
                                case HOPPER_MINECART:
                                    data = 5;
                                    break;
                                case COMMANDBLOCK_MINECART:
                                    data = 6;
                                    break;
                            }
                            if (data != 0)
                                wrapper.set(Type.INT, 0, data);
                        } else {
                            type = EntityType1_13.ObjectType.fromEntityType(entityType);
                        }

                        if (type.isPresent()) {
                            wrapper.set(Type.BYTE, 0, (byte) type.get().getId());
                        }
                        if (type.isPresent() && type.get() == EntityType1_13.ObjectType.FALLING_BLOCK) {
                            int blockState = wrapper.get(Type.INT, 0);
                            int combined = BlockItemPackets1_13.toOldId(blockState);
                            combined = ((combined >> 4) & 0xFFF) | ((combined & 0xF) << 12);
                            wrapper.set(Type.INT, 0, combined);
                        } else if (type.isPresent() && type.get() == EntityType1_13.ObjectType.ITEM_FRAME) {
                            int data = wrapper.get(Type.INT, 0);
                            switch (data) {
                                case 3:
                                    data = 0;
                                    break;
                                case 4:
                                    data = 1;
                                    break;
                                case 5:
                                    data = 3;
                                    break;
                            }
                            wrapper.set(Type.INT, 0, data);
                        }
                    }
                });
            }
        });

        // Spawn mob packet
        protocol.registerOutgoing(State.PLAY, 0x3, 0x3, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.UUID); // 1 - Entity UUID
                map(Type.VAR_INT); // 2 - Entity Type
                map(Type.DOUBLE); // 3 - X
                map(Type.DOUBLE); // 4 - Y
                map(Type.DOUBLE); // 5 - Z
                map(Type.BYTE); // 6 - Yaw
                map(Type.BYTE); // 7 - Pitch
                map(Type.BYTE); // 8 - Head Pitch
                map(Type.SHORT); // 9 - Velocity X
                map(Type.SHORT); // 10 - Velocity Y
                map(Type.SHORT); // 11 - Velocity Z
                map(Types1_14.METADATA_LIST, Types1_13_2.METADATA_LIST); // 12 - Metadata

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int type = wrapper.get(Type.VAR_INT, 1);
                        EntityType1_14.EntityType entityType = EntityType1_14.getTypeFromId(type);
                        addTrackedEntity(
                                wrapper.user(),
                                wrapper.get(Type.VAR_INT, 0),
                                entityType
                        );
                        Optional<Integer> oldId = EntityTypeMapping.getOldId(type);
                        if (!oldId.isPresent()) {
                            Optional<EntityData> oldType = getEntityData(entityType);
                            if (!oldType.isPresent()) {
                                ViaBackwards.getPlatform().getLogger().warning("Could not find 1.13.2 entity type for 1.14 entity type " + type + "/" + entityType);
                                wrapper.cancel();
                            } else {
                                wrapper.set(Type.VAR_INT, 1, oldType.get().getReplacementId());
                            }
                        } else {
                            wrapper.set(Type.VAR_INT, 1, oldId.get());
                        }
                    }
                });

                // Handle entity type & metadata
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int entityId = wrapper.get(Type.VAR_INT, 0);
                        AbstractEntityType type = getEntityType(wrapper.user(), entityId);

                        MetaStorage storage = new MetaStorage(wrapper.get(Types1_13_2.METADATA_LIST, 0));
                        handleMeta(
                                wrapper.user(),
                                wrapper.get(Type.VAR_INT, 0),
                                storage
                        );

                        Optional<EntityData> optEntDat = getEntityData(type);
                        if (optEntDat.isPresent()) {
                            EntityData data = optEntDat.get();

                            Optional<Integer> replacementId = EntityTypeMapping.getOldId(data.getReplacementId());
                            wrapper.set(Type.VAR_INT, 1, replacementId.orElse(EntityType1_13.EntityType.ZOMBIE.getId()));
                            if (data.hasBaseMeta())
                                data.getDefaultMeta().handle(storage);
                        }

                        // Rewrite Metadata
                        wrapper.set(
                                Types1_13_2.METADATA_LIST,
                                0,
                                storage.getMetaDataList()
                        );
                    }
                });
            }
        });

        // Spawn Experience Orb
        protocol.registerOutgoing(State.PLAY, 0x01, 0x01, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity id

                // Track entity
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        addTrackedEntity(
                                wrapper.user(),
                                wrapper.get(Type.VAR_INT, 0),
                                EntityType1_14.EntityType.XP_ORB
                        );
                    }
                });
            }
        });

        // Spawn painting
        protocol.registerOutgoing(State.PLAY, 0x04, 0x04, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT);
                map(Type.UUID);
                map(Type.VAR_INT);
                map(Type.POSITION1_14, Type.POSITION);
                map(Type.BYTE);

                // Track entity
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        addTrackedEntity(
                                wrapper.user(),
                                wrapper.get(Type.VAR_INT, 0),
                                EntityType1_14.EntityType.PAINTING
                        );
                    }
                });
            }
        });

        // Spawn player packet
        protocol.registerOutgoing(State.PLAY, 0x05, 0x05, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.UUID); // 1 - Player UUID
                map(Type.DOUBLE); // 2 - X
                map(Type.DOUBLE); // 3 - Y
                map(Type.DOUBLE); // 4 - Z
                map(Type.BYTE); // 5 - Yaw
                map(Type.BYTE); // 6 - Pitch
                map(Types1_14.METADATA_LIST, Types1_13_2.METADATA_LIST); // 7 - Metadata

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int entityId = wrapper.get(Type.VAR_INT, 0);

                        EntityType1_14.EntityType entType = EntityType1_14.EntityType.PLAYER;
                        // Register Type ID
                        addTrackedEntity(wrapper.user(), entityId, entType);
                        wrapper.set(Types1_13_2.METADATA_LIST, 0,
                                handleMeta(
                                        wrapper.user(),
                                        entityId,
                                        new MetaStorage(wrapper.get(Types1_13_2.METADATA_LIST, 0))
                                ).getMetaDataList()
                        );
                    }
                });
            }
        });

        // Destroy entities
        protocol.registerOutgoing(State.PLAY, 0x37, 0x35, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT_ARRAY); // 0 - Entity IDS

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        for (int entity : wrapper.get(Type.VAR_INT_ARRAY, 0))
                            getEntityTracker(wrapper.user()).removeEntity(entity);
                    }
                });
            }
        });

        // Metadata packet
        protocol.registerOutgoing(State.PLAY, 0x43, 0x3F, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Types1_14.METADATA_LIST, Types1_13_2.METADATA_LIST); // 1 - Metadata list
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int entityId = wrapper.get(Type.VAR_INT, 0);

                        wrapper.set(Types1_13_2.METADATA_LIST, 0,
                                handleMeta(
                                        wrapper.user(),
                                        entityId,
                                        new MetaStorage(wrapper.get(Types1_13_2.METADATA_LIST, 0))
                                ).getMetaDataList()
                        );
                    }
                });
            }
        });

        //join game
        protocol.registerOutgoing(State.PLAY, 0x25, 0x25, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT); // 0 - Entity ID
                map(Type.UNSIGNED_BYTE); // 1 - Gamemode
                map(Type.INT); // 2 - Dimension

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        // Store the player
                        ClientWorld clientChunks = wrapper.user().get(ClientWorld.class);
                        int dimensionId = wrapper.get(Type.INT, 1);
                        clientChunks.setEnvironment(dimensionId);

                        int entityId = wrapper.get(Type.INT, 0);

                        // Register Type ID
                        addTrackedEntity(wrapper.user(), entityId, EntityType1_14.EntityType.PLAYER);

                        wrapper.write(Type.UNSIGNED_BYTE, (short) 0);

                        wrapper.passthrough(Type.UNSIGNED_BYTE); // Max Players
                        wrapper.passthrough(Type.STRING); // Level Type
                        wrapper.read(Type.VAR_INT); //Read View Distance
                    }
                });
            }
        });
    }

    @Override
    protected void registerRewrites() {
        setDisplayNameJson(true);
        setDisplayNameMetaType(MetaType1_13_2.OptChat);

        regEntType(EntityType1_14.EntityType.CAT, EntityType1_14.EntityType.OCELOT).mobName("Cat");
        regEntType(EntityType1_14.EntityType.TRADER_LLAMA, EntityType1_14.EntityType.LLAMA).mobName("Trader Llama");
        regEntType(EntityType1_14.EntityType.FOX, EntityType1_14.EntityType.WOLF).mobName("Fox");
        regEntType(EntityType1_14.EntityType.PANDA, EntityType1_14.EntityType.POLAR_BEAR).mobName("Panda");
        regEntType(EntityType1_14.EntityType.PILLAGER, EntityType1_14.EntityType.VILLAGER).mobName("Pillager");
        regEntType(EntityType1_14.EntityType.WANDERING_TRADER, EntityType1_14.EntityType.VILLAGER).mobName("Wandering Trader");
        regEntType(EntityType1_14.EntityType.RAVAGER, EntityType1_14.EntityType.COW).mobName("Ravager");

        registerMetaHandler().handle(e -> {
            Metadata meta = e.getData();
            int typeId = meta.getMetaType().getTypeID();
            if (typeId <= 15) {
                meta.setMetaType(MetaType1_13_2.byId(typeId));
            }

            MetaType type = meta.getMetaType();

            if (type == MetaType1_13_2.Slot) {
                Item item = (Item) meta.getValue();
                meta.setValue(getProtocol().getBlockItemPackets().handleItemToClient(item));
            } else if (type == MetaType1_13_2.BlockID) {
                int blockstate = (Integer) meta.getValue();
                meta.setValue(getProtocol().getNewBlockStateId(blockstate));
            }

            return meta;
        });

        registerMetaHandler().filter(EntityType1_14.EntityType.PILLAGER, 15).removed();

        registerMetaHandler().filter(EntityType1_14.EntityType.FOX, 15).removed();
        registerMetaHandler().filter(EntityType1_14.EntityType.FOX, 16).removed();
        registerMetaHandler().filter(EntityType1_14.EntityType.FOX, 17).removed();
        registerMetaHandler().filter(EntityType1_14.EntityType.FOX, 18).removed();

        registerMetaHandler().filter(EntityType1_14.EntityType.PANDA, 15).removed();
        registerMetaHandler().filter(EntityType1_14.EntityType.PANDA, 16).removed();
        registerMetaHandler().filter(EntityType1_14.EntityType.PANDA, 17).removed();
        registerMetaHandler().filter(EntityType1_14.EntityType.PANDA, 18).removed();
        registerMetaHandler().filter(EntityType1_14.EntityType.PANDA, 19).removed();
        registerMetaHandler().filter(EntityType1_14.EntityType.PANDA, 20).removed();

        registerMetaHandler().filter(EntityType1_14.EntityType.CAT, 18).removed();
        registerMetaHandler().filter(EntityType1_14.EntityType.CAT, 19).removed();
        registerMetaHandler().filter(EntityType1_14.EntityType.CAT, 20).removed();

        registerMetaHandler().handle(e -> {
            AbstractEntityType type = e.getEntity().getType();
            Metadata meta = e.getData();
            if (type.isOrHasParent(EntityType1_14.EntityType.ABSTRACT_ILLAGER_BASE) || type == EntityType1_14.EntityType.RAVAGER || type == EntityType1_14.EntityType.WITCH) {
                int index = e.getIndex();
                if (index == 14) {
                    //TODO handle
                    throw new RemovedValueException();
                } else if (index > 14) {
                    meta.setId(index - 1);
                }
            }
            return meta;
        });

        registerMetaHandler().filter(EntityType1_14.EntityType.AREA_EFFECT_CLOUD, 10).handle(e -> {
            Metadata meta = e.getData();
            Particle particle = (Particle) meta.getValue();
            particle.setId(getOldParticleId(particle.getId()));
            return meta;
        });

        registerMetaHandler().filter(EntityType1_14.EntityType.FIREWORKS_ROCKET, 8).handle(e -> {
            Metadata meta = e.getData();
            meta.setMetaType(MetaType1_13_2.VarInt);
            Integer value = (Integer) meta.getValue();
            if (value == null) meta.setValue(0);
            return meta;
        });

        registerMetaHandler().filter(EntityType1_14.EntityType.ABSTRACT_ARROW, true).handle(e -> {
            Metadata meta = e.getData();
            int index = e.getIndex();
            if (index == 9) {
                throw new RemovedValueException();
            } else if (index > 9) {
                meta.setId(index - 1);
            }
            return meta;
        });

        MetaHandler villagerDataHandler = e -> {
            Metadata meta = e.getData();
            VillagerData villagerData = (VillagerData) meta.getValue();
            meta.setValue(villagerDataToProfession(villagerData));
            meta.setMetaType(MetaType1_13_2.VarInt);
            return meta;
        };

        registerMetaHandler().filter(EntityType1_14.EntityType.ZOMBIE_VILLAGER, 18).handle(villagerDataHandler);
        registerMetaHandler().filter(EntityType1_14.EntityType.VILLAGER, 15).handle(villagerDataHandler);

        registerMetaHandler().filter(EntityType1_14.EntityType.ZOMBIE, true).handle(e -> {
            Metadata meta = e.getData();
            int index = e.getIndex();
            if (index >= 16) {
                meta.setId(index + 1);
            }
            return meta;
        });

        // Remove bed location - todo send sleep packet
        registerMetaHandler().filter(EntityType1_14.EntityType.LIVINGENTITY, true).handle(e -> {
            Metadata meta = e.getData();
            int index = e.getIndex();
            if (index == 12) {
                Position position = (Position) meta.getValue();
                if(position != null){
                    try {
                        PacketWrapper wrapper = new PacketWrapper(0x33, null, e.getUser());
                        wrapper.write(Type.VAR_INT, e.getEntity().getEntityId());
                        wrapper.write(Type.POSITION, position);
                        wrapper.send(Protocol1_13_2To1_14.class);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }else{
                    try {
                        PacketWrapper wrapper = new PacketWrapper(0x6, null, e.getUser());
                        wrapper.write(Type.VAR_INT, e.getEntity().getEntityId());
                        wrapper.write(Type.UNSIGNED_BYTE, (short) 2);
                        wrapper.send(Protocol1_13_2To1_14.class);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                throw new RemovedValueException();
            } else if (index > 12) {
                meta.setId(index - 1);
            }
            return meta;
        });

        registerMetaHandler().handle(e -> {
            Metadata meta = e.getData();
            int index = e.getIndex();
            if (index == 6) {
                throw new RemovedValueException();
            } else if (index > 6) {
                meta.setId(index - 1);
            }
            return meta;
        });

        registerMetaHandler().handle(e -> {
            Metadata meta = e.getData();
            int typeId = meta.getMetaType().getTypeID();
            if (typeId > 15) {
                ViaBackwards.getPlatform().getLogger().warning("New 1.14 metadata was not handled: " + meta + " entity: " + e.getEntity().getType());
                return null;
            }
            return meta;
        });

        registerMetaHandler().filter(EntityType1_14.EntityType.OCELOT, 13).handle(e -> {
            Metadata meta = e.getData();
            meta.setId(15);
            meta.setMetaType(MetaType1_13_2.VarInt);
            meta.setValue(0);
            return meta;
        });

        registerMetaHandler().filter(EntityType1_14.EntityType.CAT).handle(e -> {
            Metadata meta = e.getData();
            if (meta.getId() == 15) {
                meta.setValue(1);
            } else if (meta.getId() == 13) {
                meta.setValue((byte) ((byte) meta.getValue() & 0x4));
            }
            return meta;
        });
    }

    public int villagerDataToProfession(VillagerData data) {
        switch (data.getProfession()) {
            case 1: // Armorer
            case 10: // Mason
            case 13: // Toolsmith
            case 14: // Weaponsmith
                return 3; // Blacksmith
            case 2: // Butcher
            case 8: // Leatherworker
                return 4; // Butcher
            case 3: // Cartographer
            case 9: // Librarian
                return 1; // Librarian
            case 4: // Cleric
                return 2; // Priest
            case 5: // Farmer
            case 6: // Fisherman
            case 7: // Fletcher
            case 12: // Shepherd
                return 0; // Farmer
            case 0: // None
            case 11: // Nitwit
            default:
                return 5; // Nitwit
        }
    }

    public static int getOldParticleId(int id) {
        if (id >= 45) {
            id -= 1; // new 39 -> 44
        }
        if (id >= 30) {
            id -= 1; // skip new short happy villager
        }
        if (id >= 28) {
            id -= 1; // new 24 -> 27
        }
        if (id >= 14) {
            id -= 1; // new water drip 11 -> 13
        }
        if (id >= 12) {
            id -= 2; // new lava drips 10, 11
        }
        return id;
    }
}
