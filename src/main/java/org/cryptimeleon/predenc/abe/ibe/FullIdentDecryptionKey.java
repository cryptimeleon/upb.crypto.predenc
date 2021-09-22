package org.cryptimeleon.predenc.abe.ibe;

import org.cryptimeleon.craco.common.predicate.KeyIndex;
import org.cryptimeleon.craco.enc.DecryptionKey;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.predenc.MasterSecret;

import java.util.Objects;

/**
 * A {@link DecryptionKey} for the {@link FullIdent}.
 * <p>
 * This key is generated by
 * {@link FullIdent#generateDecryptionKey(MasterSecret, KeyIndex)}.
 */
public class FullIdentDecryptionKey implements DecryptionKey {

    @Represented(restorer = "G1")
    private GroupElement d_id; //s * Q_id

    public FullIdentDecryptionKey(GroupElement d_id) {
        this.d_id = d_id;
    }

    public FullIdentDecryptionKey(Representation repr, FullIdentPublicParameters pp) {
        new ReprUtil(this).register(pp.getGroupG1(), "G1").deserialize(repr);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    public GroupElement getD_id() {
        return d_id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((d_id == null) ? 0 : d_id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FullIdentDecryptionKey other = (FullIdentDecryptionKey) obj;
        return Objects.equals(d_id, other.d_id);
    }
}
