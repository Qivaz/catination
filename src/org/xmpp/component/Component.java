/**
 * Copyright (C) 2004-2009 Jive Software. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.xmpp.component;

import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

/**
 * Components enhance the functionality of an XMPP server. Components receive
 * all packets addressed to a particular sub-domain. For example,
 * <tt>test_component.example.com</tt>. So, a packet sent to
 * <tt>joe@test_component.example.com</tt> would be delivered to the component.
 * Note that the sub-domains defined as components are unrelated to DNS entries
 * for sub-domains. All XMPP routing at the socket level is done using the
 * primary server domain (<tt>example.com</tt> in the example above); sub-domains are
 * only used for routing within the XMPP server.
 * 
 * @author Matt Tucker
 */
public interface Component {

    /**
     * Returns the name of this component.
     *
     * @return the name of this component.
     */
    public String getName();

    /**
     * Returns the description of this component.
     *
     * @return the description of this component.
     */
    public String getDescription();

    /**
     * Processes a packet sent to this Component.
     *
     * @param packet the packet.
     * @see ComponentManager#sendPacket(Component, Packet)
     */
    public void processPacket(Packet packet);

    /**
     * Initializes this component with a ComponentManager and the JID
     * that this component is available at (e.g. <tt>service.example.com</tt>). If a
     * ComponentException is thrown then the component will not be loaded.<p>
     *
     * The initialization code must not rely on receiving packets from the server since
     * the component has not been fully initialized yet. This means that at this point the
     * component must not rely on information that is obtained from the server such us
     * discovered items.
     *
     * @param jid the XMPP address that this component is available at.
     * @param componentManager the component manager.
     * @throws ComponentException if an error occured while initializing the component.
     */
    public void initialize(JID jid, ComponentManager componentManager) throws ComponentException;

    /**
     * Notification message indicating that the component will start receiving incoming
     * packets. At this time the component may finish pending initialization issues that
     * require information obtained from the server.<p>
     *
     * It is likely that most of the component will leave this method empty.
     */
    public void start();

    /**
     * Shuts down this component. All component resources must be released as
     * part of shutdown.
     */
    public void shutdown();
}