package org.ossnipes.snipes.console;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

public class SocketManager 
{
	public SocketManager(Socket s) throws IOException
	{
		if (s == null)
		{
			throw new IllegalArgumentException("s cannot be null.");
		}

		if (!s.isConnected())
		{
			throw new IllegalStateException("s is not connected.");
		}

		this._s = s;
		InputStream rawInS = this._s.getInputStream();
		OutputStream rawOutS = this._s.getOutputStream();

		this._tin = new BufferedReader(new InputStreamReader(rawInS));
		this._tout = new PrintStream(rawOutS);

		this._din = new DataInputStream(rawInS);
		this._dout = new DataOutputStream(rawOutS);

		this._oin = null;
		this._oout = null;
	}

	public void send(String data)
	{
		this._tout.print(data);
	}
	
	public void sendln(String data)
	{
		this._tout.println(data);
	}

	public void send(int data) throws IOException
	{
		this._dout.write(data);
	}

	public void send(double data) throws IOException
	{
		this._dout.writeDouble(data);
	}

	public void send(char data) throws IOException
	{
		this._dout.writeChar(data);
	}

	public void send(float data) throws IOException
	{
		this._dout.writeFloat(data);
	}

	public void send(Object o) throws IOException
	{
		this._oout.writeObject(o);
	}

	public String recv() throws IOException
	{
		return this._tin.readLine();
	}

	public int recvInt() throws IOException
	{
		return this._din.readInt();
	}

	public double recvDouble() throws IOException
	{
		return this._din.readDouble();
	}

	public char recvChar() throws IOException
	{
		return this._din.readChar();
	}

	public float recvFloat() throws IOException
	{
		return this._din.readFloat();
	}

	public Object recvObject() throws IOException, ClassNotFoundException
	{
		return this._oin.readObject();
	}
	
	public boolean textReady() throws IOException
	{
		return _tin.ready();
	}

	public void close()
	{
		try
		{
			this._s.close();
		} catch (IOException owell)
		{
			// We don't care
		}
	}

	private final Socket _s;
	private final DataOutputStream _dout;
	private final DataInputStream _din;
	private final BufferedReader _tin;
	private final PrintStream _tout;
	private final ObjectOutputStream _oout;
	private final ObjectInputStream _oin;
}
