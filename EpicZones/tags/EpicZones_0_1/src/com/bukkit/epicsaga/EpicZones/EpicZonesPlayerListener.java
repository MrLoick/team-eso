package com.bukkit.epicsaga.EpicZones;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerItemEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;

//import sun.security.mscapi.KeyStore.MY;

/**
 * Handle events for all Player related events
 * @author jblaske
 */
public class EpicZonesPlayerListener extends PlayerListener 
{
    private final EpicZones plugin;
    private final String NO_PERM_ENTER = "You do not have permission to enter ";
    private final String NO_PERM_BORDER = "You have reached the border of the map.";
    private final String NO_PERM_BUCKET = "You do not have permissions to do that in this zone.";
    private final int EMPTY_BUCKET = 325;
    private Map<Integer,Integer> bucketTypes = new HashMap<Integer,Integer>();
    
    public EpicZonesPlayerListener(EpicZones instance) 
    {
        plugin = instance;
        bucketTypes.put(326, 326);
        bucketTypes.put(327, 327);
    }

    public @Override void onPlayerMove(PlayerMoveEvent event) 
    {
    	
    	Player player = event.getPlayer();
    	EpicZonePlayer ezp = General.getPlayer(player.getName());
    	int playerHeight = event.getTo().getBlockY();
    	Point playerPoint = new Point(event.getTo().getBlockX(), event.getTo().getBlockZ());
    	//System.out.println("playerPoint: " + playerPoint.toString());
    	
    	if(playerWithinBorder(playerPoint))    	
    	{
    		
    		//player.sendMessage(EpicZones.permissions.getGroup(player.getName()));
    		
	    	if(ezp.getCurrentLocation() == null){ezp.setCurrentLocation(event.getTo());}
	    	
	    	if(ezp.getCurrentZone() != null && !ezp.getCurrentZone().pointWithin(playerPoint))
	    	{
	    		player.sendMessage(ezp.getCurrentZone().getExitText());
	    		ezp.setCurrentZone(null);
	    	}
	    	
	    	for(EpicZone z: General.myZones)
	    	{
	    		if (ezp.getCurrentZone() == null || z != ezp.getCurrentZone())
	    		{
	    			 if (ezp.getCurrentZone() == null || ezp.getCurrentZone().getParent() == null || z != ezp.getCurrentZone().getParent())
	    			 {
	    				 if(playerHeight >= z.getFloor() && playerHeight <= z.getCeiling())
	    				 {
			    			if(z.pointWithin(playerPoint))
			    			{
			    				if(General.hasPermissions(ezp, z, "entry"))
			    				{
									ezp.setCurrentZone(z);
									player.sendMessage(z.getEnterText());
			    				}
			    				else
			    				{
			    					if (ezp.getLastWarned().before(new Date())){
			    						player.sendMessage(NO_PERM_ENTER + z.getName());
			    						ezp.Warn();
			    						}
			    					player.teleportTo(ezp.getCurrentLocation());
			    					event.setTo(ezp.getCurrentLocation());
			    					event.setCancelled(true);
			    				}
			    			}
	    				 }
	    			 }
	    		}
	    	}
	    	ezp.setCurrentLocation(event.getTo());
    	}
    	else
    	{
    		if (ezp.getLastWarned().before(new Date())){
				player.sendMessage(NO_PERM_BORDER);
				ezp.Warn();
				}
			player.teleportTo(ezp.getCurrentLocation());
			event.setTo(ezp.getCurrentLocation());
			event.setCancelled(true);
    	}
    }
        
    public @Override void onPlayerLogin(PlayerLoginEvent event)
    {
    	General.addPlayer(event.getPlayer().getEntityId(), event.getPlayer().getName());
    }
    
    public @Override void onPlayerQuit(PlayerEvent event)
    {
    	General.removePlayer(event.getPlayer().getEntityId());
    }

    public @Override void onPlayerCommand(PlayerChatEvent event)
    {
    	if(!event.isCancelled())
	    {
	    	String[] split = event.getMessage().split("\\s");
	    	
	    	if (split[0].equalsIgnoreCase("/who"))
	    	{
	    		int pageNumber = 1;
	    		
	    		if (split.length > 1)
	    		{
	    			if (split[1].equalsIgnoreCase("all"))
	    			{
	    				if (split.length > 2)
	    				{
	    					try  
	    				    {  
	    				      pageNumber = Integer.parseInt(split[2]);  
	    				    }  
	    				    catch(NumberFormatException nfe)  
	    				    {  
	    				      pageNumber = 1;
	    				    }
	    				}
	    				buildWho(event.getPlayer(), pageNumber, true);
	    				return;
	    			}
	    			else
	    			{
	    				try  
					    {  
					      pageNumber = Integer.parseInt(split[1]);  
					    }  
					    catch(NumberFormatException nfe)  
					    {  
					      pageNumber = 1;
					    }
	    			}
	    		}
	    		buildWho(event.getPlayer(), pageNumber, false);
	    		event.setCancelled(true);
	    	}
	    	else if(split[0].equalsIgnoreCase("/reloadez"))
	    	{
	    		General.config.load();
				//General.config.save();
				General.loadZones(null);
				event.getPlayer().sendMessage("EpicZones Reloaded.");
	    		event.setCancelled(true);
	    	}
    	}
    }
    
