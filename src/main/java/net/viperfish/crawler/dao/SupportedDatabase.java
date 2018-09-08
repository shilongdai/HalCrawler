package net.viperfish.crawler.dao;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.db.H2DatabaseType;
import com.j256.ormlite.db.MysqlDatabaseType;

public enum SupportedDatabase {
	H2(new H2DatabaseType()), MYSQL(new MysqlDatabaseType());

	private DatabaseType dbType;

	SupportedDatabase(DatabaseType dt) {
		this.dbType = dt;
	}

	public DatabaseType getDatabaseType() {
		return dbType;
	}

}
