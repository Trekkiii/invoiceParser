package com.github.fnpac.invoice.core.handler;

import com.github.fnpac.invoice.core.handler.imgproc.OpenCVExtractor;
import com.github.fnpac.invoice.core.handler.imgproc.Settings;
import com.github.fnpac.invoice.core.models.InvoiceInfo;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.github.fnpac.invoice.core.handler.Commons.EM_SPACE;
import static com.github.fnpac.invoice.core.utils.Utils.bufferedImage2GrayscaleMat;

/**
 * Created by 刘春龙 on 2017/10/24.
 */
public class PdfTableHandler {

    private static final Logger logger = Logger.getLogger(PdfTableHandler.class.getName());

    private static final int minWidth = 280;
    private static final int minHeight = 15;

    private final Map<String, Map<String, String>> tagKeys;
    private static final Map<String, String> invoiceCode;
    private final Rectangle invoiceCodeRegion;

    // region Keys
    public static final String Invoice_Buyer_Region = "Invoice_Buyer_Region";
    public static final String Invoice_PriceTax_Region = "Invoice_PriceTax_Region";
    public static final String Invoice_Seller_Region = "Invoice_Seller_Region";

    static {

        // 加载图像处理库
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // 发票信息
        invoiceCode = new LinkedHashMap<>();
        invoiceCode.put(InvoiceInfo.Invoice_Code, "\\S*?\\s*?发票代码\\s*[:：]{1}\\s*(.*)\\s*");
        invoiceCode.put(InvoiceInfo.Invoice_No, "\\S*?\\s*?发票号码\\s*[:：]{1}\\s*(.*)\\s*");
        invoiceCode.put(InvoiceInfo.Invoice_Date, "\\S*?\\s*?开票日期\\s*[:：]{1}\\s*(.*)\\s*");
        invoiceCode.put(InvoiceInfo.Invoice_Checksum, "\\S*?\\s*?校\\s*验\\s*码\\s*[:：]{1}\\s*(.*)");
    }

    private OpenCVExtractor extractor;
    private Settings settings;

    public PdfTableHandler(Settings settings) {
        this.settings = settings;
        this.extractor = new OpenCVExtractor(settings);

        tagKeys = new LinkedHashMap<>();
        invoiceCodeRegion = new Rectangle(400, 5, 620 - 400, 90 - 5);// 默认值，需要根据解析的发票页面更新坐标
    }

    public PdfTableHandler() {
        this(new Settings());// 使用默认配置
    }

    /**
     * 使用设置中指定的DPI渲染指定页码范围内的PDF页面，并将图像保存在指定的目录中
     *
     * @param document  PDDocument
     * @param startPage 起始页，第一页页码为1
     * @param endPage   结束页
     * @param outputDir 输出目录
     * @throws IOException
     */
    public void savePdfPagesAsPNG(PDDocument document, int startPage, int endPage, Path outputDir) throws IOException {
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        for (int page = startPage - 1; page < endPage; ++page) {// pdfbox 默认第一页页码为0
            savePdfPageAsPNG(pdfRenderer, page, outputDir);
        }
    }

    /**
     * 使用设置中指定的DPI渲染单个PDF页面，并将图像保存在指定的目录中
     *
     * @param document  PDDocument
     * @param page      页码
     * @param outputDir 输出目录
     * @throws IOException
     */
    public void savePdfPageAsPNG(PDDocument document, int page, Path outputDir) throws IOException {
        savePdfPagesAsPNG(document, page, page, outputDir);
    }

    /**
     * 使用设置中指定的DPI渲染单个PDF页面，并将图像保存在指定的目录中
     *
     * @param renderer  PDFRenderer
     * @param page      页码
     * @param outputDir 输出目录
     * @throws IOException
     */
    private void savePdfPageAsPNG(PDFRenderer renderer, int page, Path outputDir) throws IOException {
        BufferedImage bim;
        synchronized (this) {
            bim = renderer.renderImageWithDPI(page, settings.getPdfRenderingDpi(), ImageType.RGB);
        }

        Path outPath = outputDir.resolve(Paths.get("page_" + (page + 1) + ".png"));
        logger.info("out: " + outPath.toString());
        ImageIOUtil.writeImage(bim, outPath.toString(), settings.getPdfRenderingDpi());
    }

