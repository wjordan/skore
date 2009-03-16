package util;

import static net.tetromi.log.LOG.info;
import net.tetromi.secure.SecureMessage;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * @author will
 * @date Mar 12, 2009 12:34:09 AM
 */
public class EncryptedIntTest {

    @BeforeTest
    public void setUp() {
    }

    EncryptedInt ei;
    private final int value = 7;

    @Test
    public void test() throws Exception {
        final byte[] hello = CryptUtil.getKey("Hello World!");
        ei = new EncryptedInt(hello, value);
        final byte[] secure = ei.getSecure();

        final SecureMessage secureMessage = new SecureMessage(secure);
        info(Arrays.toString(secure));
        info(String.valueOf(secureMessage.isValid(hello)));
        info(Arrays.toString(secureMessage.getData()));
        final int encInt = new EncryptedInt(hello, new ByteArray(secureMessage.getMessage()).getData()).get();
        info(""+encInt);
        Assert.assertEquals(encInt, value);
    }

}
