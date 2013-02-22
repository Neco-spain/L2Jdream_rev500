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
package com.dipedro.gameserver.handler.skillhandlers;

import com.dipedro.gameserver.handler.ISkillHandler;
import com.dipedro.gameserver.managers.CastleManager;
import com.dipedro.gameserver.managers.GrandBossManager;
import com.dipedro.gameserver.model.L2Clan;
import com.dipedro.gameserver.model.L2Effect;
import com.dipedro.gameserver.model.L2Object;
import com.dipedro.gameserver.model.L2Skill;
import com.dipedro.gameserver.model.actor.L2Character;
import com.dipedro.gameserver.model.actor.instance.L2PcInstance;
import com.dipedro.gameserver.model.entity.siege.Castle;
import com.dipedro.gameserver.network.SystemMessageId;
import com.dipedro.gameserver.network.serverpackets.SystemMessage;
import com.dipedro.gameserver.templates.skills.L2SkillType;
import com.dipedro.gameserver.thread.ThreadPoolManager;

public class ClanGate implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = { L2SkillType.CLAN_GATE };

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		L2PcInstance player = null;
		if(activeChar instanceof L2PcInstance)
		{
			player = (L2PcInstance) activeChar;
		}
		else
		{
			return;
		}

		if(player.isInEvent() || player.isInsideZone(L2Character.ZONE_NOLANDING) || player.isInOlympiadMode() || player.isInsideZone(L2Character.ZONE_PVP) || GrandBossManager.getInstance().getZone(player) != null)
		{
			player.sendMessage("Cannot open the portal here.");
			return;
		}

		L2Clan clan = player.getClan();
		if(clan != null)
		{
			if(CastleManager.getInstance().getCastleByOwner(clan) != null)
			{
				Castle castle = CastleManager.getInstance().getCastleByOwner(clan);
				if(player.isCastleLord(castle.getCastleId()))
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new RemoveClanGate(castle.getCastleId(), player), skill.getTotalLifeTime());
					castle.createClanGate(player.getX(), player.getY(), player.getZ() + 20);
					player.getClan().broadcastToOnlineMembers(new SystemMessage(SystemMessageId.COURT_MAGICIAN_CREATED_PORTAL));
					player.setIsParalyzed(true);
				}
			}
		}

		L2Effect effect = player.getFirstEffect(skill.getId());
		if(effect != null && effect.isSelfEffect())
		{
			effect.exit();
		}
		skill.getEffectsSelf(player);
	}

	private class RemoveClanGate implements Runnable
	{
		private final int castle;
		private final L2PcInstance player;

		private RemoveClanGate(int castle, L2PcInstance player)
		{
			this.castle = castle;
			this.player = player;
		}

		@Override
		public void run()
		{
			if(player != null)
			{
				player.setIsParalyzed(false);
			}

			CastleManager.getInstance().getCastleById(castle).destroyClanGate();
		}
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}