package net.viperfish.crawler.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import net.viperfish.crawler.core.DatabaseObject;
import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

public class ORMLiteDatabase<ID, T> implements DatabaseObject<ID, T> {

	private static DataSourceConnectionSource conn;
	private Dao<T, ID> dao;
	private Class<T> classType;

	public ORMLiteDatabase(Class<T> type) {
		this.classType = type;
	}

	public static void connect(String url, String username, String password) throws SQLException {
		ConnectionFactory connFactory = new DriverManagerConnectionFactory(url, username, password);

		PoolableConnectionFactory poolFactory = new PoolableConnectionFactory(connFactory, null);

		ObjectPool<PoolableConnection> objPool = new GenericObjectPool<>(poolFactory);

		poolFactory.setPool(objPool);

		PoolingDataSource<PoolableConnection> pooledDatasource = new PoolingDataSource<>(objPool);

		conn = new DataSourceConnectionSource(pooledDatasource, url);

	}

	public static void closeConn() {
		conn.closeQuietly();
	}

	public ORMLiteDatabase<ID, T> connect() throws SQLException {
		dao = DaoManager.createDao(conn, classType);
		return this;
	}

	@Override
	public void save(T s) throws IOException {
		try {
			dao.createOrUpdate(s);
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void remove(ID id) throws IOException {
		try {
			dao.deleteById(id);
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}

	@Override
	public T find(ID id) throws IOException {
		try {
			return dao.queryForId(id);
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}

	@Override
	public Collection<T> find(Collection<ID> ids) throws IOException {
		List<T> result = new LinkedList<>();
		for (ID i : ids) {
			T obj = find(i);
			if (obj != null) {
				result.add(find(i));
			}
		}
		return result;
	}

	@Override
	public void save(Collection<T> collection) throws IOException {
		List<T> local = new LinkedList<>(collection);
		try {
			dao.callBatchTasks(new Callable<Object>() {

				@Override
				public Object call() throws Exception {
					for (int i = 0; i < local.size(); ++i) {
						save(local.get(i));
					}
					return new Object();
				}

			});
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	@Override
	public void close() {
	}

	public List<T> findBy(String field, Object value) throws IOException {
		try {
			return dao.queryForEq(field, value);
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}

	public void executeSql(String sql, String... arguments) throws SQLException {
		dao.executeRaw(sql, arguments);
	}

	protected Dao<T, ID> dao() {
		return this.dao;
	}

}
