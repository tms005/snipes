package plugins.bouncer;

import org.ossnipes.snipes.bot.SnipesBot;

public class BotClient implements PseudoClient
{

	public BotClient(SnipesBot sb)
	{
		this._sb = sb;
	}

	@Override
	public String getNick()
	{
		return "*bot";
	}

	@Override
	public void performCommand(String command, BouncerConnection bc)
	{
		System.err.println("Doing command :)");

		// Not done in isLineTo because we don't want the line falling through
		// to IRCClient.
		// TODO: AT LEAST this command does not work.
		if (!ClientUtils.isMessage(command, true))
		{
			System.err.println("Was not a message.");
			return;
		}
		// Unused, for now.

		if (command.indexOf(':') == -1)
		{
			// They don't comply with the protocol.
			return;
		}

		String msg = command.substring(command.indexOf(':') + 1);
		if (msg.equalsIgnoreCase("helloworld"))
		{
			bc.sendRawLineToClient(":"
					+ ClientUtils.getHostname(this, this._sb.getConfiguration())
					+ " PRIVMSG " + this._sb.getNick() + " :Hello world!");
		}
		// TODO: Identify command, etc.
	}

	@Override
	public boolean isLineTo(String line, BouncerConnection bc)
	{
		if (!bc.isAuthed())
		{
			return false;
		}
		else
		{
			return ClientUtils.isMessageToMe(this, line);
		}
	}

	private final SnipesBot _sb;
}
