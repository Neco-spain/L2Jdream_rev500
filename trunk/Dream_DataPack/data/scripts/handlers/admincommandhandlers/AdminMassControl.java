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

import com.dipedro.gameserver.datatables.xml.AdminCommandAccessRights;
import com.dipedro.gameserver.handler.IAdminCommandHandler;
import com.dipedro.gameserver.model.GMAudit;
import com.dipedro.gameserver.model.L2World;
import com.dipedro.gameserver.model.actor.instance.L2PcInstance;

public class AdminMassControl implements IAdminCommandHandler
{
	private static String[] ADMIN_COMMANDS =
	{
		"admin_masskill", 
		"admin_massress"
	};

	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		AdminCommandAccessRights.getInstance().hasAccess(command, activeChar.getAccessLevel());

		GMAudit.auditGMAction(activeChar.getName(), command, (activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target"), "");

		if(command.startsWith("admin_mass"))
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command);
				st.nextToken();

				if(st.nextToken().equalsIgnoreCase("kill"))
				{
					int counter = 0;

					for(L2PcInstance player : L2World.getInstance().getAllPlayers())
					{
						if(!player.isGM())
						{
							counter++;
							player.getStatus().setCurrentHp(0);
							player.doDie(player);
							activeChar.sendChatMessage(0, 0, "SYS", "You've Killed " + counter + " players.");
						}
					}
				}
				else if(st.nextToken().equalsIgnoreCase("ress"))
				{
					int counter = 0;

					for(L2PcInstance player : L2World.getInstance().getAllPlayers())
					{
						if(!player.isGM() && player.isDead())
						{
							counter++;
							player.doRevive();
							activeChar.sendChatMessage(0, 0, "SYS", "You've Ressurected " + counter + " players.");
						}
					}
				}
			}
			catch(Exception ex)
			{
				
			}
		}
		return true;
	}

	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}