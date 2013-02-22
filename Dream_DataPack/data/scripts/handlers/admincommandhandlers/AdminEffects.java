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
package handlers.admincommandhandlers;

import java.util.StringTokenizer;

import com.dipedro.gameserver.communitybbs.Manager.RegionBBSManager;
import com.dipedro.gameserver.datatables.SkillTable;
import com.dipedro.gameserver.datatables.xml.AdminCommandAccessRights;
import com.dipedro.gameserver.handler.IAdminCommandHandler;
import com.dipedro.gameserver.model.GMAudit;
import com.dipedro.gameserver.model.L2Object;
import com.dipedro.gameserver.model.L2Skill;
import com.dipedro.gameserver.model.L2World;
import com.dipedro.gameserver.model.actor.L2Character;
import com.dipedro.gameserver.model.actor.L2Npc;
import com.dipedro.gameserver.model.actor.L2Summon;
import com.dipedro.gameserver.model.actor.instance.L2ChestInstance;
import com.dipedro.gameserver.model.actor.instance.L2PcInstance;
import com.dipedro.gameserver.network.SystemMessageId;
import com.dipedro.gameserver.network.serverpackets.CharInfo;
import com.dipedro.gameserver.network.serverpackets.Earthquake;
import com.dipedro.gameserver.network.serverpackets.ExRedSky;
import com.dipedro.gameserver.network.serverpackets.ExShowScreenMessage;
import com.dipedro.gameserver.network.serverpackets.L2GameServerPacket;
import com.dipedro.gameserver.network.serverpackets.MagicSkillUser;
import com.dipedro.gameserver.network.serverpackets.PlaySound;
import com.dipedro.gameserver.network.serverpackets.SignsSky;
import com.dipedro.gameserver.network.serverpackets.SocialAction;
import com.dipedro.gameserver.network.serverpackets.StopMove;
import com.dipedro.gameserver.network.serverpackets.SunRise;
import com.dipedro.gameserver.network.serverpackets.SunSet;
import com.dipedro.gameserver.network.serverpackets.SystemMessage;
import com.dipedro.gameserver.network.serverpackets.UserInfo;

