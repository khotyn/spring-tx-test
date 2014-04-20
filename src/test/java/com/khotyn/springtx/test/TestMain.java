package com.khotyn.springtx.test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.UnexpectedRollbackException;

/**
 * @author khotyn 9/14/13 1:15 PM
 */
@RunWith(JUnit4.class)
public class TestMain {
    private SpringTxTest springTxTest;

    @Before
    public void startUp() {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
                "applicationContext.xml");
        springTxTest = applicationContext.getBean("springTxTest", SpringTxTest.class);
        springTxTest.before();
    }

    @Test
    public void testDummy() {
        Assert.assertEquals("Hello, world!", springTxTest.helloWorld());
    }

    @Test
    public void testMySqlConnection() {
        Assert.assertEquals(1, springTxTest.mysqlConnectionTest());
    }

    @Test
    public void testSimpleTx() {
        Assert.assertEquals(1, springTxTest.simpleTxTest());
    }

    @Test
    public void testTxRollback() {
        try {
            springTxTest.txRollbackTest();
        } catch (Exception e) {
            // Do nothing at all.
        } finally {
            Assert.assertEquals(1, springTxTest.mysqlConnectionTest());
        }
    }

    /**
     * PROPAGATION_REQUIRES：内部事务设置了 {@link org.springframework.transaction.TransactionStatus#setRollbackOnly()} 来触发回滚，
     * 外部事务接受到了一个 {@link UnexpectedRollbackException} 也被回滚
     */
    @Test
    public void testTxRollbackInnerTxRollbackPropagationRequires() {
        try {
            springTxTest.txRollbackInnerTxRollbackPropagationRequires();
        } catch (UnexpectedRollbackException e) {
            // Do nothing at all.
        } finally {
            Assert.assertEquals(1, springTxTest.mysqlConnectionTest());
        }

    }

    /**
     * PROPAGATION_REQUIRES_NEW：外部事务发生回滚，内部事务继续提交，不受影响
     */
    @Test
    public void testTxRollbackInnerTxRollbackPropagationRequiresNew() {
        try {
            springTxTest.txRollbackInnerTxRollbackPropagationRequiresNew();
        } catch (Exception e) {
            // Do nothing at all.
        } finally {
            Assert.assertEquals(2, springTxTest.mysqlConnectionTest());
        }

    }

    /**
     * PROPAGATION_REQUIRES_NEW：内部事务通过设置 {@link org.springframework.transaction.TransactionStatus#setRollbackOnly()} 来触发回滚，
     * 外部事务依旧可以不受影响，正常提交
     */
    @Test
    public void testTxRollbackInnerTxRollbackPropagationRequiresNew2() {
        springTxTest.txRollbackInnerTxRollbackPropagationRequiresNew2();
        Assert.assertEquals(2, springTxTest.mysqlConnectionTest());
    }

    /**
     * PROPAGATION_REQUIRES_NEW：内部事务抛出了 {@link RuntimeException} 异常发生了回滚，外部事务接收到这个异常也会发生回滚
     */
    @Test
    public void testTxRollbackInnerTxRollbackPropagationRequiresNew3() {
        try {
            springTxTest.txRollbackInnerTxRollbackPropagationRequiresNew3();
        } catch (RuntimeException e) {
            // Do nothing at all.
        } finally {
            Assert.assertEquals(1, springTxTest.mysqlConnectionTest());
        }

    }

    /**
     * PROPAGATION_NESTED：内部事务通过设置 {@link org.springframework.transaction.TransactionStatus#setRollbackOnly()} 来触发回滚，
     * 外部事务依旧可以不受影响，正常提交
     */
    @Test
    public void testTxRollbackInnerTxRollbackPropagationNested() {
        springTxTest.txRollbackInnerTxRollbackPropagationNested();
        Assert.assertEquals(2, springTxTest.mysqlConnectionTest());
    }

    /**
     * PROPAGATION_NESTED：外部事务通过设置 {@link org.springframework.transaction.TransactionStatus#setRollbackOnly()} 来触发回滚，由于
     * savepoint 在外部事务的开头，所以内部事务应该也会被一起回滚掉
     */
    @Test
    public void testTxRollbackInnerTxRollbackPropagationNested2() {
        springTxTest.txRollbackInnerTxRollbackPropagationNested2();
        Assert.assertEquals(1, springTxTest.mysqlConnectionTest());
    }

    /**
     * PROPAGATION_MANDATORY：强制性的事务，当前的事务上下文中不存在事务的话，会抛出 {@link IllegalTransactionStateException}
     */
    @Test(expected = IllegalTransactionStateException.class)
    public void testTxRollbackInnerTxRollbackPropagationMandatory() {
        springTxTest.txRollbackInnerTxRollbackPropagationMandatory();
    }

    /**
     * PROPAGATION_MANDATORY：强制性的事务，内部的事务发生回滚，那么外围的事务也会发生回滚，表现地和 {@link org.springframework.transaction.TransactionDefinition#PROPAGATION_REQUIRED} 一样，也会抛出 {@link UnexpectedRollbackException}
     */
    @Test(expected = UnexpectedRollbackException.class)
    public void testTxRollbackInnerTxRollbackPropagationMandatory2() {
        springTxTest.txRollbackInnerTxRollbackPropagationMandatory2();
    }

    /**
     * PROPAGATION_NEVER：不允许当前事务上下文中存在事务，如果没有，就正常执行
     */
    @Test
    public void testTxRollbackInnerTxRollbackPropagationNever() {
        springTxTest.txRollbackInnerTxRollbackPropagationNever();
        Assert.assertEquals(2, springTxTest.mysqlConnectionTest());
    }

    /**
     * PROPAGATION_NEVER：不允许当前事务上下文中存在事务，如果有，则抛出 {@link IllegalTransactionStateException}
     */
    @Test(expected = IllegalTransactionStateException.class)
    public void testTxRollbackInnerTxRollbackPropagationNever2() {
        springTxTest.txRollbackInnerTxRollbackPropagationNever2();
    }

    /**
     * PROPAGATION_NEVER：不允许当前事务上下文中存在事务，当两个 NEVER 的嵌套在一起的时候，应该也是能够执行成功的。
     */
    @Test
    public void testTxRollbackInnerTxRollbackPropagationNever3() {
        springTxTest.txRollbackInnerTxRollbackPropagationNever3();
        Assert.assertEquals(3, springTxTest.mysqlConnectionTest());
    }

    /**
     * PROPAGATION_NOT_SUPPORTED：不支持事务，外围的事务回滚不会导致它包含的内容回滚
     */
    @Test
    public void testTxRollbackInnerTxRollbackPropagationNotSupport() {
        springTxTest.txRollbackInnerTxRollbackPropagationNotSupport();
        Assert.assertEquals(2, springTxTest.mysqlConnectionTest());
    }

    /**
     * PROPAGATION_NOT_SUPPORTED：不支持事务，内部发生异常，外部捕获，都不会发生回滚
     */
    @Test
    public void testTxRollbackInnerTxRollbackPropagationNotSupport2() {
        springTxTest.txRollbackInnerTxRollbackPropagationNotSupport2();
        Assert.assertEquals(3, springTxTest.mysqlConnectionTest());
    }

    /**
     * PROPAGATION_SUPPORTS：如果当前事务上下文中没有事务，那么就按照没有事务的方式执行代码
     */
    @Test
    public void testTxRollbackInnerTxRollbackPropagationSupports() {
        try {
            springTxTest.txRollbackInnerTxRollbackPropagationSupports();
        } catch (CustomRuntimeException e) {
            // Do nothing
        } finally {
            Assert.assertEquals(2, springTxTest.mysqlConnectionTest());
        }
    }

    /**
     * PROPAGATION_SUPPORTS：如果当前事务上下文中存在事务，那么合并到当前上下文的事务中去，表现地和 {@link org.springframework.transaction.TransactionDefinition#PROPAGATION_REQUIRED} 一样
     */
    @Test(expected = UnexpectedRollbackException.class)
    public void testTxRollbackInnerTxRollbackPropagationSupports2() {
        springTxTest.txRollbackInnerTxRollbackPropagationSupports2();
    }

    @After
    public void deleteInsertion() {
        springTxTest.cleanUp();
    }
}
