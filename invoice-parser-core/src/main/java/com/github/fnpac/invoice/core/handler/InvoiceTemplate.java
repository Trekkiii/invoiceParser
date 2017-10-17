package com.github.fnpac.invoice.core.handler;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by 刘春龙 on 2017/10/17.
 */
public class InvoiceTemplate {

    // region Keys
    public static final String Invoice_Info_Region = "Invoice_Info_Region";
    public static final String Invoice_Buyer_Region = "Invoice_Buyer_Region";
    public static final String Invoice_Seller_Region = "Invoice_Seller_Region";
    public static final String Invoice_Persons_Region = "Invoice_Persons_Region";
    public static final String Invoice_PriceTax_Region = "Invoice_PriceTax_Region";
    public static final String Invoice_Total_Region = "Invoice_Total_Region";
    public static final String Invoice_MachineCode_Region = "Invoice_MachineCode_Region";
    public static final String Invoice_Password_Region = "Invoice_Password_Region";

    /**
     * 模板
     */
    private final static Map<String, Map<String, Rectangle>> templateMap;

    private final static String defaultProvince = "北京";

    static {

        templateMap = new HashMap<>();
        Map<String, Rectangle> keyRegions = new HashMap<>();
        keyRegions.put(Invoice_Info_Region, new Rectangle(435, 14, 620 - 435, 90 - 14));
        keyRegions.put(Invoice_Buyer_Region, new Rectangle(55, 94, 360 - 55, 150 - 94));
        keyRegions.put(Invoice_Seller_Region, new Rectangle(55, 300, 360 - 55, 357 - 300));
        keyRegions.put(Invoice_Persons_Region, new Rectangle(30, 359, 410 - 30, 380 - 359));
        keyRegions.put(Invoice_PriceTax_Region, new Rectangle(25, 280, 590 - 25, 298 - 280));
        keyRegions.put(Invoice_Total_Region, new Rectangle(407, 255, 590 - 407, 280 - 255));
        keyRegions.put(Invoice_MachineCode_Region, new Rectangle(25, 70, 160 - 25, 90 - 70));
        keyRegions.put(Invoice_Password_Region, new Rectangle(380, 94, 590 - 380, 150 - 94));

        templateMap.put("北京", keyRegions);
    }

    public static Map<String, Rectangle> getTemplate(String province) {

        if (!templateMap.containsKey(province)) {
            province = defaultProvince;
        }
        return templateMap.get(province);
    }
}
