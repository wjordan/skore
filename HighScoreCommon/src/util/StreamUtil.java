package util;

import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedInputStream;

/**
 * Static data stream reader/writer helper methods.
 * @author will
 * @date Mar 10, 2009 2:14:14 AM
 */
public class StreamUtil {

    /** Reads an InputStream into a byte array using a BufferedInputStream. */
    public static byte[] readStream(InputStream is) throws IOException {
        final BufferedInputStream bis = new BufferedInputStream(is);
        byte[] b = new byte[bis.available()];
        int i = b.length;
        while (i > 0) i = bis.read(b, 0, b.length);
        bis.close();
        is.close();
        return b;
    }

}
