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
package com.dipedro.gameserver.model.actor.instance;

import com.dipedro.gameserver.ai.CtrlIntention;
import com.dipedro.gameserver.ai.L2AttackableAI;
import com.dipedro.gameserver.model.L2World;
import com.dipedro.gameserver.model.L2WorldRegion;
import com.dipedro.gameserver.model.actor.L2Attackable;
import com.dipedro.gameserver.model.actor.L2Character;
import com.dipedro.gameserver.model.actor.knownlist.GuardNoHTMLKnownList;
import com.dipedro.gameserver.model.actor.position.L2CharPosition;
import com.dipedro.gameserver.network.serverpackets.ActionFailed;
import com.dipedro.gameserver.network.serverpackets.MyTargetSelected;
import com.dipedro.gameserver.network.serverpackets.ValidateLocation;
import com.dipedro.gameserver.templates.chars.L2NpcTemplate;
import com.dipedro.gameserver.thread.ThreadPoolManager;
import com.dipedro.util.random.Rnd;

public final class L2GuardNoHTMLInstance extends L2Attackable
{
	private int _homeX;
	private int _homeY;
	private int _homeZ;
	private static final int RETURN_INTERVAL = 60000;

	public class ReturnTask implements Runnable
	{
		@Override
		public void run()
		{
			if(getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE) {
				returnHome();
			}
		}
	}

	public L2GuardNoHTMLInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		getKnownList();
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new ReturnTask(), RETURN_INTERVAL, RETURN_INTERVAL + Rnd.nextInt(60000));
	}

	@Override
	public final GuardNoHTMLKnownList getKnownList()
	{
		if(super.getKnownList() == null || !(super.getKnownList() instanceof GuardNoHTMLKnownList))
		{
			setKnownList(new GuardNoHTMLKnownList(this));
		}

		return(GuardNoHTMLKnownList) super.getKnownList();
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return attacker instanceof L2MonsterInstance;
	}

	public void getHomeLocation()
	{
		_homeX = getX();
		_homeY = getY();
		_homeZ = getZ();
	}

	public int getHomeX()
	{
		return _homeX;
	}

	public void returnHome()
	{
		if(!isInsideRadius(_homeX, _homeY, 150, false))
		{
			clearAggroList();
			getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(_homeX, _homeY, _homeZ, 0));
		}
	}

	@Override
	public void onSpawn()
	{
		_homeX = getX();
		_homeY = getY();
		_homeZ = getZ();
		L2WorldRegion region = L2World.getInstance().getRegion(getX(), getY());
		if(region != null && !region.isActive())
		{
			((L2AttackableAI) getAI()).stopAITask();
		}
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		if(getObjectId() != player.getTargetId())
		{
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
			player.setTarget(this);

			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			if(containsTarget(player))
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
			}
			else
			{
				if(!isInsideRadius(player, INTERACTION_DISTANCE, false, false))
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				}
				else
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, null);
				}
			}
		}
	}

}