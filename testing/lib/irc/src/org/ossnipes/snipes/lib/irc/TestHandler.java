package org.ossnipes.snipes.lib.irc;

/** Just a simple class to test the IRCEventListener interface. */
class TestHandler implements IRCEventListener
{
	public TestHandler(IRCBase parent)
	{
		_parent = parent;
		_parent.addEventListener(this);
	}

	public Event[] register()
	{
		return new Event[] {Event.IRC_RESPONSE_CODE};
	}

	public void handleEvent(Event ev, EventArgs args)
	{
		if (ev == Event.IRC_RESPONSE_CODE)
		{
			System.out.println("Test: text=" + args.getParam("text") + "server=" + args.getParam("server") + "code=" + args.getParam("code"));
		}
	}

	private IRCBase _parent;
}