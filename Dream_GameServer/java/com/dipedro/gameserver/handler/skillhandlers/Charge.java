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

import java.util.logging.Logger;

import com.dipedro.gameserver.handler.ISkillHandler;
import com.dipedro.gameserver.model.L2Effect;
import com.dipedro.gameserver.model.L2Object;
import com.dipedro.gameserver.model.L2Skill;
import com.dipedro.gameserver.model.actor.L2Character;
import com.dipedro.gameserver.model.actor.instance.L2PcInstance;
import com.dipedro.gameserver.templates.skills.L2SkillType;

public class Charge implements ISkillHandler
{
	static Logger _log = Logger.getLogger(Charge.class.getName());

	private static final L2SkillType[] SKILL_IDS = { };

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		for(int index = 0; index < targets.length; index++)
		{
			if(!(targets[index] instanceof L2PcInstance))
			{
				continue;
			}

			L2PcInstance target = (L2PcInstance) targets[index];
			skill.getEffects(activeChar, target);
		}

		L2Effect effect = activeChar.getFirstEffect(skill.getId());
		if(effect != null && effect.isSelfEffect())
		{
			effect.exit();
		}

		skill.getEffectsSelf(activeChar);
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}