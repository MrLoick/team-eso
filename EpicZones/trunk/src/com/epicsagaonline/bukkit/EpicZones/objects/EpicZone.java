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

package com.epicsagaonline.bukkit.EpicZones.objects;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Player;

import com.epicsagaonline.bukkit.EpicZones.EpicZones;
import com.epicsagaonline.bukkit.EpicZones.objects.EpicZonePermission.PermNode;
import com.epicsagaonline.bukkit.EpicZones.objects.EpicZonePermission.PermType;

public class EpicZone {

	public enum ZoneType{ POLY, CIRCLE }

	private String tag = "";
	private String name = "";
	private ZoneType type = ZoneType.POLY;
	private int floor = 0;
	private int ceiling = 128;
	private String world = "";
	private Polygon polygon = new Polygon();
	private Point center = new Point();
	private Rectangle boundingBox = new Rectangle(); 
	private String enterText = "";
	private String exitText = "";
	private EpicZone parent = null;
	private Map<String, EpicZone> children = new HashMap<String, EpicZone>();
	private Set<String> childrenTags = new HashSet<String>();
	private boolean hasParentFlag = false;
	private boolean pvp = false;
	private boolean hasRegen = false;
	private Date lastRegen = new Date();
	private EpicZoneRegen regen = new EpicZoneRegen();
	private int radius = 0;
	private ArrayList<String> mobs = new ArrayList<String>();
	private boolean fire = false;
	private boolean explode = false;
	private ArrayList<String> owners = new ArrayList<String>();
	private boolean sanctuary = false;
	private boolean fireBurnsMobs = false;
	private ArrayList<EpicZonePermission> permissions = new ArrayList<EpicZonePermission>();

	public EpicZone(){}

	public EpicZone(EpicZone prime)
	{
		this.tag = prime.tag;
		this.name = prime.name;
		this.floor = prime.floor;
		this.ceiling = prime.ceiling;
		this.world = prime.world;
		this.polygon = prime.polygon;
		this.center = prime.center;
		this.boundingBox = prime.boundingBox;
		this.enterText = prime.enterText;
		this.exitText = prime.exitText;
		this.parent = prime.parent;
		this.children = prime.children;
		this.childrenTags = prime.childrenTags;
		this.hasParentFlag = prime.hasParentFlag;
		this.pvp = prime.pvp;
		this.hasRegen = prime.hasRegen;
		this.lastRegen = prime.lastRegen;
		this.regen = prime.regen;
		this.radius = prime.radius;
		this.mobs = prime.mobs;
		this.fire = prime.fire;
		this.explode = prime.explode;
		this.owners = prime.owners;
		this.sanctuary = prime.sanctuary;
		this.permissions = prime.permissions;
	}

	public EpicZone(String zoneData)
	{

		String[] split = zoneData.split("\\|");

		if(split.length == 10)
		{
			this.tag = split[0].replaceAll("[^a-zA-Z0-9]", "");
			this.world = split[1];
			this.name = split[2];
			this.enterText = split[4];
			this.exitText = split[5];
			this.floor = Integer.valueOf(split[6]);
			this.ceiling = Integer.valueOf(split[7]);
			this.parent = null;
			this.children = null;
			this.regen = new EpicZoneRegen();

			buildFlags(split[3]);
			buildChildren(split[8]);
			buildPolygon(split[9]);

			rebuildBoundingBox();

			//Log.Write("Created Zone [" + this.name + "]");
		}

	}

	public String getTag(){return tag;}
	public String getName(){return name;}
	public int getFloor(){return floor;}
	public int getCeiling(){return ceiling;}
	public Polygon getPolygon(){return polygon;}
	public String getEnterText(){return enterText;}
	public String getExitText(){return exitText;}
	public String getWorld(){return world;}
	public EpicZone getParent(){return parent;}
	public Map<String, EpicZone> getChildren(){return children;}
	public Set<String> getChildrenTags(){return childrenTags;}
	public boolean hasChildren(){return childrenTags.size() > 0;}
	public boolean hasParent(){return hasParentFlag;}
	public boolean hasRegen(){return hasRegen;}
	public EpicZoneRegen getRegen(){return regen;}
	public int getRadius(){return radius;}
	public Point getCenter(){return center;}
	public ArrayList<String> getMobs(){return mobs;}
	public boolean getFire(){return fire;}
	public boolean getExplode(){return explode;}
	public boolean getSanctuary(){return sanctuary;}
	public ArrayList<String> getOwners(){return owners;} 
	public boolean getFireBurnsMobs(){return fireBurnsMobs;}
	public ZoneType getType(){return type;}
	public ArrayList<EpicZonePermission> getPermissions() {return permissions;}

