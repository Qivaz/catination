package com.catination.server.xmpp.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.filter.keepalive.KeepAliveFilter;
import org.apache.mina.filter.keepalive.KeepAliveMessageFactory;

public class HeartBeatFilter extends KeepAliveFilter {
	/** A logger for this class */
	protected final Log log = LogFactory.getLog(getClass());
	
	private static final int HEART_BEAT_INT = 60*10; // Internal in seconds
	public HeartBeatFilter(KeepAliveMessageFactory messageFactory) {
		super(messageFactory);
		// TODO Auto-generated constructor stub
		this.setForwardEvent(true);
		this.setRequestInterval(HEART_BEAT_INT);
		
		log.debug("HeartBeatFilter created");
	}

}
