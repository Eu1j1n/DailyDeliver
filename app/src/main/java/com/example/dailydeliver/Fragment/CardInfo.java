package com.example.dailydeliver.Fragment;

public class CardInfo {
    private String kakaopay_purchase_corp;
    private String kakaopay_purchase_corp_code;
    private String kakaopay_issuer_corp;
    private String kakaopay_issuer_corp_code;
    private String bin;
    private String card_type;
    private String install_month;
    private String approved_id;
    private String card_mid;
    private String interest_free_install;
    private String installment_type;
    private String card_item_code;

    public CardInfo(String kakaopay_purchase_corp, String kakaopay_purchase_corp_code, String kakaopay_issuer_corp, String kakaopay_issuer_corp_code, String bin, String card_type, String install_month, String approved_id, String card_mid, String interest_free_install, String installment_type, String card_item_code) {
        this.kakaopay_purchase_corp = kakaopay_purchase_corp;
        this.kakaopay_purchase_corp_code = kakaopay_purchase_corp_code;
        this.kakaopay_issuer_corp = kakaopay_issuer_corp;
        this.kakaopay_issuer_corp_code = kakaopay_issuer_corp_code;
        this.bin = bin;
        this.card_type = card_type;
        this.install_month = install_month;
        this.approved_id = approved_id;
        this.card_mid = card_mid;
        this.interest_free_install = interest_free_install;
        this.installment_type = installment_type;
        this.card_item_code = card_item_code;
    }

    public String getKakaopay_purchase_corp() {
        return kakaopay_purchase_corp;
    }

    public void setKakaopay_purchase_corp(String kakaopay_purchase_corp) {
        this.kakaopay_purchase_corp = kakaopay_purchase_corp;
    }

    public String getKakaopay_purchase_corp_code() {
        return kakaopay_purchase_corp_code;
    }

    public void setKakaopay_purchase_corp_code(String kakaopay_purchase_corp_code) {
        this.kakaopay_purchase_corp_code = kakaopay_purchase_corp_code;
    }

    public String getKakaopay_issuer_corp() {
        return kakaopay_issuer_corp;
    }

    public void setKakaopay_issuer_corp(String kakaopay_issuer_corp) {
        this.kakaopay_issuer_corp = kakaopay_issuer_corp;
    }

    public String getKakaopay_issuer_corp_code() {
        return kakaopay_issuer_corp_code;
    }

    public void setKakaopay_issuer_corp_code(String kakaopay_issuer_corp_code) {
        this.kakaopay_issuer_corp_code = kakaopay_issuer_corp_code;
    }

    public String getBin() {
        return bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }

    public String getCard_type() {
        return card_type;
    }

    public void setCard_type(String card_type) {
        this.card_type = card_type;
    }

    public String getInstall_month() {
        return install_month;
    }

    public void setInstall_month(String install_month) {
        this.install_month = install_month;
    }

    public String getApproved_id() {
        return approved_id;
    }

    public void setApproved_id(String approved_id) {
        this.approved_id = approved_id;
    }

    public String getCard_mid() {
        return card_mid;
    }

    public void setCard_mid(String card_mid) {
        this.card_mid = card_mid;
    }

    public String getInterest_free_install() {
        return interest_free_install;
    }

    public void setInterest_free_install(String interest_free_install) {
        this.interest_free_install = interest_free_install;
    }

    public String getInstallment_type() {
        return installment_type;
    }

    public void setInstallment_type(String installment_type) {
        this.installment_type = installment_type;
    }

    public String getCard_item_code() {
        return card_item_code;
    }

    public void setCard_item_code(String card_item_code) {
        this.card_item_code = card_item_code;
    }
}

