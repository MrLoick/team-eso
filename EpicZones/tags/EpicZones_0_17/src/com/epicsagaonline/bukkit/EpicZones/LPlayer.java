/*

This file is part of EpicZones

Copyright (C) 2011 by Team ESO

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.

 */

/**
 * @author jblaske@gmail.com
 * @license MIT License
 */

package com.epicsagaonline.bukkit.EpicZones;

import java.awt.Point;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerItemEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import com.epicsagaonline.bukkit.EpicZones.EpicZonePlayer.EpicZoneMode;


/**
 * Handle events for all Player related events
 * @author jblaske
 */
public class LPlayer extends PlayerListener
{

	private static final String NO_PERM_BUCKET = "You do not have permissions to do that in this zone.";
	private static final int EMPTY_BUCKET = 325;
	private Set<Integer> itemsOfDestruction = new HashSet<Integer>();

	public LPlayer(EpicZones instance)
	{
		itemsOfDestruction.add(259);
		itemsOfDestruction.add(326);
		itemsOfDestruction.add(327);
	}

	public @Override void onPlayerMove(PlayerMoveEvent event)
	{

		Player player = event.getPlayer();
		EpicZonePlayer ezp = General.getPlayer(player.getName());
		int playerHeight = event.getTo().getBlockY();
		Point playerPoint = new Point(event.getTo().getBlockX(), event.getTo().getBlockZ());

		if(General.ShouldCheckPlayer(ezp))
		{
			if(!ezp.isTeleporting())
			{
				if(ezp.getCurrentLocation() == null){ezp.setCurrentLocation(event.getFrom());}
				if(!PlayerWithinZoneLogic(player, ezp, playerHeight, playerPoint))
				{
					ezp.setIsTeleporting(true);
					player.teleportTo(ezp.getCurrentLocation());
					ezp.setIsTeleporting(false);
					event.setTo(ezp.getCurrentLocation());
					event.setCancelled(true);
				}
				else
				{
					ezp.setCurrentLocation(event.getFrom());
				}
			}
			ezp.Check();
		}
		ezp.setHasMoved(true);
	}

	public @Override void onPlayerTeleport(PlayerMoveEvent event)
	{

		Player player = event.getPlayer();
		EpicZonePlayer ezp = General.getPlayer(player.getName());
		int playerHeight = event.getTo().getBlockY();
		Point playerPoint = new Point(event.getTo().getBlockX(), event.getTo().getBlockZ());

		if(General.ShouldCheckPlayer(ezp))
		{
			if(!ezp.isTeleporting())
			{
				if(ezp.getEntityID() != player.getEntityId()){ezp.setEntityID(player.getEntityId());}
				if(ezp.getCurrentLocation() == null){ezp.setCurrentLocation(event.getFrom());}
				if(!PlayerWithinZoneLogic(player, ezp, playerHeight, playerPoint))
				{
					ezp.setIsTeleporting(true);
					player.teleportTo(ezp.getCurrentLocation());
					ezp.setIsTeleporting(false);
					event.setTo(ezp.getCurrentLocation());
					event.setCancelled(true);
				}
				else
				{
					ezp.setCurrentLocation(event.getTo());
				}
			}
			ezp.Check();
		}
	}

	public @Override void onPlayerLogin(PlayerLoginEvent event)
	{
		if(event.getResult() == Result.ALLOWED)
		{
			General.addPlayer(event.getPlayer().getEntityId(), event.getPlayer().getName());
		}
	}

	public @Override void onPlayerQuit(PlayerEvent event)
	{
		General.removePlayer(event.getPlayer().getEntityId());
	}

