package com.github.fnpac.example.qrcode;

import com.github.fnpac.example.pdf.PdfTextReader;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by 刘春龙 on 2017/9/6.
 */
public class ResolveImage {

    private static final Logger logger = Logger.getLogger(PdfTextReader.class.getName());

    public static void main(String[] args) throws URISyntaxException, IOException {

        File imgFile = new File("images\\2.png");
        BufferedImage image = ImageIO.read(imgFile);
        LuminanceSource source = new BufferedImageLuminanceSource(image);
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
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }

}
