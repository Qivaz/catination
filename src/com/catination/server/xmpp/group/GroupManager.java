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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.catination.server.util.Config;
import com.catination.server.xmpp.XmppServer;
import com.catination.server.xmpp.net.ConnectionCloseListener;

/** 
 * This class manages the groups connected to the server.
 *
 *
 */
public class GroupManager {

	private static final Log log = LogFactory.getLog(GroupManager.class);

	private static final String RESOURCE_NAME = "SC";

	private static GroupManager instance;

	private Map<String, Group> preGroups = new ConcurrentHashMap<String, Group>();

	private Map<String, Group> groups = new ConcurrentHashMap<String, Group>();

	private GroupListener groupListener = new GroupListener();

	private GroupManager() {

		List groups = Config.getList("xmpp.group.preset");
		Iterator iter = groups.iterator();
		while (iter.hasNext()) {
			Group group = this.createGroup((String)iter.next());
			this.addGroup(group);
			log.debug("added: "+group);
		}
	}

	/**
	 * Returns the singleton instance of GroupManager.
	 * 
	 * @return the instance
	 */
	public static GroupManager getInstance() {
		if (instance == null) {
			synchronized (GroupManager.class) {
				instance = new GroupManager();
			}
		}
		return instance;
	}

	/**
	 * Creates a new Group and returns it.
	 *  
	 * @param conn the connection
	 * @return a newly created group
	 */
	public Group createGroup(String name) {
		Group group = new Group(GroupManager.this, name);
		// Add to pre-set groups
		preGroups.put(group.getName(), group);

		log.debug("Group created.");
		return group;
	}

	/**
	 * Adds a new group that has been authenticated. 
	 *  
	 * @param group the group
	 */
	public void addGroup(Group group) {
		preGroups.remove(group.getName());
		groups.put(group.getName(), group);
	}

	/**
	 * Returns the group associated with the name.
	 * 
	 * @param from the client address
	 * @return the group associated with the JID
	 */
	public Group getGroup(String name) {
		if (name == null || name.isEmpty()) {
			return null;
		}

		return groups.get(name);
	}

	/**
	 * Returns a list that contains all authenticated client groups.
	 * 
	 * @return a list that contains all client groups
	 */
	public Collection<Group> getGroups() {
		return groups.values();
	}

	/**
	 * Removes a client group.
	 * 
	 * @param group the group to be removed
	 * @return true if the group was successfully removed 
	 */
	public boolean removeGroup(Group group) {
		if (group == null) {
			return false;
		}
		// Remove the group from list
		boolean removed = groups.remove(group.getName()) != null;
		boolean preRemoved = preGroups.remove(group.getName()) != null;

		// Decrement the counter of user groups
		if (removed || preRemoved) {
			return true;
		}
		return false;
	}

	/**
	 * Closes the all groups. 
	 */
	public void closeAllGroups() {
		try {
			// Send the close stream header to all connections
			Set<Group> groups = new HashSet<Group>();
			groups.addAll(preGroups.values());
			groups.addAll(this.groups.values());

			for (Group group : groups) {
				try {
					//
				} catch (Throwable t) {
				}
			}
		} catch (Exception e) {
		}
	}

	/**
	 * A listner to handle a group that has been closed.
	 */
	private class GroupListener implements ConnectionCloseListener {

		public void onConnectionClose(Object handback) {
			try {
				Group group = (Group) handback;
				removeGroup(group);
			} catch (Exception e) {
				log.error("Could not close socket", e);
			}
		}
	}

}
