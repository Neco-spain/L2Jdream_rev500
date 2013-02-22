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
package com.dipedro.gameserver.script;

import com.dipedro.gameserver.GameTimeController;
import com.dipedro.gameserver.RecipeController;
import com.dipedro.gameserver.datatables.SkillTable;
import com.dipedro.gameserver.datatables.sql.CharNameTable;
import com.dipedro.gameserver.datatables.sql.ClanTable;
import com.dipedro.gameserver.datatables.sql.ItemTable;
import com.dipedro.gameserver.datatables.sql.SpawnTable;
import com.dipedro.gameserver.datatables.xml.CharTemplateTable;
import com.dipedro.gameserver.datatables.xml.LevelUpData;
import com.dipedro.gameserver.datatables.xml.MapRegionTable;
import com.dipedro.gameserver.datatables.xml.NpcTable;
import com.dipedro.gameserver.datatables.xml.SkillTreeTable;
import com.dipedro.gameserver.datatables.xml.TeleportLocationTable;
import com.dipedro.gameserver.idfactory.IdFactory;
import com.dipedro.gameserver.model.L2World;
import com.dipedro.gameserver.model.entity.Announcements;

public interface EngineInterface
{
	public CharNameTable charNametable = CharNameTable.getInstance();

	public IdFactory idFactory = IdFactory.getInstance();
	public ItemTable itemTable = ItemTable.getInstance();

	public SkillTable skillTable = SkillTable.getInstance();

	public RecipeController recipeController = RecipeController.getInstance();

	public SkillTreeTable skillTreeTable = SkillTreeTable.getInstance();
	public CharTemplateTable charTemplates = CharTemplateTable.getInstance();
	public ClanTable clanTable = ClanTable.getInstance();

	public NpcTable npcTable = NpcTable.getInstance();

	public TeleportLocationTable teleTable = TeleportLocationTable.getInstance();
	public LevelUpData levelUpData = LevelUpData.getInstance();
	public L2World world = L2World.getInstance();
	public SpawnTable spawnTable = SpawnTable.getInstance();
	public GameTimeController gameTimeController = GameTimeController.getInstance();
	public Announcements announcements = Announcements.getInstance();
	public MapRegionTable mapRegions = MapRegionTable.getInstance();

	public void addQuestDrop(int npcID, int itemID, int min, int max, int chance, String questID, String[] states);

	public void addEventDrop(int[] items, int[] count, double chance, DateRange range);

	public void onPlayerLogin(String[] message, DateRange range);
}