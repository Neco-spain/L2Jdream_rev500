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

import com.dipedro.gameserver.datatables.sql.ItemTable;
import com.dipedro.gameserver.model.multisell.MultiSellEntry;
import com.dipedro.gameserver.model.multisell.MultiSellIngredient;
import com.dipedro.gameserver.model.multisell.MultiSellListContainer;

public class MultiSellList extends L2GameServerPacket
{
	private static final String _S__D0_MULTISELLLIST = "[S] D0 MultiSellList";

	protected int _listId, _page, _finished;
	protected MultiSellListContainer _list;

	public MultiSellList(MultiSellListContainer list, int page, int finished)
	{
		_list = list;
		_listId = list.getListId();
		_page = page;
		_finished = finished;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xd0);
		writeD(_listId);
		writeD(_page);
		writeD(_finished);
		writeD(0x28);
		writeD(_list == null ? 0 : _list.getEntries().size());

		if(_list != null)
		{
			for(MultiSellEntry ent : _list.getEntries())
			{
				writeD(ent.getEntryId());
				writeD(0x00);
				writeD(0x00);
				writeC(1);
				writeH(ent.getProducts().size());
				writeH(ent.getIngredients().size());

				for(MultiSellIngredient i : ent.getProducts())
				{
					writeH(i.getItemId());
					writeD(ItemTable.getInstance().getTemplate(i.getItemId()).getBodyPart());
					writeH(ItemTable.getInstance().getTemplate(i.getItemId()).getType2());
					writeD(i.getItemCount());
					writeH(i.getEnchantmentLevel());
					writeD(0x00);
					writeD(0x00);
				}

				for(MultiSellIngredient i : ent.getIngredients())
				{
					int items = i.getItemId();
					int typeE = 65335;
					if(items != 65336 && items != 65436)
					{
						typeE = ItemTable.getInstance().getTemplate(i.getItemId()).getType2();
					}
					writeH(items);
					writeH(typeE);
					writeD(i.getItemCount());
					writeH(i.getEnchantmentLevel());
					writeD(0x00);
					writeD(0x00);
				}
			}
		}
	}

	@Override
	public String getType()
	{
		return _S__D0_MULTISELLLIST;
	}

}