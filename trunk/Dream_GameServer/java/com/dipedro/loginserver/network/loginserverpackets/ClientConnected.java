package com.dipedro.loginserver.network.loginserverpackets;

import java.io.IOException;

import com.dipedro.loginserver.L2LoginClient;
import com.dipedro.loginserver.network.serverpackets.ServerBasePacket;

public class ClientConnected extends ServerBasePacket
{
	public ClientConnected(L2LoginClient cl)
	{
		writeC(0x05);
		writeS(cl.getIntetAddress());
	}

	@Override
	public byte[] getContent() throws IOException
	{
		return getBytes();
	}
}
