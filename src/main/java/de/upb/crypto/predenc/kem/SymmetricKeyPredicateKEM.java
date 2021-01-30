package de.upb.crypto.predenc.kem;


import de.upb.crypto.craco.common.predicate.CiphertextIndex;
import de.upb.crypto.craco.common.predicate.KeyIndex;
import de.upb.crypto.craco.enc.DecryptionKey;
import de.upb.crypto.craco.enc.EncryptionKey;
import de.upb.crypto.craco.enc.SymmetricKey;
import de.upb.crypto.craco.kem.KeyDerivationFunction;
import de.upb.crypto.craco.kem.KeyMaterial;
import de.upb.crypto.craco.kem.SymmetricKeyKEM;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.predenc.MasterSecret;
import de.upb.crypto.predenc.Predicate;

/**
 * A KEM that is implemented by the composition of a {@link PredicateKEM} providing {@link KeyMaterial} and a
 * {@link KeyDerivationFunction} that derives a {@link SymmetricKey} from the {@link KeyMaterial} produced by the KEM.
 * <p>
 * This should be used in combination with an symmetric encryption scheme to implement the standard hybrid encryption
 * technique.
 */
public class SymmetricKeyPredicateKEM extends SymmetricKeyKEM implements PredicateKEM<SymmetricKey> {

    public SymmetricKeyPredicateKEM(PredicateKEM<? extends KeyMaterial> kem,
                                    KeyDerivationFunction<? extends SymmetricKey> kdf) {
        super(kem, kdf);
    }

    public SymmetricKeyPredicateKEM(Representation repr) {
        super(repr);
    }

    @Override
    public MasterSecret getMasterSecret(Representation repr) {
        return ((PredicateKEM<? extends KeyMaterial>) kem).getMasterSecret(repr);
    }

    @Override
    public DecryptionKey generateDecryptionKey(MasterSecret msk, KeyIndex kind) {
        return ((PredicateKEM<? extends KeyMaterial>) kem).generateDecryptionKey(msk, kind);
    }

    @Override
    public EncryptionKey generateEncryptionKey(CiphertextIndex cind) {
        return ((PredicateKEM<? extends KeyMaterial>) kem).generateEncryptionKey(cind);
    }

    @Override
    public Predicate getPredicate() {
        return ((PredicateKEM<? extends KeyMaterial>) kem).getPredicate();
    }
}
