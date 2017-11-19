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
package com.catination.server.xmpp.group;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xmpp.packet.Packet;

/** 
 * This is an abstract class for a session between the server and a client.
 *
 *
 */
public class Group {

    public static final int MAJOR_VERSION = 1;

    public static final int MINOR_VERSION = 0;

    /** 
     * The session status when closed 
     */
    public static final int STATUS_DESTROIED = 0;

    /**
     * The session status when connected
     */
    public static final int STATUS_CREATED = 1;

    private static final Log log = LogFactory.getLog(Group.class);

    protected GroupManager groupManager;

    private String serverName;

    private String groupName;

    private int status = STATUS_DESTROIED;

    private long startDate = System.currentTimeMillis();

    private long lastActiveDate;

    private long clientPacketCount = 0;

    private long serverPacketCount = 0;

    private final Map<String, Object> users = new HashMap<String, Object>();

    /**
     * Constructor. Creates a new JID using server name and stream ID.
     * 
     * @param serverName the server name
     * @param conn the connection
     * @param streamID the stream ID
     */
    public Group(GroupManager gm, String groupName) {
        this.groupManager = gm;//GroupManager.getInstance();
        this.groupName = groupName;
    }
    
    public String getName() {
    	return groupName;
    }

    public void putUser(String key, Object value) {
        synchronized (users) {
            users.put(key, value);
        }
    }

    public Object getUser(String key) {
        synchronized (users) {
            return users.get(key);
        }
    }
    
    public Collection<Object> getUsers() {
        synchronized (users) {
            return users.values();
        }
    }

    public void removeUser(String key) {
        synchronized (users) {
            users.remove(key);
        }
    }

    /**
     * Process the packet.
     * 
     * @param packet the packet to process
     */
    public void process(Packet packet) {
        try {
            deliver(packet);
        } catch (Exception e) {
            log.error("Internal server error", e);
        }
    }

    /**
     * Delivers the packet to the associated connection.
     * 
     * @param packet the packet to deliver
     */
    public void deliver(Packet packet) {
        
    }

    /**
     * Delivers raw text to the associated connection.
     * 
     * @param text the XML stanza string to deliver
     */
    public void deliverRawText(String text) {
        
    }
    
    public String toString() {
    	return "Group["+groupManager+", "+groupName+"]";
    }
}
