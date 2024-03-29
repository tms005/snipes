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
 * If not, see http://www.gnu.org/licenses/.
 */

import java.util.HashMap;
import java.util.Map;


/** Utility methods used a lot in the bot and not really tied to a certain class.
 * This class also contains an important method that is at the core of the Snipes
 * event-sending mechanism. All the methods in this class are static.
 *
 * @author Jack McCracken (Auv5)
 * @since Snipes 0.6
 */
public class BotUtils
implements BotConstants
{
	// We can't be created by anyone but us. We don't even create us :P.
	private BotUtils() {}

	/** Takes a String array and a Object array and outputs a Map of Strings (keys) and Objects.<BR/>
	 * The main use of this method is to convert arrays given as parameters to {@link EventArgs#EventArgs(String[], String[])}<BR/>
	 * to a {@link HashMap} (how event arguments are stored internally.)
	 * @param keys The first array, used as the keys in the map.
	 * @param values The second array, used as values in the map for the keys with the same index.
	 * @return A String Object Map, with the keys array as keys and the values array as values.
	 * @throws IllegalArgumentException If keys.length != values.length.
	 */
	public static Map<String,Object> stringObjectArraysToStringObjectMap(String[] keys, Object[] values)
	{
		// Do the lengths not match, if not, throw a IllegalArgumentException.
		if (keys.length != values.length)
		{
			throw new IllegalArgumentException("Length of keys array must be the same as length of values array.");
		}
		// Create a map to hold the result.
		Map<String,Object> result = new HashMap<String,Object>();
		// It really doesn't matter which one we loop through :).
		for (int i = 0; i < keys.length; i++)
		{
			// Put the key with the value.
			result.put(keys[i], values[i]);
		}
		return result;
	}


	/** This method is the heart of the Snipes event-sending mechanism. It sends the event specified by ev with the
	 * parameters in args to all registered {@link IRCEventListener}s.<BR/>
	 * This method treats {@link IRCBase}s specially, casting them down to IRCBase and calling it's {@link IRCBase#handleInternalEvent(Event, EventArgs)}
	 * method.
	 * @param ev The enumerated identifier for the event to be sent.
	 * @param args The arguments object to be passed to the functions.
	 * @param bot The bot that this event originated from. This is used to get the event handlers registered to it.
	 */
	public static void sendEvent(Event ev, EventArgs args, IRCBase bot)
	{
		// Is it a internal event?
		boolean isInternal = false;

		// Loop through the events
		for (int i = 0; i < INT_EVENTS.length; i++)
		{
			// Check if it's internal.
			if (INT_EVENTS[i] == ev)
			{
				isInternal = true;
				// No sense in continuing checks.
				break;
			}
		}

		// Loop through the listeners
		for (EventHandlerManager ehm : bot.getListeners())
		{
			boolean isBase = ehm.isIRCBase();
			if (!isBase)
			{
				if (ehm.isSubscribed(ev))
				{
					ehm.sendEvent(ev, args);
				}
			}
			else
			{
				if (isInternal)
				{
					((IRCBase)ehm.getManaged()).handleInternalEvent(ev,args);
				}
				ehm.sendEvent(ev,args);
			}
		}
	}

	/** Does the behaviour of {@link Integer#parseInt(String)}, but without throwing a 
	 * Exception. It returns null on error. This method is converted from a method in the
	 * fourth post in <a href="http://www.coderanch.com/t/401142/java/java/check-if-String-value-numeric">This thread</a>
	 * 15
	 * @param input The input String Object.
	 * @return The Integer Object of the number if it is parsable, null otherwise.
	 */
	public static Integer convertToInt( String input )
	{
		try
		{
			// Try and parse the integer
			Integer i = Integer.parseInt( input );
			return i;
		}
		catch(Exception e)
		{
			return null;
		}
	}
	
	/** Determines if a String can be successfully parsed as a Integer.
	 * @param s The String to check
	 * @return True if the String can be parsed as a Integer.
	 */
	public static boolean isInteger(String s)
	{
		/*// HACK: We have to use a stack trace :(.
		if (s == null)
		{
			return false;
		}
		try
		{
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException e) {return false;}*/
		return s.matches("^\\d+$");
	}
}
