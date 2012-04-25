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
package com.l2jfrozen.gameserver.handler.skillhandlers;

//import java.util.logging.Logger;

import com.l2jfrozen.gameserver.ai.CtrlEvent;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.handler.ISkillHandler;
import com.l2jfrozen.gameserver.managers.DuelManager;
import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.model.L2Summon;
import com.l2jfrozen.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.skills.Formulas;
import com.l2jfrozen.util.random.Rnd;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.2.2.9 $ $Date: 2005/04/03 15:55:04 $
 */
public class Continuous implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS = {
			L2Skill.SkillType.BUFF,
			L2Skill.SkillType.DEBUFF,
			L2Skill.SkillType.DOT,
			L2Skill.SkillType.MDOT,
			L2Skill.SkillType.POISON,
			L2Skill.SkillType.BLEED,
			L2Skill.SkillType.HOT,
			L2Skill.SkillType.CPHOT,
			L2Skill.SkillType.MPHOT,
			//L2Skill.SkillType.MANAHEAL,
			//L2Skill.SkillType.MANA_BY_LEVEL,
			L2Skill.SkillType.FEAR,
			L2Skill.SkillType.CONT,
			L2Skill.SkillType.WEAKNESS,
			L2Skill.SkillType.REFLECT,
			L2Skill.SkillType.UNDEAD_DEFENSE,
			L2Skill.SkillType.AGGDEBUFF,
			L2Skill.SkillType.FORCE_BUFF };
	private L2Skill _skill;
	
	@Override
	public void useSkill(final L2Character activeChar,L2Skill skill2,final L2Object[] targets)
	{
		if(activeChar == null)
			return;

		L2PcInstance player = null;
		if(activeChar instanceof L2PcInstance)
			player = (L2PcInstance) activeChar;

		if(skill2.getEffectId() != 0)
		{
			int skillLevel = skill2.getEffectLvl();
			int skillEffectId = skill2.getEffectId();
			if(skillLevel == 0)
			{
				_skill = SkillTable.getInstance().getInfo(skillEffectId, 1);
			}
			else
			{
				_skill = SkillTable.getInstance().getInfo(skillEffectId, skillLevel);
			}

			if(_skill != null)
				skill2 = _skill;
		}
		
		final L2Skill skill = skill2;
		if(skill == null){
			return;
		}
		
		boolean bss = activeChar.checkBss();
		boolean sps = activeChar.checkSps();
		boolean ss = activeChar.checkSs();
		
		for(final L2Object target2 : targets)
		{
			L2Character target = (L2Character) target2;

			if(target == null){
				continue;
			}
			
			if(target instanceof L2PcInstance && activeChar instanceof L2PlayableInstance && skill.isOffensive())
			{
				L2PcInstance _char = (activeChar instanceof L2PcInstance)?(L2PcInstance)activeChar:((L2Summon)activeChar).getOwner();
				L2PcInstance _attacked = (L2PcInstance) target;
				if(_attacked.getClanId()!=0 && _char.getClanId()!=0 && 
				   _attacked.getClanId() == _char.getClanId() && _attacked.getPvpFlag()==0)
					continue;
				if(_attacked.getAllyId()!=0 && _char.getAllyId()!=0 && 
				   _attacked.getAllyId()==_char.getAllyId() && _attacked.getPvpFlag()==0)
					continue;
			}

			if(skill.getSkillType() != L2Skill.SkillType.BUFF && skill.getSkillType() != L2Skill.SkillType.HOT && skill.getSkillType() != L2Skill.SkillType.CPHOT && skill.getSkillType() != L2Skill.SkillType.MPHOT && skill.getSkillType() != L2Skill.SkillType.UNDEAD_DEFENSE && skill.getSkillType() != L2Skill.SkillType.AGGDEBUFF && skill.getSkillType() != L2Skill.SkillType.CONT)
			{
				if(target.reflectSkill(skill))
					target = activeChar;
			}

			// Walls and Door should not be buffed
			if(target instanceof L2DoorInstance && (skill.getSkillType() == L2Skill.SkillType.BUFF || skill.getSkillType() == L2Skill.SkillType.HOT))
				continue;

			// Anti-Buff Protection prevents you from getting buffs by other players
			if (activeChar instanceof L2PlayableInstance && target != activeChar && target.isBuffProtected() && !skill.isHeroSkill()
				    && (skill.getSkillType() == L2Skill.SkillType.BUFF
				    || skill.getSkillType() == L2Skill.SkillType.HEAL_PERCENT
					|| skill.getSkillType() == L2Skill.SkillType.FORCE_BUFF
					|| skill.getSkillType() == L2Skill.SkillType.MANAHEAL_PERCENT
					|| skill.getSkillType() == L2Skill.SkillType.COMBATPOINTHEAL
					|| skill.getSkillType() == L2Skill.SkillType.REFLECT))
					continue;
			
			// Player holding a cursed weapon can't be buffed and can't buff
			if(skill.getSkillType() == L2Skill.SkillType.BUFF)
			{
				if(target != activeChar)
				{
					if(target instanceof L2PcInstance && ((L2PcInstance) target).isCursedWeaponEquiped())
						continue;
					else if(player != null && player.isCursedWeaponEquiped())
						continue;
				}
			}

			//Possibility of a lethal strike
			if(!target.isRaid() && !(target instanceof L2NpcInstance && ((L2NpcInstance) target).getNpcId() == 35062))
			{
				int chance = Rnd.get(100);
				Formulas.getInstance();
				if(skill.getLethalChance2() > 0 && chance < Formulas.calcLethal(activeChar, target, skill.getLethalChance2()))
				{
					if(target instanceof L2NpcInstance)
					{
						target.reduceCurrentHp(target.getCurrentHp() - 1, activeChar);
						activeChar.sendPacket(new SystemMessage(SystemMessageId.LETHAL_STRIKE));
					}
				}
				else
				{
					Formulas.getInstance();
					if(skill.getLethalChance1() > 0 && chance < Formulas.calcLethal(activeChar, target, skill.getLethalChance1()))
					{
						if(target instanceof L2NpcInstance)
						{
							target.reduceCurrentHp(target.getCurrentHp() / 2, activeChar);
							activeChar.sendPacket(new SystemMessage(SystemMessageId.LETHAL_STRIKE));
						}
					}
				}
			}

			if(skill.isOffensive())
			{
				
				boolean acted = Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss);

				if(!acted)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.ATTACK_FAILED));
					continue;
				}

			}
			else if(skill.getSkillType() == L2Skill.SkillType.BUFF)
			{
				if(!Formulas.getInstance().calcBuffSuccess(target, skill))
				{
					if(player!=null)
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
						sm.addString(target.getName());
						sm.addSkillName(skill.getDisplayId());
						activeChar.sendPacket(sm);
					}
					continue;
				}
			}
			
			boolean stopped = false;

			L2Effect[] effects = target.getAllEffects();
			if(effects != null)
			{
				for(L2Effect e : effects)
				{
					if(e != null)
						if(e.getSkill().getId() == skill.getId())
						{
							e.exit(false);
							stopped = true;
						}
				}
			}
			
			effects = null;

			if(skill.isToggle() && stopped)
				break;

			// If target is not in game anymore...
			if((target instanceof L2PcInstance) && ((L2PcInstance) target).isOnline()==0)
				continue;
			

			// if this is a debuff let the duel manager know about it
			// so the debuff can be removed after the duel
			// (player & target must be in the same duel)
			if(target instanceof L2PcInstance && player != null 
					&& ((L2PcInstance) target).isInDuel() 
					&& (skill.getSkillType() == L2Skill.SkillType.DEBUFF 
						|| skill.getSkillType() == L2Skill.SkillType.BUFF) 
					&& player.getDuelId() == ((L2PcInstance) target).getDuelId())
			{
				DuelManager dm = DuelManager.getInstance();
				if(dm!=null)
				{
					effects = skill.getEffects(activeChar, target, ss, sps, bss);
					if(effects!=null)
						for(L2Effect buff : effects)
							if(buff != null)
								dm.onBuff(((L2PcInstance) target), buff);
				}
				dm = null;
			}
			else
				skill.getEffects(activeChar, target, ss, sps, bss);

			if(skill.getSkillType() == L2Skill.SkillType.AGGDEBUFF)
			{
				if(target instanceof L2Attackable)
					target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, (int) skill.getPower());
				else if(target instanceof L2PlayableInstance)
				{
					if(target.getTarget() == activeChar)
						target.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, activeChar);
					else
						target.setTarget(activeChar);
				}
			}

			if(target.isDead() && skill.getTargetType() == L2Skill.SkillTargetType.TARGET_AREA_CORPSE_MOB && target instanceof L2NpcInstance)
			{
				((L2NpcInstance) target).endDecayTask();
			}
		}
		
		if(!skill.isToggle()){
			
			if (skill.isMagic())
			{
				if (bss){
					activeChar.removeBss();
				}else if(sps){
					activeChar.removeSps();
				}
				
			}else{
				
				activeChar.removeSs();
				
			}
			
		}
		
		player = null;
		
		// self Effect :]
		L2Effect effect = activeChar.getFirstEffect(skill.getId());
		if(effect != null && effect.isSelfEffect())
		{
			//Replace old effect with new one.
			effect.exit(false);
		}
		skill.getEffectsSelf(activeChar);

		effect = null;
	}

	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
