package com.catination.server.xmpp;

import java.io.Serializable;
import java.util.Date;

public class MsgFileForm implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5610104800122400080L;
	
	private String from;
	private String to;
	private String id;
	private String name;
	private String mime;
	private String size;
	private String hash;
	private Date date;
	
	public MsgFileForm() {
	}
	
	public MsgFileForm(String from, String to, String id, String name,
			String mime, String size, String hash, Date date) {
		this.from = from;
		this.to = to;
		this.id = id;
		this.name = name;
		this.mime = mime;
		this.size = size;
		this.hash = hash;
		this.date = date;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getFrom() {
		return this.from;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getTo() {
		return this.to;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}
	
	public void setMime(String mime) {
		this.mime = mime;
	}

	public String getMime() {
		return this.mime;
	}
	
	public void setSize(String size) {
		this.size = size;
	}

	public String getSize() {
		return this.size;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getHash() {
		return this.hash;
	}
	
	public void setCreatedDate(Date createdDate) {
		this.date = createdDate;
	}

	public Date getCreatedDate() {
		return date;
	}
}
