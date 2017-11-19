package com.catination.server.xmpp.codec;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.keepalive.KeepAliveMessageFactory;

public class HeartBeatMessageFactory implements KeepAliveMessageFactory {
	/** A logger for this class */
	protected final Log log = LogFactory.getLog(getClass());

	private static final String HB_REQ = "0x11";   
	private static final String HB_RSP = "0x12";
	
	@Override
	public boolean isRequest(IoSession session, Object message) {
		// TODO Auto-generated method stub
		log.info("isRequest() "+message);
		if (message.equals(HB_REQ)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isResponse(IoSession session, Object message) {
		// TODO Auto-generated method stub
		log.info("isResponse() "+message);
		if (message.equals(HB_RSP)) {
			return true;
		}
		return false;
	}

	@Override
	public Object getRequest(IoSession session) {
		// TODO Auto-generated method stub
		log.info("getRequest() "+session);
		return HB_REQ;
	}

	@Override
	public Object getResponse(IoSession session, Object request) {
		// TODO Auto-generated method stub
		log.info("getResponse() "+session);
		return HB_RSP;
	}

}
