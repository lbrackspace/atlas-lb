package org.openstack.atlas.api.validation.verifiers;

import org.openstack.atlas.docs.loadbalancers.api.v1.Meta;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DuplicateMetaVerifier implements Verifier<List<Meta>> {

    public VerifierResult verify(List<Meta> metaList) {
        Set<String> keys = new HashSet<String>();

        if(metaList == null) {
            return new VerifierResult(true); // Don't flag the user for duplicate metaList when the metaList must not be empty verifier already flagged the user.
        }

        for (Meta meta : metaList) {
            if (!keys.add(meta.getKey())) {
                return new VerifierResult(false);
            }
        }

        return new VerifierResult(true);
    }
}
