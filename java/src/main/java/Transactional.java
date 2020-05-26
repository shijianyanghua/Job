/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.transaction.annotation;

import org.springframework.transaction.TransactionDefinition;

import java.lang.annotation.*;

/**
 * Describes transaction attributes on a method or class. //描述事务的属性在一个方法或者类上（个人觉得应该是使用事务的属性在方法或者类上）
 * <p>
 * 这种注释类型通常可以直接与Spring的注释类型进行比较，
 * 实际上将直接将数据转换为后者，因此Spring的事务支持代码不必知道注释。 如果没有规则与异常相关，则将其视为回滚运行时异常
 *
 * <p>This annotation type is generally directly comparable to Spring's
 * {@link org.springframework.transaction.interceptor.RuleBasedTransactionAttribute}
 * class, and in fact {@link AnnotationTransactionAttributeSource} will directly
 * convert the data to the latter class, so that Spring's transaction support code
 * does not have to know about annotations. If no rules are relevant to the exception,
 * it will be treated like
 * {@link org.springframework.transaction.interceptor.DefaultTransactionAttribute}
 * (rolling back on runtime exceptions).
 *
 * <p>For specific information about the semantics of this annotation's attributes,
 * consider the {@link org.springframework.transaction.TransactionDefinition} and
 * {@link org.springframework.transaction.interceptor.TransactionAttribute} javadocs.
 *
 * @author Colin Sampaleanu
 * @author Juergen Hoeller
 * @see org.springframework.transaction.interceptor.TransactionAttribute
 * @see org.springframework.transaction.interceptor.DefaultTransactionAttribute
 * @see org.springframework.transaction.interceptor.RuleBasedTransactionAttribute
 * @since 1.2
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Transactional {

    /**
     * A qualifier value for the specified transaction.
     * <p>May be used to determine the target transaction manager,
     * matching the qualifier value (or the bean name) of a specific
     * {@link org.springframework.transaction.PlatformTransactionManager}
     * bean definition.
     * <p>
     * 主要是spring不提供事务管理器，可以通过这个去定义自己想要的事务管理器，例如hibernate的、JTA的、JDBC的
     * 大致上有以下几种
     * org.springframework.jdbc.datasource.DataSourceTransactionManager  DBC及iBATIS、MyBatis框架事务管理器
     * org.springframework.orm.jdo.JdoTransactionManager Jdo事务管理器
     * org.springframework.orm.jpa.JpaTransactionManager Jpa事务管理器
     * org.springframework.orm.hibernate3.HibernateTransactionManager  hibernate事务管理器
     * org.springframework.transaction.jta.JtaTransactionManager Jta事务管理器
     */
    String value() default "";

    /**
     * The transaction propagation type.
     * Defaults to {@link Propagation#REQUIRED}.
     * <p>
     * * 事务传播类型 默认是required 具体解释如下：
     *
     * @Transactional(propagation=Propagation.REQUIRED) ：如果有事务, 那么加入事务, 没有的话新建一个(默认情况下)
     * @Transactional(propagation=Propagation.NOT_SUPPORTED) ：容器不为这个方法开启事务
     * @Transactional(propagation=Propagation.REQUIRES_NEW) ：不管是否存在事务,都创建一个新的事务,原来的挂起,新的执行完毕,继续执行老的事务
     * @Transactional(propagation=Propagation.MANDATORY) ：必须在一个已有的事务中执行,否则抛出异常
     * @Transactional(propagation=Propagation.NEVER) ：必须在一个没有的事务中执行,否则抛出异常(与Propagation.MANDATORY相反)
     * @Transactional(propagation=Propagation.SUPPORTS) ：如果其他bean调用这个方法,在其他bean中声明事务,那就用事务.如果其他bean没有声明事务,那就不用事务.
     * <p>
     */
    Propagation propagation() default Propagation.REQUIRED;

    /**
     * 事务的隔离机制 默认是数据库的隔离机制 需要是innodb引擎数据库才可以
     * <p>
     * <p>
     * READ_UNCOMMITTED(TransactionDefinition.ISOLATION_READ_UNCOMMITTED),  	读取未提交数据(会出现脏读, 不可重复读) 基本不使用
     * READ_COMMITTED(TransactionDefinition.ISOLATION_READ_COMMITTED), 读取已提交数据(会出现不可重复读和幻读)
     * REPEATABLE_READ(TransactionDefinition.ISOLATION_REPEATABLE_READ), 可重复读(会出现幻读)
     * SERIALIZABLE(TransactionDefinition.ISOLATION_SERIALIZABLE); 串行化
     */
    Isolation isolation() default Isolation.DEFAULT;

    /**
     * The timeout for this transaction.
     * Defaults to the default timeout of the underlying transaction system.
     *
     * @see org.springframework.transaction.interceptor.TransactionAttribute#getTimeout()
     * <p>
     * 事务超时时间 默认-1 永远不超时 如果设置之后  达到时间自动回滚
     */
    int timeout() default TransactionDefinition.TIMEOUT_DEFAULT;

    /**
     * {@code true} if the transaction is read-only.
     * Defaults to {@code false}.
     * <p>This just serves as a hint for the actual transaction subsystem;
     * it will <i>not necessarily</i> cause failure of write access attempts.
     * A transaction manager which cannot interpret the read-only hint will
     * <i>not</i> throw an exception when asked for a read-only transaction.
     *
     * @see org.springframework.transaction.interceptor.TransactionAttribute#isReadOnly()
     * <p>
     * 设置是否只读 默认是false  如果设置之后会锁住库 如果有插入的话 会报异常 java.sql.SQLException: Connection is read-only. Queries leading to data modification are not allowed
     */
    boolean readOnly() default false;

    /**
     * Defines zero (0) or more exception {@link Class classes}, which must be a
     * subclass of {@link Throwable}, indicating which exception types must cause
     * a transaction rollback.
     * <p>This is the preferred way to construct a rollback rule, matching the
     * exception class and subclasses.
     * <p>Similar to {@link org.springframework.transaction.interceptor.RollbackRuleAttribute#RollbackRuleAttribute(Class clazz)}
     * <p>
     * 回滚的异常 只能是回滚runtimeExceptional
     * CheckedException不回滚：
     * Java认为Checked异常都是可以被处理的异常，所以Java程序必须显式的处理Checked异常，如果程序没有处理checked异常，程序在编译时候将发生错误。
     * 我们比较熟悉的Checked异常有
     * Java.lang.ClassNotFoundException
     * Java.lang.NoSuchMetodException
     * java.io.IOException
     * <p>
     * RunTimeException 回滚：
     * Runtime如除数是0和数组下标越界等，其产生频繁，处理麻烦，若显示申明或者捕获将会对程序的可读性和运行效率影响很大。所以由系统自动检测并将它们交给缺省的异常处理程序。当然如果你有处理要求也可以显示捕获它们。
     * 我们比较熟悉的RumtimeException类的子类有
     * Java.lang.ArithmeticException
     * Java.lang.ArrayStoreExcetpion
     * Java.lang.ClassCastException
     * Java.lang.IndexOutOfBoundsException
     * Java.lang.NullPointerException
     *
     * @Transactional(rollbackFor=RuntimeException.class)
     * 指定多个异常类：@Transactional(rollbackFor={RuntimeException.class, Exception.class})
     */
    Class<? extends Throwable>[] rollbackFor() default {};

    /**
     * Defines zero (0) or more exception names (for exceptions which must be a
     * subclass of {@link Throwable}), indicating which exception types must cause
     * a transaction rollback.
     * <p>This can be a substring, with no wildcard support at present.
     * A value of "ServletException" would match
     * {@link javax.servlet.ServletException} and subclasses, for example.
     * <p><b>NB: </b>Consider carefully how specific the pattern is, and whether
     * to include package information (which isn't mandatory). For example,
     * "Exception" will match nearly anything, and will probably hide other rules.
     * "java.lang.Exception" would be correct if "Exception" was meant to define
     * a rule for all checked exceptions. With more unusual {@link Exception}
     * names such as "BaseBusinessException" there is no need to use a FQN.
     * <p>Similar to {@link org.springframework.transaction.interceptor.RollbackRuleAttribute#RollbackRuleAttribute(String exceptionName)}
     * <p>
     *
     *
     * 这个是支持输入的类名称 详细功能同上
     *
     * @Transactional(rollbackForClassName="RuntimeException")
     * 指定多个异常类名称：@Transactional(rollbackForClassName={"RuntimeException","Exception"})
     */
    String[] rollbackForClassName() default {};

    /**
     * Defines zero (0) or more exception {@link Class Classes}, which must be a
     * subclass of {@link Throwable}, indicating which exception types must <b>not</b>
     * cause a transaction rollback.
     * <p>This is the preferred way to construct a rollback rule, matching the
     * exception class and subclasses.
     * <p>Similar to {@link org.springframework.transaction.interceptor.NoRollbackRuleAttribute#NoRollbackRuleAttribute(Class clazz)}
     *
     * 不会滚的异常可以在这设置 ，也可以自己实现异常 但是必须要继承runtimeExceptional
     */
    Class<? extends Throwable>[] noRollbackFor() default {};

    /**
     * Defines zero (0) or more exception names (for exceptions which must be a
     * subclass of {@link Throwable}) indicating which exception types must <b>not</b>
     * cause a transaction rollback.
     * <p>See the description of {@link #rollbackForClassName()} for more info on how
     * the specified names are treated.
     * <p>Similar to {@link org.springframework.transaction.interceptor.NoRollbackRuleAttribute#NoRollbackRuleAttribute(String exceptionName)}
     *
     *  同上 这个是支持输入的类名称  不会滚的异常可以在这设置 ，也可以自己实现异常 但是必须要继承runtimeExceptional
     */
    String[] noRollbackForClassName() default {};

}
