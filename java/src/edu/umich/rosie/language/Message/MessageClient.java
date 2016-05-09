package edu.umich.rosie.language.Message;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import edu.umich.rosie.language.IMessagePasser;
import edu.umich.rosie.language.LanguageConnector.MessageType;
import edu.umich.rosie.language.Service.ServiceBrowser;
import edu.umich.rosie.language.Service.ServiceBrowserListener;
import edu.umich.rosie.language.Service.ServiceDescription;

public class MessageClient implements ServiceBrowserListener, IMessagePasser
{
	private ServiceBrowser browser;
	private ServiceDescription descriptor;

	private Socket mySocket;

	private ArrayList<IMessageListener> listeners = new ArrayList<IMessageListener>();
	private Semaphore listenerMutex = new Semaphore(1);

	private MessageSendingThread server;
	private MessageResponseThread responder;

	public MessageClient()
			throws IOException
	{
//		try
//		{
//			browser = new ServiceBrowser();
//			browser.addServiceBrowserListener(this);
//			browser.setServiceName(MessageServer.static_ServiceName);
//			browser.startListener();
//			browser.startLookup();
//
//			try
//			{
//				Thread.sleep(2000);
//			}
//			catch (InterruptedException ie)
//			{
//			} // ignore
//
//			browser.stopLookup();
//			browser.stopListener();
//		}
//		catch (SocketException e)
//		{
//		}
//
//		if (!isConnected())
//		{
//			System.err.println("MessageClient: Failed to find server broadcasting.");
//		}
	}

	public void newConnection(String name, int port)
			throws IOException
	{
		mySocket = new Socket(name, port);

		server = new MessageSendingThread(mySocket);
		responder = new MessageResponseThread(mySocket, listeners, listenerMutex);

		server.start();
		responder.start();
	}

	public void newConnection(InetAddress ip, int port)
			throws IOException
	{
		mySocket = new Socket();
		mySocket.connect(new InetSocketAddress(ip, port), 5000);

		server = new MessageSendingThread(mySocket);
		responder = new MessageResponseThread(mySocket, listeners, listenerMutex);

		server.start();
		responder.start();
	}

	public void serviceReply(ServiceDescription descriptor)
	{
		if (this.descriptor == null)
		{
			this.descriptor = descriptor;

			try
			{
				newConnection(descriptor.getAddress(), descriptor.getPort());
			}
			catch (IOException ioe)
			{
				System.err.println("Failed to connect to server: " + descriptor.toString());
			}
		}
	}

	@Override
	public void addMessageListener(IMessageListener listener)
	{
		try
		{
			listenerMutex.acquire();

			try
			{
				listeners.add(listener);
			}
			finally
			{
				listenerMutex.release();
			}
		}
		catch (InterruptedException e)
		{
			System.err.println("Failed to add listener");
		}
	}

	@Override
	public void removeMessageListener(IMessageListener listener)
	{
		try
		{
			listenerMutex.acquire();

			try
			{
				listeners.remove(listener);
			}
			finally
			{
				listenerMutex.release();
			}
		}
		catch (InterruptedException e)
		{
			System.err.println("Failed to remove listener");
		}
	}

	private int messageID = 0;

	@Override
	public void sendMessage(String msg, MessageType type)
	{
		try
		{
			server.messages.put(new RosieMessage(messageID++, type, msg));
		}
		catch (InterruptedException e)
		{
			System.err.println("Failed to put server message");
		}
	}

	public boolean isConnected()
	{
		return server != null;
	}
}
