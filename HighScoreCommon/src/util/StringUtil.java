package util;

import org.bouncycastle.util.encoders.Base64;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;

/**
 * UTF-8 and Base64-encoded String processing utility functions.
 * @author will
 * @date Feb 28, 2009 5:57:58 PM
 */
public class StringUtil {
    private static String utf8 = "UTF-8";
    public static Charset UTF8 = Charset.forName(utf8);

    /** Encodes a byte array to a Base64-encoded String. */
    public static String enc64(byte[] b) {
        return new String(Base64.encode(b),UTF8);
    }

    /** Decodes a Base64-encoded String to a byte array. */
    public static byte[] dec64(String s) {
        return Base64.decode(s);
    }

    /** Encodes a byte array to a URL64-encoded String. */
    public static String toUrl(byte[] b) {
        return toUrl(Base64.encode(b));
    }

    /** Decodes a URL64-encoded String into a byte array. */
    public static byte[] fromUrl64(String s) {
        return Base64.decode(fromUrl(s));
    }

    /** Encodes a string into a URL-encoded format. */
    public static String toUrl(String str) {
        try {
            return URLEncoder.encode(str,utf8);
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    /** Decodes a URL-encoded string. */
    public static String fromUrl(String s) {
        try {
            return URLDecoder.decode(s, utf8);
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    /** Converts a String to bytes using UTF-8 format. */
    public static byte[] toByte(String s) {
        return s.getBytes(UTF8);
    }

    /** Decodes a UTF-8 formatted byte array to a String. */
    public static String toStr(byte[] s) {
        return new String(s,UTF8);
    }
}
