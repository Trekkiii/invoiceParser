package com.github.fnpac.invoice.core.handler;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by 刘春龙 on 2017/9/6.
 */
public class QrcodeHandler {

    private static final Logger logger = Logger.getLogger(QrcodeHandler.class.getName());

    public static String getQrcode(BufferedImage qrImage) {
        LuminanceSource source = new BufferedImageLuminanceSource(qrImage);
        Binarizer binarizer = new HybridBinarizer(source);
        BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);

        Map hints = new HashMap();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        MultiFormatReader multiFormatReader = new MultiFormatReader();

        try {
            Result result = multiFormatReader.decode(binaryBitmap, hints);
            logger.info("result: " + result.toString());
            logger.info("resultFormat: " + result.getBarcodeFormat());
            logger.info("resultText: " + result.getText());

            return result.getText();
        } catch (NotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}
