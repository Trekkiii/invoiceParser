package com.github.fnpac.invoice.core.handler;

import com.github.fnpac.invoice.common.exception.NestedException;
import com.github.fnpac.invoice.core.model.InvoiceInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.fnpac.invoice.core.handler.InvoiceTemplate.*;

/**
 * Created by 刘春龙 on 2017/10/16.
 */
public class PDFTextHandler {

    private static final Logger logger = Logger.getLogger(PDFTextHandler.class.getName());

    //换行符
    private static final String LF = "\r\n";

    private static final BigDecimal width = new BigDecimal(624);
    private static final BigDecimal height = new BigDecimal(396);
    private static final BigDecimal WidthHeightRate = width.divide(height, 2, BigDecimal.ROUND_HALF_UP);

    private static final String provinceKey = "provinceKey";
    private static final String provinceRegExp = "(\\S+?)增值.+?发票";
    private static final Rectangle provinceRect = new Rectangle(150, 5, 430 - 150, 70 - 5);

    private static final Map<String, Map<String, String>> tagKeys;

    static {
        tagKeys = new HashMap<>();

        // 发票信息
        Map<String, String> regMap = new HashMap<>();
        regMap.put(InvoiceInfo.Invoice_Code, "发票代码\\s*[:：]{1}");
        regMap.put(InvoiceInfo.Invoice_No, "发票号码\\s*[:：]{1}");
        regMap.put(InvoiceInfo.Invoice_Date, "开票日期\\s*[:：]{1}");
        regMap.put(InvoiceInfo.Invoice_Checksum, "校\\s*验\\s*码\\s*[:：]{1}");
        tagKeys.put(Invoice_Info_Region, regMap);

        // 购买方
        regMap = new HashMap<>();
        regMap.put(InvoiceInfo.Buyer_Name, "名\\s称\\s*[:：]{1}");
        regMap.put(InvoiceInfo.Buyer_No, "纳税人识别号\\s*[:：]{1}");
        regMap.put(InvoiceInfo.Buyer_AddressPhone, "地\\s*址[、]电\\s*话\\s*[:：]{1}");
        regMap.put(InvoiceInfo.Buyer_Bank, "开户行及账号\\s*[:：]{1}");
        tagKeys.put(Invoice_Buyer_Region, regMap);

        // 销售方
        regMap = new HashMap<>();
        regMap.put(InvoiceInfo.Seller_Name, "名\\s*称\\s*[:：]{1}");
        regMap.put(InvoiceInfo.Seller_No, "纳税人识别号\\s*[:：]{1}");
        regMap.put(InvoiceInfo.Seller_AddressPhone, "地\\s*址[、]电\\s*话\\s*[:：]{1}");
        regMap.put(InvoiceInfo.Seller_Bank, "开户行及账号\\s*[:：]{1}");
        tagKeys.put(Invoice_Seller_Region, regMap);

        // 机器编号
        regMap = new HashMap<>();
        regMap.put(InvoiceInfo.Machine_Code, "机器编号\\s*[:：]{1}");
        tagKeys.put(Invoice_MachineCode_Region, regMap);

        // 收款人,复核,开票人
        // Map里的元素顺序不可变
        regMap = new LinkedHashMap<>();
        regMap.put(InvoiceInfo.Payee, "收款人\\s*[:：]{1}\\s*(\\S*)\\s*");
        regMap.put(InvoiceInfo.Checker, "复核\\s*[:：]{1}\\s*(\\S*)\\s*");
        regMap.put(InvoiceInfo.Drawer, "开票人\\s*[:：]{1}\\s*(\\S*)");
        tagKeys.put(Invoice_Persons_Region, regMap);

        // 价税合计
        // Map里的元素顺序不可变
        regMap = new LinkedHashMap<>();
        regMap.put(InvoiceInfo.PriceTax_Upper, "价税合计\\s*[\\(（]{1}大写[）\\)]{1}\\s*(\\S*)\\s*");
        regMap.put(InvoiceInfo.PriceTax_Lower, "[\\(（]{1}小写[）\\)]{1}\\s*(.*)");
        tagKeys.put(Invoice_PriceTax_Region, regMap);

        // 总金额、总税额
        // Map里的元素顺序不可变
        regMap = new LinkedHashMap<>();
        regMap.put(InvoiceInfo.Total_Money, "([￥¥]\\s*\\d{1,}\\.\\d{1,2})\\s*");
        regMap.put(InvoiceInfo.Total_Tax, "([￥¥]\\s*\\d{1,}\\.\\d{1,2}|\\*)");
        tagKeys.put(Invoice_Total_Region, regMap);

        // 密码区。特殊处理。不设计在Map中
    }


    /**
     * 模板
     */
    private Map<String, Rectangle> keyRegions;

    public PDFTextHandler() {
    }

    /**
     * 检查发票pdf的长宽比例是否合法
     *
     * @param pdDocument
     */
    public void check(PDDocument pdDocument) {
        // 检查宽高比
        BigDecimal pageWidth = new BigDecimal(pdDocument.getPage(0).getMediaBox().getWidth());
        BigDecimal pageHeight = new BigDecimal(pdDocument.getPage(0).getMediaBox().getHeight());
        float diffRate = pageWidth.divide(pageHeight, 2, BigDecimal.ROUND_HALF_UP)
                .divide(WidthHeightRate, 2, BigDecimal.ROUND_HALF_UP).floatValue();
        if (diffRate > 1.05 || diffRate < 0.95) {
            throw new NestedException("Invalid page size, invalid width/height scale");
        }
    }

