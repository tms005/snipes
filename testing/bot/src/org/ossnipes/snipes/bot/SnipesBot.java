package org.ossnipes.snipes.bot;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ossnipes.snipes.lib.irc.Event;
import org.ossnipes.snipes.lib.irc.EventArgs;
import org.ossnipes.snipes.lib.irc.IRCBase;

public class SnipesBot extends IRCBase implements PropertyConstants,
		SnipesConstants
{

	public SnipesBot(String[] args) throws UnknownHostException, IOException
	{
		this.checkClassDeps();

		this.parseCmdArgs(args);

		this.initializeConfiguration();

		this.readSetNickRealname();

		this.readSetDebugVerbose();

		this.readServerPortConnect();

		this.readChannelsAndJoin();

		this.readLoadPlugins();
	}

	private void checkClassDeps()
	{
		try
		{
			Class.forName("java.util.HashMap");
		} catch (ClassNotFoundException e)
		{
			System.err
					.println("Checks have shown that you do not have the Java Collections API. This API is required to run Snipes."
							+ " The Java Collections API has existed in Java since version 1.2, and you are running version "
							+ System.getProperty("java.version")
							+ ". Please upgrade Java and try again.");
			System.exit(Exit.EXIT_JVM_VERSION_TOO_LOW.ordinal());
		}
	}

	private void readLoadPlugins()
	{
		List<String> tempLst = new ArrayList<String>();
		tempLst.addAll(Arrays.asList(SnipesConstants.CORE_MODULES));
		tempLst.addAll(Arrays.asList(this._c.getPropertyAsStringArray(
				MODULES_PROP_NAME, MODULES_PROP_DEFAULTS)));
		String[] modulesArr = tempLst.toArray(new String[tempLst.size()]);
		this._coll = new ModuleCollection(this, modulesArr);
	}

	private void readSetNickRealname()
	{
		// We can't specify a default in the constants, because this is
		// dependent on the bot's current nick.
		this.setNick(this._c.getProperty(NICK_PROP_NAME, NICK_PROP_DEFAULT));
		this.setRealname(this._c.getProperty(REALNAME_PROP_NAME,
				REALNAME_PROP_DEFAULT));
	}

	private void readSetDebugVerbose()
	{
		Boolean debugging = this._c.getPropertyAsBoolean(DEBUG_PROP_NAME,
				DEBUG_PROP_DEFAULT);
		this.setDebugging(debugging != null ? debugging : false);

		Boolean verbose = this._c.getPropertyAsBoolean(VERBOSE_PROP_NAME,
				VERBOSE_PROP_DEFAULT);
		this.setVerbose(verbose != null ? verbose : false);
	}

	private void initializeConfiguration()
	{
		try
		{
			this._c = new Configuration(SnipesConstants.CONFIGURATION_FILENAME);
		} catch (IOException e)
		{
			System.err.println("Could not load configuration file "
					+ SnipesConstants.CONFIGURATION_FILENAME);
			System.exit(Exit.EXIT_CONFIGNOLOAD.ordinal());
		}
	}

	private void parseCmdArgs(String[] args)
	{
		ArgumentParser.getParser().parseArgs(this._c, args);
	}

	private void readServerPortConnect() throws IOException,
			UnknownHostException
	{
		Integer port = this._c.getPropertyAsInteger(PORT_PROP_NAME,
				PORT_PROP_DEFAULT);

		if (port == null)
		{
			System.err.println(PORT_PROP_NAME
					+ " could not be parsed as a integer. Using default value "
					+ PORT_PROP_DEFAULT + ".");
		}

		this.connect(
				this._c.getProperty(SERVER_PROP_NAME, SERVER_PROP_DEFAULT),
				port != null ? port : IRC_DEFAULT_PORT);
	}

	private void readChannelsAndJoin()
	{
		String[] channels = this._c.getPropertyAsStringArray(
				CHANNELS_PROP_NAME, CHANNELS_PROP_DEFAULTS);

		for (String channel : channels)
		{
			if (channel.length() < 2)
			{
				System.err
						.println("Channel joining: Cannot join "
								+ channel
								+ ". Channel name too short (must be the prefix and at least one other character.). Not joining...");
			}

			boolean validPrefix = false;
			// Get the first char of the channel
			char c = channel.charAt(0);
			// Loop through the valid prefixes.
			for (char prefix : IRC_CHANPREFIXES)
			{
				// If they have this prefix
				if (prefix == c)
				{
					validPrefix = true;
					// No sense in continuing checks.
					break;
				}
			}
			// Check if they gave us a valid channel prefix
			if (!validPrefix)
			{
				System.err
						.println("Channel joining: Could not join channel "
								+ channel
								+ ". Invalid or no prefix. Snipes will continue without joining this channel.");
			}
			else
			{
				this.join(channel);
			}
		}

	}

	@Override
	public void handleEvent(Event ev, EventArgs args)
	{
		// TODO: Event sending code.
	}

	public Configuration getConfiguration()
	{
		return this._c;
	}

	ModuleManager loadModule(String moduleName) throws ModuleLoadException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException
	{
		return this._coll.addModule(this, moduleName);
	}

	boolean removeModule(String moduleName)
	{
		return this._coll.removeModule(this, moduleName,
				ModuleExitState.EXIT_UNLOAD);
	}

	public boolean isModuleLoaded(String name)
	{
		return this._coll.isModuleLoaded(name);
	}

	Configuration _c;
	ModuleCollection _coll;
}