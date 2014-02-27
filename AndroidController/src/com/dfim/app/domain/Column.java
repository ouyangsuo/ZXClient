package com.dfim.app.domain;

public class Column {

	private long id;
	private String name;
	private int type;
	private String url;
	private ColumnDetail detail;

	public Column() {

	}

	public Column(long id, String name, int type, String url) {
		super();
		this.id = id;
		this.name = name;
		this.type = type;
		this.url = url;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public ColumnDetail getDetail() {
		return detail;
	}

	public void setDetail(ColumnDetail detail) {
		this.detail = detail;
	}

	@Override
	public String toString() {
		return "botique: id=" + id + ",name=" + name + ",type=" + type + ";\n";
	}
}