    /**
     * 将指定页码范围内的PDF页面的debug图像保存在指定的目录中。
     *
     * @param document  PDDocument
     * @param startPage 起始页，第一页页码为1
     * @param endPage   结束页
     * @param outputDir 输出目录
     * @throws IOException
     */
    public void savePdfPagesDebugImages(PDDocument document, int startPage, int endPage, Path outputDir) throws IOException {
        OpenCVExtractor debugExtractor = new OpenCVExtractor(settings);

        PDFRenderer renderer = new PDFRenderer(document);
        for (int page = startPage - 1; page < endPage; ++page) {// pdfbox 默认第一页页码为0
            Settings debugSettings = Settings.builder()
                    .setDebugImages(true)
                    .setDebugFileOutputDir(outputDir)
                    .setDebugFilename("page_" + (page + 1))
                    .build();
            debugExtractor.setSettings(debugSettings);

            BufferedImage bim;
            synchronized (this) {
                bim = renderer.renderImageWithDPI(page, debugSettings.getPdfRenderingDpi(), ImageType.RGB);
            }

            Mat mat = bufferedImage2GrayscaleMat(bim);// 需要先将图像转为灰度图
            debugExtractor.getTableBoundingRectangles(mat);
        }
    }

    /**
     * 将指定页码的PDF页面的debug图像保存在指定的目录中。
     *
     * @param document  PDDocument
     * @param page      页码
     * @param outputDir 输出目录
     * @throws IOException
     */
    public void savePdfPageDebugImage(PDDocument document, int page, Path outputDir) throws IOException {
        savePdfPagesDebugImages(document, page, page, outputDir);
    }

    /**
     * 解析指定页码范围内的PDF页面，并返回包含单元格文本的解析结果
     *
     * @param document  PDDocument
     * @param startPage 起始页，第一页页码为1
     * @param endPage   结束页
     * @return 包含单元格文本的解析结果
     * @throws IOException
     */
    public List<InvoiceInfo> parsePdfPages(PDDocument document, int startPage, int endPage) throws IOException {
        List<InvoiceInfo> out = new ArrayList<>();

        PDFRenderer renderer = new PDFRenderer(document);
        for (int page = startPage - 1; page < endPage; ++page) {
            BufferedImage bim;
            synchronized (this) {
                bim = renderer.renderImageWithDPI(page, settings.getPdfRenderingDpi(), ImageType.RGB);
            }
            InvoiceInfo invoiceInfo = parsePdfPage(bim, document.getPage(page), page + 1);
            out.add(invoiceInfo);
        }
        return out;
    }

    /**
     * 解析指定页码的PDF页面，并返回包含单元格文本的解析结果
     *
     * @param document PDDocument
     * @param page     页码
     * @return 包含单元格文本的解析结果
     * @throws IOException
     */
    public InvoiceInfo parsePdfPage(PDDocument document, int page) throws IOException {
        return parsePdfPages(document, page, page).get(0);
    }

    /**
     * 解析指定页码的单个PDF页面，并返回包含单元格文本的解析结果
     *
     * @param bim        图像格式的PDF页面
     * @param pdPage     PDPage格式的PDF页面
     * @param pageNumber 页码
     * @return 包含单元格文本的解析结果
     * @throws IOException
     */
    private InvoiceInfo parsePdfPage(BufferedImage bim, PDPage pdPage, int pageNumber) throws IOException {
        List<Rect> rectangles = extractor.getTableBoundingRectangles(bufferedImage2GrayscaleMat(bim));
        return parsePageByRectangles(pdPage, rectangles, pageNumber);
    }

