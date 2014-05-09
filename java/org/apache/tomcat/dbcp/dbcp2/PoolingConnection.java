/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.tomcat.dbcp.dbcp2;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.NoSuchElementException;

import org.apache.tomcat.dbcp.pool2.KeyedObjectPool;
import org.apache.tomcat.dbcp.pool2.KeyedPooledObjectFactory;
import org.apache.tomcat.dbcp.pool2.PooledObject;
import org.apache.tomcat.dbcp.pool2.impl.DefaultPooledObject;

/**
 * A {@link DelegatingConnection} that pools {@link PreparedStatement}s.
 * <p>
 * The {@link #prepareStatement} and {@link #prepareCall} methods, rather than
 * creating a new PreparedStatement each time, may actually pull the statement
 * from a pool of unused statements.
 * The {@link PreparedStatement#close} method of the returned statement doesn't
 * actually close the statement, but rather returns it to the pool.
 * (See {@link PoolablePreparedStatement}, {@link PoolableCallableStatement}.)
 *
 *
 * @see PoolablePreparedStatement
 * @author Rodney Waldhoff
 * @author Dirk Verbeeck
 * @version $Revision$ $Date$
 * @since 2.0
 */
public class PoolingConnection extends DelegatingConnection<Connection>
        implements KeyedPooledObjectFactory<PStmtKey,DelegatingPreparedStatement> {

    /** Pool of {@link PreparedStatement}s. and {@link CallableStatement}s */
    private KeyedObjectPool<PStmtKey,DelegatingPreparedStatement> _pstmtPool = null;

    /**
     * Constructor.
     * @param c the underlying {@link Connection}.
     */
    public PoolingConnection(Connection c) {
        super(c);
    }


    public void setStatementPool(
            KeyedObjectPool<PStmtKey,DelegatingPreparedStatement> pool) {
        _pstmtPool = pool;
    }


    /**
     * Close and free all {@link PreparedStatement}s or
     * {@link CallableStatement}s from the pool, and close the underlying
     * connection.
     */
    @Override
    public synchronized void close() throws SQLException {
        try {
            if (null != _pstmtPool) {
                KeyedObjectPool<PStmtKey,DelegatingPreparedStatement> oldpool = _pstmtPool;
                _pstmtPool = null;
                try {
                    oldpool.close();
                } catch(RuntimeException e) {
                    throw e;
                } catch(Exception e) {
                    throw new SQLException("Cannot close connection", e);
                }
            }
        } finally {
            try {
                getDelegateInternal().close();
            } finally {
                setClosedInternal(true);
            }
        }
    }

    /**
     * Create or obtain a {@link PreparedStatement} from the pool.
     * @param sql the sql string used to define the PreparedStatement
     * @return a {@link PoolablePreparedStatement}
     */
    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        if (null == _pstmtPool) {
            throw new SQLException(
                    "Statement pool is null - closed or invalid PoolingConnection.");
        }
        try {
            return _pstmtPool.borrowObject(createKey(sql));
        } catch(NoSuchElementException e) {
            throw new SQLException("MaxOpenPreparedStatements limit reached", e);
        } catch(RuntimeException e) {
            throw e;
        } catch(Exception e) {
            throw new SQLException("Borrow prepareStatement from pool failed", e);
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        if (null == _pstmtPool) {
            throw new SQLException(
                    "Statement pool is null - closed or invalid PoolingConnection.");
        }
        try {
            return _pstmtPool.borrowObject(createKey(sql, autoGeneratedKeys));
        }
        catch (NoSuchElementException e) {
            throw new SQLException("MaxOpenPreparedStatements limit reached", e);
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new SQLException("Borrow prepareStatement from pool failed", e);
        }
    }

    /**
     * Create or obtain a {@link PreparedStatement} from the pool.
     * @param sql the sql string used to define the PreparedStatement
     * @param resultSetType result set type
     * @param resultSetConcurrency result set concurrency
     * @return a {@link PoolablePreparedStatement}
     */
    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        if (null == _pstmtPool) {
            throw new SQLException(
                    "Statement pool is null - closed or invalid PoolingConnection.");
        }
        try {
            return _pstmtPool.borrowObject(createKey(sql,resultSetType,resultSetConcurrency));
        } catch(NoSuchElementException e) {
            throw new SQLException("MaxOpenPreparedStatements limit reached", e);
        } catch(RuntimeException e) {
            throw e;
        } catch(Exception e) {
            throw new SQLException("Borrow prepareStatement from pool failed", e);
        }
    }

    /**
     * Create or obtain a {@link CallableStatement} from the pool.
     * @param sql the sql string used to define the CallableStatement
     * @return a {@link PoolableCallableStatement}
     * @throws SQLException
     */
    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        try {
            return (CallableStatement) _pstmtPool.borrowObject(createKey(sql, StatementType.CALLABLE_STATEMENT));
        } catch (NoSuchElementException e) {
            throw new SQLException("MaxOpenCallableStatements limit reached", e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new SQLException("Borrow callableStatement from pool failed", e);
        }
    }

    /**
     * Create or obtain a {@link CallableStatement} from the pool.
     * @param sql the sql string used to define the CallableStatement
     * @param resultSetType result set type
     * @param resultSetConcurrency result set concurrency
     * @return a {@link PoolableCallableStatement}
     * @throws SQLException
     */
    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        try {
            return (CallableStatement) _pstmtPool.borrowObject(createKey(sql, resultSetType,
                            resultSetConcurrency, StatementType.CALLABLE_STATEMENT));
        } catch (NoSuchElementException e) {
            throw new SQLException("MaxOpenCallableStatements limit reached", e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new SQLException("Borrow callableStatement from pool failed", e);
        }
    }


