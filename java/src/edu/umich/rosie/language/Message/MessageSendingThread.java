package edu.umich.rosie.language.Message;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import edu.umich.rosie.language.IMessagePasser.RosieMessage;

public class MessageSendingThread extends Thread
{
	private Socket mySocket;
	private ObjectOutputStream streamer;

	public BlockingQueue<RosieMessage> messages;

	public MessageSendingThread(Socket socket)
	{
		mySocket = socket;

		try
		{
			streamer = new ObjectOutputStream(mySocket.getOutputStream());
		}
		catch (IOException e)
		{
			System.err.println("Failed to create object output stream!");
		}

		messages = new LinkedBlockingQueue<RosieMessage>();
	}

	public void run()
	{
		if (streamer == null)
		{
			return;
		}

		while (!mySocket.isClosed())
		{
			RosieMessage message;

			try
			{
				message = messages.take();

				streamer.writeObject(new RosieMessageSerializable(message));
			}
			catch (InterruptedException e1)
			{
				System.err.println("Failed to get object");
			}
			catch (IOException e)
			{
				System.err.println("Failed to write to stream!");
			}
		}
	}
}
