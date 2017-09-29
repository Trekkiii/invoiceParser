package com.github.ittalks.example.pdf;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * Created by 刘春龙 on 2017/9/5.
 */
public class PdfImgReader {

    private static final Logger logger = Logger.getLogger(PdfTextReader.class.getName());

    public static void main(String[] args) throws URISyntaxException, IOException {
        // 待解析PDF
        File pdfFile = new File(PdfTextReader.class.getClassLoader().getResource("发票.pdf").toURI());

        // 空白PDF
//        File pdfFile_out = new File("img.pdf");

        PDDocument document = PDDocument.load(pdfFile);
//        PDDocument document_out = new PDDocument();

        int pages_size = document.getNumberOfPages();

        logger.info("Pages size =============== " + pages_size);

        int imgIndex = 0;
        for (int i = 0; i < pages_size; i++) {
            PDPage page = document.getPage(i);
            PDResources resources = page.getResources();
            Iterable xobjects = resources.getXObjectNames();
            if (xobjects != null) {
                Iterator imageIter = xobjects.iterator();
                while (imageIter.hasNext()) {
                    COSName key = (COSName) imageIter.next();
                    if (resources.isImageXObject(key)) {
                        try {

                            /*
                                方式一：将PDF文档中的图片，存到一个空白PDF中。
                             */
//                            PDImageXObject image = (PDImageXObject) resources.getXObject(key);
//                            PDPage new_page = new PDPage();
//                            document_out.addPage(new_page);
//                            PDPageContentStream contentStream = new PDPageContentStream(document_out, new_page, PDPageContentStream.AppendMode.OVERWRITE, true);
//                            float scale = 1f;
//                            contentStream.drawImage(image, 0, 0, image.getWidth() * scale, image.getHeight() * scale);
//                            contentStream.close();
//                            document_out.save(pdfFile_out);

                            /*
                                方式二：将PDF文档中的图片 分别另存为图片。
                             */
                            PDImageXObject image = (PDImageXObject) resources.getXObject(key);
                            BufferedImage bufferedImage = image.getImage();
                            ImageIO.write(bufferedImage, "PNG", new File("images\\" + imgIndex + ".png"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //image count
                        imgIndex++;
                    }
                }
            }
        }
        document.close();
    }
}
