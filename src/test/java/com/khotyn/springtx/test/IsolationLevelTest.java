package com.khotyn.springtx.test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

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

/**
 * 测试 Spring 事务框架的事务隔离级别
 *
 * @author khotyn 4/20/14 3:20 PM
 */
@RunWith(JUnit4.class)
public class IsolationLevelTest {
    /**
     * 事务模板
     */
    private JdbcTemplate jdbcTemplate;
    /**
     * 事务隔离级别为可序列化的事务模板
     */
    private TransactionTemplate serializableTransactionTemplate;
    /**
     * 事务隔离级别为未授权读的事务模板
     */
    private TransactionTemplate readUncommittedTransactionTemplate;
    /**
     * 事务隔离级别为授权读的事务模板
     */
    private TransactionTemplate readCommittedTransactionTemplate;
    /**
     * 事务隔离级别为可重复读的事务模板
     */
    private TransactionTemplate repeatableReadTransactionTemplate;

    @Before
    public void startUp() {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
                "applicationContext.xml");
        serializableTransactionTemplate = applicationContext.getBean(
                "serializableTransactionTemplate", TransactionTemplate.class);
        readUncommittedTransactionTemplate = applicationContext.getBean(
                "readUncommittedTransactionTemplate", TransactionTemplate.class);
        readCommittedTransactionTemplate = applicationContext.getBean(
                "readCommittedTransactionTemplate", TransactionTemplate.class);
        repeatableReadTransactionTemplate = applicationContext.getBean(
                "repeatableReadTransactionTemplate", TransactionTemplate.class);
        DataSource dataSource = applicationContext.getBean("dataSource", DataSource.class);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    public void 测试事务隔离级别为未授权读_会发生脏读() throws ExecutionException, InterruptedException {
        Assert.assertFalse(dirtyRead(readUncommittedTransactionTemplate));
    }

    @Test
    public void 测试事务隔离级别为授权读_不会发生脏读() throws ExecutionException, InterruptedException {
        Assert.assertTrue(dirtyRead(readCommittedTransactionTemplate));
    }

    @Test
    public void 测试事务隔离级别为可重复读_不会发生脏读() throws ExecutionException, InterruptedException {
        Assert.assertTrue(dirtyRead(repeatableReadTransactionTemplate));
    }

    @Test
    public void 测试事务隔离级别为可序列化_不会发生脏读() throws ExecutionException, InterruptedException {
        Assert.assertTrue(dirtyRead(serializableTransactionTemplate));
    }

    @Test
    public void 测试事务隔离级别为未授权读_会发生不可重复读() throws ExecutionException, InterruptedException {
        Assert.assertFalse(nonRepeatableRead(readUncommittedTransactionTemplate));
    }

    @Test
    public void 测试事务隔离级别为授权读_会发生不可重复读() throws ExecutionException, InterruptedException {
        Assert.assertFalse(nonRepeatableRead(readCommittedTransactionTemplate));
    }

    @Test
    public void 测试事务隔离级别为可重复读_不会发生不可重复读() throws ExecutionException, InterruptedException {
        Assert.assertTrue(nonRepeatableRead(repeatableReadTransactionTemplate));
    }

    @Test
    public void 测试事务隔离级别为可序列化_不会发生不可重复读() throws ExecutionException, InterruptedException {
        Assert.assertTrue(nonRepeatableRead(serializableTransactionTemplate));
    }

    @Test
    public void 测试事务隔离级别为未授权读_会发生幻读() throws ExecutionException, InterruptedException {
        Assert.assertFalse(phantomRead(readUncommittedTransactionTemplate));
    }

    @Test
    public void 测试事务隔离级别为授权读_会发生幻读() throws ExecutionException, InterruptedException {
        Assert.assertFalse(phantomRead(readCommittedTransactionTemplate));
    }

