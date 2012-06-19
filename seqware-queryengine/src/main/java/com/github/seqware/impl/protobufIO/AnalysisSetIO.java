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
package com.github.seqware.impl.protobufIO;


import com.github.seqware.dto.QESupporting.SGIDPB;
import com.github.seqware.dto.QueryEngine;
import com.github.seqware.dto.QueryEngine.AnalysisSetPB;
import com.github.seqware.factory.Factory;
import com.github.seqware.model.Analysis;
import com.github.seqware.model.AnalysisSet;
import com.github.seqware.model.impl.AtomImpl;
import com.github.seqware.model.impl.MoleculeImpl;
import com.github.seqware.model.impl.inMemory.InMemoryAnalysisSet;
import com.github.seqware.util.SGID;

/**
 *
 * @author dyuen
 */
public class AnalysisSetIO implements ProtobufTransferInterface<AnalysisSetPB, AnalysisSet>{

    @Override
    public AnalysisSet pb2m(AnalysisSetPB pb) {
        AnalysisSet.Builder builder = InMemoryAnalysisSet.newBuilder();
        builder = pb.hasName() ? builder.setName(pb.getName()) : builder;
        builder = pb.hasDescription() ? builder.setDescription(pb.getDescription()) : builder;
        AnalysisSet user = builder.build();
        UtilIO.handlePB2Atom(pb.getAtom(), (AtomImpl)user);
        UtilIO.handlePB2ACL(pb.getAcl(), (MoleculeImpl)user);
        if (pb.hasPrecedingVersion()){
           user.setPrecedingVersion(pb2m(pb.getPrecedingVersion()));
        }
        for(SGIDPB refID : pb.getAnalysisIDsList()){
            SGID sgid = SGIDIO.pb2m(refID);
            Analysis ref = (Analysis)Factory.getFeatureStoreInterface().getAtomBySGID(sgid);
            user.add(ref);
        }
        return user;
    }
    

    @Override
    public AnalysisSetPB m2pb(AnalysisSet aSet) {
        QueryEngine.AnalysisSetPB.Builder builder = QueryEngine.AnalysisSetPB.newBuilder();
        builder = aSet.getName() != null ? builder.setName(aSet.getName()) : builder;
        builder = aSet.getDescription() != null ? builder.setDescription(aSet.getDescription()) : builder;
        builder.setAtom(UtilIO.handleAtom2PB(builder.getAtom(), (AtomImpl)aSet));
        builder.setAcl(UtilIO.handleACL2PB(builder.getAcl(), (MoleculeImpl)aSet));
        if (aSet.getPrecedingVersion() != null){
            builder.setPrecedingVersion(m2pb(aSet.getPrecedingVersion()));
        }
        for(Analysis ref : aSet){
            builder.addAnalysisIDs(SGIDIO.m2pb(ref.getSGID()));
        }
        AnalysisSetPB userpb = builder.build();
        return userpb;
    }
}