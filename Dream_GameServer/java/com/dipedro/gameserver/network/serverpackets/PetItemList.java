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
package com.dipedro.gameserver.network.serverpackets;

import com.dipedro.gameserver.model.actor.instance.L2ItemInstance;
import com.dipedro.gameserver.model.actor.instance.L2PetInstance;

public class PetItemList extends L2GameServerPacket
{
	private static final String _S__cb_PETITEMLIST = "[S] b2  PetItemList";
	private L2PetInstance _activeChar;

	public PetItemList(L2PetInstance character)
	{
		_activeChar = character;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xB2);

		L2ItemInstance[] items = _activeChar.getInventory().getItems();
		int count = items.length;
		writeH(count);

		for(L2ItemInstance temp : items)
		{
			writeH(temp.getItem().getType1());
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getCount());
			writeH(temp.getItem().getType2());
			writeH(0xff);
			if(temp.isEquipped())
			{
				writeH(0x01);
			}
			else
			{
				writeH(0x00);
			}
			writeD(temp.getItem().getBodyPart());
			writeH(temp.getEnchantLevel());
			writeH(0x00);
		}
	}

	@Override
	public String getType()
	{
		return _S__cb_PETITEMLIST;
	}

}