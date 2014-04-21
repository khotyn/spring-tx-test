package com.khotyn.springtx.test;

import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author khotyn 4/21/14 8:00 PM
 */
public class TxCalabashBoy implements CalabashBoy {
    /** 火娃 */
    private CalabashBoy fireCalabashBoy;
    /** 水娃 */
    private CalabashBoy waterCalabashBoy;

    @Override
    public String getName() {
        return "超级葫芦娃";
    }

    @Override
    @Transactional
    public void skill() {
        fireCalabashBoy.skill();
        waterCalabashBoy.skill();
        System.out.println("冰火双重天！");
    }

    @Override
    public int getMana() {
        return 0;
    }

    public void setFireCalabashBoy(CalabashBoy fireCalabashBoy) {
        this.fireCalabashBoy = fireCalabashBoy;
    }

    public void setWaterCalabashBoy(CalabashBoy waterCalabashBoy) {
        this.waterCalabashBoy = waterCalabashBoy;
    }
}
