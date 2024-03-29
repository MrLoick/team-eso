package com.epicsagaonline.bukkit.EpicZones;

import java.io.File;
import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import com.bukkit.dthielke.herochat.HeroChatPlugin;
import com.epicsagaonline.bukkit.EpicZones.General;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

import org.bukkit.plugin.Plugin;

/**
 * EpicZones for Bukkit
 *
 * @author jblaske
 */
public class EpicZones extends JavaPlugin 
{

	private final EpicZonesPlayerListener playerListener = new EpicZonesPlayerListener(this);
	private final EpicZonesBlockListener blockListener = new EpicZonesBlockListener(this);
	private final EpicZonesEntityListener entityListener = new EpicZonesEntityListener(this);
	private final EpicZonesVehicleListener vehicleListener = new EpicZonesVehicleListener(this);
	private final EpicZonesRegen regen = new EpicZonesRegen(this);
	private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
	private static final String CONFIG_FILE = "config.yml";

	public static HeroChatPlugin heroChat = null;
	public static PermissionHandler permissions;

	public void onEnable() {

		File file = new File(this.getDataFolder() + File.separator + CONFIG_FILE);
		General.config = new EpicZonesConfig(file);

		PluginDescriptionFile pdfFile = this.getDescription();

		try 
		{

			PluginManager pm = getServer().getPluginManager();

			pm.registerEvent(Event.Type.PLAYER_MOVE, this.playerListener, Event.Priority.Normal, this);
			pm.registerEvent(Event.Type.PLAYER_TELEPORT, this.playerListener, Event.Priority.Normal, this);
			pm.registerEvent(Event.Type.PLAYER_LOGIN, this.playerListener, Event.Priority.Monitor, this);
			pm.registerEvent(Event.Type.PLAYER_QUIT, this.playerListener, Event.Priority.Monitor, this);
			pm.registerEvent(Event.Type.PLAYER_COMMAND, this.playerListener, Event.Priority.Normal, this);
			pm.registerEvent(Event.Type.PLAYER_ITEM , this.playerListener, Event.Priority.Normal, this);

			pm.registerEvent(Event.Type.BLOCK_DAMAGED, this.blockListener, Event.Priority.Normal, this);
			pm.registerEvent(Event.Type.BLOCK_PLACED, this.blockListener, Event.Priority.Normal, this);
			pm.registerEvent(Event.Type.BLOCK_IGNITE, this.blockListener, Event.Priority.Normal, this);
			pm.registerEvent(Event.Type.BLOCK_BURN, this.blockListener, Event.Priority.Normal, this);
			
			pm.registerEvent(Event.Type.ENTITY_DAMAGED, this.entityListener, Event.Priority.Normal, this);
			pm.registerEvent(Event.Type.CREATURE_SPAWN, this.entityListener, Event.Priority.Normal, this);
			pm.registerEvent(Event.Type.ENTITY_EXPLODE, this.entityListener, Event.Priority.Normal, this);
				
			pm.registerEvent(Event.Type.VEHICLE_MOVE, this.vehicleListener, Event.Priority.Normal, this);

			getServer().getScheduler().scheduleAsyncRepeatingTask(this, regen, 10, 10);

			setupEpicZones();
			setupHeroChat();
			setupPermissions();
			
			System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled." );

		} 
		catch (Throwable e) 
		{
			System.out.println( "["+pdfFile.getName()+"]" + " error starting: "+
					e.getMessage() +" Disabling plugin" );
			this.getServer().getPluginManager().disablePlugin(this);
		}
	}

	public void onDisable() 
	{
		PluginDescriptionFile pdfFile = this.getDescription();	
		System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is disabled." );
	}

	public boolean isDebugging(final Player player) 
	{
		if (debugees.containsKey(player)) {
			return debugees.get(player);
		} else {
			return false;
		}
	}

	public void setDebugging(final Player player, final boolean value) 
	{
		debugees.put(player, value);
	}

	public void setupPermissions()
	{
		Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");
		if(EpicZones.permissions == null)
		{
			if(test != null) 
			{
				if(!test.isEnabled())
				{
					getServer().getPluginManager().enablePlugin(test);
				}
				EpicZones.permissions = ((Permissions)test).getHandler();
			}
		}
	}

	public void setupHeroChat()
	{
		Plugin test = this.getServer().getPluginManager().getPlugin("HeroChat");
		if (test != null)
		{
			heroChat = (com.bukkit.dthielke.herochat.HeroChatPlugin)test;
		}
	}

	public void setupEpicZones()
	{
		General.plugin = this;
		General.myZones.clear();
		General.myZoneTags.clear();
		General.myPlayers.clear();
		General.config.load();
		General.config.save();
		General.loadZones();
		for(Player p:getServer().getOnlinePlayers())
		{
			General.addPlayer(p.getEntityId(), p.getName());
		}
	}
}

