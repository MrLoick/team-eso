package com.bukkit.epicsaga.EpicZones;

import java.awt.Point;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRightClickEvent;

/**
 * EpicZones block listener
 * @author jblaske
 */
public class EpicZonesBlockListener extends BlockListener {
	private final EpicZones plugin;
	private static final String NO_PERM_DESTROY = "You do not have permissions to destroy in this zone.";
	private static final String NO_PERM_BUILD = "You do not have permissions to build in this zone.";

	public EpicZonesBlockListener(final EpicZones plugin) {
		this.plugin = plugin;
	}

	public @Override void onBlockDamage(BlockDamageEvent event)
	{

		Player player = event.getPlayer();
		EpicZonePlayer ezp = General.getPlayer(player.getName());
		Point blockPoint = new Point(event.getBlock().getLocation().getBlockX(), event.getBlock().getLocation().getBlockZ());
		String worldName = player.getWorld().getName();
		int blockHeight = event.getBlock().getLocation().getBlockY();
		boolean hasPerms = false;
		EpicZone currentZone = null;

		currentZone = General.getZoneForPoint(player, ezp, blockHeight, blockPoint, worldName);
		hasPerms = General.hasPermissions(player, currentZone, "destroy");

		if(!hasPerms)
		{
			if (ezp.getLastWarned().before(new Date())){
				player.sendMessage(NO_PERM_DESTROY);
				ezp.Warn();
			}
			event.setCancelled(true);
		}
	}

	public @Override void onBlockPlace(BlockPlaceEvent event)
	{

		Player player = event.getPlayer();
		EpicZonePlayer ezp = General.getPlayer(player.getName());
		Point blockPoint = new Point(event.getBlock().getLocation().getBlockX(), event.getBlock().getLocation().getBlockZ());
		String worldName = player.getWorld().getName();
		int blockHeight = event.getBlock().getLocation().getBlockY();
		boolean hasPerms = false;

		EpicZone currentZone = null;

		currentZone = General.getZoneForPoint(player, ezp, blockHeight, blockPoint, worldName);
		hasPerms = General.hasPermissions(player, currentZone, "build");

		if(!hasPerms)
		{
			if (ezp.getLastWarned().before(new Date())){
				player.sendMessage(NO_PERM_BUILD);
				ezp.Warn();
			}
			event.setCancelled(true);
		}
	}

}
