package org.ossnipes.snipes.lib.irc;

import java.util.HashMap;
import java.util.Map;

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
 * If not, see http://www.gnu.org/licenses/.
 */

/**
 * Class that handles input from the IRC server and sends them off to it's
 * parent.
 * 
 * @author Jack McCracken
 * @since Snipes 0.6
 */

class IRCInputHandler implements BotConstants, IRCConstants
{
    // Solution to the problem of the VERSION message being a PRIVMSG :\.
    private boolean finishedConnection = false;
    
	IRCInputHandler(IRCBase parent)
	{
		_parent = parent;
	}

	/**
	 * Package-level method to actually handle the lines sent by the IRC server.
	 * 
	 * @param line
	 *            The line that we are to handle. Cannot be null.
	 */
	void handle(String line)
    {
		// Is the bot verbose?
		if (BotOptions.VERBOSE)
		{
			System.out.println(line);
		}
		
		String[] exSplit = line.split(" ");
		
		
		if (exSplit.length == 0)
		{
			return;
		}

        if (!finishedConnection)
        {
            // If it's more than or equal to four, we can
            // continue.
            if (line.length() >= 4)
            {
                // Check for a response code guaranteed to be sent by the server
                // Before the MOTD. We don't want to use the end of MOTD response
                // for this, because that would mean that this wouldn't work on
                // servers with no MOTD.
                if (BotUtils.isInteger(exSplit[1]) && BotUtils.convertToInt(exSplit[1])
                        == RPL_LUSERME)
                {
                    finishedConnection = true;
                }
            }
            return;
        }
        
        
        if (exSplit.length > 1 && BotUtils.isInteger(exSplit[1]))
        {
            Map<String,Object> params = new HashMap<String,Object>();
            
            // The response code
            params.put("code", BotUtils.convertToInt(exSplit[1]));
            
            // We need to stick it together.
            String msg;
            // Boo!! Protocol fail!
            if (!exSplit[3].startsWith(":"))
			{
				msg = exSplit[3];
			} else
			{
				msg = "";
				for (int i = 3; i < exSplit.length; i++)
				{
					// Is it the first one?
					// Then we need to take away the :.
					if (i == 3)
					{
						msg += exSplit[i].substring(1);
					}
					// We just concat it! :D
					else
					{
						msg += " " + exSplit[i];
					}
				}
			}
            
            params.put("text", msg);
            
            params.put("server", exSplit[0].substring(1));
            
            // Fire off the event.
            BotUtils.sendEvent(Event.IRC_RESPONSE_CODE, new EventArgs(params), _parent);
        }
        
		// PING command: we need this or the server'll disconnect us!
		if (exSplit[0].equals("PING"))
		{
			// Key: server = The server we're connected to
			BotUtils.sendEvent(Event.IRC_PING, new EventArgs(new String[] { "server" },
			// Substringed because we need to take off the :.
					new String[] { exSplit[1].substring(1) }),_parent);
		}
		
		// PRIVMSG command: If the user sends a PRIVMSG to us or to a channel
		// Example: ":Auv5!~auv5@projectinfinity.net PRIVMSG #Snipes :A IRC PRIVMSG!"
		else if (exSplit.length >= 4 && exSplit[1].equalsIgnoreCase("PRIVMSG"))
		{
			// Hold the args.
			Map<String, Object> params = new HashMap<String, Object>();
			// Add the sender
			// We don't need to check the length,
			// it was already checked at the top.
			params.put("from", exSplit[0].split("!")[0].
			// Support for servers which don't
			// put : in front of the host.
			substring((exSplit[0].startsWith(":") ? 1 : 0)));

			// Put the hostname in from-host
			params.put("from-host", exSplit[0].split("@")[1]);

			// Add the recipient, this will be getNick()
			// if we receive a PRIVMSG personally.
			params.put("to", exSplit[2]);

			/* Start getting the actual message */
			// Variable to hold the message
			String msg;
			// Supporting non-standard servers that don't use : in front of all
			// of them, even 1
			// -word. :\
			if (!exSplit[3].startsWith(":"))
			{
				msg = exSplit[3];
			} else
			{
				msg = "";
				for (int i = 3; i < exSplit.length; i++)
				{
					// Is it the first one?
					// Then we need to take away the :.
					if (i == 3)
					{
						msg += exSplit[i].substring(1);
					}
					// We just concat it! :D
					else
					{
						msg += " " + exSplit[i];
					}
				}
			}
			
			// Stick it into the message variable.
			params.put("message", msg);

			// Fire off the event.
			BotUtils.sendEvent(Event.IRC_PRIVMSG, new EventArgs(params),_parent);
		}
		
		// When the topic is sent to us at join.
		// This is *different* than when the topic is set. That is the next if statement down.
		// Example: ":Equinox.GeekShed.net 332 SnipesBot #Snipes :Article in progress about Snipes :D"
		else if (exSplit.length >= 4 && BotUtils.isInteger(exSplit[1]) &&
				BotUtils.convertToInt(exSplit[1]) == RPL_TOPIC)
		{
			// Holds the parameters
			Map<String, Object> params = new HashMap<String, Object>();
			// Server: The server sending the notice to us.
			params.put("server", exSplit[0].substring(1));
			// Channel: The channel we're being notified about.
			params.put("channel", exSplit[3]);
			// Topic: The actual text of the topic.
			// We need to fiddle with this, taking off the : (unless we're on a nasty
			// protocol-denier :().
			// Also, we need to stick it together, I forgot about that.
			
			// Will hold the final topic string.
			String msg;
			
			if (!exSplit[4].startsWith(":"))
			{
				msg = exSplit[4];
			} else
			{
				msg = "";
				for (int i = 4; i < exSplit.length; i++)
				{
					// Is it the first one?
					// Then we need to take away the :.
					if (i == 4)
					{
						msg += exSplit[i].substring(1);
					}
					// We just concat it! :D
					else
					{
						msg += " " + exSplit[i];
					}
				}
			}
			// Actually put the parameter! :).
			params.put("topic", msg);
			
			// Fire off the event.
			BotUtils.sendEvent(Event.IRC_JOIN_TOPIC, new EventArgs(params), _parent);
		}
		// When the topic is changed by (typically) an operator
		// This is *different* than when the topic send to us at join.
		// That is the previous event. If a network offers "services" such as Chanserv with a topic
        // retention feature, then this event may trigger when joining a empty channel.
		// Example: ":Unix!~auv5@projectinfinity.net TOPIC #Snipes :Read this! It's a good document for all who want to use Snipes' plugin API :). http://ossnipes.org/docs/snipes/snipes-article.html | ?? PROFIT"
		else if (exSplit.length >= 4 && exSplit[1].equalsIgnoreCase("TOPIC") && exSplit[2].startsWith("#"))
		{
			Map<String, Object> params = new HashMap<String, Object>();
			// Stick the setter's nick in there.
			params.put("setter", exSplit[0].split("!")[0]);
			// The host of the setter
			params.put("setter-host", exSplit[0].split("@")[1]);
			// The channel the topic was set on
			params.put("channel", exSplit[2]);
			
			String msg;
			
			if (!exSplit[3].startsWith(":"))
			{
				msg = exSplit[3];
			} else
			{
				msg = "";
				for (int i = 3; i < exSplit.length; i++)
				{
					// Is it the first one?
					// Then we need to take away the :.
					if (i == 3)
					{
						msg += exSplit[i].substring(1);
					}
					// We just concat it! :D
					else
					{
						msg += " " + exSplit[i];
					}
				}
			}
			// Actually put the parameter! :).
			params.put("topic", msg);
			
			// Fire off the event! :)
			BotUtils.sendEvent(Event.IRC_TOPIC,
					new EventArgs(params),
					_parent);
		}
        // When a user joins a channel we are in.
        // Example: ":Unix!rubicon@projectinfinity.net JOIN :#Snipes"
        //TODO: Works, might need a better check though. Command is pretty small though.
        else if (exSplit.length == 3 && exSplit[1].equalsIgnoreCase("JOIN"))
        {
            // More support for protocol defiance
            String channel = exSplit[2].substring((exSplit[2].startsWith(":") ? 1 : 0));
            // Holds the arguments
            Map<String,Object> params = new HashMap<String,Object>();
            // nick -- The nick of the user joining the channel
            params.put("nick", exSplit[0].split("!")[0].substring(exSplit[0].startsWith(":") ? 1 : 0));
            // host -- The hostname of the user joining
            params.put("host", exSplit[0].split("@")[1]);
            // channel -- The channel being joined.
            // Oh, look at that. We already have a variable for that.
            params.put("channel", channel);
            // Fire off the event.
            BotUtils.sendEvent(Event.IRC_JOIN,
                    new EventArgs(params),
                    _parent);
        }

        // When a user or a network service/server sets a mode on a channel we are in.
        // Example: ":ChanServ!services@geekshed.net MODE #Snipes +qo Unix Unix"
        else if (exSplit.length >= 4 && exSplit[1].equalsIgnoreCase("MODE") && (exSplit[3].startsWith("+") || exSplit[3].startsWith("-")))
        {

            Map<String,Object> params = new HashMap<String,Object>();
            // channel -- The channel the mode is being set on
            params.put("channel", exSplit[2]);
            // TODO: Should really make this more clear. It works, but it seems a bit hard to understand. Determine if it is worth the extra time checking if it is a server.
            // It works because when splitting a String, you always get at least a 1-length array back. This means that your String does not have to contain a ! for this to work,
            // And it just returns the entire String in the first element.
            // setter -- The nick of the nick setting the mode, or the address of the server if it is a server setting it.
            params.put("setter", exSplit[0].split("!")[0].substring((exSplit[0].startsWith(":") ? 1 : 0)));

            // START getting setter-host (the hostname of the user setting the mode, or the address of the server if it's a server setting it)
            String[] atSplit = exSplit[0].split("@");

            if (atSplit.length == 1)
            {
               // It's a server. Just give them the same as setter.
                params.put("setter-host", params.get("setter"));
            }
            else if (atSplit.length > 1)
            {
                // Normal host-splitting behavior.
                params.put("setter-host", atSplit[1]);
            }
            else
            {
                System.err.println("Reached imposable case. Please check which dimension you are in. A split of a String always returns at least a 1-length String array. " +
                        "If this is not true on your implementation of the JDK, please tell me :3.");
                System.exit(2);
            }
            // END getting setter-host

            params.put("mode", exSplit[3]);

            //START getting mode-param (the parameters after the mode, e.g. the part after +qo in ":ChanServ!services@geekshed.net MODE #Snipes +qo Unix Unix")
            // Check if there are any params to the mode.
            if (exSplit.length > 4)
            {
                String msg = "";
                // If there are...
                // Stick them together and send it off in the params.
				for (int i = 4; i < exSplit.length; i++)
				{
					msg += (i == 4 ? "" : " ") + exSplit[i];
				}
                params.put("mode-params", msg);
            }
            else
            {
                // Map will keep it as null.
            }
            // START getting mode-param

            // Fire off the event.
            BotUtils.sendEvent(Event.IRC_MODE, new EventArgs(params), _parent);
        }

        // When a user parts a channel we are in.
        // Example: ":Unix!rubicon@projectinfinity.net PART #Snipes :Ciao."
        // We can't test if there's a message. The RFC says that commands can be sent without a message.
        // This command is similar to the JOIN command :\.
        else if (exSplit.length >= 3 && exSplit[2].equalsIgnoreCase("PART"))
        {
            //TODO: Do.
        }

        //TODO: Implement "PART" functionality
        //TODO: Implement "NICK" functionality
	}



	IRCBase _parent;
}