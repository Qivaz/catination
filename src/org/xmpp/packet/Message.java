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

package org.xmpp.packet;

import net.jcip.annotations.NotThreadSafe;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import java.util.Iterator;

/**
 * Message packet.<p>
 *
 * A message can have one of several {@link Type Types}. For each message type,
 * different message fields are typically used as follows:
 *
 * <p>
 * <table border="1">
 * <tr><td>&nbsp;</td><td colspan="5"><b>Message type</b></td></tr>
 * <tr><td><i>Field</i></td><td><b>Normal</b></td><td><b>Chat</b></td><td><b>Group Chat</b></td><td><b>Headline</b></td><td><b>Error</b></td></tr>
 * <tr><td><i>subject</i></td> <td>SHOULD</td><td>SHOULD NOT</td><td>SHOULD NOT</td><td>SHOULD NOT</td><td>SHOULD NOT</td></tr>
 * <tr><td><i>thread</i></td>  <td>OPTIONAL</td><td>SHOULD</td><td>OPTIONAL</td><td>OPTIONAL</td><td>SHOULD NOT</td></tr>
 * <tr><td><i>body</i></td>    <td>SHOULD</td><td>SHOULD</td><td>SHOULD</td><td>SHOULD</td><td>SHOULD NOT</td></tr>
 * <tr><td><i>error</i></td>   <td>MUST NOT</td><td>MUST NOT</td><td>MUST NOT</td><td>MUST NOT</td><td>MUST</td></tr>
 * </table>
 */
@NotThreadSafe
public class Message extends Packet {

	private final Log log = LogFactory.getLog(getClass());
	
    /**
     * Constructs a new Message.
     */
    public Message() {
        this.element = docFactory.createDocument().addElement("message");
    }

     /**
     * Constructs a new Message using an existing Element. This is useful
     * for parsing incoming message Elements into Message objects.
     *
     * @param element the message Element.
     */
    public Message(Element element) {
        super(element);
    }

    /**
     * Constructs a new Message using an existing Element. This is useful
     * for parsing incoming message Elements into Message objects. Stringprep validation
     * on the TO address can be disabled. The FROM address will not be validated since the
     * server is the one that sets that value.
     *
     * @param element the message Element.
     * @param skipValidation true if stringprep should not be applied to the TO address.
     */
    public Message(Element element, boolean skipValidation) {
        super(element, skipValidation);
    }

    /**
     * Constructs a new Message that is a copy of an existing Message.
     *
     * @param message the message packet.
     * @see #createCopy()
     */
    private Message(Message message) {
        Element elementCopy = message.element.createCopy();
        docFactory.createDocument().add(elementCopy);
        this.element = elementCopy;
        // Copy cached JIDs (for performance reasons)
        this.toJID = message.toJID;
        this.fromJID = message.fromJID;
    }

    /**
     * Returns the type of this message
     *
     * @return the message type.
     * @see Type
     */
    public Type getType() {
        String type = element.attributeValue("type");
        if (type != null) {
            return Type.valueOf(type);
        }
        else {
            return Type.normal;
        }
    }

    /**
     * Sets the type of this message.
     *
     * @param type the message type.
     * @see Type
     */
    public void setType(Type type) {
        element.addAttribute("type", type==null?null:type.toString());
    }

    /**
     * Returns the subject of this message or <tt>null</tt> if there is no subject..
     *
     * @return the subject.
     */
    public String getSubject() {
        return element.elementText("subject");
    }

    /**
     * Sets the subject of this message.
     *
     * @param subject the subject.
     */
    public void setSubject(String subject) {
        Element subjectElement = element.element("subject");
        // If subject is null, clear the subject.
        if (subject == null && subjectElement != null) {
            element.remove(subjectElement);
            return;
        }
        // Do nothing if the new subject is null
        if (subject == null) {
            return;
        }
        if (subjectElement == null) {
            subjectElement = element.addElement("subject");
        }
        subjectElement.setText(subject);
    }

