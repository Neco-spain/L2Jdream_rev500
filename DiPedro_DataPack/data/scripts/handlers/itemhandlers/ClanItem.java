package handlers.itemhandlers;

import com.dipedro.Config;
import com.dipedro.gameserver.handler.IItemHandler;
import com.dipedro.gameserver.model.actor.L2Playable;
import com.dipedro.gameserver.model.actor.instance.L2ItemInstance;
import com.dipedro.gameserver.model.actor.instance.L2PcInstance;
import com.dipedro.gameserver.network.SystemMessageId;
import com.dipedro.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Bian
 **/
public class ClanItem implements IItemHandler
{
       private static final int ITEM_IDS[] = { Config.CLAN_ITEM_ID };
       
       @Override
       public synchronized void useItem(L2Playable playable, L2ItemInstance item)
       {
               if(!(playable instanceof L2PcInstance))
                       return;
               
               if (item == null)
               {
                       return;
               }
                       
               L2PcInstance player = (L2PcInstance)playable;
               if (!player.isClanLeader())
               {
                       player.sendPacket(new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addItemName(item));
                       return;
               }
               else if (player.isInOlympiadMode())
               {
                       player.sendPacket(new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addItemName(item));
                       return;
               }
               else
               {
                       if(player.destroyItem("Consume", item.getObjectId(), 1, null, false))
                       {
                               if (Config.ALLOW_ITEM_SET_LEVEL)
                               {
                                       player.getClan().changeLevel(Config.CLAN_ITEM_LEVEL);
                                       player.sendMessage((new StringBuilder()).append("Congratulations you now have your clan level ").append(Config.CLAN_ITEM_LEVEL).append(".").toString());
                               }
                               
                               if (Config.CLAN_TITLE)
                               {
                                       player.setTitle(Config.ADD_CLAN_TITLE);
                               }
                               
                               player.ClanItem();
                       }
               }
       }
       
       @Override
       public int[] getItemIds()
       {
               return ITEM_IDS;
       }
}