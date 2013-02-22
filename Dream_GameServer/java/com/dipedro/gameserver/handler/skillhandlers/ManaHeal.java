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
import com.dipedro.gameserver.model.L2Object;
import com.dipedro.gameserver.model.L2Skill;
import com.dipedro.gameserver.model.actor.L2Character;
import com.dipedro.gameserver.model.actor.instance.L2PcInstance;
import com.dipedro.gameserver.network.SystemMessageId;
import com.dipedro.gameserver.network.serverpackets.StatusUpdate;
import com.dipedro.gameserver.network.serverpackets.SystemMessage;
import com.dipedro.gameserver.skills.Stats;
import com.dipedro.gameserver.templates.skills.L2SkillType;

public class ManaHeal implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.MANAHEAL,
		L2SkillType.MANARECHARGE,
		L2SkillType.MANAHEAL_PERCENT
	};

	@Override
	public void useSkill(L2Character actChar, L2Skill skill, L2Object[] targets)
	{
		for(L2Character target : (L2Character[]) targets)
		{
			double mp = skill.getPower();
			if(skill.getSkillType() == L2SkillType.MANAHEAL_PERCENT)
			{
				mp = target.getMaxMp() * mp / 100.0;
			}
			else
			{
				mp = (skill.getSkillType() == L2SkillType.MANARECHARGE) ? target.calcStat(Stats.RECHARGE_MP_RATE, mp, null, null) : mp;
			}
			if(!skill.isPotion()){

                if(actChar.checkBss()){
                    mp *= 2;
                    actChar.removeBss();
                }else if(actChar.checkSps()){
                    mp *= 1.5;
                    actChar.removeSps();
                }

            }

			target.setLastHealAmount((int) mp);
			target.setCurrentMp(mp + target.getCurrentMp());
			StatusUpdate sump = new StatusUpdate(target.getObjectId());
			sump.addAttribute(StatusUpdate.CUR_MP, (int) target.getCurrentMp());
			target.sendPacket(sump);

			if(actChar instanceof L2PcInstance && actChar != target)
			{
				target.sendPacket(new SystemMessage(SystemMessageId.S2_MP_RESTORED_BY_S1).addString(actChar.getName()).addNumber((int) mp));
			}
			else
			{
				target.sendPacket(new SystemMessage(SystemMessageId.S1_MP_RESTORED).addNumber((int) mp));
			}
		}
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}