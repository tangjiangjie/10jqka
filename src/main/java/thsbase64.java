

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 支持自定义查找表的Base64算法
 *
 * @author Cyao
 */
public class thsbase64 {
    public static final thsbase64 INSTANCE = new thsbase64();
    private final char[] chars;

    private final int[] INV = new int[256];

    public thsbase64() {
        this("aCcMeTKhxnwzmoPbsG4EvU8gyd02B3q6fIVWXYZjApRrDtuHkiLlN1O9F5S7JQ+/");
    }

    public thsbase64(String chars) {
        this.chars = chars.toCharArray();
        Arrays.fill(INV, -1);
        for (int i = 0, length = this.chars.length; i < length; i++) {
            INV[this.chars[i]] = i;
        }
        INV['='] = 0;
    }

    /**
     * 生成查找表
     *
     * @return
     */
    public static String generate() {
        List<String> list = new ArrayList<>();
        for (char c : INSTANCE.chars) {
            list.add(String.valueOf(c));
        }
        Collections.shuffle(list);
        return String.join("", list);
    }

    /**
     * 对字符串进行编码
     *
     * @param str 源字符串
     * @return 编码后字符串
     */
    public String encode(String str) {
        return encode(str, "UTF-8");
    }

    /**
     * 对字符串进行编码
     *
     * @param str         源字符串
     * @param charsetName 字符集名称
     * @return 编码后字符串
     */
    public String encode(String str, String charsetName) {
        byte[] bytes = str.getBytes(Charset.forName(charsetName));
        return new String(encode(bytes), StandardCharsets.US_ASCII);
    }

    /**
     * 对字符串进行解码
     *
     * @param str 编码后字符串
     * @return 解码后的字符串
     */
    public String decode(String str) {
        return decode(str, "UTF-8");
    }

    /**
     * 对字符串进行解码
     *
     * @param str         编码后字符串
     * @param charsetName 字符集名称
     * @return 解码后的字符串
     */
    public String decode(String str, String charsetName) {
        byte[] bytes = str.getBytes(StandardCharsets.US_ASCII);
        return new String(decode(bytes), Charset.forName(charsetName));
    }

    /**
     * 对字节流进行编码
     *
     * @param bytes 源字节流
     * @return 编码后字节流
     */
    public byte[] encode(final byte[] bytes) {
        int len = bytes != null ? bytes.length : 0;

        if (len == 0) {
            return new byte[0];
        }

        int evenlen = (len / 3) * 3;

        int cnt = ((len - 1) / 3 + 1) << 2;

        byte[] dest = new byte[cnt];

        for (int s = 0, d = 0, cc = 0; s < evenlen; ) {
            int i = (bytes[s++] & 0xff) << 16 | (bytes[s++] & 0xff) << 8 | (bytes[s++] & 0xff);
            dest[d++] = (byte) chars[(i >>> 18) & 0x3f];
            dest[d++] = (byte) chars[(i >>> 12) & 0x3f];
            dest[d++] = (byte) chars[(i >>> 6) & 0x3f];
            dest[d++] = (byte) chars[i & 0x3f];
        }

        int left = len - evenlen;
        if (left > 0) {
            int i = ((bytes[evenlen] & 0xff) << 10) | (left == 2 ? ((bytes[len - 1] & 0xff) << 2) : 0);
            dest[cnt - 4] = (byte) chars[i >> 12];
            dest[cnt - 3] = (byte) chars[(i >>> 6) & 0x3f];
            dest[cnt - 2] = left == 2 ? (byte) chars[i & 0x3f] : (byte) '=';
            dest[cnt - 1] = '=';
        }

        return dest;
    }

    /**
     * 对字节流进行解码
     *
     * @param bytes 源字节流
     * @return 解码后字节流
     */
    public byte[] decode(final byte[] bytes) {
        int length = bytes.length;

        if (length == 0) {
            return new byte[0];
        }

        int sndx = 0, endx = length - 1;
        int pad = bytes[endx] == '=' ? (bytes[endx - 1] == '=' ? 2 : 1) : 0;
        int cnt = endx - sndx + 1;
        int sepCnt = length > 76 ? (bytes[76] == '\r' ? cnt / 78 : 0) << 1 : 0;
        int len = ((cnt - sepCnt) * 6 >> 3) - pad;
        byte[] dest = new byte[len];

        int d = 0;
        for (int cc = 0, eLen = (len / 3) * 3; d < eLen; ) {
            int i = INV[bytes[sndx++]] << 18 | INV[bytes[sndx++]] << 12 | INV[bytes[sndx++]] << 6 | INV[bytes[sndx++]];
            dest[d++] = (byte) (i >> 16);
            dest[d++] = (byte) (i >> 8);
            dest[d++] = (byte) i;
            if (sepCnt > 0 && ++cc == 19) {
                sndx += 2;
                cc = 0;
            }
        }

        if (d < len) {
            int i = 0;
            for (int j = 0; sndx <= endx - pad; j++) {
                i |= INV[bytes[sndx++]] << (18 - j * 6);
            }
            for (int r = 16; d < len; r -= 8) {
                dest[d++] = (byte) (i >> r);
            }
        }

        return dest;
    }
}