/*
 * Copyright (C) 2010 Moduad Co., Ltd.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.catination.server.xmpp.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.dom4j.io.XMPPPacketReader;
import org.jivesoftware.openfire.net.MXParser;
import org.jivesoftware.openfire.nio.XMLLightweightParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmpp.packet.Message;
import org.xmpp.packet.Message.FileMethod;
import org.xmpp.packet.Message.Type;

import com.catination.server.service.MessageService;
import com.catination.server.service.ServiceLocator;
import com.catination.server.xmpp.MsgFileForm;
import com.catination.server.xmpp.XmppServer;

/** 
 * This class is to create new sessions, destroy sessions and deliver
 * received XML stanzas to the StanzaHandler.
 *
 *
 */
public class XmppIoHandler implements IoHandler {

	private static final Log log = LogFactory.getLog(XmppIoHandler.class);

	public static final String XML_PARSER = "XML_PARSER";

	private static final String CONNECTION = "CONNECTION";

	private static final String STANZA_HANDLER = "STANZA_HANDLER";

	private String serverName;

	private static Map<Integer, XMPPPacketReader> parsers = new ConcurrentHashMap<Integer, XMPPPacketReader>();

	private static XmlPullParserFactory factory = null;

	private IoAcceptor streamAcceptor;
	private XmppBlockingStreamIoHandler bsio;
	private BlockingQueue<MsgFileForm> bq;
	private static Map<String, MsgFileForm> map;
	private static BlockingQueue<MsgFileForm> que;
	private static ExecutorService pool = Executors.newCachedThreadPool();

	static {
		try {
			factory = XmlPullParserFactory.newInstance(
					MXParser.class.getName(), null);
			factory.setNamespaceAware(true);
		} catch (XmlPullParserException e) {
			log.error("Error creating a parser factory", e);
		}
	}

	/**
	 * Constructor. Set the server name from server instance. 
	 */
	protected XmppIoHandler() {
		serverName = XmppServer.getInstance().getServerName();

		//bq = new LinkedBlockingQueue<MsgFileForm>();
		map = new ConcurrentHashMap<String, MsgFileForm>();
		que = new LinkedBlockingQueue<MsgFileForm>();

		streamAcceptor = new NioSocketAcceptor();
		bsio = new XmppBlockingStreamIoHandler(map, que);

		streamAcceptor.getFilterChain().addLast("logger", new LoggingFilter()) ;  
		//streamAcceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));  
		streamAcceptor.setHandler(bsio) ;  
		//streamAcceptor.getSessionConfig().setReadBufferSize(2048);   
		streamAcceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10) ;  
		try {
			streamAcceptor.bind(new InetSocketAddress(9922)) ;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("bind Xmpp Stream Io failed.", e);
			e.printStackTrace();
		}
		
		pool.execute(new PushWorker());
	}
	
	class PushWorker implements Runnable {
		private MessageService messageService;
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (messageService == null) {
				messageService = ServiceLocator.getMessageService();
			}
			
			for(;;) {
				
				MsgFileForm mff = null;
				mff = getStreamFromQueue();
				
				// Push the message.
				Message msg = new Message();
				msg.setTo(mff.getTo());
				msg.setFrom(mff.getFrom());
				msg.setID(mff.getId());
				msg.setType(Type.file);
				
				Message.File mf = new Message.File(mff.getName(), 
						mff.getSize(), 
						mff.getHash(), 
						mff.getMime());
				
				msg.setFile(mf);
				msg.setFileMethod(FileMethod.download);
				log.debug("Push msg[file]="+msg);
				messageService.deliverMessage(msg, false);
			}
		}
		
	}
	
	public static Map<String, MsgFileForm> getStreamMap() {
		return map;
	}
	
	public static BlockingQueue<MsgFileForm> getStreamQueue() {
		return que;
	}
	
	public static void putStreamToMap(final MsgFileForm mff) {
		if (getStreamMap() == null) {
			return;
		}
		
		pool.execute(new Runnable() {

			@Override
			public void run() {
				XmppIoHandler.getStreamMap().put(mff.getId(), mff);
			}
		});
	}
	
	public static MsgFileForm getStreamFromQueue() {
		MsgFileForm mff = null;
		try {
			mff = que.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mff;
	}

	/**
	 * Invoked from an I/O processor thread when a new connection has been created.
	 */
	public void sessionCreated(IoSession session) throws Exception {
		log.debug("sessionCreated()..."+session.getId());
	}

	/**
	 * Invoked when a connection has been opened.
	 */
	public void sessionOpened(IoSession session) throws Exception {
		log.debug("sessionOpened()..."+session.getId());
		log.debug("remoteAddress=" + session.getRemoteAddress());

		// Create a new XML parser
		XMLLightweightParser parser = new XMLLightweightParser("UTF-8");
		session.setAttribute(XML_PARSER, parser);
		// Create a new connection
		Connection connection = new Connection(session);
		session.setAttribute(CONNECTION, connection);
		session.setAttribute(STANZA_HANDLER, new StanzaHandler(serverName, connection));
	}

	/**
	 * Invoked when a connection is closed.
	 */
	public void sessionClosed(IoSession session) throws Exception {
		log.debug("sessionClosed()..."+session.getId());
		Connection connection = (Connection) session.getAttribute(CONNECTION);
		connection.close();
	}

	/**
	 * Invoked with the related IdleStatus when a connection becomes idle.
	 */
	public void sessionIdle(IoSession session, IdleStatus status)
			throws Exception {
		log.debug("sessionIdle()..."+session.getId());
		Connection connection = (Connection) session.getAttribute(CONNECTION);
		if (log.isDebugEnabled()) {
			log.debug("Closing connection that has been idle: " + connection);
		}
		connection.close();
	}

	/**
	 * Invoked when any exception is thrown.
	 */
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		log.debug("exceptionCaught()..."+session.getId());
		log.error(cause);
	}

	/**
	 * Invoked when a message is received.
	 */
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		log.debug("messageReceived()..."+session.getId());
		log.debug("RCVD: " + message);

		// Get the stanza handler
		StanzaHandler handler = (StanzaHandler) session
				.getAttribute(STANZA_HANDLER);

		// Get the XMPP packet parser
		int hashCode = Thread.currentThread().hashCode();
		XMPPPacketReader parser = parsers.get(hashCode);
		if (parser == null) {
			parser = new XMPPPacketReader();
			parser.setXPPFactory(factory);
			parsers.put(hashCode, parser);
		}

		// The stanza handler processes the message
		try {
			handler.process((String) message, parser);
		} catch (Exception e) {
			log.error(
					"Closing connection due to error while processing message: "
							+ message, e);
			Connection connection = (Connection) session
					.getAttribute(CONNECTION);
			connection.close();
		}
	}

	/**
	 * Invoked when a message written by IoSession.write(Object) is sent out.
	 */
	public void messageSent(IoSession session, Object message) throws Exception {
		log.debug("messageSent()..."+session.getId());
	}

	public void inputClosed(IoSession session) throws Exception {
		// TODO Auto-generated method stub
		//log.debug("inputClosed()...");
	}

}