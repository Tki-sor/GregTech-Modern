package com.gregtechceu.gtceu.api.block;

import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Implemented by GTM blocks which expose their transfer and domain capabilities through
 * {@link RegisterCapabilitiesEvent}. The central registration in
 * {@code CommonProxy#registerCapabilities} dispatches to every registered block implementing it,
 * so individual blocks never subscribe to the event themselves.
 */
public interface IGTCapabilityBlock {

    void attachCapabilities(RegisterCapabilitiesEvent event);
}
