package com.github.seqware.model.test;

import com.github.seqware.factory.Factory;
import com.github.seqware.factory.ModelManager;
import com.github.seqware.model.Feature;
import com.github.seqware.model.FeatureSet;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests of BackEndInterface.
 *
 * @author dyuen
 * @author jbaran
 */
public class FeatureStoreInterfaceTest {

    protected static FeatureSet aSet;
    protected static Feature a1, a2, a3;

    static {
        ModelManager mManager = Factory.getModelManager();
        aSet = mManager.buildFeatureSet().setReference(mManager.buildReference().setName("Dummy ref").build()).build();
        // create and store some features
        a1 = mManager.buildFeature().setStart(1000000).setStop(1000100).build();
        a2 = mManager.buildFeature().setStart(1000200).setStop(1000300).build();
        a3 = mManager.buildFeature().setStart(1000400).setStop(1000500).build();
        aSet.add(a1);
        aSet.add(a2);
        aSet.add(a3);
        mManager.flush();
    }

    @Test
    public void testFeatureCreationAndIterate() {
        // get FeatureSets from the back-end
        boolean b1 = false;
        boolean b2 = false;
        boolean b3 = false;
        for (FeatureSet fSet : Factory.getFeatureStoreInterface().getFeatureSets()) {
            for (Feature f : fSet) {
                if (f.equals(a1)) {
                    b1 = true;
                } else if (f.equals(a2)) {
                    b2 = true;
                } else if (f.equals(a3)) {
                    b3 = true;
                }
            }
        }

        Assert.assertTrue(b1 && b2 && b3);
    }
}