//    TODO: possible enhancement, cache these preparedStatements as well

//    public PreparedStatement prepareStatement(String sql, int resultSetType,
//                                              int resultSetConcurrency,
//                                              int resultSetHoldability)
//        throws SQLException {
//        return super.prepareStatement(
//            sql, resultSetType, resultSetConcurrency, resultSetHoldability);
//    }
//
//    public PreparedStatement prepareStatement(String sql, int columnIndexes[])
//        throws SQLException {
//        return super.prepareStatement(sql, columnIndexes);
//    }
//
//    public PreparedStatement prepareStatement(String sql, String columnNames[])
//        throws SQLException {
//        return super.prepareStatement(sql, columnNames);
//    }

    protected PStmtKey createKey(String sql, int autoGeneratedKeys) {
        String catalog = null;
        try {
            catalog = getCatalog();
        } catch (SQLException e) {
            // Ignored
        }
        return new PStmtKey(normalizeSQL(sql), catalog, autoGeneratedKeys);
    }

    /**
     * Create a PStmtKey for the given arguments.
     * @param sql the sql string used to define the statement
     * @param resultSetType result set type
     * @param resultSetConcurrency result set concurrency
     */
    protected PStmtKey createKey(String sql, int resultSetType, int resultSetConcurrency) {
        String catalog = null;
        try {
            catalog = getCatalog();
        } catch (SQLException e) {
            // Ignored
        }
        return new PStmtKey(normalizeSQL(sql), catalog, resultSetType, resultSetConcurrency);
    }

    /**
     * Create a PStmtKey for the given arguments.
     * @param sql the sql string used to define the statement
     * @param resultSetType result set type
     * @param resultSetConcurrency result set concurrency
     * @param stmtType statement type
     */
    protected PStmtKey createKey(String sql, int resultSetType, int resultSetConcurrency, StatementType stmtType) {
        String catalog = null;
        try {
            catalog = getCatalog();
        } catch (SQLException e) {
            // Ignored
        }
        return new PStmtKey(normalizeSQL(sql), catalog, resultSetType, resultSetConcurrency, stmtType);
    }

    /**
     * Create a PStmtKey for the given arguments.
     * @param sql the sql string used to define the statement
     */
    protected PStmtKey createKey(String sql) {
        String catalog = null;
        try {
            catalog = getCatalog();
        } catch (SQLException e) {
            // Ignored
        }
        return new PStmtKey(normalizeSQL(sql), catalog);
    }

    /**
     * Create a PStmtKey for the given arguments.
     * @param sql the SQL string used to define the statement
     * @param stmtType statement type
     */
    protected PStmtKey createKey(String sql, StatementType stmtType) {
        String catalog = null;
        try {
            catalog = getCatalog();
        } catch (SQLException e) {
            // Ignored
        }
        return new PStmtKey(normalizeSQL(sql), catalog, stmtType, null);
    }

    /**
     * Normalize the given SQL statement, producing a
     * cannonical form that is semantically equivalent to the original.
     */
    protected String normalizeSQL(String sql) {
        return sql.trim();
    }

    /**
     * {@link KeyedPooledObjectFactory} method for creating
     * {@link PoolablePreparedStatement}s or {@link PoolableCallableStatement}s.
     * The <code>stmtType</code> field in the key determines whether
     * a PoolablePreparedStatement or PoolableCallableStatement is created.
     *
     * @param key the key for the {@link PreparedStatement} to be created
     * @see #createKey(String, int, int, StatementType)
     */
    @Override
    public PooledObject<DelegatingPreparedStatement> makeObject(PStmtKey key)
            throws Exception {
        if(null == key) {
            throw new IllegalArgumentException("Prepared statement key is null or invalid.");
        }
        if (null == key.getResultSetType() && null == key.getResultSetConcurrency() && null == key.getAutoGeneratedKeys()) {
            if (key.getStmtType() == StatementType.PREPARED_STATEMENT ) {
                @SuppressWarnings({"rawtypes", "unchecked"}) // Unable to find way to avoid this
                PoolablePreparedStatement pps = new PoolablePreparedStatement(
                        getDelegate().prepareStatement(key.getSql()), key, _pstmtPool, this);
                return new DefaultPooledObject<DelegatingPreparedStatement>(pps);
            }
            return new DefaultPooledObject<DelegatingPreparedStatement>(
                    new PoolableCallableStatement(getDelegate().prepareCall( key.getSql()), key, _pstmtPool, this));
        } else if (null == key.getResultSetType() && null == key.getResultSetConcurrency()){
            @SuppressWarnings({"rawtypes", "unchecked"}) // Unable to find way to avoid this
            PoolablePreparedStatement pps = new PoolablePreparedStatement(
                    getDelegate().prepareStatement(key.getSql(), key.getAutoGeneratedKeys().intValue()), key, _pstmtPool, this);
            return new DefaultPooledObject<DelegatingPreparedStatement>(pps);
        } else { // Both _resultSetType and _resultSetConcurrency are non-null here (both or neither are set by constructors)
            if(key.getStmtType() == StatementType.PREPARED_STATEMENT) {
                @SuppressWarnings({"rawtypes", "unchecked"}) // Unable to find way to avoid this
                PoolablePreparedStatement pps = new PoolablePreparedStatement(getDelegate().prepareStatement(
                        key.getSql(), key.getResultSetType().intValue(),key.getResultSetConcurrency().intValue()), key, _pstmtPool, this);
                return new DefaultPooledObject<DelegatingPreparedStatement>(pps);
            }
            return new DefaultPooledObject<DelegatingPreparedStatement>(
                    new PoolableCallableStatement( getDelegate().prepareCall(
                            key.getSql(),key.getResultSetType().intValue(), key.getResultSetConcurrency().intValue()), key, _pstmtPool, this));
        }
    }

    /**
     * {@link KeyedPooledObjectFactory} method for destroying
     * PoolablePreparedStatements and PoolableCallableStatements.
     * Closes the underlying statement.
     *
     * @param key ignored
     * @param p the wrapped pooled statement to be destroyed.
     */
    @Override
    public void destroyObject(PStmtKey key,
            PooledObject<DelegatingPreparedStatement> p)
            throws Exception {
        p.getObject().getInnermostDelegate().close();
    }

    /**
     * {@link KeyedPooledObjectFactory} method for validating
     * pooled statements. Currently always returns true.
     *
     * @param key ignored
     * @param p ignored
     * @return <tt>true</tt>
     */
    @Override
    public boolean validateObject(PStmtKey key,
            PooledObject<DelegatingPreparedStatement> p) {
        return true;
    }

    /**
     * {@link KeyedPooledObjectFactory} method for activating
     * pooled statements.
     *
     * @param key ignored
     * @param p wrapped pooled statement to be activated
     */
    @Override
    public void activateObject(PStmtKey key,
            PooledObject<DelegatingPreparedStatement> p) throws Exception {
        p.getObject().activate();
    }

    /**
     * {@link KeyedPooledObjectFactory} method for passivating
     * {@link PreparedStatement}s or {@link CallableStatement}s.
     * Invokes {@link PreparedStatement#clearParameters}.
     *
     * @param key ignored
     * @param p a wrapped {@link PreparedStatement}
     */
    @Override
    public void passivateObject(PStmtKey key,
            PooledObject<DelegatingPreparedStatement> p) throws Exception {
        DelegatingPreparedStatement dps = p.getObject();
        dps.clearParameters();
        dps.passivate();
    }

    @Override
    public String toString() {
        if (_pstmtPool != null ) {
            return "PoolingConnection: " + _pstmtPool.toString();
        }
        return "PoolingConnection: null";
    }

    /**
     * The possible statement types.
     * @since 2.0
     */
    protected static enum StatementType {
        CALLABLE_STATEMENT,
        PREPARED_STATEMENT
    }
}
