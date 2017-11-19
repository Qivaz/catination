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
package com.catination.server.xmpp.handler;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.xmpp.packet.Message;
import org.xmpp.packet.Message.FileMethod;
import org.xmpp.packet.Message.Type;
import org.xmpp.packet.Packet.MsgFile;

import com.catination.server.model.Msg;
import com.catination.server.service.MessageService;
import com.catination.server.service.ServiceLocator;
import com.catination.server.service.UserNotFoundException;
import com.catination.server.xmpp.MsgFileForm;
import com.catination.server.xmpp.UnauthorizedException;
import com.catination.server.xmpp.XmppServer;
import com.catination.server.xmpp.net.XmppBlockingStreamIoHandler;
import com.catination.server.xmpp.net.XmppIoHandler;
import com.catination.server.xmpp.session.ClientSession;


/** 
 * This class is to handle the TYPE_IQ jabber:iq:register protocol.
 *
 *
 */
public class MsgFileHandler extends MsgHandler {

	private static final String TYPE = "file";
	private MessageService messageService;

	

	/**
	 * Constructor.
	 */
	public MsgFileHandler() {
		messageService = ServiceLocator.getMessageService();
	}

	/**
	 * Handles the received Message packet.
	 * 
	 * @param packet the packet
	 * @return the response to send back
	 * @throws UnauthorizedException if the message is not authorized
	 */
	public Message handleMessage(Message packet) throws UnauthorizedException {
		log.info("packet="+packet);
		ClientSession session = sessionManager.getSession(packet.getFrom());
		String from = null;
		try {
			from = session.getUsername();
		} catch (UserNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String to = packet.getTo().toString();

		Msg msg = new Msg();
		msg.setMessage(packet.getBody());
		msg.setFrom(from);
		msg.setTo(to);
		msg.setType(Type.file);
		log.debug("msg[file]="+msg);
		messageService.saveMessage(msg);
		
		// Prepare to receive file.
		packet.getMsgFile();

		//// Directly delivering to the destination users.
		//messageService.deliverMessage(packet);
		// Just return it back
		Message reply = new Message();
		reply.setID(packet.getID());
		reply.setTo(session.getAddress());
		reply.getElement().addAttribute("from", packet.getTo().toString());
		reply.setType(Type.file);
		reply.setFileMethod(FileMethod.uploading);
		session.process(reply);
		
		
		MsgFile mf = packet.getMsgFile();
		MsgFileForm mff = new MsgFileForm(mf.getFrom(), mf.getTo(), packet.getID(), mf.getName(), mf.getType(), mf.getSize(), mf.getHash(), new Date());
		
		//writeSerialization(mff);
		//writeQueue(mff);
		writeMap(mff);
		
		return null;
	}
	
	/*public void writeSerialization(MsgFileForm mff) {
		String bufFile = "F:\\MsgFile.dat";
		try {
			FileOutputStream fos = new FileOutputStream(bufFile);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			
			oos.writeObject(mff);
			oos.flush();
			oos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeQueue(final MsgFileForm mff) {
		pool.execute(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					bq.put(mff);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});
	}*/
	
	public void writeMap(final MsgFileForm mff) {
		XmppIoHandler.putStreamToMap(mff);
	}
	
	public MsgFileForm readQueue() {
		return XmppIoHandler.getStreamFromQueue();
	}

	/**
	 * Returns the namespace of the handler.
	 * 
	 * @return the namespace
	 */
	public String getType() {
		return TYPE;
	}

}
