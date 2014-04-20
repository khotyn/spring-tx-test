package com.khotyn.springtx.test;

/**
 * A test bean to test spring transaction behaviour.
 *
 * @author khotyn 9/14/13 1:48 PM
 */
public interface SpringTxTest {
    public void before();

    public String helloWorld();

    public int mysqlConnectionTest();

    public int simpleTxTest();

    public void txRollbackTest();

    public void txRollbackInnerTxRollbackPropagationRequires();

    public void txRollbackInnerTxRollbackPropagationRequiresNew();

    public void txRollbackInnerTxRollbackPropagationRequiresNew2();

    public void txRollbackInnerTxRollbackPropagationRequiresNew3();

    public void txRollbackInnerTxRollbackPropagationNested();

    public void txRollbackInnerTxRollbackPropagationNested2();

    public void txRollbackInnerTxRollbackPropagationMandatory();

    public void txRollbackInnerTxRollbackPropagationMandatory2();

    public void txRollbackInnerTxRollbackPropagationNever();

    public void txRollbackInnerTxRollbackPropagationNever2();

    public void txRollbackInnerTxRollbackPropagationNever3();

    public void txRollbackInnerTxRollbackPropagationNotSupport();

    public void txRollbackInnerTxRollbackPropagationNotSupport2();

    public void txRollbackInnerTxRollbackPropagationSupports();

    public void txRollbackInnerTxRollbackPropagationSupports2();

    public void cleanUp();
}
