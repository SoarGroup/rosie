package edu.umich.rosie.language.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.net.UnknownHostException;

public final class ServiceResponder implements Runnable
{

	protected static InetAddress multicastAddressGroup;
	protected static int multicastPort;

	static
	{
		try
		{
			multicastAddressGroup = InetAddress
					.getByName(ServiceConstants.MULTICAST_ADDRESS_GROUP);
			multicastPort = ServiceConstants.MULTICAST_PORT;
		}
		catch (UnknownHostException uhe)
		{
			System.err.println("Unexpected exception: " + uhe);
			uhe.printStackTrace();
			System.exit(1);
		}
	}

	protected String serviceName;
	protected ServiceDescription descriptor;
	protected boolean shouldRun = true;
	protected MulticastSocket socket;
	protected DatagramPacket queuedPacket;
	protected DatagramPacket receivedPacket;
	protected Thread myThread;

	public ServiceResponder(String serviceName)
	{
		this.serviceName = serviceName;
		try
		{
			socket = new MulticastSocket(multicastPort);
			socket.joinGroup(multicastAddressGroup);
			socket.setSoTimeout(ServiceConstants.RESPONDER_SOCKET_TIMEOUT);

		}
		catch (IOException ioe)
		{
			System.err.println("Unexpected exception: " + ioe);
			ioe.printStackTrace();
			System.exit(1);
		}
	}

	public ServiceDescription getDescriptor()
	{
		return descriptor;
	}

	public void setDescriptor(ServiceDescription descriptor)
	{
		this.descriptor = descriptor;
	}

	public String getServiceName()
	{
		return serviceName;
	}

	protected String getEncodedServiceName()
	{
		try
		{
			return URLEncoder.encode(getServiceName(), "UTF-8");
		}
		catch (UnsupportedEncodingException uee)
		{
			return null;
		}
	}

	public void setServiceName(String serviceName)
	{
		this.serviceName = serviceName;
	}

	public void startResponder()
	{
		if (myThread == null || !myThread.isAlive())
		{
			shouldRun = true;
			myThread = new Thread(this, "ServiceResponder");
			myThread.setDaemon(true);
			myThread.start();
		}
	}

	public void stopResponder()
	{
		if (myThread != null && myThread.isAlive())
		{
			shouldRun = false;
			myThread.interrupt();
		}
	}

	protected void sendQueuedPacket()
	{
		if (queuedPacket == null)
		{
			return;
		}
		try
		{
			socket.send(queuedPacket);
			queuedPacket = null;
		}
		catch (IOException ioe)
		{
			System.err.println("Unexpected exception: " + ioe);
			ioe.printStackTrace();
			/* resume operation */
		}
	}

	public void run()
	{

		while (shouldRun)
		{

			byte[] buf = new byte[ServiceConstants.DATAGRAM_LENGTH];
			receivedPacket = new DatagramPacket(buf, buf.length);

			try
			{
				socket.receive(receivedPacket); // note a timeout in effect

				if (isQueryPacket())
				{
					DatagramPacket replyPacket = getReplyPacket();
					queuedPacket = replyPacket;
					sendQueuedPacket();
				}
			}
			catch (SocketTimeoutException ste)
			{
				/*
				 * ignored; this exception is by design to break the blocking
				 * from socket.receive
				 */
			}
			catch (IOException ioe)
			{
				System.err.println("Unexpected exception: " + ioe);
				ioe.printStackTrace();
				/* resume operation */
			}

		}
	}

	protected boolean isQueryPacket()
	{
		if (receivedPacket == null)
		{
			return false;
		}

		String dataStr = new String(receivedPacket.getData());
		int pos = dataStr.indexOf((char) 0);
		if (pos > -1)
		{
			dataStr = dataStr.substring(0, pos);
		}

		if (dataStr.startsWith("SERVICE QUERY " + getEncodedServiceName()))
		{
			return true;
		}

		return false;
	}

	protected DatagramPacket getReplyPacket()
	{
		StringBuffer buf = new StringBuffer();
		try
		{
			buf.append("SERVICE REPLY " + getEncodedServiceName() + " ");
			buf.append(descriptor.toString());
		}
		catch (NullPointerException npe)
		{
			System.err.println("Unexpected exception: " + npe);
			npe.printStackTrace();
			return null;
		}

		byte[] bytes = buf.toString().getBytes();
		DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
		packet.setAddress(multicastAddressGroup);
		packet.setPort(multicastPort);

		return packet;
	}

	public void addShutdownHandler()
	{
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			public void run()
			{
				stopResponder();
			}
		});
	}

}
