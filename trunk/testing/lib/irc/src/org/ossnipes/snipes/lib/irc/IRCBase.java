package org.ossnipes.snipes.lib.irc;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import javax.net.SocketFactory;

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
 * Main class for the Snipes IRC framework. The Snipes IRC framework is a
 * framework that is meant to replace the current implementation of IRC
 * communication in The Open Source Snipes Project (PircBot) so that the project
 * can "stand on it's own two feet" and be able to change from license to
 * license as the author sees fit. There was also a need for SSL support, so
 * that is built into the framework with the {@link SnipesSSLSocketFactory}
 * class.
 * 
 * @author Jack McCracken (<a
 *         href="http://ossnipes.org/">http://ossnipes.org</a>)
 * 
 * @since Snipes 0.6
 * 
 */
public abstract class IRCBase implements IRCConstants, BotConstants
{

	/** The default constructor, performs no action. */
	// Default constructor
	public IRCBase()
	{
		// Init maps.
		topics = new HashMap<String,String>();
	}

	/**
	 * Connects to the IRC server.
	 * 
	 * @param server
	 *            The server IP/host to connect to. If null, a
	 *            {@link IllegalArgumentException} is thrown.
	 * @param port
	 *            The port to connect on. If it is not between 1 and 65535,
	 *            throws {@link IllegalArgumentException}.
	 * @param factory
	 *            The SocketFactory to use. If null, a SnipesSocketFactory is
	 *            used.
	 * @throws IOException
	 *             If there is a unknown IO error while connecting to the
	 *             server.
	 * @throws UnknownHostException
	 *             If the host specified by "server" doesn't exist.
	 */
	public void connect(String server, int port, SocketFactory factory)
			throws IOException, UnknownHostException
	{
		Thread.currentThread().setName("Snipes-IRC-Framework-Main");
		// If port <= 0 || port > 65535, throw IllegalArgumentException
		// because port numbers can only be 1-65535 (The maximum value for a
		// 16-bit integer)
		if (port <= 0 || port > 65535)
		{
			throw new IllegalArgumentException(
					"Port must be between 1 and 65535.");
		}

		// If the server is null, throw a IllegalArgumentException (do I really
		// have to explain this?)
		if (server == null)
		{
			throw new IllegalArgumentException("Server may not be null");
		}

		// This used to be a series of ifs, but we can just turn it into a
		// conditional statement
		// Check if the factory is null, if it is, use SnipesSocketFactory's
		// default.
		_factory = (factory != null ? factory : SnipesSocketFactory
				.getDefault());

		// Create the socket, pass it to the new manager.
		_manager = new IRCSocketManager(_factory.createSocket(server, port));

		// Initialise the IRCInputHandler
		_handler = new IRCInputHandler(this);

		// Quick, init the IRCReciever before the server kills us for not
		// registering our USER, NICK and PING commands :P!
		_reciever = new IRCReciever(_manager, _handler);
		// Create/Start the recv Thread
		Thread t = new Thread(_reciever);
		t.start();

		// We can start!
		sendInit();
		// We're connected!
	}
	
	/** Sends a few lines we need to the server before we start */
	private void sendInit()
	{
		_manager.sendRaw("USER " + _user + " 0 Snipes :" + _realname);
		_manager.sendRaw("NICK " + _nick);
	}

	/**
	 * Connects to the IRC server using a SnipesSocketFactory.
	 * 
	 * @param server
	 *            The server IP/host to connect to. If null, a
	 *            {@link IllegalArgumentException} is thrown.
	 * @param port
	 *            The port to connect on. If it is not between 1 and 65535,
	 *            throws {@link IllegalArgumentException}.
	 * @throws IOException
	 *             If there is a unknown IO error while connecting to the
	 *             server.
	 * @throws UnknownHostException
	 *             If the host specified by "server" doesn't exist.
	 * @see #connect(String, int, SocketFactory)
	 */
	public void connect(String server, int port) throws IOException,
			UnknownHostException
	{
		// Defaults to a SnipesSocketFactory if third parameter is null.
		connect(server, port, null);
	}

	/**
	 * Connects to the IRC server using a SnipesSocketFactory and the default
	 * port (see below.)
	 * 
	 * @param server
	 *            The server IP/host to connect to. If null, a
	 *            {@link IllegalArgumentException} is thrown.
	 * @param port
	 *            The port to connect on. If it is not between 1 and 65535,
	 *            throws {@link IllegalArgumentException}.
	 * @throws IOException
	 *             If there is a unknown IO error while connecting to the
	 *             server.
	 * @throws UnknownHostException
	 *             If the host specified by "server" doesn't exist.
	 * @see #connect(String, int, SocketFactory)
	 * @see BotConstants#IRC_DEFAULT_PORT for the port used by default by
	 *      Snipes. I won't specify it here, as it may change.
	 */
	public void connect(String server) throws IOException, UnknownHostException
	{
		// Connect to the server with the default IRC port and 
		// SocketFactory.
		connect(server, IRC_DEFAULT_PORT, null);
	}

	public void setNick(String nick)
	{
		if (nick == null)
		{
			throw new IllegalArgumentException("Nick cannot be null.");
		}
		this._nick = nick;
		if (_reciever.isConnected())
		{
			_manager.sendRaw("NICK " + _nick);
		}
	}
	
	protected void join(String channel)
	{
		_manager.sendRaw("JOIN " + channel);
	}
	/** Used to handle a event sent by {@link #sendEvent(Event, EventArgs)}.
	 * @param ev The event that was sent.
	 * @param args The arguments for the event.
	 */
	public abstract void handleEvent(Event ev, EventArgs args);
	
	public final void handleInternalEvent(Event ev, EventArgs args)
	{
		switch (ev)
		{
		case IRC_PING:
		{
			_manager.sendRaw("PONG :" + (String)args.getParam("server"));
		}
		}
	}
	
	public String getNick()
	{
		return _nick;
	}
	
	/** Sends a event to the bot, checking if it is a internal one,
	 *  and if it is, it calls the appropriate method.
	 * @param ev The event to send.
	 * @param args The arguments to use.
	 */
	public void sendEvent(Event ev, EventArgs args)
	{
		_handler.sendEvent(ev, args);
	}
	
	protected void setVerbose(boolean on)
	{
		BotOptions.VERBOSE = on;
	}
	
	protected boolean isVerbose()
	{
		return BotOptions.VERBOSE;
	}
	
	protected void who(String target)
	{
		_manager.sendRaw("WHO :" + target);
	}

	/** The current nick of the bot */
	private String _nick = DEFAULT_NICK;
	/**
	 * The current username (see {@link BotConstants#DEFAULT_USER} for a
	 * definition of username)
	 */
	private String _user = DEFAULT_USER;
	
	private String _realname = DEFAULT_REALNAME;

	/** The SocketFactory which all connections to a server will be created with */
	private SocketFactory _factory;
	/**
	 * The IRCSocketManager that will be used to manage the current server
	 * connection
	 */
	private IRCSocketManager _manager;
	/**
	 * The IRCReciever that will be in a separate thread, passing messages to
	 * the handler
	 */
	private IRCReciever _reciever;
	/** The IRCInputHandler that will pass all received messages to us. */
	private IRCInputHandler _handler;
	
	/** Holds all the topics of the channels we're in. */
	private Map<String,String> topics;

}
