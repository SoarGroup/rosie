package edu.umich.rosie.language.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import edu.umich.rosie.language.IMessagePasser.IMessageListener;

public class MessageResponseThread extends Thread
{
	private Socket mySocket;
	private ObjectInputStream streamer;

	private ArrayList<IMessageListener> listeners;
	private Semaphore listenerMutex;

	public MessageResponseThread(Socket socket, ArrayList<IMessageListener> listeners, Semaphore listenerMutex)
	{
		mySocket = socket;
		this.listeners = listeners;
		this.listenerMutex = listenerMutex;

		try
		{
			streamer = new ObjectInputStream(mySocket.getInputStream());
		}
		catch (IOException e)
		{
			System.err.println("Failed to create object output stream!");
		}
	}

	public void run()
	{
		if (streamer == null)
		{
			return;
		}

		while (!mySocket.isClosed())
		{
			RosieMessageSerializable message = null;

			try
			{
				message = (RosieMessageSerializable) streamer.readObject();

				listenerMutex.acquire();

				try
				{
					for (IMessageListener l : listeners)
					{
						l.receiveMessage(message.toRosieMessage());
					}
				}
				finally
				{
					listenerMutex.release();
				}
			}
			catch (IOException e)
			{
				System.err.println("Failed to read from stream!");
				try
				{
					mySocket.close();
				}
				catch (IOException e1)
				{
					System.err.println("Failed to close socket!");
					break;
				}
			}
			catch (ClassNotFoundException e)
			{
				System.err.println("Class Not Found Exception" + e.toString());
			}
			catch (InterruptedException e)
			{
				System.err.println("Failed to aquire listener mutex");
			}
		}
	}
}