	public String getPoints()
	{
		String result = "";
		Polygon poly = this.getPolygon();

		if(poly != null)
		{
			for(int i = 0; i < poly.npoints; i++)
			{
				result = result + poly.xpoints[i] + ":" + poly.ypoints[i] + " ";
			}
		}
		else
		{

			{
				result = this.center.x + ":" + this.center.y;	
			}

		}

		return result;
	}

	public void addChild(EpicZone childZone)
	{
		if(this.children == null){this.children = new HashMap<String, EpicZone>();}
		this.children.put(childZone.getTag(), childZone);
	}

	public void setPolygon(String value)
	{
		buildPolygon(value);
	}

	public void setPermissions(ArrayList<EpicZonePermission> value)
	{
		this.permissions = value;
	}

	public void addChildTag(String tag)
	{
		this.childrenTags.add(tag);
	}

	public void addPermission(String member, String node, String permission)
	{
		if(member != null && node != null && permission != null)
		{
			EpicZonePermission newPerm = new EpicZonePermission();
			newPerm.setMember(member);
			newPerm.setNode(PermNode.valueOf(node.toUpperCase()));
			newPerm.setPermission(PermType.valueOf(permission.toUpperCase()));
			this.permissions.add(newPerm);
		}
	}

	public void setType(String value)
	{
		this.type = ZoneType.valueOf(value);
	}

	public void setFire(Boolean value)
	{
		this.fire = value;
	}

	public void setExplode(Boolean value)
	{
		this.explode = value;
	}

	public void setFireBurnsMobs(Boolean value)
	{
		this.fireBurnsMobs = value;
	}

	public void setSanctuary(Boolean value)
	{
		this.sanctuary = value;
	}

	public void setAllowFire(Boolean value)
	{
		this.fire = value;
	}

	public void setAllowExplode(Boolean value)
	{
		this.explode = value;
	}

	public void removeChild(String tag)
	{
		if(this.childrenTags != null)
		{
			this.childrenTags.remove(tag);
		}

		if(this.children != null)
		{
			this.children.remove(tag);
		}
	}

	public void setWorld(String value)
	{
		this.world = value;
	}

	public void setTag(String value)
	{
		this.tag=value;
	}

	public void setName(String value)
	{
		this.name=value;
	}

	public void setFloor(int value)
	{
		this.floor=value;
	}

	public void setCeiling(int value)
	{
		this.ceiling=value;
	}

	public void setEnterText(String value)
	{
		this.enterText=value;
	}

	public void setExitText(String value)
	{
		this.exitText=value;
	}

	public void setRadius(int value)
	{
		this.radius = value;
		if(this.polygon.npoints == 1)
		{
			this.center.x = this.polygon.xpoints[0];
			this.center.y = this.polygon.ypoints[0];
		}
	}

	public boolean pointWithin(Point point)
	{
		boolean result = false;
		if(this.boundingBox != null)
		{
			if(this.boundingBox.contains(point))
			{
				if(this.polygon != null)
				{
					if(this.polygon.contains(point))
					{
						result = true;
					}
				}
			}
		}
		else if(this.center != null)
		{
			if(this.pointWithinCircle(point))
			{
				result = true;
			}
		}
		return result;
	}

	public void setCenter(Point value)
	{
		this.center = value;
	}

	public void setParent(EpicZone parent)
	{
		this.parent = parent;
		this.hasParentFlag = true;
	}

