package com.dipedro.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import com.dipedro.Config;
import com.dipedro.gameserver.model.actor.L2Npc;
import com.dipedro.gameserver.model.entity.event.FunEvent;
import com.dipedro.gameserver.model.entity.olympiad.Olympiad;
import com.dipedro.gameserver.network.serverpackets.ActionFailed;
import com.dipedro.gameserver.network.serverpackets.NpcHtmlMessage;
import com.dipedro.gameserver.templates.chars.L2NpcTemplate;

public class L2CustomEventManagerInstance extends L2Npc
{
	public FunEvent 		_event = null;

    public L2CustomEventManagerInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }

	@Override
	public void showChatWindow(L2PcInstance player, int val)
	{
		if (player == null || _event == null)
			return;
		
		if (Config.ALLOW_AIO_BLOCK_EVENT)
		{
			if (player.isAio())
			{
				player.sendMessage("You can't participate To Events whit Aio Character!");
				player.sendPacket(new ActionFailed());
				return;
			}
		}
		if (!player.getEventName().equals("") && !player.getEventName().equalsIgnoreCase(_event.EVENT_NAME))
		{
			player.sendMessage("You are already registered in other Fun Event.");
			player.sendPacket(new ActionFailed());
			return;
		}
		if (Olympiad.getInstance().isRegistered(player) || player.getOlympiadGameId() != -1)
		{
			player.sendMessage("You are already registered in olympiad match.");
			player.sendPacket(new ActionFailed());
			return;
		}

		NpcHtmlMessage npcHtmlMessage = _event.getChatWindow(player);

		if (npcHtmlMessage != null)
		{
	    	npcHtmlMessage.replace("%objectId%", String.valueOf(getObjectId()));
	    	player.sendPacket(npcHtmlMessage);
	    }

		player.sendPacket(new ActionFailed());
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String currentCommand = st.nextToken();

		if (currentCommand.startsWith("join"))
		{
			int joinTeamId = Integer.parseInt(st.nextToken());
			_event.addPlayer(player, joinTeamId);
			showChatWindow(player, 0);
		}
		else if (currentCommand.startsWith("leave"))
		{
			_event.removePlayer(player);
			showChatWindow(player, 0);
		}
		else
			showChatWindow(player, 0);
	}
}