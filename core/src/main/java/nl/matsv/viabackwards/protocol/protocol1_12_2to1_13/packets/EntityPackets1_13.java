package nl.matsv.viabackwards.protocol.protocol1_12_2to1_13.packets;

import nl.matsv.viabackwards.ViaBackwards;
import nl.matsv.viabackwards.api.exceptions.RemovedValueException;
import nl.matsv.viabackwards.api.rewriters.EntityRewriter;
import nl.matsv.viabackwards.protocol.protocol1_12_2to1_13.Protocol1_12_2To1_13;
import nl.matsv.viabackwards.protocol.protocol1_12_2to1_13.data.EntityTypeMapping;
import nl.matsv.viabackwards.protocol.protocol1_12_2to1_13.data.PaintingMapping;
import nl.matsv.viabackwards.protocol.protocol1_12_2to1_13.data.ParticleMapping;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.entities.Entity1_13Types;
import us.myles.ViaVersion.api.entities.EntityType;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_12;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.version.Types1_12;
import us.myles.ViaVersion.api.type.types.version.Types1_13;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.ChatRewriter;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.Particle;

import java.util.Optional;

public class EntityPackets1_13 extends EntityRewriter<Protocol1_12_2To1_13> {

    @Override
    protected void registerPackets(Protocol1_12_2To1_13 protocol) {

        //Spawn Object
        protocol.out(State.PLAY, 0x00, 0x00, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT);
                map(Type.UUID);
                map(Type.BYTE);
                map(Type.DOUBLE);
                map(Type.DOUBLE);
                map(Type.DOUBLE);
                map(Type.BYTE);
                map(Type.BYTE);
                map(Type.INT);

                handler(getObjectTrackerHandler());

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        Optional<Entity1_13Types.ObjectType> optionalType = Entity1_13Types.ObjectType.findById(wrapper.get(Type.BYTE, 0));
                        if (!optionalType.isPresent()) return;

                        final Entity1_13Types.ObjectType type = optionalType.get();
                        if (type == Entity1_13Types.ObjectType.FALLING_BLOCK) {
                            int blockState = wrapper.get(Type.INT, 0);
                            int combined = BlockItemPackets1_13.toOldId(blockState);
                            combined = ((combined >> 4) & 0xFFF) | ((combined & 0xF) << 12);
                            wrapper.set(Type.INT, 0, combined);
                        } else if (type == Entity1_13Types.ObjectType.ITEM_FRAME) {
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
                        } else if (type == Entity1_13Types.ObjectType.TRIDENT) {
                            wrapper.set(Type.BYTE, 0, (byte) Entity1_13Types.ObjectType.TIPPED_ARROW.getId());
                        }
                    }
                });
            }
        });

        // Spawn Experience Orb
        registerExtraTracker(0x01, Entity1_13Types.EntityType.XP_ORB);

        // Spawn Global Entity
        registerExtraTracker(0x02, Entity1_13Types.EntityType.LIGHTNING_BOLT);

        //Spawn Mob
        protocol.out(State.PLAY, 0x03, 0x03, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT);
                map(Type.UUID);
                map(Type.VAR_INT);
                map(Type.DOUBLE);
                map(Type.DOUBLE);
                map(Type.DOUBLE);
                map(Type.BYTE);
                map(Type.BYTE);
                map(Type.BYTE);
                map(Type.SHORT);
                map(Type.SHORT);
                map(Type.SHORT);
                map(Types1_13.METADATA_LIST, Types1_12.METADATA_LIST);

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int type = wrapper.get(Type.VAR_INT, 1);
                        EntityType entityType = Entity1_13Types.getTypeFromId(type, false);
                        addTrackedEntity(wrapper, wrapper.get(Type.VAR_INT, 0), entityType);

                        Optional<Integer> oldId = EntityTypeMapping.getOldId(type);
                        if (!oldId.isPresent()) {
                            if (!hasData(entityType))
                                ViaBackwards.getPlatform().getLogger().warning("Could not find 1.12 entity type for 1.13 entity type " + type + "/" + entityType);
                        } else {
                            wrapper.set(Type.VAR_INT, 1, oldId.get());
                        }
                    }
                });

                // Rewrite entity type / metadata
                handler(getMobSpawnRewriter(Types1_12.METADATA_LIST));
            }
        });

        // Spawn Player
        protocol.out(State.PLAY, 0x05, 0x05, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT);
                map(Type.UUID);
                map(Type.DOUBLE);
                map(Type.DOUBLE);
                map(Type.DOUBLE);
                map(Type.BYTE);
                map(Type.BYTE);
                map(Types1_13.METADATA_LIST, Types1_12.METADATA_LIST);

                handler(getTrackerAndMetaHandler(Types1_12.METADATA_LIST, Entity1_13Types.EntityType.PLAYER));
            }
        });

        // Spawn Painting
        protocol.out(State.PLAY, 0x04, 0x04, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT);
                map(Type.UUID);

                handler(getTrackerHandler(Entity1_13Types.EntityType.PAINTING, Type.VAR_INT));
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int motive = wrapper.read(Type.VAR_INT);
                        String title = PaintingMapping.getStringId(motive);
                        wrapper.write(Type.STRING, title);
                    }
                });
            }
        });

        // Join game
        protocol.out(State.PLAY, 0x25, 0x23, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT); // 0 - Entity ID
                map(Type.UNSIGNED_BYTE); // 1 - Gamemode
                map(Type.INT); // 2 - Dimension

                handler(getTrackerHandler(Entity1_13Types.EntityType.PLAYER, Type.INT));
                handler(getDimensionHandler(1));
            }
        });

        // Respawn Packet (save dimension id)
        protocol.registerOutgoing(State.PLAY, 0x38, 0x35, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT); // 0 - Dimension ID

                handler(getDimensionHandler(0));
            }
        });

        // Destroy Entities Packet
        registerEntityDestroy(0x35, 0x32);

        // Entity Metadata packet
        registerMetadataRewriter(0x3F, 0x3C, Types1_13.METADATA_LIST, Types1_12.METADATA_LIST);
    }

    @Override
    protected void registerRewrites() {
        // Rewrite new Entity 'drowned'
        regEntType(Entity1_13Types.EntityType.DROWNED, Entity1_13Types.EntityType.ZOMBIE_VILLAGER).mobName("Drowned");

        // Fishy
        regEntType(Entity1_13Types.EntityType.COD_MOB, Entity1_13Types.EntityType.SQUID).mobName("Cod");
        regEntType(Entity1_13Types.EntityType.SALMON_MOB, Entity1_13Types.EntityType.SQUID).mobName("Salmon");
        regEntType(Entity1_13Types.EntityType.PUFFER_FISH, Entity1_13Types.EntityType.SQUID).mobName("Puffer Fish");
        regEntType(Entity1_13Types.EntityType.TROPICAL_FISH, Entity1_13Types.EntityType.SQUID).mobName("Tropical Fish");

        // Phantom
        regEntType(Entity1_13Types.EntityType.PHANTOM, Entity1_13Types.EntityType.PARROT).mobName("Phantom").spawnMetadata(storage -> {
            // The phantom is grey/blue so let's do yellow/blue
            storage.add(new Metadata(15, MetaType1_12.VarInt, 3));
        });

        // Dolphin
        regEntType(Entity1_13Types.EntityType.DOLPHIN, Entity1_13Types.EntityType.SQUID).mobName("Dolphin");

        // Turtle
        regEntType(Entity1_13Types.EntityType.TURTLE, Entity1_13Types.EntityType.OCELOT).mobName("Turtle");

        // Rewrite Meta types
        registerMetaHandler().handle(e -> {
            Metadata meta = e.getData();
            int typeId = meta.getMetaType().getTypeID();

            // Rewrite optional chat to chat
            if (typeId == 5) {
                meta.setMetaType(MetaType1_12.String);

                if (meta.getValue() == null) {
                    meta.setValue("");
                }
            }

            // Rewrite items
            else if (typeId == 6) {
                meta.setMetaType(MetaType1_12.Slot);
                Item item = (Item) meta.getValue();
                meta.setValue(getProtocol().getBlockItemPackets().handleItemToClient(item));
            }

            // Discontinue particles
            else if (typeId == 15) {
                meta.setMetaType(MetaType1_12.Discontinued);
            }

            // Rewrite to 1.12 ids
            else if (typeId > 5) {
                meta.setMetaType(MetaType1_12.byId(
                        typeId - 1
                ));
            }

            return meta;
        });

        // Rewrite Custom Name from Chat to String
        registerMetaHandler().filter(Entity1_13Types.EntityType.ENTITY, true, 2).handle(e -> {
            Metadata meta = e.getData();
            String value = (String) meta.getValue();
            if (value.isEmpty()) return meta;
            meta.setValue(ChatRewriter.jsonTextToLegacy(value));
            return meta;
        });

        // Handle zombie metadata
        registerMetaHandler().filter(Entity1_13Types.EntityType.ZOMBIE, true, 15).removed();
        registerMetaHandler().filter(Entity1_13Types.EntityType.ZOMBIE, true).handle(e -> {
            Metadata meta = e.getData();

            if (meta.getId() > 15) {
                meta.setId(meta.getId() - 1);
            }

            return meta;
        });

        // Handle turtle metadata (Remove them all for now)
        registerMetaHandler().filter(Entity1_13Types.EntityType.TURTLE, 13).removed(); // Home pos
        registerMetaHandler().filter(Entity1_13Types.EntityType.TURTLE, 14).removed(); // Has egg
        registerMetaHandler().filter(Entity1_13Types.EntityType.TURTLE, 15).removed(); // Laying egg
        registerMetaHandler().filter(Entity1_13Types.EntityType.TURTLE, 16).removed(); // Travel pos
        registerMetaHandler().filter(Entity1_13Types.EntityType.TURTLE, 17).removed(); // Going home
        registerMetaHandler().filter(Entity1_13Types.EntityType.TURTLE, 18).removed(); // Traveling

        // Remove additional fish meta
        registerMetaHandler().filter(Entity1_13Types.EntityType.ABSTRACT_FISHES, true, 12).removed();
        registerMetaHandler().filter(Entity1_13Types.EntityType.ABSTRACT_FISHES, true, 13).removed();

        // Remove phantom size
        registerMetaHandler().filter(Entity1_13Types.EntityType.PHANTOM, 12).removed();

        // Remove boat splash timer
        registerMetaHandler().filter(Entity1_13Types.EntityType.BOAT, 12).removed();

        // Remove Trident special loyalty level
        registerMetaHandler().filter(Entity1_13Types.EntityType.TRIDENT, 7).removed();

        // Handle new wolf colors
        registerMetaHandler().filter(Entity1_13Types.EntityType.WOLF, 17).handle(e -> {
            Metadata meta = e.getData();

            meta.setValue(15 - (int) meta.getValue());

            return meta;
        });

        // Rewrite AreaEffectCloud
        registerMetaHandler().filter(Entity1_13Types.EntityType.AREA_EFFECT_CLOUD, 9).handle(e -> {
            Metadata meta = e.getData();
            Particle particle = (Particle) meta.getValue();

            ParticleMapping.ParticleData data = ParticleMapping.getMapping(particle.getId());
            e.createMeta(new Metadata(9, MetaType1_12.VarInt, data.getHistoryId()));
            e.createMeta(new Metadata(10, MetaType1_12.VarInt, 0)); //TODO particle data
            e.createMeta(new Metadata(11, MetaType1_12.VarInt, 0)); //TODO particle data

            throw RemovedValueException.EX;
        });

        // TODO REWRITE BLOCKS IN MINECART
    }

    @Override
    protected EntityType getTypeFromId(int typeId) {
        return Entity1_13Types.getTypeFromId(typeId, false);
    }

    @Override
    protected EntityType getObjectTypeFromId(final int typeId) {
        return Entity1_13Types.getTypeFromId(typeId, true);
    }

    @Override
    protected int getOldEntityId(final int newId) {
        return EntityTypeMapping.getOldId(newId).orElse(newId);
    }
}
