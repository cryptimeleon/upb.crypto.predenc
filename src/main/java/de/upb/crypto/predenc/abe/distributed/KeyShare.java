package de.upb.crypto.predenc.abe.distributed;

import de.upb.crypto.craco.common.predicate.KeyIndex;
import de.upb.crypto.math.serialization.Representable;

/**
 * Share of a decryption key.
 * Can be used to recreate the key if enough are available.
 */
public interface KeyShare extends Representable {

    public int getServerID();

    public KeyIndex getKeyIndex();
}