    /**
     * Returns the body of this message or <tt>null</tt> if there is no body.
     *
     * @return the body.
     */
    public String getBody() {
        return element.elementText("body");
    }

    /**
     * Sets the body of this message.
     *
     * @param body the body.
     */
    public void setBody(String body) {
        Element bodyElement = element.element("body");
        // If body is null, clear the body.
        if (body == null) {
            if (bodyElement != null) {
                element.remove(bodyElement);
            }
            return;
        }
        if (bodyElement == null) {
            bodyElement = element.addElement("body");
        }
        bodyElement.setText(body);
    }

    /**
     * Returns the thread value of this message, an identifier that is used for
     * tracking a conversation thread ("instant messaging session")
     * between two entities. If the thread is not set, <tt>null</tt> will be
     * returned.
     *
     * @return the thread value.
     */
    public String getThread() {
        return element.elementText("thread");
    }

    /**
     * Sets the thread value of this message, an identifier that is used for
     * tracking a conversation thread ("instant messaging session")
     * between two entities.
     *
     * @param thread thread value.
     */
    public void setThread(String thread) {
        Element threadElement = element.element("thread");
        // If thread is null, clear the thread.
        if (thread == null) {
            if (threadElement != null) {
                element.remove(threadElement);
            }
            return;
        }

        if (threadElement == null) {
            threadElement = element.addElement("thread");
        }
        threadElement.setText(thread);
    }
    
    /**
     * Returns the file-method value of this message
     *
     * @return the file-method value.
     */
    public FileMethod getFileMethod() {
        String type = element.attributeValue("file-method");
        if (type != null) {
            return FileMethod.valueOf(type);
        }
        return null;
    }

    /**
     * Sets the file-method value of this message
     *
     * @param fm file-method value.
     */
    public void setFileMethod(FileMethod fm) {
        element.addAttribute("file-method", fm==null?null:fm.toString());
    }

    /**
     * Returns the first child element of this packet that matches the
     * given name and namespace. If no matching element is found,
     * <tt>null</tt> will be returned. This is a convenience method to avoid
     * manipulating this underlying packet's Element instance directly.<p>
     *
     * Child elements in extended namespaces are used to extend the features
     * of XMPP. Examples include a "user is typing" indicator and invitations to
     * group chat rooms. Although any valid XML can be included in a child element
     * in an extended namespace, many common features have been standardized
     * as <a href="http://xmpp.org/extensions/">XMPP Extension Protocols</a>
     * (XEPs).
     *
     * @param name the element name.
     * @param namespace the element namespace.
     * @return the first matching child element, or <tt>null</tt> if there
     *      is no matching child element.
     */
    @SuppressWarnings("unchecked")
    public Element getChildElement(String name, String namespace) {
        for (Iterator<Element> i=element.elementIterator(name); i.hasNext(); ) {
            Element element = i.next();
            if (element.getNamespaceURI().equals(namespace)) {
                return element;
            }
        }
        return null;
    }

    /**
     * Adds a new child element to this packet with the given name and
     * namespace. The newly created Element is returned. This is a
     * convenience method to avoid manipulating this underlying packet's
     * Element instance directly.<p>
     *
     * Child elements in extended namespaces are used to extend the features
     * of XMPP. Examples include a "user is typing" indicator and invitations to
     * group chat rooms. Although any valid XML can be included in a child element
     * in an extended namespace, many common features have been standardized
     * as <a href="http://xmpp.org/extensions/">XMPP Extension Protocols</a>
     * (XEPs).
     *
     * @param name the element name.
     * @param namespace the element namespace.
     * @return the newly created child element.
     */
    public Element addChildElement(String name, String namespace) {
        return element.addElement(name, namespace);
    }

    /**
     * Returns a deep copy of this Message.
     *
     * @return a deep copy of this Message.
     */
    public Message createCopy() {
        return new Message(this);
    }
    