    /**
     * WARN：关于这个测试用例，按照定义，如果事务的隔离级别为可重复读，那么应该是可以发生幻读的情况的，但是 MySQL 下有一些不一样：
     * All consistent reads within the same transaction read the snapshot established by the first read.
     * 就是说如果在 MySQL 中，你如果将事务级别设置成可重复读，那么在同一个事务中，后续的读就直接读取第一次读得结果了，不会出现幻读的情况。
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void 测试事务隔离级别为可重复读_不会发生幻读() throws ExecutionException, InterruptedException {
        Assert.assertTrue(phantomRead(repeatableReadTransactionTemplate));
    }

    @Test
    public void 测试事务隔离级别为可序列化_不会发生幻读() throws ExecutionException, InterruptedException {
        Assert.assertTrue(phantomRead(serializableTransactionTemplate));
    }

    /**
     * 模拟脏读现象
     *
     * @param transactionTemplate
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private Boolean dirtyRead(TransactionTemplate transactionTemplate) throws ExecutionException, InterruptedException {
        // 1. 插入一条数据
        jdbcTemplate.update("insert into user (name, password) values (?, ?)", "Huang", "1111112");
        Future future = Executors.newSingleThreadExecutor().submit(() -> {
            return transactionTemplate.execute((status) -> {
                // 2. 事务一读取数据
                String password1 = jdbcTemplate.queryForObject("select password from user where name = ?", String.class, "Huang");
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // 4. 事务一再次读取数据
                String password2 = jdbcTemplate.queryForObject("select password from user where name = ?", String.class, "Huang");
                return password1.equals(password2);
            });
        });
        Executors.newSingleThreadExecutor().submit(() -> {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(100);
                        // 3. 事务二更新数据
                        jdbcTemplate.update("update user set password = ? where name = ?", "1111111", "Huang");
                        TimeUnit.MILLISECONDS.sleep(500);
                        // 5. 事务二再次发生回滚
                        status.setRollbackOnly();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        });

        return (Boolean) future.get();
    }

    /**
     * 模拟可重复读现象
     *
     * @param transactionTemplate
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private Boolean nonRepeatableRead(TransactionTemplate transactionTemplate) throws ExecutionException, InterruptedException {
        // 1. 插入一条数据
        jdbcTemplate.update("insert into user (name, password) values (?, ?)", "Huang", "1111112");
        Future future = Executors.newSingleThreadExecutor().submit(() -> {
            return transactionTemplate.execute((status) -> {
                // 2. 事务一读取数据
                String password1 = jdbcTemplate.queryForObject("select password from user where name = ?", String.class, "Huang");
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // 4. 事务一再次读取数据
                String password2 = jdbcTemplate.queryForObject("select password from user where name = ?", String.class, "Huang");
                return password1.equals(password2);
            });
        });
        Executors.newSingleThreadExecutor().submit(() -> {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(100);
                        // 3. 事务二更新数据
                        jdbcTemplate.update("update user set password = ? where name = ?", "1111111", "Huang");
                        // 5. 事务二提交                        
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        });

        return (Boolean) future.get();
    }

    /**
     * 模拟幻读现象
     *
     * @param transactionTemplate
     * @return
     */
    private Boolean phantomRead(TransactionTemplate transactionTemplate) throws ExecutionException, InterruptedException {
        // 1. 插入两条条数据
        jdbcTemplate.update("insert into user (name, password, age) values (?, ?, ?)", "Huang", "1111112", 25);
        jdbcTemplate.update("insert into user (name, password, age) values (?, ?, ?)", "Wu", "1111112", 24);

        Future future = Executors.newSingleThreadExecutor().submit(() -> {
            return transactionTemplate.execute((status) -> {
                // 2. 事务一读取数据
                Integer count1 = jdbcTemplate.queryForObject("select count(*) from user where age > 20 and age < 30", Integer.class);
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // 4. 事务一再次读取数据 
                Integer count2 = jdbcTemplate.queryForObject("select count(*) from user where age > 20 and age < 30", Integer.class);
                return count1.equals(count2);
            });
        });
        Executors.newSingleThreadExecutor().submit(() -> {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(100);
                        // 3. 事务二再插入一条数据
                        jdbcTemplate.update("insert into user (name, password, age) values (?, ?, ?)", "Shi", "1111112", 26);
                        // 5. 事务二提交                        
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        });

        return (Boolean) future.get();
    }

    @After
    public void cleanUp() {
        jdbcTemplate.update("delete from user");
    }
}
