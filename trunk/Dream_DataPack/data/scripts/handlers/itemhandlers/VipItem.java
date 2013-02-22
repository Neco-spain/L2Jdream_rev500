package com.dipedro.gameserver.handler.itemhandlers;

import com.dipedro.Config;
import com.dipedro.gameserver.handler.IItemHandler;
import com.dipedro.gameserver.model.L2ItemInstance;
import com.dipedro.gameserver.model.actor.instance.L2PlayableInstance;
import com.dipedro.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Arty

 **/

public class VipItem  implements IItemHandler
{
        private static final int ITEM_IDS[] = { Config.VIP_ITEM };

        @Override
        public synchronized void useItem(L2PlayableInstance playable, 
                        L2ItemInstance item) {
                if(!(playable instanceof L2PcInstance))
                        return;

                L2PcInstance player = (L2PcInstance)playable;
                
                if (player.isInOlympiadMode())
                        player.sendMessage("Voce nao pode usar este item em olympiadas.");
                else
                {
                        if(playable.destroyItem("Consume", item.getObjectId(), 1, null, 
                                        false)) {
                                player.setVip(true);
                                player.setEndTime("vip", Config.VIP_DIAS);
                                player.sendMessage("Voce se tornou um Vip, voce tera " +
                                                "privilegios de acessar Npcs Vips, Seu Vip Durarar "
                                                +Config.VIP_DIAS+" Dias.");
                                player.broadcastUserInfo();
                        }
                }
        }

        public int[] getItemIds()
        {
                return ITEM_IDS;
        }
}