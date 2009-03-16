package net.tetromi.secure;

import util.ByteArray;
import static util.CryptUtil.hmac;
import util.StringUtil;

import java.util.Arrays;

/**
 * Secure message data object using MAC.
 * @author will
 * @date Mar 12, 2009 2:09:36 AM
 */
public class SecureMessage {
    final byte[] data;
    final byte[] mac;
    boolean invalid = false;

    /** Decodes a message stream. */
    public SecureMessage(byte[] message) {
        byte[] tdata,tmac;
        try {
            final ByteArray buffer = new ByteArray(message);
            final int len = buffer.readInt();
            tdata = new byte[len];
            buffer.read(tdata,0,len);
            tmac = new byte[buffer.length()-buffer.position()];
            buffer.read(tmac,0,tmac.length);
        } catch (Exception e) {
            invalid = true;
            data = null;
            mac = null;
            return;
        }
        data = tdata;
        mac = tmac;
    }

    public SecureMessage(String message) {
        this(StringUtil.dec64(message));
    }

    public SecureMessage(byte[] key, byte[] data) {
        this.data = data;
        mac = hmac(key,data);
    }

    public byte[] getMessage() {
        if(invalid) return null;
        final ByteArray buffer = new ByteArray((Integer.SIZE/8) + data.length+mac.length);
        buffer.writeInt(data.length);
        buffer.write(data);
        buffer.write(mac);
        return buffer.getData();
    }

    public boolean isValid(byte[] key) {
        if(invalid) return false;
        try {
            return Arrays.equals(mac,hmac(key,data));
        } catch (Exception e) {
            return false;
        }
    }

    public byte[] getData() {
        return data;
    }

}
