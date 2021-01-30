package de.upb.crypto.predenc.enc.representation.params;

import de.upb.crypto.craco.common.plaintexts.GroupElementPlainText;
import de.upb.crypto.craco.common.plaintexts.PlainText;
import de.upb.crypto.craco.enc.CipherText;
import de.upb.crypto.craco.enc.DecryptionKey;
import de.upb.crypto.craco.enc.EncryptionKey;
import de.upb.crypto.craco.enc.representation.RepresentationTest;
import de.upb.crypto.craco.enc.representation.RepresentationTestParams;
import de.upb.crypto.predenc.abe.fuzzy.large.*;
import de.upb.crypto.predenc.enc.representation.PredEncRepresentationTestParams;
import de.upb.crypto.predenc.paramgens.IBEFuzzySW05TestParamGenerator;

import java.math.BigInteger;

/**
 * Parameters used in the {@link RepresentationSubTest} for the Fuzzy IBE scheme {@link IBEFuzzySW05}.
 */
public class IBEFuzzySW05Params {

    public static RepresentationTestParams getParams() {
        // setup Fuzzy environment with security parameter = 80, number of attributes = 6, threshold = 3
        IBEFuzzySW05Setup setup = new IBEFuzzySW05Setup();
        setup.doKeyGen(80, BigInteger.valueOf(6), BigInteger.valueOf(3), false, true);

        IBEFuzzySW05PublicParameters pp = setup.getPublicParameters();
        IBEFuzzySW05MasterSecret msk = setup.getMasterSecret();
        IBEFuzzySW05 fuzzy = new IBEFuzzySW05(pp);

        // generate identities to test
        Identity omegaPubKey = IBEFuzzySW05TestParamGenerator.generatePublicKeyIdentity();
        Identity omegaValidPrivKey = IBEFuzzySW05TestParamGenerator.generatePrivateKeyValidIdentity();

        // generate key pair corresponding to the identities generated before
        EncryptionKey publicKey = fuzzy.generateEncryptionKey(omegaPubKey);
        DecryptionKey validSecretKey = fuzzy.generateDecryptionKey(msk, omegaValidPrivKey);

        // encrypt random plaintext under the public key generated before
        PlainText plaintext = new GroupElementPlainText(pp.getGroupGT().getUniformlyRandomElement());
        CipherText ciphertext = fuzzy.encrypt(plaintext, publicKey);

        return new PredEncRepresentationTestParams(fuzzy, publicKey, validSecretKey, plaintext, ciphertext, msk);
    }
}
