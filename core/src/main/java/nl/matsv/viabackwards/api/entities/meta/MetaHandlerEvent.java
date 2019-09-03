/*
 * Copyright (c) 2016 Matsv
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nl.matsv.viabackwards.api.entities.meta;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nl.matsv.viabackwards.api.entities.storage.EntityTracker;
import nl.matsv.viabackwards.api.entities.storage.MetaStorage;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public class MetaHandlerEvent {
    private final UserConnection user;
    private final EntityTracker.StoredEntity entity;
    private final int index;
    private final Metadata data;
    private final MetaStorage storage;
    private List<Metadata> extraData;

    public boolean hasData() {
        return data != null;
    }

    public Optional<Metadata> getMetaByIndex(int index) {
        for (Metadata meta : storage.getMetaDataList())
            if (index == meta.getId())
                return Optional.of(meta);
        return Optional.empty();
    }

    public void clearExtraData() {
        extraData = null;
    }

    public void createMeta(Metadata metadata) {
        (extraData != null ? extraData : (extraData = new ArrayList<>())).add(metadata);
    }
}
