package com.khotyn.springtx.test;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * 抽象的葫芦娃，提供法力计算的逻辑
 *
 * @author khotyn 4/21/14 7:12 PM
 */
public abstract class AbstractCalabashBoy implements InitializingBean, CalabashBoy {
    /** JDBC 模板 */
    private JdbcTemplate jdbcTemplate;
    /** 数据库模板 */
    private DataSource   dataSource;
    protected String     name;
    protected int        manaConsume;

    /**
     * 获取当前法力值的大小
     * @return
     */
    public int getMana() {
        return jdbcTemplate.queryForObject("select mana from calabash_boy where name = ?",
            Integer.class, getName());
    }

    /**
     * 加入法力消耗的逻辑，具体的技能效果由 doSkill() 方法实现
     */
    @Override
    public void skill() {
        if (getManaConsume() > getMana()) {
            throw new RuntimeException("法力不够，求给力啊！");
        }

        jdbcTemplate.update("update calabash_boy set mana = ? where name = ?", getMana()
                                                                               - getManaConsume(),
            getName());
        doSkill();
    }

    /**
     * 具体的技能
     */
    public abstract void doSkill();

    @Override
    public void afterPropertiesSet() throws Exception {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public int getManaConsume() {
        return manaConsume;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setManaConsume(int manaConsume) {
        this.manaConsume = manaConsume;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