	private void buildFlags(String data)
	{

		mobs.add("all");

		if(data.length() > 0)
		{
			String[] dataList = data.split("\\s");

			for(int i = 0;i < dataList.length; i++)
			{
				String[] split = dataList[i].split(":");
				String flag = split[0].toLowerCase();

				if(flag.equals("pvp"))
				{
					this.pvp = split[1].equalsIgnoreCase("true");
				}
				else if(flag.equals("regen"))
				{
					this.regen.setAmount(Integer.parseInt(split[1].trim()));
					if (split.length > 2)
					{
						this.regen.setInterval(Integer.parseInt(split[2].trim()));
						if(split.length > 3)
						{
							this.regen.setDelay(Integer.parseInt(split[3].trim()));
						}
					}
				}
				else if(flag.equals("mobs"))
				{
					BuildMobsFlag(split);
				}
				else if(flag.equals("fire"))
				{
					this.fire = split[1].equalsIgnoreCase("true");
				}
				else if(flag.equals("explode"))
				{
					this.explode = split[1].equalsIgnoreCase("true");
				}
				else if(flag.equals("owners"))
				{
					BuildOwnersFlag(split);
				}
				else if(flag.equals("sanctuary"))
				{
					this.sanctuary = split[1].equalsIgnoreCase("true");
				}
			}
		}
	}

	public void setMobs(String data)
	{
		if (data.length() > 0)
		{
			String[] split;
			split = (data + ":").split(":");
			if(split.length == 0)
			{
				split = (data + ",").split(",");
			}
			if(split.length == 0)
			{
				split = (data + " ").split(" ");
			}
			if(split.length > 0)
			{
				BuildMobsFlag(split);
			}
		}
	}

	private void BuildMobsFlag(String[] split)
	{
		if(split.length > 0)
		{
			mobs = new ArrayList<String>();
			boolean validType;
			for(String mobType: split)
			{
				mobType = mobType.trim();
				validType = false;
				try
				{
					if(mobType.equalsIgnoreCase("none") ||
							mobType.equalsIgnoreCase("monsters") || 
							mobType.equalsIgnoreCase("monster") ||
							mobType.equalsIgnoreCase("animals") ||
							mobType.equalsIgnoreCase("animal") ||
							mobType.equalsIgnoreCase("all"))
					{
						validType = true;
					}
					else
					{
						CreatureType.valueOf(mobType);
						validType = true;				
					}
				}
				catch(Exception e)
				{
					validType = false;	
				}

				if (validType)
				{
					if(mobType.equalsIgnoreCase("none"))
					{
						mobs.add("none");
						return;
					}
					if(mobType.equalsIgnoreCase("animals") || mobType.equalsIgnoreCase("animal"))
					{
						if(!mobs.contains(CreatureType.SQUID.toString()))
						{
							mobs.add(CreatureType.SQUID.toString());
						}

						if(!mobs.contains(CreatureType.CHICKEN.toString()))
						{
							mobs.add(CreatureType.CHICKEN.toString());
						}

						if(!mobs.contains(CreatureType.COW.toString()))
						{
							mobs.add(CreatureType.COW.toString());
						}

						if(!mobs.contains(CreatureType.SHEEP.toString()))
						{
							mobs.add(CreatureType.SHEEP.toString());
						}

						if(!mobs.contains(CreatureType.PIG.toString()))
						{
							mobs.add(CreatureType.PIG.toString());
						}
						if(!mobs.contains(CreatureType.WOLF.toString()))
						{
							mobs.add(CreatureType.WOLF.toString());
						}
					}	
					else if(mobType.equalsIgnoreCase("monsters") || mobType.equalsIgnoreCase("monster"))
					{
						if(!mobs.contains(CreatureType.CREEPER.toString()))
						{
							mobs.add(CreatureType.CREEPER.toString());
						}
						if(!mobs.contains(CreatureType.ZOMBIE.toString()))
						{
							mobs.add(CreatureType.ZOMBIE.toString());
						}
						if(!mobs.contains(CreatureType.GHAST.toString()))
						{
							mobs.add(CreatureType.GHAST.toString());
						}
						if(!mobs.contains(CreatureType.GIANT.toString()))
						{
							mobs.add(CreatureType.GIANT.toString());
						}
						if(!mobs.contains(CreatureType.SKELETON.toString()))
						{
							mobs.add(CreatureType.SKELETON.toString());
						}
						if(!mobs.contains(CreatureType.SLIME.toString()))
						{
							mobs.add(CreatureType.SLIME.toString());
						}
						if(!mobs.contains(CreatureType.SPIDER.toString()))
						{
							mobs.add(CreatureType.SPIDER.toString());
						}
					}
					else if(mobType.equalsIgnoreCase("all"))
					{
						mobs.add("all");
					}				
					else if(!mobs.contains(CreatureType.valueOf(mobType).toString()))
					{
						mobs.add(CreatureType.valueOf(mobType).toString());
					}
				}
			}
		}
		else
		{
			mobs.add("all");	
		}
	}

