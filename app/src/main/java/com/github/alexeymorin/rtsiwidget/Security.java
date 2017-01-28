package com.github.alexeymorin.rtsiwidget;

public class Security {
    public final String secId;
    public final String currentValue;
    public final String lastChangePrc;
    public final String time;
    public final String tradeDate;

    public Security(String secId, String currentValue, String lastChangePrc, String time, String tradeDate) {
        this.secId = secId;
        this.currentValue = currentValue;
        this.lastChangePrc = lastChangePrc;
        this.time = time;
        this.tradeDate = tradeDate;
    }
}
