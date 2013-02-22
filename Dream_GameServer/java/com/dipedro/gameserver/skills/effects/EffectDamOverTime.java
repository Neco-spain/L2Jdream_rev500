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
package com.dipedro.gameserver.skills.effects;

import com.dipedro.gameserver.model.L2Effect;
import com.dipedro.gameserver.model.L2Skill.SkillTargetType;
import com.dipedro.gameserver.model.actor.L2Attackable;
import com.dipedro.gameserver.network.SystemMessageId;
import com.dipedro.gameserver.network.serverpackets.SystemMessage;
import com.dipedro.gameserver.skills.Env;
import com.dipedro.gameserver.templates.skills.L2EffectType;

class EffectDamOverTime extends L2Effect
{
	public EffectDamOverTime(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.DMG_OVER_TIME;
	}

	@Override
	public boolean onActionTime()
	{
		if(getEffected().isDead())
		{
			return false;
		}

		double damage = calc();

		if(damage >= getEffected().getCurrentHp() - 1)
		{
			if(getSkill().isToggle())
			{
				getEffected().sendPacket(new SystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_HP));
				getEffected().removeEffect(this);
				this.exit();
				return false;
			}

			if(getSkill().getId() != 4082)
			{
				if(getEffected().getCurrentHp() <= 1)
				{
					return true;
				}

				damage = getEffected().getCurrentHp() - 1;
			}
		}

		boolean awake = !(getEffected() instanceof L2Attackable) && !(getSkill().getTargetType() == SkillTargetType.TARGET_SELF && getSkill().isToggle());

		getEffected().reduceCurrentHp(damage, getEffector(), awake);

		return true;
	}
}