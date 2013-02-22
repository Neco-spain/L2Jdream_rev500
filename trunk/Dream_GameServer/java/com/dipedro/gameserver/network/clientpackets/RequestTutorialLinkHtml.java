/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.dipedro.gameserver.network.clientpackets;

import com.dipedro.gameserver.model.actor.instance.L2PcInstance;
import com.dipedro.gameserver.model.quest.QuestState;

public class RequestTutorialLinkHtml extends L2GameClientPacket
{
	private static final String _C__7B_REQUESTTUTORIALLINKHTML = "[C] 7b RequestTutorialLinkHtml";

	String _bypass;

	@Override
	protected void readImpl()
	{
		_bypass = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		try
		{
		}
		catch(Exception e)
		{
		}

		QuestState qs = player.getQuestState("Q255_Tutorial");
		if(qs != null)
		{
			qs.getQuest().notifyEvent(_bypass, null, player);
		}
	}

	@Override
	public String getType()
	{
		return _C__7B_REQUESTTUTORIALLINKHTML;
	}
}