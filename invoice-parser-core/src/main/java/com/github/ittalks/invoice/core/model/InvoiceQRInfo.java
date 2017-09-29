package com.github.ittalks.invoice.core.model;

import com.github.ittalks.invoice.common.exception.NestedException;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by 刘春龙 on 2017/9/6.
 */
public class InvoiceQRInfo {

    String type; // 0101 专票   0104 普票  0110 电子发票
    String invoiceCode; // 发票代码（长度10位或者12位）
    String invoiceNumber;// 发票号码（长度8位）
    String invoiceAmount;// 开具金额、不含税价（普票，电子发票可以不传）
    String billTime;// 开票时间（时间格式为：2017-05-11，现在不支持其他格式）
    String checkCode;// 校验码（专票，机动车票可以不传）
    String shortCheckCode;// 校验码（发票后六位，专票，机动车票可以不传）
    String randomCode;

    // , ,发票代码,发票号码,金额,开票时间,校验码
    // 01,10,011001600111,16046427,20.75,20170821,15979619795705055963,AFA0,
    public InvoiceQRInfo(String qrStr) {
        if (StringUtils.isEmpty(qrStr)) {
            throw new NestedException("invalid qrStr, qrStr is null");
        }

        String[] segs = qrStr.split(",");

        if (segs.length < 7) {
            throw new NestedException("invalid qrStr:<" + qrStr + ">, segment length, pls check it");
        }

        this.type = segs[0] + segs[1];
        this.invoiceCode = segs[2];
        this.invoiceNumber = segs[3];
        this.invoiceAmount = segs[4];
        try {
            Date billTime = new SimpleDateFormat("yyyyMMdd").parse(segs[5]);
            this.billTime = new SimpleDateFormat("yyyy-MM-dd").format(billTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (!type.equals("0101")) {// 专票可以不传
            if (segs[6] == null || segs[6].length() < 6) {
                throw new NestedException("invalid checkCode, pls check it");
            }
            this.checkCode = segs[6];
            this.shortCheckCode = this.checkCode.substring(this.checkCode.length() - 6);
        }
        if (segs.length > 7) {
            this.randomCode = segs[7];
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInvoiceCode() {
        return invoiceCode;
    }

    public void setInvoiceCode(String invoiceCode) {
        this.invoiceCode = invoiceCode;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getInvoiceAmount() {
        return invoiceAmount;
    }

    public void setInvoiceAmount(String invoiceAmount) {
        this.invoiceAmount = invoiceAmount;
    }

    public String getBillTime() {
        return billTime;
    }

    public void setBillTime(String billTime) {
        this.billTime = billTime;
    }

    public String getCheckCode() {
        return checkCode;
    }

    public void setCheckCode(String checkCode) {
        this.checkCode = checkCode;
    }

    public String getShortCheckCode() {
        return shortCheckCode;
    }

    public void setShortCheckCode(String shortCheckCode) {
        this.shortCheckCode = shortCheckCode;
    }

    public String getRandomCode() {
        return randomCode;
    }

    public void setRandomCode(String randomCode) {
        this.randomCode = randomCode;
    }

    @Override
    public String toString() {
        return "InvoiceQRInfo{" +
                "type='" + type + '\'' +
                ", invoiceCode='" + invoiceCode + '\'' +
                ", invoiceNumber='" + invoiceNumber + '\'' +
                ", invoiceAmount='" + invoiceAmount + '\'' +
                ", billTime='" + billTime + '\'' +
                ", checkCode='" + checkCode + '\'' +
                ", shortCheckCode='" + shortCheckCode + '\'' +
                ", randomCode='" + randomCode + '\'' +
                '}';
    }
}