public class AdminEffects implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_invis",
		"admin_earthquake",
		"admin_earthquake_menu",
		"admin_bighead",
		"admin_shrinkhead",
		"admin_gmspeed",
		"admin_gmspeed_menu",
		"admin_unpara_all",
		"admin_para_all",
		"admin_unpara",
		"admin_para",
		"admin_unpara_all_menu",
		"admin_para_all_menu",
		"admin_unpara_menu",
		"admin_para_menu",
		"admin_polyself",
		"admin_unpolyself",
		"admin_polyself_menu",
		"admin_unpolyself_menu",
		"admin_clearteams",
		"admin_setteam_close",
		"admin_setteam",
		"admin_social",
		"admin_effect",
		"admin_social_menu",
		"admin_effect_menu",
		"admin_abnormal",
		"admin_abnormal_menu",
		"admin_play_sounds",
		"admin_play_sound",
		"admin_atmosphere",
		"admin_atmosphere_menu",
		"admin_npc_say",
		"admin_debuff"
	};

	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		AdminCommandAccessRights.getInstance().hasAccess(command, activeChar.getAccessLevel());

		GMAudit.auditGMAction(activeChar.getName(), command, (activeChar.getTarget() != null?activeChar.getTarget().getName():"no-target"), "");

		StringTokenizer st = new StringTokenizer(command);
		st.nextToken();

		if (command.startsWith("admin_invis"))
		{
			if (!activeChar.getAppearance().getInvisible())
			{
				activeChar.getAppearance().setInvisible();
				activeChar.decayMe();
				activeChar.broadcastUserInfo();
				activeChar.spawnMe();
				activeChar.sendMessage("You are invisible now!");
			}
			else
			{
				activeChar.getAppearance().setVisible();
				activeChar.broadcastUserInfo();
				activeChar.sendMessage("You are visible now!");
			}
			RegionBBSManager.getInstance().changeCommunityBoard();
		}
		else if(command.startsWith("admin_earthquake"))
		{
			try
			{
				String val1 = st.nextToken();
				int intensity = Integer.parseInt(val1);
				String val2 = st.nextToken();
				int duration = Integer.parseInt(val2);
				Earthquake eq = new Earthquake(activeChar.getX(), activeChar.getY(), activeChar.getZ(), intensity, duration);
				activeChar.broadcastPacket(eq);
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Use: //earthquake <intensity> <duration>");
			}
		}
		else if(command.startsWith("admin_atmosphere"))
		{
			try
			{
				String type = st.nextToken();
				String state = st.nextToken();
				adminAtmosphere(type, state, activeChar);
			}
			catch(Exception ex)
			{
				
			}
		}
		else if(command.startsWith("admin_npc_say"))
		{
			try
			{
				L2Object target = activeChar.getTarget();

				if(activeChar.getSayMode() != null)
				{
					activeChar.setSayMode(null);
					activeChar.sendMessage("NpcSay mode off");
				}
				else
				{
					if(target != null && target instanceof L2Npc)
					{
						activeChar.setSayMode(target);
						activeChar.sendMessage("NpcSay mode on for " + target.getName());
					}
					else
					{
						activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));

						return false;
					}
				}
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Target Npc before. Use: //npc_say");
			}
		}
		else if(command.equals("admin_play_sounds"))
		{
			AdminHelpPage.showHelpPage(activeChar, "effects/songs.htm");
		}
		else if(command.startsWith("admin_play_sounds"))
		{
			try
			{
				AdminHelpPage.showHelpPage(activeChar, "effects/songs" + command.substring(17) + ".htm");
			}
			catch(StringIndexOutOfBoundsException e)
			{
				
			}
		}
		else if(command.startsWith("admin_play_sound"))
		{
			try
			{
				AdminHelpPage.showHelpPage(activeChar, "effects/songs.htm");
				playAdminSound(activeChar, command.substring(17));
			}
			catch(StringIndexOutOfBoundsException e)
			{
				
			}
		}
		else if(command.startsWith("admin_para") || command.startsWith("admin_para_menu"))
		{
			try
			{
				L2Object target = activeChar.getTarget();
				L2Character player = null;
				if(target instanceof L2Character)
				{
					player = (L2Character) target;

					player.startAbnormalEffect(L2Character.ABNORMAL_EFFECT_HOLD_2);

					player.setIsParalyzed(true);
					StopMove sm = new StopMove(player);
					player.sendPacket(sm);
					player.broadcastPacket(sm);
				}
			}
			catch(Exception e)
			{
				
			}
		}
		else if(command.equals("admin_unpara") || command.equals("admin_unpara_menu"))
		{
			try
			{
				L2Object target = activeChar.getTarget();
				L2Character player = null;

				if(target instanceof L2Character)
				{
					player = (L2Character) target;
					player.stopAbnormalEffect((short) 0x0400);
					player.stopAbnormalEffect((short) 0x0800);
					player.setIsParalyzed(false);
				}
			}
			catch(Exception e)
			{
				
			}
		}
		else if(command.startsWith("admin_para_all"))
		{
			try
			{
				for(L2PcInstance player : activeChar.getKnownList().getKnownPlayers().values())
				{
					if(!player.isGM())
					{
						player.startAbnormalEffect(0x0400);
						player.setIsParalyzed(true);
						StopMove sm = new StopMove(player);
						player.sendPacket(sm);
						player.broadcastPacket(sm);
					}
				}
			}
			catch(Exception e)
			{
				
			}
		}
		else if(command.startsWith("admin_unpara_all"))
		{
			try
			{
				for(L2PcInstance player : activeChar.getKnownList().getKnownPlayers().values())
				{
					player.stopAbnormalEffect(0x0400);
					player.setIsParalyzed(false);
				}
			}
			catch(Exception e)
			{
				
			}
		}
		else if(command.startsWith("admin_bighead"))
		{
			try
			{
				L2Object target = activeChar.getTarget();
				L2Character player = null;

				if(target instanceof L2Character)
				{
					player = (L2Character) target;
					player.startAbnormalEffect(0x2000);
				}
			}
			catch(Exception e)
			{
				
			}
		}
		else if(command.startsWith("admin_shrinkhead"))
		{
			try
			{
				L2Object target = activeChar.getTarget();
				L2Character player = null;

				if(target instanceof L2Character)
				{
					player = (L2Character) target;
					player.stopAbnormalEffect((short) 0x2000);
				}
			}
			catch(Exception e)
			{
				
			}
		}
		else if(command.startsWith("admin_gmspeed"))
		{
			try
			{
				int val = Integer.parseInt(st.nextToken());
				boolean sendMessage = activeChar.getFirstEffect(7029) != null;

				activeChar.stopSkillEffects(9009);

				if(val == 0 && sendMessage)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.EFFECT_S1_DISAPPEARED).addSkillName(9009));
				}
				else if(val >= 1 && val <= 5)
				{
					L2Skill gmSpeedSkill = SkillTable.getInstance().getInfo(9009, val);
					activeChar.doSimultaneousCast(gmSpeedSkill);
					if (val == 1)
						activeChar.sendChatMessage(0, 0, "SYS", "Your speed is [50] fast.");
					else if (val == 2)
						activeChar.sendChatMessage(0, 0, "SYS", "Your speed is [100] fast.");
					else if (val == 3)
						activeChar.sendChatMessage(0, 0, "SYS", "Your speed is [150] fast.");
					else if (val == 4)
						activeChar.sendChatMessage(0, 0, "SYS", "Your speed is [200] fast.");
					else if (val == 5)
						activeChar.sendChatMessage(0, 0, "SYS", "Your speed is [250] fast.");
				}
			}
			catch(Exception e)
			{
				activeChar.sendChatMessage(0, 0, "SYS", "//gmspeed [0...5].");
			}
			finally
			{
				activeChar.updateEffectIcons();
			}
		}
		else if(command.startsWith("admin_polyself"))
		{
			try
			{
				String id = st.nextToken();

				activeChar.getPoly().setPolyInfo("npc", id);
				activeChar.teleToLocation(activeChar.getX(), activeChar.getY(), activeChar.getZ(), false);

				CharInfo info1 = new CharInfo(activeChar);
				activeChar.broadcastPacket(info1);
				UserInfo info2 = new UserInfo(activeChar);
				activeChar.sendPacket(info2);
			}
			catch(Exception e)
			{
				activeChar.sendChatMessage(0, 0, "SYS", "Usage: //polyself <npcId>");
			}
		}
		else if(command.startsWith("admin_unpolyself"))
		{
			try
			{
				activeChar.getPoly().setPolyInfo(null, "1");
				activeChar.decayMe();
				activeChar.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());

				CharInfo info1 = new CharInfo(activeChar);
				activeChar.broadcastPacket(info1);
				UserInfo info2 = new UserInfo(activeChar);
				activeChar.sendPacket(info2);
			}
			catch(Exception e)
			{
				
			}
		}
		else if(command.equals("admin_clear_teams"))
		{
			try
			{
				for(L2PcInstance player : activeChar.getKnownList().getKnownPlayers().values())
				{
					player.setTeam(0);
					player.broadcastUserInfo();
				}
			}
			catch(Exception e)
			{
				
			}
		}
		else if(command.startsWith("admin_setteam_close"))
		{
			try
			{
				String val = st.nextToken();

				int teamVal = Integer.parseInt(val);

				for(L2PcInstance player : activeChar.getKnownList().getKnownPlayers().values())
				{
					if(activeChar.isInsideRadius(player, 400, false, true))
					{
						player.setTeam(0);

						if(teamVal != 0)
						{
							player.sendChatMessage(0, 0, "SYS", "You have joined team " + teamVal);
						}

						player.broadcastUserInfo();
					}
				}
			}
			catch(Exception e)
			{
				activeChar.sendChatMessage(0, 0, "SYS", "Usage: //setteam_close <teamId>");
			}
		}
		else if(command.startsWith("admin_setteam"))
		{
			try
			{
				String val = command.substring(14);
				
				int teamVal = Integer.parseInt(val);
				
				L2Object target = activeChar.getTarget();
				L2PcInstance player = null;
				
				if(target instanceof L2PcInstance)
				{
					player = (L2PcInstance) target;
				}
				else
				{
					return false;
				}
				
				player.setTeam(teamVal);
				
				if(teamVal != 0)
				{
					player.sendChatMessage(0, 0, "SYS", "You have joined team " + teamVal);
				}
				player.broadcastUserInfo();
			}
			
			catch (Exception e)
			{
				activeChar.sendChatMessage(0, 0, "SYS", "Usage: //setteam <teamId>");
			}
		}
		else if(command.startsWith("admin_social"))
		{
			try
			{
				String target = null;
				L2Object obj = activeChar.getTarget();

				if(st.countTokens() == 2)
				{
					int social = Integer.parseInt(st.nextToken());

					target = st.nextToken();

					if(target != null)
					{
						L2PcInstance player = L2World.getInstance().getPlayer(target);

						if(player != null)
						{
							if(performSocial(social, player, activeChar))
							{
								activeChar.sendChatMessage(0, 0, "SYS", player.getName() + " was affected by your request.");
							}
						}
						else
						{
							try
							{
								int radius = Integer.parseInt(target);

								for(L2Object object : activeChar.getKnownList().getKnownObjects().values())
								{
									if(activeChar.isInsideRadius(object, radius, false, false))
									{
										performSocial(social, object, activeChar);
									}
								}

								activeChar.sendChatMessage(0, 0, "SYS", radius + " units radius affected by your request.");
							}
							catch(NumberFormatException nbe)
							{
								activeChar.sendChatMessage(0, 0, "SYS", "Incorrect parameter");
							}
						}
					}
				}
				else if(st.countTokens() == 1)
				{
					int social = Integer.parseInt(st.nextToken());

					if(obj == null)
					{
						obj = activeChar;
					}
					if(obj != null)
					{
						if(performSocial(social, obj, activeChar))
						{
							activeChar.sendChatMessage(0, 0, "SYS", obj.getName() + " was affected by your request.");
						}
						else
						{
							activeChar.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
						}
					}
				}
				else if(!command.contains("menu"))
				{
					activeChar.sendChatMessage(0, 0, "SYS", "Usage: //social <social_id> [player_name|radius]");
				}
			}
			catch(Exception e)
			{
				
			}
		}
		else if(command.startsWith("debuff"))
		{
			try
			{
				L2Object target = activeChar.getTarget();
				L2Character player = null;

				if(target instanceof L2Character)
				{
					player = (L2Character) target;
					player.stopAllEffects();
					activeChar.sendChatMessage(0, 0, "SYS", "Effects has been cleared from " + player + ".");
				}
			}
			catch(Exception e)
			{
				
			}
		}
		else if(command.startsWith("admin_abnormal"))
		{
			try
			{
				String target = null;
				L2Object obj = activeChar.getTarget();

				if(st.countTokens() == 2)
				{
					String parm = st.nextToken();

					int abnormal = Integer.decode("0x" + parm);

					target = st.nextToken();

					if(target != null)
					{
						L2PcInstance player = L2World.getInstance().getPlayer(target);

						if(player != null)
						{
							if(performAbnormal(abnormal, player))
							{
								activeChar.sendChatMessage(0, 0, "SYS", player.getName() + "'s abnormal status was affected by your request.");
							}
							else
							{
								activeChar.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
							}
						}
						else
						{
							try
							{
								int radius = Integer.parseInt(target);

								for(L2Object object : activeChar.getKnownList().getKnownObjects().values())
								{
									if(activeChar.isInsideRadius(object, radius, false, false))
									{
										performAbnormal(abnormal, object);
									}
								}

								activeChar.sendChatMessage(0, 0, "SYS", radius + " units radius affected by your request.");
							}
							catch(NumberFormatException nbe)
							{
								activeChar.sendChatMessage(0, 0, "SYS", "Usage: //abnormal <hex_abnormal_mask> [player|radius]");
							}
						}
					}
				}
				else if(st.countTokens() == 1)
				{
					int abnormal = Integer.decode("0x" + st.nextToken());

					if(obj == null)
					{
						obj = activeChar;
					}
					if(obj != null)
					{
						if(performAbnormal(abnormal, obj))
						{
							activeChar.sendChatMessage(0, 0, "SYS", obj.getName() + "'s abnormal status was affected by your request.");
						}
						else
						{
							activeChar.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
						}
					}
				}
				else if(!command.contains("menu"))
				{
					activeChar.sendChatMessage(0, 0, "SYS", "Usage: //abnormal <abnormal_mask> [player_name|radius]");
				}
			}
			catch(Exception e)
			{
				
			}
		}
		else if(command.startsWith("admin_effect"))
		{
			try
			{
				L2Object obj = activeChar.getTarget();

				int level = 1, hittime = 1;
				int skill = Integer.parseInt(st.nextToken());

				if(st.hasMoreTokens())
				{
					level = Integer.parseInt(st.nextToken());
				}
				if(st.hasMoreTokens())
				{
					hittime = Integer.parseInt(st.nextToken());
				}
				if(obj == null)
				{
					obj = activeChar;
				}
				if(obj != null)
				{
					if(!(obj instanceof L2Character))
					{
						activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
					}
					else
					{
						L2Character target = (L2Character) obj;

						target.broadcastPacket(new MagicSkillUser(target, activeChar, skill, level, hittime, 0));
						activeChar.sendChatMessage(0, 0, "SYS", obj.getName() + " performs MSU " + skill + "/" + level + " by your request.");
					}
				}
			}
			catch(Exception e)
			{
				activeChar.sendChatMessage(0, 0, "SYS", "Usage: //effect skill [level | level hittime]");
			}
		}
		if(command.contains("menu"))
		{
			showMainPage(activeChar, command);
		}
		return true;
	}

	private boolean performAbnormal(int action, L2Object target)
	{
		if(target instanceof L2Character)
		{
			L2Character character = (L2Character) target;

			if((character.getAbnormalEffect() & action) == action)
			{
				character.stopAbnormalEffect(action);
			}
			else
			{
				character.startAbnormalEffect(action);
			}
			return true;
		}
		else
		{
			return false;
		}
	}

	private boolean performSocial(int action, L2Object target, L2PcInstance activeChar)
	{
		try
		{
			if(target instanceof L2Character)
			{
				if(target instanceof L2Summon || target instanceof L2ChestInstance)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
					return false;
				}
				if(target instanceof L2Npc && (action < 1 || action > 3))
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
					return false;
				}
				if(target instanceof L2PcInstance && (action < 2 || action > 16))
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
					return false;
				}

				L2Character character = (L2Character) target;
				character.broadcastPacket(new SocialAction(target.getObjectId(), action));
			}
			else
			{
				return false;
			}
		}
		catch(Exception e)
		{
			
		}
		return true;
	}

	private void adminAtmosphere(String type, String state, L2PcInstance activeChar)
	{
		L2GameServerPacket packet = null;

		if(type.equals("signsky"))
		{
			if(state.equals("dawn"))
			{
				packet = new SignsSky(2);
			}
			else if(state.equals("dusk"))
			{
				packet = new SignsSky(1);
			}
		}
		else if(type.equals("sky"))
		{
			if(state.equals("night"))
			{
				packet = new SunSet();
			}
			else if(state.equals("day"))
			{
				packet = new SunRise();
			}
			else if(state.equals("red"))
			{
				packet = new ExRedSky(10);
			}
		}
		else
		{
			activeChar.sendChatMessage(0, 0, "SYS", "Usage: //atmosphere <signsky dawn|dusk>|<sky day|night|red>");
		}

		if(packet != null)
		{
			for(L2PcInstance player : L2World.getInstance().getAllPlayers())
			{
				player.sendPacket(packet);
			}
		}
	}

	private void playAdminSound(L2PcInstance activeChar, String sound)
	{
		PlaySound _snd = new PlaySound(1, sound, 0, 0, 0, 0, 0);
		activeChar.sendPacket(_snd);
		activeChar.broadcastPacket(_snd);
		activeChar.sendPacket(new ExShowScreenMessage("Playing " + sound + ".", 4000, 0x02, true)); 
	}

	private void showMainPage(L2PcInstance activeChar, String command)
	{
		String filename = "effects_menu";

		if(command.contains("abnormal"))
		{
			filename = "effects/abnormal";
		}
		else if(command.contains("social"))
		{
			filename = "effects/social";
		}

		AdminHelpPage.showHelpPage(activeChar, filename + ".htm");
	}
	
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}