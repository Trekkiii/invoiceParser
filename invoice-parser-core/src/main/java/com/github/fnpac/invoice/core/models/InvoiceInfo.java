package com.github.fnpac.invoice.core.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 刘春龙 on 2017/10/16.
 */
public class InvoiceInfo {

    //包括发票代码，发票号码，开票日期，校验码等信息
    public static final String Invoice_Info = "Invoice_Info";
    //发票代码
    public static final String Invoice_Code = "Invoice_Code";
    //发票号码
    public static final String Invoice_No = "Invoice_No";
    //发票代码
    public static final String Invoice_Date = "Invoice_Date";
    //校验码
    public static final String Invoice_Checksum = "Invoice_Checksum";

    //购买方信息
    public static final String Buyer_Info = "Buyer_Info";
    public static final String Buyer_Name = "Buyer_Name";
    public static final String Buyer_AddressPhone = "Buyer_AddressPhone";
    //纳税人识别号
    public static final String Buyer_No = "Buyer_No";
    public static final String Buyer_Bank = "Buyer_Bank";

    //销售方信息
    public static final String Seller_Info = "Seller_Info";
    public static final String Seller_Name = "Seller_Name";
    public static final String Seller_AddressPhone = "Seller_AddressPhone";
    //纳税人识别号
    public static final String Seller_No = "Seller_No";
    public static final String Seller_Bank = "Seller_Bank";

    //机器编码
    public static final String Machine_Code = "Machine_Code";
    //密码区
    public static final String Password = "Password";

    //收款人
    public static final String Payee = "Payee";
    //复核
    public static final String Checker = "Checker";
    //开票人
    public static final String Drawer = "Drawer";

    //价税合计
    public static final String PriceTax_Info = "PriceTax_Info";
    //价税大写
    public static final String PriceTax_Upper = "PriceTax_Upper";
    //价税小写
    public static final String PriceTax_Lower = "PriceTax_Lower";

    //总金额
    public static final String Total_Money = "Total_Money";
    //总税额
    public static final String Total_Tax = "Total_Tax";

    //保存信息的值
    private Map<String, String> mapValues = new HashMap<>();

    public Map<String, String> getMapValues() {
        return mapValues;
    }

    public void setMapValues(Map<String, String> mapValues) {
        this.mapValues = mapValues;
    }

    public void setMapValue(String key, String value) {
        mapValues.put(key, value);
    }
}