    /**
     * Sets the file of the message.
     *
     * @param file the file of the message.
     */
    public void setFile(File file) {
        Element fileElement = element.element("file");
        // If file is null, clear the subject.
        if (file == null && fileElement != null) {
            element.remove(fileElement);
            return;
        }
        // Do nothing if the new file is null
        if (file == null) {
            return;
        }
        if (fileElement == null) {
        	fileElement = element.addElement("file");
        }
        fileElement.addAttribute("name", file.name);
        fileElement.addAttribute("size", file.size);
        fileElement.addAttribute("hash", file.hash);
        fileElement.addAttribute("content-type", file.type);
    }
    
    /**
     * Gets the file of the message.
     *
     * @return file the file of the message.
     */
    public String getFile() {
    	return element.elementText("file");
    }
    
    /**
     * Represents a message subject, its language and the content of the subject.
     */
    public static class File {

        private String name;
        private String size;
        private String hash;
        private String type;

        public File(String name, String size, String hash, String type) {
            if (name == null) {
                throw new NullPointerException("name cannot be null.");
            }
            if (size == null) {
                throw new NullPointerException("size cannot be null.");
            }
            if (hash == null) {
                throw new NullPointerException("hash cannot be null.");
            }
            if (type == null) {
                throw new NullPointerException("type cannot be null.");
            }
            
            this.name = name;
            this.size = size;
            this.hash = hash;
            this.type = type;
        }

        /**
         * Returns the name of this file.
         *
         * @return the name of this file.
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the size of this file.
         *
         * @return the size of this file.
         */
        public String getSize() {
            return size;
        }
        
        /**
         * Returns the hash of this file.
         *
         * @return the hash of this file.
         */
        public String getHash() {
            return hash;
        }
        
        /**
         * Returns the MIME type of this file.
         *
         * @return the MIME type of this file.
         */
        public String getType() {
            return type;
        }


        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + this.name.hashCode();
            result = prime * result + this.size.hashCode();
            result = prime * result + this.type.hashCode();
            result = prime * result + this.hash.hashCode();
            return result;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            File other = (File) obj;
            // simplified comparison because language and subject are always set
            return this.name.equals(other.name) 
            		&& this.size.equals(other.size) 
            		&& this.hash.equals(other.hash) 
            		&& this.type.equals(other.type);
        }
    }
    
    /**
     * Type-safe enumeration for the type of file-method. The types are:
     *
     *  <ul>
     *      <li>{@link #normal Message.FileMethod.upload} -- Request from client.
     *      <li>{@link #chat Message.FileMethod.uploading} -- Response to client.
     *      <li>{@link #groupchat Message.FileMethod.download} -- Request to client.
     *      <li>{@link #headline Message.FileMethod.downloading} -- Response from client.
     * </ul>
     */
    public enum FileMethod {
        upload,
        uploading,
        download,
        downloading
    }

    /**
     * Type-safe enumeration for the type of a message. The types are:
     *
     *  <ul>
     *      <li>{@link #normal Message.Type.normal} -- (Default) a normal text message
     *          used in email like interface.
     *      <li>{@link #chat Message.Type.chat} -- a typically short text message used
     *          in line-by-line chat interfaces.
     *      <li>{@link #groupchat Message.Type.groupchat} -- a chat message sent to a
     *          groupchat server for group chats.
     *      <li>{@link #headline Message.Type.headline} -- a text message to be displayed
     *          in scrolling marquee displays.
     *      <li>{@link #error Message.Type.error} -- indicates a messaging error.
     * </ul>
     */
    public enum Type {

        /**
         * (Default) a normal text message used in email like interface.
         */
        normal,

        /**
         * Typically short text message used in line-by-line chat interfaces.
         */
        chat,

        /**
         * Chat message sent to a groupchat server for group chats.
         */
        groupchat,
        
        /**
         * Chat message sent to a server to prepare for sending file.
         */
        file,

        /**
         * Text message to be displayed in scrolling marquee displays.
         */
        headline,

        /**
         * Indicates a messaging error.
         */
        error;
    }
}