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
package com.dipedro.gameserver.handler.skillhandlers;

import com.dipedro.Config;
import com.dipedro.gameserver.ai.CtrlIntention;
import com.dipedro.gameserver.handler.ISkillHandler;
import com.dipedro.gameserver.model.L2Effect;
import com.dipedro.gameserver.model.L2Object;
import com.dipedro.gameserver.model.L2Skill;
import com.dipedro.gameserver.model.actor.L2Character;
import com.dipedro.gameserver.model.actor.L2Summon;
import com.dipedro.gameserver.model.actor.instance.L2ItemInstance;
import com.dipedro.gameserver.model.actor.instance.L2PcInstance;
import com.dipedro.gameserver.model.actor.instance.L2SummonInstance;
import com.dipedro.gameserver.network.SystemMessageId;
import com.dipedro.gameserver.network.serverpackets.SystemMessage;
import com.dipedro.gameserver.skills.Formulas;
import com.dipedro.gameserver.skills.Stats;
import com.dipedro.gameserver.templates.item.L2WeaponType;
import com.dipedro.gameserver.templates.skills.L2SkillType;
import com.dipedro.gameserver.util.Util;

public class Blow implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
		{
		L2SkillType.BLOW
		};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (activeChar.isAlikeDead())
			return;
		
		boolean bss = activeChar.checkBss();
		boolean sps = activeChar.checkSps();
		boolean ss = activeChar.checkSs();
		
		Formulas.getInstance();
		
		for (L2Character target: (L2Character[]) targets)
		{
			if (target.isAlikeDead())
				continue;
			
			// Check firstly if target dodges skill
			final boolean skillIsEvaded = Formulas.calcPhysicalSkillEvasion(target, skill);
			
			byte _successChance = 0;// = SIDE;
			
			if(skill.getName().equals("Backstab"))
			{
				if(activeChar.isBehindTarget())
					_successChance = (byte) Config.BACKSTAB_ATTACK_BEHIND;
				else if(activeChar.isFrontTarget())
					_successChance = (byte) Config.BACKSTAB_ATTACK_FRONT;
				else
					_successChance = (byte) Config.BACKSTAB_ATTACK_SIDE;
			}
			else
			{
				if(activeChar.isBehindTarget())
					_successChance = (byte) Config.BLOW_ATTACK_BEHIND;
				else if(activeChar.isFrontTarget())
					_successChance = (byte) Config.BLOW_ATTACK_FRONT;
				else
					_successChance = (byte) Config.BLOW_ATTACK_SIDE;
			}
			
			//If skill requires Crit or skill requires behind,
			//calculate chance based on DEX, Position and on self BUFF
			boolean success = true;
			if ((skill.getCondition() & L2Skill.COND_BEHIND) != 0)
			{
				if(skill.getName().equals("Backstab"))
				{
					success = (_successChance == Config.BACKSTAB_ATTACK_BEHIND);
				}
				else
				{
					success = (_successChance == Config.BLOW_ATTACK_BEHIND);
				}
			}
			
			if ((skill.getCondition() & L2Skill.COND_CRIT) != 0)
				success = (success && Formulas.getInstance().calcBlow(activeChar, target, _successChance));
			
			if (!skillIsEvaded && success)
			{
				if (skill.hasEffects())
				{
					target.stopSkillEffects(skill.getId());
					if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
					{
						skill.getEffects(activeChar, target);
						SystemMessage sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
						sm.addSkillName(skill);
						target.sendPacket(sm);
					}
					else
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.ATTACK_FAILED);
						sm.addSkillName(skill);
						activeChar.sendPacket(sm);
						return;
					}
				}
				
				L2ItemInstance weapon = activeChar.getActiveWeaponInstance();
				boolean soul = false;
				if(weapon!=null)
				{
					soul = (ss && (weapon.getItemType() == L2WeaponType.DAGGER ));
				}
				
				boolean shld = Formulas.calcShldUse(activeChar, target);
				
				// Critical hit
				boolean crit = false;
				
				//Critical damage condition is applied for sure if there is skill critical condition
				if((skill.getCondition() & L2Skill.COND_CRIT) != 0)
				{
					crit = true;
					//if there is not critical condition, calculate critical chance
				}
				else if(Formulas.calcCrit(skill.getBaseCritRate() * 10 * Formulas.getInstance().getSTRBonus(activeChar)))
					crit = true;
				
				double damage = (int) Formulas.calcBlowDamage(activeChar, target, skill, shld, crit, soul);
				
				if (skill.getDmgDirectlyToHP() && target instanceof L2PcInstance)
				{
					//no vegeange implementation
					final L2Character[] ts = {target, activeChar};
					
					/*
					 * This loop iterates over previous array but, if skill damage is not reflected
					 * it stops on first iteration (target) and misses activeChar
					 */
					for (L2Character targ : ts)
					{
						L2PcInstance player = (L2PcInstance) targ;
						//L2PcInstance player = (L2PcInstance)target;
						if (!player.isInvul())
						{
							// Check and calculate transfered damage
							L2Summon summon = player.getPet();
							if (summon instanceof L2SummonInstance && Util.checkIfInRange(900, player, summon, true))
							{
								int tDmg = (int) damage * (int) player.getStat().calcStat(Stats.TRANSFER_DAMAGE_PERCENT, 0, null, null) / 100;
								
								// Only transfer dmg up to current HP, it should
								// not be killed
								if (summon.getCurrentHp() < tDmg)
									tDmg = (int) summon.getCurrentHp() - 1;
								if (tDmg > 0)
								{
									summon.reduceCurrentHp(tDmg, activeChar);
									damage -= tDmg;
								}
							}
							if (damage >= player.getCurrentHp())
							{
								if (player.isInDuel())
									player.setCurrentHp(1);
								else
								{
									player.setCurrentHp(0);
									if (player.isInOlympiadMode())
									{
										player.abortAttack();
										player.abortCast();
										player.getStatus().stopHpMpRegeneration();
										//player.setIsDead(true);
										player.setIsPendingRevive(true);
										if (player.getPet() != null)
											player.getPet().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE,null);
									}
									else
										player.doDie(activeChar);
								}
							}
							else
								player.setCurrentHp(player.getCurrentHp() - damage);
						}
						SystemMessage smsg = new SystemMessage(SystemMessageId.S1_GAVE_YOU_S2_DMG);
						smsg.addString(activeChar.getName());
						smsg.addNumber((int) damage);
						player.sendPacket(smsg);
						
						// stop if no vengeance, so only target will be effected
						if (!player.vengeanceSkill(skill))
							break;
					} // end for
				} // end skill directlyToHp check
				else
				{
					target.reduceCurrentHp(damage, activeChar);
					
					// vengeance reflected damage
					if (target.vengeanceSkill(skill))
						activeChar.reduceCurrentHp(damage, target);
				}
				
				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}
				if(activeChar instanceof L2PcInstance)
				{
					L2PcInstance activePlayer = (L2PcInstance) activeChar;
					
					activePlayer.sendDamageMessage(target, (int)damage, false, true, false);
				}
				
				//Possibility of a lethal strike
				Formulas.calcLethalHit(activeChar, target, skill);
				
			}
			else 
			{
				if(skillIsEvaded)
					if(target instanceof L2PcInstance)
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.AVOIDED_S1S_ATTACK);
						sm.addString(activeChar.getName());
						((L2PcInstance) target).sendPacket(sm);
					}
				
				SystemMessage sm = new SystemMessage(SystemMessageId.ATTACK_FAILED);
				sm.addSkillName(skill);
				activeChar.sendPacket(sm);
				return;
			}
			
			//Self Effect
			if (skill.hasSelfEffects())
			{
				final L2Effect effect = activeChar.getFirstEffect(skill.getId());
				if (effect != null && effect.isSelfEffect())
					effect.exit(false);
				skill.getEffectsSelf(activeChar);
			}
		}
		
		if (skill.isMagic())
		{
			if (bss)
			{
				activeChar.removeBss();
			}
			else if(sps)
			{
				activeChar.removeSps();
			}
			
		}
		else
		{
			
			activeChar.removeSs();
			
		}
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
