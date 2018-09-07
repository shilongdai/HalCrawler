package net.viperfish.crawler.core;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "EmphasizedText")
public class EmphasizedTextContent extends TextContent {

	@DatabaseField
	private EmphasizedType method;

	public EmphasizedTextContent() {
		super();
	}

	public EmphasizedType getMethod() {
		return method;
	}

	public void setMethod(EmphasizedType method) {
		this.method = method;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		EmphasizedTextContent other = (EmphasizedTextContent) obj;
		if (method != other.method)
			return false;
		return true;
	}

}
