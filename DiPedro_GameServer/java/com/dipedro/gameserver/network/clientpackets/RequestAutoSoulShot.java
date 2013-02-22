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
package com.dipedro.gameserver.network.clientpackets;

import com.dipedro.gameserver.model.actor.instance.L2ItemInstance;
import com.dipedro.gameserver.model.actor.instance.L2PcInstance;
import com.dipedro.gameserver.network.SystemMessageId;
import com.dipedro.gameserver.network.serverpackets.ExAutoSoulShot;
import com.dipedro.gameserver.network.serverpackets.SystemMessage;

public final class RequestAutoSoulShot extends L2GameClientPacket
{
	private static final String _C__CF_REQUESTAUTOSOULSHOT = "[C] CF RequestAutoSoulShot";

	private int _itemId;
	private int _type;

	@Override
	protected void readImpl()
	{
		_itemId = readD();
		_type = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if(activeChar == null)
		{
			return;
		}

		if(activeChar.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_NONE && activeChar.getActiveRequester() == null && !activeChar.isDead())
		{
			L2ItemInstance item = activeChar.getInventory().getItemByItemId(_itemId);

			if(item != null)
			{
				if(_type == 1)
				{
					if (!activeChar.getInventory().canManipulateWithItemId(item.getItemId()))
					{
						activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_USE_SOULSHOTS));
						return;
					}
					
					if(_itemId < 6535 || _itemId > 6540)
					{
						if(_itemId == 6645 || _itemId == 6646 || _itemId == 6647)
						{
							if (activeChar.getPet() != null)
							{
								// Cannot activate bss automation during Olympiad.
								if (_itemId == 6647 && activeChar.isInOlympiadMode())
								{
									activeChar.sendPacket(new SystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
									return;
								}
								
								if (_itemId == 6645)
								{
									if (activeChar.getPet().getSoulShotsPerHit() > item.getCount())
									{
										activeChar.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_SOULSHOTS_FOR_PET));
										return;
									}
								}
								else
								{
									if (activeChar.getPet().getSpiritShotsPerHit() > item.getCount())
									{
										activeChar.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_SPIRITHOTS_FOR_PET));
										return;
									}
								}
							}
							
							activeChar.addAutoSoulShot(_itemId);
							ExAutoSoulShot atk = new ExAutoSoulShot(_itemId, _type);
							activeChar.sendPacket(atk);
							activeChar.sendPacket(new SystemMessage(SystemMessageId.USE_OF_S1_WILL_BE_AUTO).addString(item.getItemName()));
							activeChar.rechargeAutoSoulShot(true, true, true);
						}
						else
						{
							if(activeChar.getActiveWeaponItem() != activeChar.getFistsWeaponItem() && item.getItem().getCrystalType() == activeChar.getActiveWeaponItem().getCrystalType())
							{
								if(_itemId >= 3947 && _itemId <= 3952 && activeChar.isInOlympiadMode())
								{
									activeChar.sendPacket(new SystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT).addString(item.getItemName()));
								}
								else
								{
									activeChar.addAutoSoulShot(_itemId);
									activeChar.sendPacket(new ExAutoSoulShot(_itemId, _type));
									activeChar.rechargeAutoSoulShot(true, true, false);
								}
							}
							else
							{
								if(_itemId >= 2509 && _itemId <= 2514 || _itemId >= 3947 && _itemId <= 3952 || _itemId == 5790)
								{
									activeChar.sendPacket(new SystemMessage(SystemMessageId.SPIRITSHOTS_GRADE_MISMATCH));
								}
								else
								{
									activeChar.sendPacket(new SystemMessage(SystemMessageId.SOULSHOTS_GRADE_MISMATCH));
								}
							}
						}
					}
				}
				else if(_type == 0)
				{
					activeChar.removeAutoSoulShot(_itemId);

					activeChar.sendPacket(new ExAutoSoulShot(_itemId, _type));
					//activeChar.sendPacket(new SystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED).addString(item.getItemName()));
				}
			}
		}
	}

	@Override
	public String getType()
	{
		return _C__CF_REQUESTAUTOSOULSHOT;
	}

}