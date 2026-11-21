package com.hp.rematchcage.compat.jade;

import com.hp.rematchcage.block.RematchCageBlock;
import com.hp.rematchcage.block.RematchCageBlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public final class RematchCageJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(RematchCageJadeProvider.INSTANCE, RematchCageBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(RematchCageJadeProvider.INSTANCE, RematchCageBlock.class);
    }
}
