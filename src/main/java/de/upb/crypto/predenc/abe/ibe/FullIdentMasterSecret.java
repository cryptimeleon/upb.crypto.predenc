package de.upb.crypto.predenc.abe.ibe;

import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.ReprUtil;
import de.upb.crypto.math.serialization.annotations.Represented;
import de.upb.crypto.math.structures.rings.zn.Zp;
import de.upb.crypto.math.structures.rings.zn.Zp.ZpElement;
import de.upb.crypto.predenc.MasterSecret;

import java.util.Objects;

/**
 * The {@link MasterSecret} for the {@link FullIdent} generated by
 * {@link FullIdentSetup}.
 */
public class FullIdentMasterSecret implements MasterSecret {


    // Uniformly random element in Z_{size(GroupG1)}*
    @Represented(restorer = "zp")
    private ZpElement s;

    public FullIdentMasterSecret(ZpElement s) {
        this.s = s;
    }

    public FullIdentMasterSecret(Representation repr, FullIdentPublicParameters pp) {
        Zp zp = new Zp(pp.getGroupG1().size());
        new ReprUtil(this).register(zp, "zp").deserialize(repr);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    public ZpElement getS() {
        return s;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((s == null) ? 0 : s.hashCode());
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
        FullIdentMasterSecret other = (FullIdentMasterSecret) obj;
        return Objects.equals(s, other.s);
    }

}
