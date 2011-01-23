package org.ossnipes.snipes.lib.irc;

/* 
 * 
 * Copyright 2010 Jack McCracken
 * This file is part of The Snipes IRC Framework.
 * 
 * The Snipes IRC Framework is free software: you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * The Snipes IRC Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 * Although this project was created for use within The Open Source Snipes Project, it is legally 
 * a completely different project. This means that you may distribute it (along with 
 * the source) with your own GPL-compatible projects without having to distribute the actual
 * Implementation project (The Open Source Snipes Project) with it.
 * 
 * Note for other developers of this project: Please include this in any files you create, so that it
 * may be made "legally" a part of the project.
 * 
 * You should have received a copy of the GNU General Public License along with The Snipes IRC Framework. 
 * If not, see http:www.gnu.org/licenses/.
 */

/** This class represents a user on IRC
 * @author Jack McCracken
 * @since Snipes 0.6
 */
public class IRCUser
implements BotConstants,
IRCEventListener
{
	IRCUser(final IRCBase parent, final String nick)
	{
		_parent = parent;
        _parent.addEventListener(this);
	}
	
    public void handleEvent(Event ev, EventArgs args)
    {
        switch (ev)
        {
            
        }
    }
    
	/** Gets this user Object's hostname. */
	public void getHostname()
	{
		// Have we gotten this user's host before?
		if (_hostname == null)
		{
			// Populate the hostname variable.
			populateUserHost(null);
		}
	}
	
	private void beginGettingUserHost()
	{
        _parent.who("");
	}
    
    private void populateUserHost(String host)
    {
        
    }

	void setHostname(String hostname)
	{
        if (_hostname == null)
        {
            throw new IllegalArgumentException("hostname cannot be null.");
        }
        _hostname = null;
	}
	
	public String getNick()
	{
		return _nick;
	}
	
    
    public Event[] register() {return new Event[] {Event.IRC_RESPONSE_CODE};}
	// Class-scope variables
	String _hostname;
	String _nick;
	IRCBase _parent;
}