	public @Override void onPlayerItem(PlayerItemEvent event)
	{

		if (itemsOfDestruction.contains((event.getPlayer().getItemInHand().getTypeId())))
		{

			Player player = event.getPlayer();
			EpicZonePlayer ezp = General.getPlayer(player.getName());
			Point blockPoint = new Point(event.getBlockClicked().getLocation().getBlockX(), event.getBlockClicked().getLocation().getBlockZ());
			String worldName = player.getWorld().getName();
			int blockHeight = event.getBlockClicked().getLocation().getBlockY();
			boolean hasPerms = false;

			Zone currentZone = null;
			if(General.pointWithinBorder(blockPoint, player))
			{
				currentZone = General.getZoneForPoint(blockHeight, blockPoint, worldName);
				hasPerms = General.hasPermissions(player, currentZone, "build");

				if(!hasPerms)
				{
					if (ezp.getLastWarned().before(new Date()))
					{
						player.sendMessage(NO_PERM_BUCKET);
						ezp.Warn();
					}
					event.setCancelled(true);
				}
			}
		}
		else if(event.getPlayer().getItemInHand().getTypeId() == EMPTY_BUCKET)
		{
			Player player = event.getPlayer();
			EpicZonePlayer ezp = General.getPlayer(player.getName());
			Point blockPoint = new Point(event.getBlockClicked().getLocation().getBlockX(), event.getBlockClicked().getLocation().getBlockZ());
			String worldName = player.getWorld().getName();
			int blockHeight = event.getBlockClicked().getLocation().getBlockY();
			boolean hasPerms = false;

			Zone currentZone = null;
			if(General.pointWithinBorder(blockPoint, player))
			{
				currentZone = General.getZoneForPoint(blockHeight, blockPoint, worldName);
				hasPerms = General.hasPermissions(player, currentZone, "destroy");

				if(!hasPerms)
				{
					if (ezp.getLastWarned().before(new Date()))
					{
						player.sendMessage(NO_PERM_BUCKET);
						ezp.Warn();
					}
					event.setCancelled(true);
				}
			}
		}
		else if(event.getPlayer().getItemInHand().getTypeId() == General.config.zoneTool)
		{
			if(General.getPlayer(event.getPlayer().getEntityId()).getMode() == EpicZoneMode.ZoneDraw)
			{
				Point point = new Point(event.getBlockClicked().getLocation().getBlockX(), event.getBlockClicked().getLocation().getBlockZ());
				General.getPlayer(event.getPlayer().getEntityId()).getEditZone().addPoint(point);
				event.getPlayer().sendMessage("Point " + point.x + ":" + point.y + " added to zone.");
			}
		}
	}

	private boolean PlayerWithinZoneLogic(Player player, EpicZonePlayer ezp, int playerHeight, Point playerPoint)
	{

		Zone foundZone = null;
		String worldName = player.getWorld().getName();
		if(General.pointWithinBorder(playerPoint, player))
		{
			foundZone = FindZone(player, ezp, playerHeight, playerPoint, worldName);

			if(foundZone != null)
			{
				if (ezp.getCurrentZone() == null || foundZone != ezp.getCurrentZone())
				{
					if(General.hasPermissions(player, foundZone, "entry"))
					{
						if(ezp.getCurrentZone() != null){ezp.setPreviousZoneTag(ezp.getCurrentZone().getTag());}
						ezp.setCurrentZone(foundZone);
						HeroChatIntegration.joinChat(foundZone.getTag(), ezp, player);
						if(foundZone.getEnterText().length() > 0){player.sendMessage(foundZone.getEnterText());}
					}
					else
					{
						General.WarnPlayer(player, ezp, General.NO_PERM_ENTER + foundZone.getName());
						return false;
					}
				}

			}
			else
			{
				if(General.hasPermissions(player, null, "entry"))
				{
					if (ezp.getCurrentZone() != null)
					{
						if(ezp.getCurrentZone().getExitText().length() > 0){player.sendMessage(ezp.getCurrentZone().getExitText());}
						HeroChatIntegration.leaveChat(ezp.getCurrentZone().getTag(), player);
						ezp.setCurrentZone(null);
					}
				}
				else
				{
					General.WarnPlayer(player, ezp, General.NO_PERM_ENTER + player.getWorld().getName());
					return false;
				}
			}
		}
		else
		{
			General.WarnPlayer(player, ezp, General.NO_PERM_BORDER);
			return false;
		}

		return true;

	}

	private Zone FindZone(Player player, EpicZonePlayer ezp, int playerHeight, Point playerPoint, String worldName)
	{

		Zone result = null;

		if(ezp.getCurrentZone() != null)
		{

			String resultTag;
			result = ezp.getCurrentZone();
			resultTag = General.isPointInZone(result, playerHeight, playerPoint, worldName);
			if(resultTag.length() > 0)
			{
				if(!resultTag.equalsIgnoreCase(ezp.getCurrentZone().getTag()))
				{
					result = General.myZones.get(resultTag);
				}
			}
			else
			{
				result = null;
			}
		}
		else
		{
			result = General.getZoneForPoint(playerHeight, playerPoint, worldName);
		}

		return result;

	}
}