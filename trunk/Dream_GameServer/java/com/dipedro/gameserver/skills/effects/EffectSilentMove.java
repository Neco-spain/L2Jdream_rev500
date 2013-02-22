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
import com.dipedro.gameserver.model.actor.L2Character;
import com.dipedro.gameserver.model.actor.instance.L2PcInstance;
import com.dipedro.gameserver.network.SystemMessageId;
import com.dipedro.gameserver.network.serverpackets.SystemMessage;
import com.dipedro.gameserver.skills.Env;
import com.dipedro.gameserver.templates.skills.L2EffectType;
import com.dipedro.gameserver.templates.skills.L2SkillType;

final class EffectSilentMove extends L2Effect
{
	public EffectSilentMove(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();

		L2Character effected = getEffected();
		if(effected instanceof L2PcInstance)
		{
			((L2PcInstance) effected).setSilentMoving(true);
		}
	}

	@Override
	public void onExit()
	{
		super.onExit();

		L2Character effected = getEffected();
		if(effected instanceof L2PcInstance)
		{
			((L2PcInstance) effected).setSilentMoving(false);
		}
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.SILENT_MOVE;
	}

	@Override
	public boolean onActionTime()
	{
		if(getSkill().getSkillType() != L2SkillType.CONT)
		{
			return false;
		}

		if(getEffected().isDead())
		{
			return false;
		}

		double manaDam = calc();

		if(manaDam > getEffected().getCurrentMp())
		{
			getEffected().sendPacket(new SystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP));
			return false;
		}

		getEffected().reduceCurrentMp(manaDam);
		return true;
	}
}