	public void BuildOwnersFlag(String[] split)
	{

		boolean skip = true;
		this.owners = new ArrayList<String>();

		if(split.length > 0)
		{
			this.owners = new ArrayList<String>();
			for(String owner: split)
			{
				if(!skip)
				{
					owners.add(owner.trim());
				}
				else
				{
					skip = false;
				}
			}
		}
	}

	public void setPVP(boolean value)
	{
		this.pvp = value;
	}

	public void setRegen(String value)
	{
		this.regen = new EpicZoneRegen(value);
		this.hasRegen  = (this.regen.getAmount() != 0 || this.regen.getBedBonus() > 0);
	}

	private void buildChildren(String data)
	{

		if(data.length()>0)
		{
			String[] dataList = data.split("\\s");

			for(int i = 0;i < dataList.length; i++)
			{
				this.childrenTags.add(dataList[i]);
			}
		}

	}

	private void buildPolygon(String data)
	{
		String[] dataList = data.split("\\s");

		if (dataList.length > 2)
		{
			this.polygon = new Polygon();
			this.center = null;
			for(int i = 0;i < dataList.length; i++)
			{
				String[] split = dataList[i].split(":");
				this.polygon.addPoint(Integer.valueOf(split[0]), Integer.valueOf(split[1]));
			}
		}
		else if(dataList.length >= 1)
		{
			String[] split = dataList[0].split(":");
			this.polygon = null;
			this.center = new Point(Integer.valueOf(split[0]), Integer.valueOf(split[1]));
			if(dataList.length > 1)
			{
				this.radius = Integer.valueOf(dataList[1]);
			}
		}
	}

	public void addPoint(Point point)
	{
		if(this.polygon == null){this.polygon = new Polygon();}
		this.polygon.addPoint(point.x, point.y);
	}

	public void clearPolyPoints()
	{
		this.polygon = new Polygon();
	}

	public void rebuildBoundingBox()
	{
		if(this.polygon != null)
		{
			this.boundingBox = this.polygon.getBounds();
		}
		else	
		{
			this.boundingBox = null;
		}
	}

	public boolean getPVP() 
	{
		return this.pvp;
	}

	public void Regen()
	{
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MILLISECOND, this.regen.getInterval());
		this.lastRegen = cal.getTime();
	}

	public Date getAdjustedRegenDelay()
	{
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MILLISECOND, -this.regen.getDelay());
		return cal.getTime();
	}
	
	public Date getAdjustedRestDelay()
	{
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MILLISECOND, -this.regen.getRestDelay());
		return cal.getTime();
	}

	public boolean timeToRegen()
	{
		if (this.hasRegen)
		{
			if(this.lastRegen.before(new Date()))
			{
				return true;
			}
		}
		return false;
	}

	public boolean pointWithinCircle(Point test)
	{
		double x = test.x - this.center.x;
		double y = test.y - this.center.y;
		double xsquared = x * x;
		double ysquared = y * y;
		double distanceFromCenter = Math.sqrt(xsquared + ysquared);
		return distanceFromCenter <= this.radius;

	}

	public boolean isOwner(String playerName)
	{
		return owners.contains(playerName);
	}

	public void addOwner(String playerName)
	{
		owners.add(playerName);
	}

	public void removeOwner(String playerName)
	{
		owners.remove(playerName);
	}

	public boolean hasPermission(Player player, String node, PermType permission)
	{
		
		boolean result = false;
		
		for( EpicZonePermission perm : permissions)
		{
			if(perm.getMember().equalsIgnoreCase(player.getName()))
			{
				if(perm.getNode().equals(PermNode.valueOf(node.toUpperCase())))
				{
					if(perm.getPermission() == permission)
					{
						result = true;
						break;
					}
				}
			}
			
			for( String groupName : EpicZones.permissions.getGroupNames(player))
			{
				if(perm.getMember().equalsIgnoreCase(groupName))
				{
					if(perm.getNode().equals(PermNode.valueOf(node.toUpperCase())))
					{
						if(perm.getPermission() == PermType.ALLOW)
						{
							result = true;
							break;
						}
					}
				}
			}
		}
		
		return result;
		
	}
}