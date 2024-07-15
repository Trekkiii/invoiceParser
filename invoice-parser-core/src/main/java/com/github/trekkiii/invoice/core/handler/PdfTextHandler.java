package com.github.trekkiii.invoice.core.handler;

import com.github.trekkiii.invoice.common.exception.NestedException;
import com.github.trekkiii.invoice.core.handler.template.InvoiceTemplate;
import com.github.trekkiii.invoice.core.models.InvoiceInfo;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.springframework.util.StringUtils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.trekkiii.invoice.core.handler.Commons.*;


/**
 * Created by 刘春龙 on 2017/10/16.
 */
@Deprecated
public class PdfTextHandler {

    private static final Logger logger = Logger.getLogger(PdfTextHandler.class.getName());

    // 省份解析
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
        tagKeys.put(InvoiceTemplate.Invoice_Info_Region, regMap);

        // 购买方
        regMap = new HashMap<>();
        regMap.put(InvoiceInfo.Buyer_Name, "名\\s*称\\s*[:：]{1}");
        regMap.put(InvoiceInfo.Buyer_No, "纳税人识别号\\s*[:：]{1}");
        regMap.put(InvoiceInfo.Buyer_AddressPhone, "地\\s*址[、]电\\s*话\\s*[:：]{1}");
        regMap.put(InvoiceInfo.Buyer_Bank, "开户行及账号\\s*[:：]{1}");
        tagKeys.put(InvoiceTemplate.Invoice_Buyer_Region, regMap);

        // 销售方
        regMap = new HashMap<>();
        regMap.put(InvoiceInfo.Seller_Name, "名\\s*称\\s*[:：]{1}");
        regMap.put(InvoiceInfo.Seller_No, "纳税人识别号\\s*[:：]{1}");
        regMap.put(InvoiceInfo.Seller_AddressPhone, "地\\s*址[、]电\\s*话\\s*[:：]{1}");
        regMap.put(InvoiceInfo.Seller_Bank, "开户行及账号\\s*[:：]{1}");
        tagKeys.put(InvoiceTemplate.Invoice_Seller_Region, regMap);

        // 机器编号
        regMap = new HashMap<>();
        regMap.put(InvoiceInfo.Machine_Code, "机器编号\\s*[:：]{1}");
        tagKeys.put(InvoiceTemplate.Invoice_MachineCode_Region, regMap);

        // 收款人,复核,开票人
        // Map里的元素顺序不可变
        regMap = new LinkedHashMap<>();
        regMap.put(InvoiceInfo.Payee, "收\\s*款\\s*人\\s*[:：]{1}\\s*(\\S*)\\s*");
        regMap.put(InvoiceInfo.Checker, "复\\s*核\\s*[:：]{1}\\s*(\\S*)\\s*");
        regMap.put(InvoiceInfo.Drawer, "开\\s*票\\s*人\\s*[:：]{1}\\s*(\\S*)");
        tagKeys.put(InvoiceTemplate.Invoice_Persons_Region, regMap);

        // 价税合计
        // Map里的元素顺序不可变
        regMap = new LinkedHashMap<>();
        regMap.put(InvoiceInfo.PriceTax_Upper, "价税合计\\s*[\\(（]{1}大写[）\\)]{1}\\s*(\\S*)\\s*");
        regMap.put(InvoiceInfo.PriceTax_Lower, "[\\(（]{1}小写[）\\)]{1}\\s*(.*)");
        tagKeys.put(InvoiceTemplate.Invoice_PriceTax_Region, regMap);

        // 总金额、总税额
        // Map里的元素顺序不可变
        regMap = new LinkedHashMap<>();
        regMap.put(InvoiceInfo.Total_Money, "([￥¥]\\s*\\d{1,}\\.\\d{1,2})\\s*");
        regMap.put(InvoiceInfo.Total_Tax, "([￥¥]\\s*\\d{1,}\\.\\d{1,2}|\\*)");
        tagKeys.put(InvoiceTemplate.Invoice_Total_Region, regMap);

        // 密码区。特殊处理。不设计在Map中
    }


    /**
     * 模板
     */
    private Map<String, Rectangle> keyRegions;

    public PdfTextHandler() {
    }

    /**
     * 检查发票pdf的长宽比例是否合法
     *
     * @param pdDocument
     */
    public void check(PDDocument pdDocument) {
        // 检查宽高比
        BigDecimal width = new BigDecimal(pdDocument.getPage(0).getMediaBox().getWidth());
        BigDecimal height = new BigDecimal(pdDocument.getPage(0).getMediaBox().getHeight());
        BigDecimal scale = width.divide(height, 2, BigDecimal.ROUND_HALF_UP);
        if (!InvoiceTemplate.isAcceptable(scale)) {
            throw new NestedException("Invalid page size, invalid width/height scale");
        }
    }

    private void initTemplate(String sender, String province) {
        keyRegions = InvoiceTemplate.getTemplate(sender, province);
    }

    public InvoiceInfo parsePageByTemplate(String sender, PDPage pdPage) {

        try {
            // 预提取，获取发票省份，根据省份选择模板
            String province = getProvince(pdPage);
            if (StringUtils.isEmpty(province)) {
                throw new NestedException("Invalid pdf");
            }

            // 初始化模板
            initTemplate(sender, province);

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
        for (String regionKey : keyRegions.keySet()) {
            Rectangle rectangle = keyRegions.get(regionKey);// 根据key，获取对应的区域坐标

            Rectangle2D rect = new Rectangle2D.Double(rectangle.getX(), rectangle.getY(),
                    rectangle.getWidth(), rectangle.getHeight());
            stripper.addRegion(regionKey, rect);
        }
        // 提取文本
        stripper.extractRegions(pdPage);
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
                // 去除unicode为12288全角空格字符
                regionText = regionText.replace(EM_SPACE, ' ').trim();
                Map<String, String> regMap = tagKeys.get(regionKey);
                if (regionKey.equals(InvoiceTemplate.Invoice_PriceTax_Region) || regionKey.equals(InvoiceTemplate.Invoice_Persons_Region)
                        || regionKey.equals(InvoiceTemplate.Invoice_Total_Region)) {// 价税合计 & 收款人,复核,开票人 & 总金额、总税额

                    String[] items = splitByLF(regionText);
                    if (items != null) {
                        for (String item : items) {
                            item = item.trim();
                            if (regMap != null) {

                                // 收 款 人: 李文莉 复 核: 张瀚 开 票 人: 牛蕊
                                // 收款人\s*[:：]{1}\s*(\S*)\s*复核\s*[:：]{1}\s*(\S*)\s*开票人\s*[:：]{1}\s*(\S*)
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
                    }
                } else if (regionKey.equals(InvoiceTemplate.Invoice_Password_Region)) {// 密码区

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

        String[] items = splitByLF(regionText);
        if (items != null) {
            for (String item : items) {
                item = item.trim();
                if (regMap != null) {
                    // 名　　　　称:滴滴出行科技有限公司   全角
                    // 名        称: 沈阳京东世纪贸易有限公司  半角
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

    private String[] splitByLF(String text) {
        if (text.contains(LF_WIN)) {
            return text.split(LF_WIN);
        } else if (text.contains(LF_LINUX)) {
            return text.split(LF_LINUX);
        } else if (text.contains(LF_MAC)) {
            return text.split(LF_MAC);
        }
        return new String[]{text};
    }
}
