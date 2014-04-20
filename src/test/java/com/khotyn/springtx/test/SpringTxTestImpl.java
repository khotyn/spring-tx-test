package com.khotyn.springtx.test;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

/**
 * @author khotyn 9/14/13 1:46 PM
 */
public class SpringTxTestImpl implements SpringTxTest {
    private JdbcTemplate jdbcTemplate;
    private TransactionTemplate transactionTemplate;
    private TransactionTemplate requiresNewTransactionTemplate;
    private TransactionTemplate nestedTransactionTemplate;
    private TransactionTemplate mandatoryTransactionTemplate;
    private TransactionTemplate neverTransactionTemplate;
    private TransactionTemplate notSupportedTransactionTemplate;
    private TransactionTemplate supportsTransactionTemplate;

    @Override
    public void before() {
        jdbcTemplate.update("insert into user (name, password) values (?, ?)", "Wu", "1111112");
    }

    @Override
    public String helloWorld() {
        return "Hello, world!";
    }

    @Override
    public int mysqlConnectionTest() {
        return countUser();
    }

    @Override
    public int simpleTxTest() {
        return (Integer) transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus transactionStatus) {
                return countUser();
            }
        });
    }

    @Override
    public void txRollbackTest() {

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                jdbcTemplate.update("insert into user (name, password) values (?, ?)", "Huang",
                        "1111112");
                throw new RuntimeException("Rollback!");
            }
        });
    }

    @Override
    public void txRollbackInnerTxRollbackPropagationRequires() {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                jdbcTemplate.update("insert into user (name, password) values (?, ?)", "Huang",
                        "1111112");
                transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        jdbcTemplate.update("insert into user (name, password) values (?, ?)",
                                "Huang", "1111112");
                        // 内部事务设置了 setRollbackOnly，
                        status.setRollbackOnly();
                    }
                });
            }
        });
    }

    @Override
    public void txRollbackInnerTxRollbackPropagationRequiresNew() {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {

                requiresNewTransactionTemplate.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        jdbcTemplate.update("insert into user (name, password) values (?, ?)",
                                "Huang", "1111112");
                    }
                });

                // 外部事务发生回滚，内部事务应该不受影响还是能够提交
                throw new RuntimeException();
            }
        });
    }

    @Override
    public void txRollbackInnerTxRollbackPropagationRequiresNew2() {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                jdbcTemplate.update("insert into user (name, password) values (?, ?)", "Huang",
                        "1111112");
                // Nested transaction committed.
                requiresNewTransactionTemplate.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        jdbcTemplate.update("insert into user (name, password) values (?, ?)",
                                "Huang", "1111112");
                        // 内部事务发生回滚，但是外部事务不应该发生回滚
                        status.setRollbackOnly();
                    }
                });
            }
        });
    }

    @Override
    public void txRollbackInnerTxRollbackPropagationRequiresNew3() {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                jdbcTemplate.update("insert into user (name, password) values (?, ?)", "Huang",
                        "1111112");

                requiresNewTransactionTemplate.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        jdbcTemplate.update("insert into user (name, password) values (?, ?)",
                                "Huang", "1111112");
                        // 内部事务抛出 RuntimeException，外部事务接收到异常，依旧会发生回滚
                        throw new RuntimeException();
                    }
                });
            }
        });
    }

    @Override
    public void txRollbackInnerTxRollbackPropagationNested() {
        nestedTransactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                jdbcTemplate.update("insert into user (name, password) values (?, ?)", "Huang",
                        "1111112");

                nestedTransactionTemplate.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        jdbcTemplate.update("insert into user (name, password) values (?, ?)",
                                "Huang", "1111112");
                        // 内部事务设置了 rollbackOnly，外部事务应该不受影响，可以继续提交
                        status.setRollbackOnly();
                    }
                });
            }
        });
    }

    @Override
    public void txRollbackInnerTxRollbackPropagationNested2() {
        nestedTransactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                jdbcTemplate.update("insert into user (name, password) values (?, ?)", "Huang",
                        "1111112");

                nestedTransactionTemplate.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        jdbcTemplate.update("insert into user (name, password) values (?, ?)",
                                "Huang", "1111112");
                    }
                });
                // 外部事务设置了 rollbackOnly，内部事务应该也被回滚掉
                transactionStatus.setRollbackOnly();
            }
        });
    }

    @Override
    public void txRollbackInnerTxRollbackPropagationMandatory() {
        mandatoryTransactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                jdbcTemplate.update("insert into user (name, password) values (?, ?)", "Huang",
                        "1111112");
            }
        });
    }

    @Override
    public void txRollbackInnerTxRollbackPropagationMandatory2() {
        nestedTransactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                jdbcTemplate.update("insert into user (name, password) values (?, ?)", "Huang",
                        "1111112");

                mandatoryTransactionTemplate.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        jdbcTemplate.update("insert into user (name, password) values (?, ?)",
                                "Huang", "1111112");
                        // 内部事务回滚了，外部事务也跟着回滚
                        status.setRollbackOnly();
                    }
                });
            }
        });
    }

    @Override
    public void txRollbackInnerTxRollbackPropagationNever() {
        neverTransactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                jdbcTemplate.update("insert into user (name, password) values (?, ?)", "Huang",
                        "1111112");
            }
        });
    }

    @Override
    public void txRollbackInnerTxRollbackPropagationNever2() {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                jdbcTemplate.update("insert into user (name, password) values (?, ?)", "Huang",
                        "1111112");
                neverTransactionTemplate.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        jdbcTemplate.update("insert into user (name, password) values (?, ?)",
                                "Huang", "1111112");
                    }
                });
            }
        });
    }

    @Override
    public void txRollbackInnerTxRollbackPropagationNever3() {
        neverTransactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                jdbcTemplate.update("insert into user (name, password) values (?, ?)", "Huang",
                        "1111112");
                neverTransactionTemplate.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        jdbcTemplate.update("insert into user (name, password) values (?, ?)",
                                "Huang", "1111112");
                    }
                });
            }
        });
    }

    @Override
    public void txRollbackInnerTxRollbackPropagationNotSupport() {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                jdbcTemplate.update("insert into user (name, password) values (?, ?)", "Huang",
                        "1111112");
                notSupportedTransactionTemplate.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        jdbcTemplate.update("insert into user (name, password) values (?, ?)",
                                "Huang", "1111112");
                    }
                });
                // 外部事务回滚，不会把内部的也连着回滚 
                transactionStatus.setRollbackOnly();
            }
        });
    }

    @Override
    public void txRollbackInnerTxRollbackPropagationNotSupport2() {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                jdbcTemplate.update("insert into user (name, password) values (?, ?)", "Huang",
                        "1111112");

                try {
                    notSupportedTransactionTemplate.execute(new TransactionCallbackWithoutResult() {
                        @Override
                        protected void doInTransactionWithoutResult(TransactionStatus status) {
                            jdbcTemplate.update("insert into user (name, password) values (?, ?)",
                                    "Huang", "1111112");
                            throw new CustomRuntimeException();
                        }
                    });
                } catch (CustomRuntimeException e) {
                    // Do nothing.
                }
            }
        });
    }

    @Override
    public void txRollbackInnerTxRollbackPropagationSupports() {
        supportsTransactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                jdbcTemplate.update("insert into user (name, password) values (?, ?)", "Huang",
                        "1111112");
                throw new CustomRuntimeException();
            }
        });
    }

    @Override
    public void txRollbackInnerTxRollbackPropagationSupports2() {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                jdbcTemplate.update("insert into user (name, password) values (?, ?)", "Huang",
                        "1111112");
                supportsTransactionTemplate.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        jdbcTemplate.update("insert into user (name, password) values (?, ?)",
                                "Huang", "1111112");
                        status.setRollbackOnly();
                    }
                });
            }
        });
    }

    @Override
    public void cleanUp() {
        jdbcTemplate.update("delete from user");
    }

    private int countUser() {
        return jdbcTemplate.queryForObject("select count(*) from user", Integer.class);
    }

    // ~ Setters
    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    public void setRequiresNewTransactionTemplate(TransactionTemplate nestedTransactionTemplate) {
        this.requiresNewTransactionTemplate = nestedTransactionTemplate;
    }

    public void setNestedTransactionTemplate(TransactionTemplate nestedTransactionTemplate) {
        this.nestedTransactionTemplate = nestedTransactionTemplate;
    }

    public void setMandatoryTransactionTemplate(TransactionTemplate mandatoryTransactionTemplate) {
        this.mandatoryTransactionTemplate = mandatoryTransactionTemplate;
    }

    public void setNeverTransactionTemplate(TransactionTemplate neverTransactionTemplate) {
        this.neverTransactionTemplate = neverTransactionTemplate;
    }

    public void setNotSupportedTransactionTemplate(TransactionTemplate notSupportedTransactionTemplate) {
        this.notSupportedTransactionTemplate = notSupportedTransactionTemplate;
    }

    public void setSupportsTransactionTemplate(TransactionTemplate supportsTransactionTemplate) {
        this.supportsTransactionTemplate = supportsTransactionTemplate;
    }
}
