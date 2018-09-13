package net.viperfish.crawler.html.dao;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.BaseDataType;
import com.j256.ormlite.support.DatabaseResults;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;

public class URlDataPersister extends BaseDataType {

	private static URlDataPersister persister = new URlDataPersister();

	public URlDataPersister() {
		super(SqlType.STRING);
	}

	public static URlDataPersister getSingleton() {
		return persister;
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr) {
		return null;
	}

	@Override
	public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos)
		throws SQLException {
		return results.getString(columnPos);
	}

	@Override
	public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
		URL obj = (URL) javaObject;
		return obj.toExternalForm();
	}

	@Override
	public Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos)
		throws SQLException {
		String result = results.getString(columnPos);
		try {
			return new URL(result);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {
		try {
			return new URL(sqlArg.toString());
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}

}
