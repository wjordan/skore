package util;

import net.tetromi.secure.SecureMessage;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.StreamCipher;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.engines.RC4Engine;
import org.bouncycastle.crypto.params.KeyParameter;

import java.io.IOException;
import java.util.Arrays;

/**
 * An EncryptedInt is an Integer whose stored value is both internally encrypted and
 * tamper-proof.
 *
 * This is useful for protecting a game's final high score or key gameplay variables
 * against runtime memory modifications from casual memory-cheating tools.
 *
 *
 * @author will
 * @date Mar 11, 2009 11:29:56 PM
 */
public class EncryptedInt {
    private final ByteArray buffer = new ByteArray(4);
    private final StreamCipher encCipher = new RC4Engine();
    private final StreamCipher decCipher = new RC4Engine();
    private final Digest sha = new SHA1Digest();

    private final byte[] hash = new byte[sha.getDigestSize()];
    private final byte[] hashCheck = new byte[sha.getDigestSize()];

    private int encrypted;

    private final byte[] privateKey;
    private boolean tamper = false;

    public EncryptedInt(byte[] privateKey, byte[] message) throws IOException {
        this(privateKey,-1);
        final SecureMessage sm = new SecureMessage(message);
        if(!sm.isValid(privateKey)) throw new IOException();
        buffer.reset();
        buffer.write(sm.getData());
        buffer.reset();
        encrypted = buffer.readInt();
    }

    public EncryptedInt(byte[] privateKey, int initialValue) {
        this.privateKey = privateKey;
        final KeyParameter kp = new KeyParameter(privateKey);
        encCipher.init(true, kp);
        decCipher.init(false,kp);
        set(initialValue);
    }

    /** Encrypts and stores the new value, updating the hash. */
    public void set(int value) {
        buffer.reset();
        buffer.writeInt(value);
        buffer.crypt(encCipher);
        buffer.reset();
        encrypted = buffer.readInt();
        sha.update(buffer.getData(),0,4);
        sha.doFinal(hash,0);
    }

    /** Decrypts and returns the stored value, checking its integrity. */
    public Integer get() {
        buffer.reset();
        buffer.writeInt(encrypted);
        buffer.crypt(decCipher);
        buffer.reset();
        final int i = buffer.readInt();
        sha.update(buffer.getData(),0,4);
        sha.doFinal(hashCheck,0);
        if(!Arrays.equals(hash,hashCheck)) {
            // Silently flag tamper attempt - game operation remains the same,
            // but the tamper will be evident when the score is submitted with an
            // incorrectly authenticated message.
            tamper = true;
        }
        return i;
    }

    /**
     * Creates a message suitable for sending over an insecure channel.
     * Only someone with knowledge of the shared secret key will be able to
     * decode and/or validate the integrity of the stored value.
     * @return an encrypted, authenticated message containing the value.
     */
    public byte[] getSecure() {
        buffer.reset();
        buffer.writeInt(encrypted);
        final byte[] msg = new SecureMessage(privateKey,buffer.getData()).getMessage();
        // Break message integrity if tamper was evident
        msg[0] ^= (tamper ? 1 : 0);
        return msg;
    }

    /**
     * Reads a secure message and returns the original stored value.
     * @param message The encoded message (32bit RC4-encrypted integer + HMAC)
     * @param privateKey The key used for RC4 and HMAC.
     * @return the decrypted, authenticated integer.
     */
    public static Integer readSecure(byte[] message, byte[] privateKey) {
        final ByteArray buffer = new ByteArray(message);
        final SecureMessage msg = new SecureMessage(message);
        if(msg.isValid(privateKey)) {
            buffer.reset();
            buffer.write(msg.getData());
            return buffer.readInt();
        } else {
            return null;
        }
    }
}
