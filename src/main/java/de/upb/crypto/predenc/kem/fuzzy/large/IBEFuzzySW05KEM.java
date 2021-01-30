package de.upb.crypto.predenc.kem.fuzzy.large;

import de.upb.crypto.craco.common.attributes.BigIntegerAttribute;
import de.upb.crypto.craco.common.plaintexts.PlainText;
import de.upb.crypto.craco.common.predicate.CiphertextIndex;
import de.upb.crypto.craco.common.predicate.KeyIndex;
import de.upb.crypto.craco.enc.CipherText;
import de.upb.crypto.craco.enc.DecryptionKey;
import de.upb.crypto.craco.enc.EncryptionKey;
import de.upb.crypto.craco.kem.KeyEncapsulationMechanism.KeyAndCiphertext;
import de.upb.crypto.craco.kem.KeyMaterial;
import de.upb.crypto.craco.kem.UniqueByteKeyMaterial;
import de.upb.crypto.craco.kem.UnqualifiedKeyException;
import de.upb.crypto.math.structures.groups.GroupElement;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.rings.zn.Zp;
import de.upb.crypto.predenc.MasterSecret;
import de.upb.crypto.predenc.abe.fuzzy.large.*;
import de.upb.crypto.predenc.kem.PredicateKEM;
import de.upb.crypto.predenc.kem.SymmetricKeyPredicateKEM;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A KEM based on the Fuzzy IBE large universe construction ({@link IBEFuzzySW05}).
 * <p>
 * For the basic idea of the construction consider a {@link IBEIBEFuzzySW05SW05CipherText} (\omega', E', E'', (E_i)_{
 * i}) of
 * {@link IBEFuzzySW05}, where E' = m * Y^s for some message m, random number s and Y defined in the
 * scheme's setup {@link IBEFuzzySW05Setup}. The KEM now outputs E' dropping factor m as a key, i.e. Y^s, and
 * (\omega', E'',
 * (E_i)_{i}) as its encapsulation ({@link IBEFuzzySW05KEMCipherText}). The decryption of {@link IBEFuzzySW05}
 * just recovers Y^s from (\omega', E'', (E_i)_{i}) and computes E' / Y^s to obtain m. In the same way we can use this
 * to decapsulate the key Y^s.
 * <p>
 * This scheme only supplies {@link KeyMaterial}. It needs to be used in combination with a KDF to obtain a symmetric
 * key. For this, see {@link SymmetricKeyPredicateKEM}.
 */
public class IBEFuzzySW05KEM extends AbstractIBEFuzzySW05 implements PredicateKEM<KeyMaterial> {

    public IBEFuzzySW05KEM(IBEFuzzySW05PublicParameters pp) {
        this.pp = pp;
        this.zp = new Zp(pp.getGroupG1().size());
    }

    public IBEFuzzySW05KEM(Representation repr) {
        this.pp = new IBEFuzzySW05PublicParameters(repr);
        this.zp = new Zp(pp.getGroupG1().size());
    }

    /**
     * Essentially {@link IBEFuzzySW05#encrypt(PlainText, EncryptionKey)} but instead of encrypting some
     * {@link PlainText} m, it outputs the first component of the ciphertext
     * ({@link IBEIBEFuzzySW05SW05CipherText#getEPrime()} ) dropping
     * the factor m as a key along with the second ({@link IBEFuzzySW05KEMCipherText#eTwoPrime}) and third
     * ({@link IBEFuzzySW05KEMCipherText#eElementMap}) component as the encapsulation of this key.
     *
     * @param publicKey {@link IBEFuzzySW05EncryptionKey } created by
     *                  {@link AbstractIBEFuzzySW05#generateEncryptionKey(CiphertextIndex)}
     * @return {@link KeyMaterial} key = Y^s and its encapsulation ({@link IBEFuzzySW05KEMCipherText })
     */
    @Override
    public KeyAndCiphertext<KeyMaterial> encaps(EncryptionKey publicKey) {
        if (!(publicKey instanceof IBEFuzzySW05EncryptionKey))
            throw new IllegalArgumentException("Not a valid public key for this scheme");

        IBEFuzzySW05EncryptionKey pk = (IBEFuzzySW05EncryptionKey) publicKey;

        Zp.ZpElement s = zp.getUniformlyRandomUnit();
        // Y^s = e (g1, g2)^s, for efficiency exponentiation pulled in group G_1
        GroupElement yToTheS = pp.getE().apply(pp.getG1().pow(s), pp.getG2());

        Identity omegaPrime = pk.getIdentity();
        // E'' = g^s
        GroupElement eTwoPrime = pp.getG().pow(s);
        Map<BigInteger, GroupElement> eElementMap = computeE(omegaPrime, s);

        KeyAndCiphertext<KeyMaterial> output = new KeyAndCiphertext<>();
        // key = Y^s
        output.key = new UniqueByteKeyMaterial(yToTheS, pp.getGroupGT().size().intValue());
        output.encapsulatedKey = new IBEFuzzySW05KEMCipherText(omegaPrime, eTwoPrime, eElementMap);

        return output;
    }

    /**
     * @param encapsulatedKey encapsulation of a {@link KeyMaterial} ({@link IBEFuzzySW05KEMCipherText})
     * @param privateKey      {@link IBEFuzzySW05DecryptionKey} generated by
     *                        {@link AbstractIBEFuzzySW05#generateDecryptionKey(MasterSecret, KeyIndex)}
     * @return the key encapsulated by {@code encapsulatedKey}
     */
    @Override
    public KeyMaterial decaps(CipherText encapsulatedKey, DecryptionKey privateKey) {
        if (!(encapsulatedKey instanceof IBEFuzzySW05KEMCipherText))
            throw new IllegalArgumentException("Invalid cipher text for this scheme.");
        if (!(privateKey instanceof IBEFuzzySW05DecryptionKey))
            throw new IllegalArgumentException("Invalid private key for this scheme");

        IBEFuzzySW05KEMCipherText ct = (IBEFuzzySW05KEMCipherText) encapsulatedKey;
        IBEFuzzySW05DecryptionKey sk = (IBEFuzzySW05DecryptionKey) privateKey;

        Identity omega = sk.getIdentity();

        Map<BigInteger, GroupElement> rElementMap = sk.getRElementMap();
        Map<BigInteger, GroupElement> dElementMap = sk.getDElementMap();

        // get the intersection of omega and omegaPrime
        Set<BigIntegerAttribute> intersection = new HashSet<>(omega.getAttributes());
        intersection.retainAll(ct.getOmegaPrime().getAttributes());

        if (intersection.size() < pp.getIdentityThresholdD().intValue()) {
            throw new UnqualifiedKeyException("Not enough intersection, therefore decryption failed");
        }

        Set<BigIntegerAttribute> attributeSet = subset(intersection, pp.getIdentityThresholdD().intValue());

        return new UniqueByteKeyMaterial(restoreYs(ct, dElementMap, rElementMap, attributeSet),
                pp.getGroupGT().size().intValue());
    }

    @Override
    public CipherText getEncapsulatedKey(Representation repr) {
        return new IBEFuzzySW05KEMCipherText(repr, pp);
    }

    @Override
    public EncryptionKey getEncapsulationKey(Representation repr) {
        return new IBEFuzzySW05EncryptionKey(repr);
    }

    @Override
    public DecryptionKey getDecapsulationKey(Representation repr) {
        return new IBEFuzzySW05DecryptionKey(repr, pp);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof IBEFuzzySW05KEM)) {
            return false;
        }
        IBEFuzzySW05KEM other = (IBEFuzzySW05KEM) o;

        return super.equals(other);
    }
}
