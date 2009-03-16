package util;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.StreamCipher;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.engines.RC4Engine;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import static util.StringUtil.enc64;
import static util.StringUtil.toByte;

import java.util.Random;

/**
 * Encryption wrapper utility class.
 *
 * @author will
 * @date Feb 28, 2009 5:56:57 PM
 */
public class CryptUtil {
    private static final Digest sha = new SHA1Digest();


    /** Generates a 160-bit private key from a secure passphrase.
     * Do HMAC algorithm for generating the hashed key. */
    public static byte[] getKey(String phrase) {
        return hash(phrase);

//        PBKDF2Parameters p = new PBKDF2Parameters("HmacSHA1", "UTF-8",
//                new byte[]{1,3,3,7}, 1000);
//        PBKDF2Engine e = new PBKDF2Engine(p);
//        final byte[] key = e.deriveKey(phrase);
//        ByteArray ba = new ByteArray(key);
//        ba.setLength(16);
//        return ba.getData();
    }

    /** @return HMAC (Hash Message authentication Code) for the given byte array. */
    public static byte[] hmac(byte[] key, byte[] data) {
        Mac mac = new HMac(sha);
        mac.init(new KeyParameter(key));
        mac.update(data,0,data.length);
        final byte[] out = new byte[mac.getMacSize()];
        mac.doFinal(out,0);
        return out;
    }

    /** @return a random 160-bit key. */
    public static byte[] getRandomKey() {
        byte[] bytes = new byte[16];
        new Random().nextBytes(bytes);
        return hash(bytes);

//        try {
//            final KeyGenerator instance = KeyGenerator.getInstance(AES);
//            instance.init(128); // 192 and 256 bits may not be available
//            return instance.generateKey().getEncoded();
//        } catch (NoSuchAlgorithmException e) {
//            LOG.error("AES algorithm not found!");
//            return null;
//        }
    }

    public static byte[] encrypt(byte[] key, byte[] s) {
        return crypt(key, s, true);

//        ByteArray ba = new ByteArray(s);
//        ba.crypt(new ARC4(key));
//        return ba.getData();

//        ByteArray ba = new ByteArray(key);
//        ba.setLength(16);
//        aesCipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(ba.getData(), AES));
//        return aesCipher.doFinal(s);
    }

    /** (En|De)crypts a block of data using a BouncyCastle crypto engine (currently RC4). */
    private static byte[] crypt(byte[] key, byte[] s, boolean doEncrypt) {
        StreamCipher rc4 = new RC4Engine();
        rc4.init(doEncrypt,new KeyParameter(key));
        byte[] out = new byte[s.length];
        rc4.processBytes(s, 0, s.length, out, 0);
        return out;
    }

    public static byte[] decrypt(byte[] key, byte[] s) {
        return crypt(key,s,false);

//        ByteArray ba = new ByteArray(key);
//        ba.setLength(16);
//        aesCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(ba.getData(), AES));
//        return aesCipher.doFinal(s);
    }

    /** @return hash computed from a concatenation of byte arrays. */
    public static byte[] hash(byte[] ... arrays) {
        ByteArray ba = new ByteArray();
        for(byte[] array : arrays) {
            ba.write(array);
        }
        return hash(ba.getData());
    }

    /** @return hash computer from a string. */
    public static byte[] hash(String str) {
        return hash(toByte(str));
    }

    /** @return 160-bit SHA1 hash computed from a byte array. */
    public static synchronized byte[] hash(byte[] b) {
        sha.reset();
        sha.update(b,0,b.length);
        final byte[] out = new byte[sha.getDigestSize()];
        sha.doFinal(out,0);
        return out;
    }

    public static void main(String[] args) {
        System.out.println(enc64(getKey("PASSWoRD")));
    }

}