    private void initTemplate(String province) {
        keyRegions = InvoiceTemplate.getTemplate(province);
    }

    public InvoiceInfo parsePageByTemplate(PDPage pdPage) {

        try {
            // 预提取，获取发票省份，根据省份选择模板
            String province = getProvince(pdPage);
            if (StringUtils.isEmpty(province)) {
                throw new NestedException("Invalid pdf");
            }

            // 初始化模板
            initTemplate(province);

            PDFTextStripperByArea stripper = new PDFTextStripperByArea();
            stripper.setSortByPosition(true);

            // 首先，将所有的区域中的文本抽取出来
            extractTextByRegion(pdPage, stripper);

            // 将抽取出来的文本读取出来
            return readTextByKeys(stripper);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 获取发票的省份
     *
     * @return
     */
    private String getProvince(PDPage pdPage) throws IOException {
        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        stripper.setSortByPosition(true);

        Rectangle2D rect = new Rectangle2D.Double(provinceRect.getX(), provinceRect.getY(),
                provinceRect.getWidth(), provinceRect.getHeight());
        stripper.addRegion(provinceKey, rect);

        stripper.extractRegions(pdPage);
        String regionText = stripper.getTextForRegion(provinceKey);
        Pattern pattern = Pattern.compile(provinceRegExp);
        Matcher matcher = pattern.matcher(regionText);
        if (matcher.find()) {
            if (matcher.groupCount() > 0) {
                return matcher.group(1);
            }
        }
        return null;
    }

    /**
     * 从特定区域中提取文本
     *
     * @param pdPage   一页pdf
     * @param stripper PDF文本提取器
     */
    private void extractTextByRegion(PDPage pdPage, PDFTextStripperByArea stripper) throws IOException {
        long startTime = System.currentTimeMillis();
        for (String regionKey : keyRegions.keySet()) {
            Rectangle rectangle = keyRegions.get(regionKey);// 根据key，获取对应的区域坐标

            Rectangle2D rect = new Rectangle2D.Double(rectangle.getX(), rectangle.getY(),
                    rectangle.getWidth(), rectangle.getHeight());
            stripper.addRegion(regionKey, rect);
        }
        // 提取文本
        stripper.extractRegions(pdPage);
        logger.info("pdfbox解析用时：" + String.valueOf(System.currentTimeMillis() - startTime));
    }

    /**
     * 读取文本
     *
     * @param stripper
     * @return
     */
    private InvoiceInfo readTextByKeys(PDFTextStripperByArea stripper) {
        InvoiceInfo invoiceInfo = new InvoiceInfo();
        for(String regionKey : keyRegions.keySet()) {
            String regionText = stripper.getTextForRegion(regionKey);

            if (regionText != null) {
                regionText = regionText.trim();
                Map<String, String> regMap = tagKeys.get(regionKey);
                if (regionKey.equals(Invoice_PriceTax_Region) || regionKey.equals(Invoice_Persons_Region)
                        || regionKey.equals(Invoice_Total_Region)) {// 价税合计 & 收款人,复核,开票人 & 总金额、总税额
                    String[] items = regionText.split(LF);
                    for (String item : items) {
                        item = item.trim();
                        if (regMap != null) {

                            List<String> keyList = new ArrayList<>();
                            StringBuilder regbuff = new StringBuilder("");
                            for (Map.Entry<String, String> regEntry : regMap.entrySet()) {
                                regbuff.append(regEntry.getValue());
                                keyList.add(regEntry.getKey());
                            }

                            Pattern pattern = Pattern.compile(regbuff.toString());
                            Matcher matcher = pattern.matcher(item);
                            if (matcher.find()) {
                                int groupCount = matcher.groupCount();
                                if (groupCount == keyList.size()) {
                                    for (int i = 0; i < groupCount; i++) {
                                        invoiceInfo.setMapValue(keyList.get(i), matcher.group(i + 1));
                                    }
                                }
                            }
                        }
                    }
                } else if (regionKey.equals(Invoice_Password_Region)) {// 密码区

                } else {// 读取通用数据项
                    commonReadTextItem(regMap, regionText, invoiceInfo);
                }
            }
        }
        return invoiceInfo;
    }

    /**
     * 读取通用数据项
     *
     * @param regMap
     * @param regionText
     * @param invoiceInfo
     */
    private void commonReadTextItem(Map<String, String> regMap, String regionText,
                                    InvoiceInfo invoiceInfo) {
        String[] items = regionText.split(LF);
        for (String item : items) {
            item = item.trim();
            if (regMap != null) {
                for (Map.Entry<String, String> regEntry : regMap.entrySet()) {
                    Pattern pattern = Pattern.compile(regEntry.getValue());
                    Matcher matcher = pattern.matcher(item);
                    if (matcher.find()) {
                        int lastIndex = matcher.end();
                        String extText = item.substring(lastIndex).trim();
                        invoiceInfo.setMapValue(regEntry.getKey(), extText);
                        break;
                    }
                }
            }
        }
    }
}
