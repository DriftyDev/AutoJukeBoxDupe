package me.drifty.ajdn;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.PacketTypeEnum;
import org.bukkit.plugin.java.JavaPlugin;

public final class AutoJukeboxDupeNerf extends JavaPlugin {
	
	@Override
	public void onEnable() {
		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Client.BLOCK_DIG) {
			@Override
			public void onPacketReceiving(PacketEvent event) {
				final PacketContainer packet = event.getPacket();
			
				if(type.equals(PacketTypeEnum.USE_ITEM)) {
					final BlockPosition blockPos = wrappedPacket.getLocation();
					
					event.setCancelled(true);
				}
			}
		});
	}
	
}
