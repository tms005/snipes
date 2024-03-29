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

/**
 * A exception thrown when a operation is attempted that would require
 * connection to a IRC server, and we are not connected.
 * 
 * @author jack
 * 
 */
public class NotConnectedException extends RuntimeException
{
	private static final long serialVersionUID = 7694269847054717555L;

	public NotConnectedException()
	{
		super();
	}

	public NotConnectedException(String message)
	{
		super(message);
	}

	public NotConnectedException(Throwable cause)
	{
		super(cause);
	}

	public NotConnectedException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
