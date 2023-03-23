package cn.xanderye.android.deepalwidget.util;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 2021/1/29.
 * 编码工具类 整合各种编码
 *
 * @author XanderYe
 */
public class CodecUtil {

    private static final String HEX_STRING = "0123456789ABCDEF";

    /**
     * byte数组转base64
     * @param bytes
     * @return java.lang.String
     * @author XanderYe
     * @date 2021/1/29
     */
    public static String base64Encode(byte[] bytes) {
        return Base64.getMimeEncoder().encodeToString(bytes);
    }

    /**
     * 字符串转base64
     * @param str
     * @return java.lang.String
     * @author XanderYe
     * @date 2021/1/29
     */
    public static String base64Encode(String str) {
        return base64Encode(str.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 指定编码字符串转base64
     * @param str
     * @param charset
     * @return java.lang.String
     * @author XanderYe
     * @date 2021/1/29
     */
    public static String base64Encode(String str, Charset charset) {
        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }
        return base64Encode(str.getBytes(charset));
    }

    /**
     * base64字符串转byte数组
     * @param base64Str
     * @return byte[]
     * @author XanderYe
     * @date 2021/1/29
     */
    public static byte[] base64DecodeToByteArray(String base64Str) {
        return Base64.getMimeDecoder().decode(base64Str.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * base64字符串转byte数组
     * @param base64Str
     * @param charset
     * @return byte[]
     * @author XanderYe
     * @date 2021/1/29
     */
    public static byte[] base64DecodeToByteArray(String base64Str, Charset charset) {
        return Base64.getMimeDecoder().decode(base64Str.getBytes(charset));
    }

    /**
     * base64字符串转字符串
     * @param base64Str
     * @return byte[]
     * @author XanderYe
     * @date 2021/1/29
     */
    public static String base64Decode(String base64Str) {
        return base64Decode(base64Str, StandardCharsets.UTF_8);
    }

    /**
     * base64字符串转指定编码字符串
     * @param base64Str
     * @return byte[]
     * @author XanderYe
     * @date 2021/1/29
     */
    public static String base64Decode(String base64Str, Charset charset) {
        return new String(base64DecodeToByteArray(base64Str), charset);
    }

    /**
     * URL编码
     * @param str
     * @return java.lang.String
     * @author XanderYe
     * @date 2021/1/29
     */
    public static String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
    }

    /**
     * URL解码
     * @param str
     * @return java.lang.String
     * @author XanderYe
     * @date 2021/1/29
     */
    public static String urlDecode(String str) {
        try {
            return URLDecoder.decode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
    }

    /**
     * URL编码
     * @param str
     * @param charset
     * @return java.lang.String
     * @author XanderYe
     * @date 2021/1/29
     */
    public static String urlEncode(String str, String charset) throws UnsupportedEncodingException {
        return URLEncoder.encode(str, charset);
    }

    /**
     * URL解码
     * @param str
     * @param charset
     * @return java.lang.String
     * @author XanderYe
     * @date 2021/1/29
     */
    public static String urlDecode(String str, String charset) throws UnsupportedEncodingException {
        return URLDecoder.decode(str, charset);
    }

    /**
     * byte数组转十六进制字符串
     *
     * @param bytes
     * @return java.lang.String
     * @author XanderYe
     * @date 2019/8/6
     */
    public static String byteArrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(aByte & 0xFF);
            if (hex.length() < 2) {
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * 字符串转成字节流
     */
    public static byte[] hexStringToByteArray(String hex) {
        if (hex != null) {
            hex = hex.replace(" ", "").toUpperCase();
            int len = (hex.length() / 2);
            byte[] bytes = new byte[len];
            char[] chars = hex.toCharArray();
            for (int i = 0; i < len; i++) {
                int pos = i * 2;
                bytes[i] = (byte) (HEX_STRING.indexOf(chars[pos]) << 4 | HEX_STRING.indexOf(chars[pos + 1]));
            }
            return bytes;
        }
        return null;
    }

    /**
     * Unicode转中文
     * @param unicode
     * @return java.lang.String
     * @author XanderYe
     * @date 2020-03-29
     */
    public static String unicodeToString(String unicode) {
        String[] uns = unicode.split("\\\\u");
        StringBuilder returnStr = new StringBuilder();
        for (int i = 1; i < uns.length; i++) {
            returnStr.append((char) Integer.valueOf(uns[i], 16).intValue());
        }
        return returnStr.toString();
    }

    /**
     * 中文转Unicode
     * @param string
     * @return java.lang.String
     * @author XanderYe
     * @date 2020-03-29
     */
    public static String stringToUnicode(String string) {
        char[] chars = string.toCharArray();
        StringBuilder returnStr = new StringBuilder();
        for (char aChar : chars) {
            returnStr.append("\\u").append(Integer.toString(aChar, 16));
        }
        return returnStr.toString();
    }

    /**
     * 含有Unicode的字符串转中文
     * @param unicodeStr
     * @return java.lang.String
     * @author XanderYe
     * @date 2020-03-29
     */
    public static String unicodeStrToString(String unicodeStr) {
        int length = unicodeStr.length();
        int count = 0;
        String regex = "\\\\u[a-f0-9A-F]{1,4}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(unicodeStr);
        StringBuilder sb = new StringBuilder();

        while(matcher.find()) {
            String oldChar = matcher.group();
            String newChar = unicodeToString(oldChar);
            int index = matcher.start();

            sb.append(unicodeStr, count, index);
            sb.append(newChar);
            count = index+oldChar.length();
        }
        sb.append(unicodeStr, count, length);
        return sb.toString();
    }

    /**
     * MD5算法
     * @param str
     * @return java.lang.String
     * @author XanderYe
     * @date 2022/3/3
     */
    public static String MD5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            byte[] bytes = md.digest();
            return new BigInteger(1, bytes).toString(16);
        } catch (NoSuchAlgorithmException ignored) {
        }
        return null;
    }
}
