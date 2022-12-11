package pt.ulisboa.tecnico.captor.captorsharedlibrary.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Base64Util {

    private static final char[] chars =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
                    .toCharArray();

    /**
     * Decode
     */

    public static byte[] decodeString(String str) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            decode(str, outputStream);
        }
        catch (IOException e) {
            throw new RuntimeException();
        }
        return outputStream.toByteArray();
    }

    private static void decode(String str, OutputStream stream) throws IOException {
        int i = 0;
        int len = str.length();
        while (true) {
            while (i < len && str.charAt(i) <= ' ') {
                i++;
            }
            if (i == len) {
                break;
            }
            int aux = (decodeChar(str.charAt(i)) << 18)
                            + (decodeChar(str.charAt(i + 1)) << 12)
                            + (decodeChar(str.charAt(i + 2)) << 6)
                            + (decodeChar(str.charAt(i + 3)));

            stream.write((aux >> 16) & 255);
            if (str.charAt(i + 2) == '=') {
                break;
            }
            stream.write((aux >> 8) & 255);
            if (str.charAt(i + 3) == '=') {
                break;
            }
            stream.write(aux & 255);
            i += 4;
        }
    }

    private static int decodeChar(char c) {
        if (c >= 'A' && c <= 'Z')
            return ((int) c) - 65;
        else if (c >= 'a' && c <= 'z')
            return ((int) c) - 97 + 26;
        else if (c >= '0' && c <= '9')
            return ((int) c) - 48 + 26 + 26;
        else
            switch (c) {
                case '+' :
                    return 62;
                case '/' :
                    return 63;
                case '=' :
                    return 0;
                default :
                    throw new RuntimeException(
                            "unexpected code: " + c);
            }
    }

    /**
     *  Encode
     */

    public static String encodeString(byte[] data) {
        return encode(data, 0, data.length, null).toString();
    }

    public static StringBuffer encode(
            byte[] data,
            int start,
            int len,
            StringBuffer buf) {

        if (buf == null)
            buf = new StringBuffer(data.length * 3 / 2);
        int end = len - 3;
        int i = start;
        int n = 0;

        while (i <= end) {
            int d = ((((int) data[i]) & 0x0ff) << 16)
                            | ((((int) data[i + 1]) & 0x0ff) << 8)
                            | (((int) data[i + 2]) & 0x0ff);
            buf.append(chars[(d >> 18) & 63]);
            buf.append(chars[(d >> 12) & 63]);
            buf.append(chars[(d >> 6) & 63]);
            buf.append(chars[d & 63]);
            i += 3;
            if (n++ >= 14) {
                n = 0;
                buf.append("\r\n");
            }
        }
        if (i == start + len - 2) {
            int d = ((((int) data[i]) & 0x0ff) << 16)
                            | ((((int) data[i + 1]) & 255) << 8);
            buf.append(chars[(d >> 18) & 63]);
            buf.append(chars[(d >> 12) & 63]);
            buf.append(chars[(d >> 6) & 63]);
            buf.append("=");
        }
        else if (i == start + len - 1) {
            int d = (((int) data[i]) & 0x0ff) << 16;
            buf.append(chars[(d >> 18) & 63]);
            buf.append(chars[(d >> 12) & 63]);
            buf.append("==");
        }
        return buf;
    }
}
