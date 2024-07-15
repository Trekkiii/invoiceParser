package com.github.trekkiii.invoice.core.handler.template;

import org.springframework.util.StringUtils;

import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 刘春龙 on 2017/10/17.
 */
public class InvoiceTemplate {

    // Region Keys
    public static final String Invoice_Info_Region = "Invoice_Info_Region";
    public static final String Invoice_Buyer_Region = "Invoice_Buyer_Region";
    public static final String Invoice_Seller_Region = "Invoice_Seller_Region";
    public static final String Invoice_Persons_Region = "Invoice_Persons_Region";
    public static final String Invoice_PriceTax_Region = "Invoice_PriceTax_Region";
    public static final String Invoice_Total_Region = "Invoice_Total_Region";
    public static final String Invoice_MachineCode_Region = "Invoice_MachineCode_Region";
    public static final String Invoice_Password_Region = "Invoice_Password_Region";

    private final static String defaultProvince = "北京";// 默认使用北京的发票位置模板
    private final static String defaultCorporation = "default";// 默认不区分公司

    /**
     * 模板
     */
    private static final List<BigDecimal> widthHeightScales;// 发票宽高比例模板
    private static final Map<String, String> mailCorporationMapping;// 邮箱公司映射
    // - Province
    //      - Corporation
    //          - keyRegions
    private final static Map<String, Map<String, Map<String, Rectangle>>> templateMap;// 发票位置模板

    static {

        /**
         * 发票宽高比例模板
         */
        widthHeightScales = new ArrayList<>();
        // 通用，北京
        BigDecimal width = new BigDecimal(623.583);
        BigDecimal height = new BigDecimal(396.0);
        widthHeightScales.add(width.divide(height, 2, BigDecimal.ROUND_HALF_UP));
        // 辽宁
        width = new BigDecimal(609.44);
        height = new BigDecimal(394.01);
        widthHeightScales.add(width.divide(height, 2, BigDecimal.ROUND_HALF_UP));
        // 天津
        width = new BigDecimal(610.0);
        height = new BigDecimal(394.0);
        widthHeightScales.add(width.divide(height, 2, BigDecimal.ROUND_HALF_UP));

        /**
         * 邮箱公司映射
         */
        mailCorporationMapping = new HashMap<>();
//        mailCorporationMapping.put("", "");

        /**
         * 发票位置模板
         */
        templateMap = new HashMap<>();
        // 默认使用北京的发票位置模板，默认不区分公司
        Map<String, Rectangle> keyRegions = new HashMap<>();
        keyRegions.put(Invoice_Info_Region, new Rectangle(435, 14, 620 - 435, 90 - 14));
        keyRegions.put(Invoice_Buyer_Region, new Rectangle(55, 94, 360 - 55, 150 - 94));
        keyRegions.put(Invoice_Seller_Region, new Rectangle(55, 300, 360 - 55, 357 - 300));
        keyRegions.put(Invoice_Persons_Region, new Rectangle(30, 359, 410 - 30, 380 - 359));
        keyRegions.put(Invoice_PriceTax_Region, new Rectangle(25, 280, 590 - 25, 298 - 280));
        keyRegions.put(Invoice_Total_Region, new Rectangle(407, 255, 590 - 407, 280 - 255));
        keyRegions.put(Invoice_MachineCode_Region, new Rectangle(25, 70, 160 - 25, 90 - 70));
        keyRegions.put(Invoice_Password_Region, new Rectangle(380, 94, 590 - 380, 150 - 94));
        Map<String, Map<String, Rectangle>> corpKeyRegions = new HashMap<>();
        corpKeyRegions.put(defaultCorporation, keyRegions);
        templateMap.put(defaultProvince, corpKeyRegions);
        // 辽宁
        keyRegions = new HashMap<>();
        keyRegions.put(Invoice_Info_Region, new Rectangle(425, 5, 592 - 425, 77 - 5));
        keyRegions.put(Invoice_Buyer_Region, new Rectangle(42, 84, 349 - 42, 143 - 84));
        keyRegions.put(Invoice_Seller_Region, new Rectangle(46, 293, 346 - 46, 346 - 293));
        keyRegions.put(Invoice_Persons_Region, new Rectangle(23, 350, 433 - 23, 374 - 350));
        keyRegions.put(Invoice_PriceTax_Region, new Rectangle(24, 270, 590 - 24, 290 - 270));
        keyRegions.put(Invoice_Total_Region, new Rectangle(392, 255, 592 - 392, 269 - 255));
        keyRegions.put(Invoice_MachineCode_Region, new Rectangle(22, 60, 180 - 22, 80 - 60));
        keyRegions.put(Invoice_Password_Region, new Rectangle(365, 85, 592 - 365, 143 - 85));
        corpKeyRegions = new HashMap<>();
        corpKeyRegions.put(defaultCorporation, keyRegions);
        templateMap.put("辽宁", corpKeyRegions);
        // 深圳
        templateMap.put("深圳", corpKeyRegions);
        // 天津
        keyRegions = new HashMap<>();
        keyRegions.put(Invoice_Info_Region, new Rectangle(417, 5, 592 - 417, 77 - 5));
        keyRegions.put(Invoice_Buyer_Region, new Rectangle(44, 83, 346 - 44, 143 - 83));
        keyRegions.put(Invoice_Seller_Region, new Rectangle(45, 293, 346 - 45, 349 - 293));
        keyRegions.put(Invoice_Persons_Region, new Rectangle(22, 350, 395 - 22, 375 - 350));
        keyRegions.put(Invoice_PriceTax_Region, new Rectangle(22, 270, 590 - 22, 290 - 270));
        keyRegions.put(Invoice_Total_Region, new Rectangle(390, 247, 593 - 390, 269 - 247));
        keyRegions.put(Invoice_MachineCode_Region, new Rectangle(21, 66, 180 - 21, 80 - 66));
        keyRegions.put(Invoice_Password_Region, new Rectangle(362, 82, 590 - 362, 143 - 82));
        corpKeyRegions = new HashMap<>();
        corpKeyRegions.put(defaultCorporation, keyRegions);
        templateMap.put("天津", corpKeyRegions);
    }

    /**
     * @param sender   发票所属邮件的发件人
     * @param province 发票的省份
     * @return
     */
    public static Map<String, Rectangle> getTemplate(String sender, String province) {

        if (StringUtils.isEmpty(province) || !templateMap.containsKey(province)) {
            province = defaultProvince;
        }
        Map<String, Map<String, Rectangle>> corpKeyRegions = templateMap.get(province);

        String corporation = defaultCorporation;// 开具发票的公司，默认default
        if (mailCorporationMapping.containsKey(sender)) {
            corporation = mailCorporationMapping.get(sender);
        }
        if (!corpKeyRegions.containsKey(corporation)) {
            corporation = defaultCorporation;
        }
        return corpKeyRegions.get(corporation);
    }

    /**
     * 判断是否是可处理的发票宽高比例
     *
     * @param scaleValue 发票的宽高比例
     * @return
     */
    public static boolean isAcceptable(BigDecimal scaleValue) {
        boolean flag = false;
        for (BigDecimal scale : widthHeightScales) {
            float diff = scaleValue.divide(scale, 2, BigDecimal.ROUND_HALF_UP).floatValue();
            if (diff >= 0.95 && diff <= 1.05) {
                flag = true;// 可处理
                break;
            }
        }
        return flag;
    }
}
