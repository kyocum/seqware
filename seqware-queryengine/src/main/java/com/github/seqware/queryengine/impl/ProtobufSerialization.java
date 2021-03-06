/*
 * Copyright (C) 2012 SeqWare
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.seqware.queryengine.impl;

import com.github.seqware.queryengine.backInterfaces.SerializationInterface;
import com.github.seqware.queryengine.impl.protobufIO.*;
import com.github.seqware.queryengine.model.*;
import com.github.seqware.queryengine.model.impl.AtomImpl;
import com.github.seqware.queryengine.model.impl.FeatureList;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.protobuf.Message;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * <p>ProtobufSerialization class.</p>
 *
 * @author dyuen
 * @version $Id: $Id
 */
public class ProtobufSerialization implements SerializationInterface {

    public final static BiMap<Class, ProtobufTransferInterface> biMap = new ImmutableBiMap.Builder<Class, ProtobufTransferInterface>().put(FeatureList.class, new FeatureListIO())
            .put(FeatureSet.class, new FeatureSetIO()).put(PluginRun.class, new PluginRunIO()).put(Plugin.class, new PluginIO())
            .put(Reference.class, new ReferenceIO()).put(ReferenceSet.class, new ReferenceSetIO()).put(Tag.class, new TagIO())
            .put(TagSet.class, new TagSetIO()).put(User.class, new UserIO()).put(Group.class, new GroupIO())
            .build();

    /** {@inheritDoc} */
    @Override
    public byte[] serialize(Atom atom) {
        Class cl = ((AtomImpl)atom).getHBaseClass();
        ProtobufTransferInterface pb = biMap.get(cl);
        Message m2pb = pb.m2pb((AtomImpl)atom);
        return Bytes.add(Bytes.toBytes(getSerializationConstant()), m2pb.toByteArray());
    }

    /** {@inheritDoc} */
    @Override
    public <T extends AtomImpl> T deserialize(byte[] bytes, Class<T> type) {
        int serialConstant = Bytes.toInt(Bytes.head(bytes, 4));
        if (serialConstant == getSerializationConstant()){
            ProtobufTransferInterface pb = biMap.get(type);
            T t = (T)pb.byteArr2m(Bytes.tail(bytes, bytes.length-4));
            return t;
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public int getSerializationConstant() {
        return 10000;
    }
}
