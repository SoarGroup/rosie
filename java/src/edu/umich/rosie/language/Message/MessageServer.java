package edu.umich.rosie.language.Message;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import edu.umich.rosie.language.IMessagePasser;
import edu.umich.rosie.language.LanguageConnector.MessageType;
import edu.umich.rosie.language.Service.ServiceDescription;
import edu.umich.rosie.language.Service.ServiceResponder;

public class MessageServer implements IMessagePasser
{
	public static final String static_ServiceName = "Soar-RJMessageServer";
	public static final String static_ServiceInstanceName = "Soar-RJMessageServer";

	private ServerSocket serverSocket;
	private ServiceDescription serviceDescription;
	private ServiceResponder serviceResponder;

	private ArrayList<IMessageListener> listeners = new ArrayList<IMessageListener>();
	private Semaphore listenerMutex = new Semaphore(1);

	private ArrayList<MessageSendingThread> clients = new ArrayList<MessageSendingThread>();
	private ArrayList<MessageResponseThread> responders = new ArrayList<MessageResponseThread>();
	private Semaphore clientMutex = new Semaphore(1);

	private ServerAcceptorThread acceptorThread;

	private class ServerAcceptorThread extends Thread
	{
		public void run()
		{
			System.out.println("Opened socket at " + serverSocket.getInetAddress() + ":" + serverSocket.getLocalPort());

			while (true)
			{
				try
				{
					Socket socket = serverSocket.accept();
					System.out.println("Connection from: " + socket.getInetAddress());

					try
					{
						clientMutex.acquire();

						try
						{
							MessageSendingThread sThread = new MessageSendingThread(socket);
							MessageResponseThread rThread = new MessageResponseThread(socket, listeners, listenerMutex);

							clients.add(sThread);
							responders.add(rThread);

							sThread.start();
							rThread.start();
						}
						finally
						{
							clientMutex.release();
						}
					}
					catch (InterruptedException e)
					{
						System.err.println("Failed to aquire mutex.");
					}
				}
				catch (IOException ie)
				{
					System.err.println("Exception: " + ie);
				}
			}
		}
	}

	public MessageServer(int port)
	{
		try
		{
			serverSocket = new ServerSocket(port);
		}
		catch (IOException ioe)
		{
			System.err.println("Could not bind a server socket to a free port: " + ioe);
			System.exit(1);
		}

//		serviceDescription = new ServiceDescription();
//		serviceDescription.setAddress(serverSocket.getInetAddress());
//		serviceDescription.setPort(serverSocket.getLocalPort());
//		serviceDescription.setInstanceName(static_ServiceInstanceName);
//		
//		serviceResponder = new ServiceResponder(static_ServiceName);
//		serviceResponder.setDescriptor(serviceDescription);
//		serviceResponder.addShutdownHandler();
//		serviceResponder.startResponder();
//
        acceptorThread = new ServerAcceptorThread();
		acceptorThread.start();
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
			clientMutex.acquire();

			try
			{
				for (MessageSendingThread c : clients)
				{
					c.messages.put(new RosieMessage(messageID++, type, msg));
				}
			}
			finally
			{
				clientMutex.release();
			}
		}
		catch (InterruptedException e)
		{
			System.err.println("Failed to send message!");
		}
	}
}
