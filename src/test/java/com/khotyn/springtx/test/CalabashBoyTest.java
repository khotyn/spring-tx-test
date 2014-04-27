package com.khotyn.springtx.test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

/**
 * 葫芦娃事务测试
 *
 * @author khotyn 4/21/14 7:01 PM
 */
@RunWith(JUnit4.class)
public class CalabashBoyTest {
    /** 事务模板 */
    private TransactionTemplate transactionTemplate;
    /** JDBC 模板 */
    private JdbcTemplate        jdbcTemplate;
    /** 火娃 */
    private CalabashBoy         fireCalabashBoy;
    /** 水娃 */
    private CalabashBoy         waterCalabashBoy;
    /** 超级葫芦娃 */
    private CalabashBoy         txCalabashBoy;
    /** 又一个超级葫芦娃 */
    private CalabashBoy         aopCalabashBoy;

    @Before
    public void 初始化() {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
            "applicationContext.xml");
        transactionTemplate = applicationContext.getBean("transactionTemplate",
            TransactionTemplate.class);
        DataSource dataSource = applicationContext.getBean("dataSource", DataSource.class);
        jdbcTemplate = new JdbcTemplate(dataSource);
        fireCalabashBoy = applicationContext.getBean("fireCalabashBoy", CalabashBoy.class);
        waterCalabashBoy = applicationContext.getBean("waterCalabashBoy", CalabashBoy.class);
        txCalabashBoy = applicationContext.getBean("txCalabashBoy", CalabashBoy.class);
        aopCalabashBoy = applicationContext.getBean("aopCalabashBoy", CalabashBoy.class);
    }

    @Test
    public void 编程式事务测试() {
        jdbcTemplate.update("insert into calabash_boy (name, mana) values (?, ?)", "Fire Calabash",
            "100");
        jdbcTemplate.update("insert into calabash_boy (name, mana) values (?, ?)",
            "Water Calabash", "200");
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                fireCalabashBoy.skill();
                waterCalabashBoy.skill();
            }
        });

        Assert.assertEquals(0, fireCalabashBoy.getMana());
        Assert.assertEquals(90, waterCalabashBoy.getMana());
    }

    @Test
    public void 编程式事务测试_回滚() {
        jdbcTemplate.update("insert into calabash_boy (name, mana) values (?, ?)", "Fire Calabash",
            "100");
        jdbcTemplate.update("insert into calabash_boy (name, mana) values (?, ?)",
            "Water Calabash", "100");
        try {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    fireCalabashBoy.skill();
                    waterCalabashBoy.skill();
                }
            });
        } catch (Exception e) {
            Assert.assertEquals("法力不够，求给力啊！", e.getMessage());
        }

        Assert.assertEquals(100, fireCalabashBoy.getMana());
        Assert.assertEquals(100, waterCalabashBoy.getMana());
    }

    @Test
    public void 声明式事务_测试() {
        jdbcTemplate.update("insert into calabash_boy (name, mana) values (?, ?)", "Fire Calabash",
            "100");
        jdbcTemplate.update("insert into calabash_boy (name, mana) values (?, ?)",
            "Water Calabash", "200");
        aopCalabashBoy.skill();
        Assert.assertEquals(0, fireCalabashBoy.getMana());
        Assert.assertEquals(90, waterCalabashBoy.getMana());
    }

    @Test
    public void 声明式事务_回滚() {
        jdbcTemplate.update("insert into calabash_boy (name, mana) values (?, ?)", "Fire Calabash",
            "100");
        jdbcTemplate.update("insert into calabash_boy (name, mana) values (?, ?)",
            "Water Calabash", "100");
        try {
            aopCalabashBoy.skill();
        } catch (Exception e) {
            Assert.assertEquals("法力不够，求给力啊！", e.getMessage());
        }
        Assert.assertEquals(100, fireCalabashBoy.getMana());
        Assert.assertEquals(100, waterCalabashBoy.getMana());
    }

    @Test
    public void 注解驱动的声明式事务_测试() {
        jdbcTemplate.update("insert into calabash_boy (name, mana) values (?, ?)", "Fire Calabash",
            "100");
        jdbcTemplate.update("insert into calabash_boy (name, mana) values (?, ?)",
            "Water Calabash", "200");
        txCalabashBoy.skill();
        Assert.assertEquals(0, fireCalabashBoy.getMana());
        Assert.assertEquals(90, waterCalabashBoy.getMana());
    }

    @Test
    public void 注解驱动的声明式事务_回滚() {
        jdbcTemplate.update("insert into calabash_boy (name, mana) values (?, ?)", "Fire Calabash",
            "100");
        jdbcTemplate.update("insert into calabash_boy (name, mana) values (?, ?)",
            "Water Calabash", "100");
        try {
            txCalabashBoy.skill();
        } catch (Exception e) {
            Assert.assertEquals("法力不够，求给力啊！", e.getMessage());
        }
        Assert.assertEquals(100, fireCalabashBoy.getMana());
        Assert.assertEquals(100, waterCalabashBoy.getMana());
    }

    @After
    public void 清理测试数据() {
        jdbcTemplate.update("delete from calabash_boy");
    }
}
