package org.xmpp.packet;

import org.dom4j.Element;

public class HeartBeatMessage extends Packet {

	private boolean cdma = false;
	private int		sid;
	private int		nid;

	private boolean gsm = false;
	private int		lac;
	private int		cid;

	private double lon;
	private double lat;

	public HeartBeatMessage(Element element) {
		super(element);
		this.lon = 99.9;
		this.lat = 99.9;
	}

	public HeartBeatMessage(HeartBeatMessage hbm) {
		super(hbm.element);
		this.lon = hbm.lon;
		this.lat = hbm.lat;
	}

	public HeartBeatMessage(Element element, double lon, double lat) {
		super(element);
		this.lon = lon;
		this.lat = lat;
	}

	public HeartBeatMessage(Element element, boolean isCdma, int l1, int l2) {
		super(element);

		if (isCdma) {
			cdma = true;
			sid = l1;
			nid = l2;
		} else {
			gsm = true;
			lac = l1;
			cid = l2;
		}
	}

	public boolean isCdma() { return cdma; }
	public int getSid() { return sid; }
	public int getNid() { return nid; }
	public void setLon(int sid) { this.sid = sid; }
	public void setLat(int nid) { this.nid = nid; }

	public boolean isGsm() { return gsm; }
	public int getLac() { return lac; }
	public int getCid() { return cid; }
	public void setLac(int lac) { this.lac = lac; }
	public void setCid(int cid) { this.cid = cid; }

	public double getLon() { return lon; }
	public double getLat() { return lat; }
	public void setLon(double lon) { this.lon = lon; }
	public void setLat(double lat) { this.lat = lat; }

	@Override
	public String toXML() {
		// TODO Auto-generated method stub
		//return "TEST_HEART_BEAT";
		StringBuilder buf = new StringBuilder();
		buf.append("<hb>");
		buf.append("<location>");
		if (cdma) {
			buf.append("<cdma>");

			buf.append("<sid>");
			buf.append(sid);
			buf.append("</sid>");
			buf.append("<nid>");
			buf.append(nid);
			buf.append("</nid>");

			buf.append("</cdma>");
		} else if (gsm) {
			buf.append("<gsm>");

			buf.append("<lac>");
			buf.append(lac);
			buf.append("</lac>");
			buf.append("<cid>");
			buf.append(cid);
			buf.append("</cid>");

			buf.append("</gsm>");
		} else {
			buf.append("<lon>");
			buf.append(String.valueOf(lon));
			buf.append("</lon>");
			buf.append("<lat>");
			buf.append(String.valueOf(lat));
			buf.append("</lat>");
		}
		buf.append("</location>");
		buf.append("</hb>");
		return buf.toString();
	}

	@Override
	public HeartBeatMessage createCopy() {
		// TODO Auto-generated method stub
		return new HeartBeatMessage(this);
	}

}
