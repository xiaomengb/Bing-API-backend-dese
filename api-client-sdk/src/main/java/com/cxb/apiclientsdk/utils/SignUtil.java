package com.cxb.apiclientsdk.utils;

import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;

import java.awt.*;

/**
 * 签名算法工具类
 *
 * @author cxb
 */
public class SignUtil {

    private SignUtil() {
    }

    public static String getSign(String once, String timestamp, String secretKey) {
        Digester digester = new Digester(DigestAlgorithm.SHA256);
        String content = once + "." + timestamp + "." + secretKey;
        return digester.digestHex(content);
    }
}
