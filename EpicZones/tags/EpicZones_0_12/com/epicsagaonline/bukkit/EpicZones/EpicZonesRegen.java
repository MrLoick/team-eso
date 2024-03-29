package com.epicsagaonline.bukkit.EpicZones;

import java.util.ArrayList;
import org.bukkit.entity.Player;

public class EpicZonesRegen implements Runnable {

	private final EpicZones plugin;
	private final int MAX_HEALTH = 20;

	EpicZonesRegen(final EpicZones instance)
	{
		this.plugin = instance;
	}

	public void run() {

		ArrayList<String> regenZoneTags = new ArrayList<String>();

		for(Player player: plugin.getServer().getOnlinePlayers())
		{
			if(player.getHealth() <= MAX_HEALTH)
			{
				EpicZonePlayer ezp = General.getPlayer(player.getEntityId());
				if(ezp != null)
				{
					EpicZone zone = ezp.getCurrentZone();
					if(zone != null)
					{
						if(zone.timeToRegen())
						{
							if(ezp.getEnteredZone().before(zone.getAdjustedRegenDelay()))
							{
								player.setHealth(((player.getHealth() + zone.getRegenAmount())));
								if(player.getHealth() > MAX_HEALTH)
								{
									player.setHealth(MAX_HEALTH);
								}
							}
							if(!regenZoneTags.contains(zone.getTag()))
							{
								regenZoneTags.add(zone.getTag());
							}
						}
					}
				}
			}
		}

		for(int i = 0; i < regenZoneTags.size(); i++)
		{
			General.myZones.get(regenZoneTags.get(i)).Regen();
		}
	}
}
