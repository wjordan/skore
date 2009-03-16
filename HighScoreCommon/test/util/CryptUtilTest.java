package util;

import static net.tetromi.log.LOG.info;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotSame;
import org.testng.annotations.Test;
import static util.CryptUtil.*;
import static util.StringUtil.*;

/**
 * @author will
 * @date Mar 10, 2009 3:43:08 AM
 */
public class CryptUtilTest {

    /** Check the MAC key hash against known value. */
    @Test
    public static void testGetKey() {
        final byte[] key = getKey("hello");
        final String s = enc64(key);
        final byte[] key2 = dec64(s);
        assertEquals(key,key2);
        assertEquals(s,"BKPDhYBzTeeA/cI4vlmRV0jWq5Y=");
        info("Hello="+s);
    }

    /** Generate random byte arrays of the hash byte length. */
    @Test
    public void testGetRandomKey() {
        final byte[] key = getRandomKey();
        info("key length="+key.length);
        for(int i = 0; i < 100; i++) {
            byte[] key2 = getRandomKey();
            assertEquals(key2.length,20);
            assertEquals(key.length,key2.length);
            assertNotSame(key,key2);
        }
        // Add your code here

    }

    @Test
    public void testEncrypt() {
        final String s1 = "Test key";
        final String s2 = "Test string to encrypt.";
        byte[] key = toByte(s1);
        byte[] data = toByte(s2);
        final byte[] encBytes = encrypt(key, data);
        final byte[] decBytes = decrypt(key,encBytes);
        info("decbytes="+ toStr(decBytes));
        assertEquals(decBytes,data);
    }

    /** Check the key string hash against a known value. */
    @Test
    public void testHash() {
        final byte[] bytes = hash("Hello World!");
        final String s = enc64(bytes);
        info(s);
        assertEquals(s,"Lve95gjOVATpfV8EL5X4nxwjKHE=");
    }

    /** Match empty array against known hash */
    @Test
    public void testHash2() {
        final byte[] bytes = hash(new byte[0]);
        final String s = enc64(bytes);
        info(s);
        assertEquals(s,"2jmj7l5rSw0yVb/vlWAYkK/YBwk=");
    }

    @Test
    public void testHash3() {
        String[] hello = {"Hello"," ","World!"};
        byte[][] b = new byte[3][];
        b[0] = toByte(hello[0]);
        b[1] = toByte(hello[1]);
        b[2] = toByte(hello[2]);
        final byte[] bytes = hash(b[0], b[1], b[2]);
        final String s = enc64(bytes);
        info(s);
        assertEquals(s,"Lve95gjOVATpfV8EL5X4nxwjKHE=");

        // Add your code here
    }
}
