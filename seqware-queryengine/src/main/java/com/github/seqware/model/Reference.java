package com.github.seqware.model;

import java.util.Iterator;
import java.util.UUID;

/**
 * A reference is a particular build version such as "hg19" or "hg18".
 *
 * @author dyuen
 * @author jbaran
 */
public abstract class Reference extends Molecule {

    /**
     * parent set
     */
    private ReferenceSet set;

    /**
     * Create a new reference
     */
    private Reference() {
        super();
    }

    /**
     * References are always created in association with an existing reference
     * set
     *
     * @param set the parent set for this reference
     */
    public Reference(ReferenceSet set) {
        this();
        this.set = set;
    }

    /**
     * Get the list of feature sets associated with this reference.
     *
     * @return Iterator of feature sets associated with this reference.
     */
    public abstract Iterator<FeatureSet> featureSets();
}