    /**
     * 使用从{@link OpenCVExtractor}获取的{@link Rect}，逐个单元格解析PDF页面
     *
     * @param page       PDF页面
     * @param rectangles {@link OpenCVExtractor}识别的OpenCV {@link Rect}列表
     * @param pageNumber 页码
     * @return 解析的结果
     * @throws IOException
     */
    private InvoiceInfo parsePageByRectangles(PDPage page, List<Rect> rectangles, int pageNumber) throws IOException {

        // 初始化模板
        initTemplate(page);

        InvoiceInfo invoiceInfo = new InvoiceInfo();

        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        stripper.setSortByPosition(true);

        int index = 0;

        Iterator<Rect> rectIterator = rectangles.iterator();
        while (rectIterator.hasNext()) {

            Rect rect = rectIterator.next();
            // 根据width、height过滤
            if (rect.width * settings.getDpiRatio() < minWidth || rect.height * settings.getDpiRatio() < minHeight) {
                rectIterator.remove();
                continue;
            }

            // 使用设置中指定的DPI（pdfRenderingDpi）渲染单个PDF页面，生成图像。
            // 这里需要根据比例进行相应的还原
            Rectangle r = new Rectangle(
                    (int) (rect.x * settings.getDpiRatio()),
                    (int) (rect.y * settings.getDpiRatio()),
                    (int) (rect.width * settings.getDpiRatio()),
                    (int) (rect.height * settings.getDpiRatio())
            );
            stripper.addRegion("rect@" + index, r);
            index++;
        }

        stripper.extractRegions(page);

        index = 0;
        for (Rect rect : rectangles) {
            // 提取文本
            String regionText = stripper.getTextForRegion("rect@" + index);
            index++;

//            logger.info("[x: " + (int) (rect.x * settings.getDpiRatio())
//                    + ", y: " + (int) (rect.y * settings.getDpiRatio())
//                    + ", width: " + (int) (rect.width * settings.getDpiRatio())
//                    + ", height: " + (int) (rect.height * settings.getDpiRatio()) + "]");
//            logger.info("regionText:" + regionText);

            if (regionText != null) {
                // 去除unicode为12288全角空格字符
                regionText = regionText.replace(EM_SPACE, ' ').trim();

                Iterator<String> keysIterator = tagKeys.keySet().iterator();
                while (keysIterator.hasNext()) {

                    String key = keysIterator.next();
                    Map<String, String> keyMap = tagKeys.get(key);

                    List<String> keyList = new ArrayList<>();
                    StringBuilder regExp = new StringBuilder("");
                    for (Map.Entry<String, String> keyEntry : keyMap.entrySet()) {
                        regExp.append(keyEntry.getValue());
                        keyList.add(keyEntry.getKey());
                    }

                    Pattern pattern = Pattern.compile(regExp.toString());
                    Matcher matcher = pattern.matcher(regionText);

                    if (matcher.find()) {
                        int groupCount = matcher.groupCount();
                        if (groupCount == keyList.size()) {
                            for (int i = 0; i < groupCount; i++) {
                                invoiceInfo.setMapValue(keyList.get(i), matcher.group(i + 1));
                            }
                        }

                        if (key.equals(Invoice_Buyer_Region)) {
                            int x1 = (int) ((rect.x + rect.width) * settings.getDpiRatio());
                            int y1 = 5;
                            int x2 = (int) (page.getMediaBox().getWidth() - 5);
                            int y2 = (int) (rect.y * settings.getDpiRatio() - 5);
                            invoiceCodeRegion.setBounds(x1, y1, x2 - x1, y2 - y1);
//                            logger.info("[x1: " + x1 + ", y1: " + y1 + ", x2: " + x2 + ", y2: " + y2 + "]");
                        }

                        keysIterator.remove();// 移除已解析的模板
                        break;
                    }
                }
            }
        }

        // 发票code
        stripper.addRegion("rect@invoiceCode", invoiceCodeRegion);
        stripper.extractRegions(page);

        /**
         * 处理发票code
         */
        // 提取文本
        String regionText = stripper.getTextForRegion("rect@invoiceCode");
//        logger.info("regionText:" + regionText);
        if (regionText != null) {
            // 去除unicode为12288全角空格字符
            regionText = regionText.replace(EM_SPACE, ' ').trim();
            List<String> keyList = new ArrayList<>();
            StringBuilder regExp = new StringBuilder("");
            for (Map.Entry<String, String> tmpEntry : invoiceCode.entrySet()) {
                regExp.append(tmpEntry.getValue());
                keyList.add(tmpEntry.getKey());
            }

            Pattern pattern = Pattern.compile(regExp.toString());
            Matcher matcher = pattern.matcher(regionText);

            if (matcher.find()) {
                int groupCount = matcher.groupCount();
                if (groupCount == keyList.size()) {
                    for (int i = 0; i < groupCount; i++) {
                        invoiceInfo.setMapValue(keyList.get(i), matcher.group(i + 1));
                    }
                }
            }
        }

        return invoiceInfo;
    }

