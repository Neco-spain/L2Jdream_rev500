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
import com.dipedro.gameserver.skills.Env;
import com.dipedro.gameserver.templates.skills.L2EffectType;

public class EffectDanceStun extends L2Effect
{
	public EffectDanceStun(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.STUN;
	}

	@Override
	public void onStart()
	{
		getEffected().startAbnormalEffect(L2Character.ABNORMAL_EFFECT_DANCE_STUNNED);
		getEffected().setIsImobilised(true);
		getEffected().disableAllSkills();
	}

	@Override
	public void onExit()
	{
		getEffected().stopAbnormalEffect(L2Character.ABNORMAL_EFFECT_DANCE_STUNNED);
		getEffected().setIsImobilised(false);
		getEffected().enableAllSkills();
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}