/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package handlers.chathandlers;

import com.dipedro.gameserver.handler.IChatHandler;
import com.dipedro.gameserver.model.actor.instance.L2PcInstance;
import com.dipedro.gameserver.network.serverpackets.CreatureSay;

public class ChatPartyRoomAll implements IChatHandler
{
	private static final int[] COMMAND_IDS = { 16 };

	public void handleChat(int type, L2PcInstance activeChar, String target, String text)
	{
		if(activeChar.isInParty())
		{
			if(activeChar.getParty().isInCommandChannel() && activeChar.getParty().isLeader(activeChar))
			{
				activeChar.getParty().getCommandChannel().broadcastToChannelMembers(new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), text));
			}
		}
	}

	public int[] getChatTypeList()
	{
		return COMMAND_IDS;
	}

}