    public @Override void onPlayerItem(PlayerItemEvent event)
    {
    	
    	if (bucketTypes.get(event.getPlayer().getItemInHand().getTypeId()) != null)
	    {
		   
		   Player player = event.getPlayer();
		   EpicZonePlayer ezp = General.getPlayer(player.getName());
	   	   Point blockPoint = new Point(event.getBlockClicked().getLocation().getBlockX(), event.getBlockClicked().getLocation().getBlockZ());
	   	   int blockHeight = event.getBlockClicked().getLocation().getBlockY();	  

		   	for(EpicZone z: General.myZones)
		   	{
		   		if(blockHeight >= z.getFloor() && blockHeight <= z.getCeiling())
				 {
	    			if(z.pointWithin(blockPoint))
	    			{
	    				if(!General.hasPermissions(ezp, z, "build"))
	    				{
	    					if (ezp.getLastWarned().before(new Date())){
	    						player.sendMessage(NO_PERM_BUCKET);
	    						ezp.Warn();
	    						}
	    					event.setCancelled(true);
	    				}
	    			}
				 }
		   	}  
	    }
    	else if(event.getPlayer().getItemInHand().getTypeId() == EMPTY_BUCKET)
    	{
			Player player = event.getPlayer();
			EpicZonePlayer ezp = General.getPlayer(player.getName());
			Point blockPoint = new Point(event.getBlockClicked().getLocation().getBlockX(), event.getBlockClicked().getLocation().getBlockZ());
			int blockHeight = event.getBlockClicked().getLocation().getBlockY();	  
		
		   	for(EpicZone z: General.myZones)
		   	{
		   		if(blockHeight >= z.getFloor() && blockHeight <= z.getCeiling())
				 {
					if(z.pointWithin(blockPoint))
					{
						if(!General.hasPermissions(ezp, z, "destroy"))
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
		   	}  	
    	}
    	
    }
    
    private void buildWho(Player player, int pageNumber, boolean allZones)
    {
    	
    	EpicZone currentZone = General.getPlayer(player.getName()).getCurrentZone();
    	if(currentZone == null){allZones = true;}
    	ArrayList<EpicZonePlayer> players = getPlayers(currentZone, allZones);
    	int playersPerPage = 8;
    	int playerCount = players.size();
   	      	
    	if (allZones)
    	{
    		player.sendMessage(playerCount + " Players Online [Page " + pageNumber + " of " + ((int)Math.ceil(playerCount / playersPerPage) + 1) + "]");
    		for(int i = (pageNumber - 1) * playersPerPage; i < (pageNumber * playersPerPage); i++)
        	{
    			if (players.size() > i)
				{
    				player.sendMessage(buildWhoPlayerName(players, i, allZones));
				}
        	}
    	}
    		else
    		{
    			player.sendMessage(playerCount + " Players Online in " + currentZone.getName() + " [Page " + pageNumber + " of " + ((int)Math.ceil(playerCount / playersPerPage) + 1) + "]");
    			for(int i = (pageNumber - 1) * playersPerPage; i < pageNumber * playersPerPage; i++)
            	{
    				if (players.size() > i)
    				{
    					player.sendMessage(buildWhoPlayerName(players, i, allZones));
    				}
            	}
    		}
    }
    
    private String buildWhoPlayerName(ArrayList<EpicZonePlayer> players, int index, boolean allZones )
    {
    	   	
    	if (allZones)
    	{
    		if(players.get(index).getCurrentZone() != null)
    		{
    			return players.get(index).getName() + " - " + players.get(index).getCurrentZone().getName();
    		}
    		else
    		{
    			return players.get(index).getName();
    		}
    	}
    	else
    	{
    		return players.get(index).getName();
    	}
    }
    
    private ArrayList<EpicZonePlayer> getPlayers(EpicZone currentZone, boolean allZones)
    {
    	if (allZones)
    	{
    		return General.myPlayers;
    	}
    	else
    	{
    		ArrayList<EpicZonePlayer> result = new ArrayList<EpicZonePlayer>();
    		for (EpicZonePlayer ezp: General.myPlayers)
    		{
    		 if (!result.contains(ezp) && ezp.getCurrentZone().equals(currentZone))
    			{
    			 	result.add(ezp);
    			}
    		}
    		return result;
    	}
    }
    
    private boolean playerWithinBorder(Point point)
    {
    	boolean result;
    	
    	result = (Math.sqrt(Math.pow(point.x, 2) + Math.pow(point.y, 2)) <= General.config.mapRadius);
    	
    	return result;
    }
}
    
    
   