    /**
     * 按y坐标对{@link Rect}进行分组，将它们按照表格行分组
     *
     * @param rectangles {@link OpenCVExtractor}识别的OpenCV {@link Rect}列表
     * @return list of Rectangle lists representing table rows.
     */
    private List<List<Rect>> groupRectanglesByRow(List<Rect> rectangles) {
        List<List<Rect>> out = new ArrayList<>();

        List<Integer> rowsCoords = rectangles.stream().map(r -> r.y).distinct().collect(Collectors.toList());
        for (int rowCoords : rowsCoords) {
            List<Rect> cols = rectangles.stream().filter(r -> r.y == rowCoords).collect(Collectors.toList());
            out.add(cols);
        }
        return out;
    }

    private static String getRegionId(int row, int col) {
        return String.format("r%d&c%d", row, col);
    }

    /**
     * 初始化模板
     *
     * @param page pdf页面
     */
    private void initTemplate(PDPage page) {
        tagKeys.clear();
        // 购买方
        HashMap<String, String> regMap = new LinkedHashMap<>();
        regMap.put(InvoiceInfo.Buyer_Name, "名\\s*称\\s*[:：]{1}\\s*(.*)\\s*");
        regMap.put(InvoiceInfo.Buyer_No, "纳税人识别号\\s*[:：]{1}\\s*(.*)\\s*");
        regMap.put(InvoiceInfo.Buyer_AddressPhone, "地\\s*址[、]\\s*电\\s*话\\s*[:：]{1}\\s*(.*)\\s*");
        regMap.put(InvoiceInfo.Buyer_Bank, "开户行及账号\\s*[:：]{1}\\s*(.*)");
        tagKeys.put(Invoice_Buyer_Region, regMap);

        // 价税合计
        regMap = new LinkedHashMap<>();
        regMap.put(InvoiceInfo.PriceTax_Upper, "(\\S*?)\\s*");
        regMap.put(InvoiceInfo.PriceTax_Lower, "[\\(（]{1}小写[）\\)]{1}\\s*(.*)");
        tagKeys.put(Invoice_PriceTax_Region, regMap);

        // 销售方
        regMap = new LinkedHashMap<>();
        regMap.put(InvoiceInfo.Seller_Name, "名\\s*称\\s*[:：]{1}\\s*(.*)\\s*");
        regMap.put(InvoiceInfo.Seller_No, "纳税人识别号\\s*[:：]{1}\\s*(.*)\\s*");
        regMap.put(InvoiceInfo.Seller_AddressPhone, "地\\s*址[、]\\s*电\\s*话\\s*[:：]{1}\\s*(.*)\\s*");
        regMap.put(InvoiceInfo.Seller_Bank, "开户行及账号\\s*[:：]{1}\\s*(.*)");
        tagKeys.put(Invoice_Seller_Region, regMap);
    }
}
