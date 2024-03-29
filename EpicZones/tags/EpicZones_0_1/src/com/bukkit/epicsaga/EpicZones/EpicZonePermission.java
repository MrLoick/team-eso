package com.bukkit.epicsaga.EpicZones;

import java.util.HashMap;
import java.util.Map;

public class EpicZonePermission {

	private String permissionObject = "";
	private Map<String,String> permissionFlags = new HashMap<String,String>();

	public String getPermissionObject(){return permissionObject;}
	public Map<String,String> getPermissionFlags(){return permissionFlags;}

	public EpicZonePermission(String permissionData)
	{
		this.permissionObject = permissionData.substring(0,permissionData.indexOf("["));
		this.permissionFlags = buildFlags(permissionData.substring(permissionData.indexOf("[") + 1, permissionData.length() - 1));
		//System.out.println("permissionFlags: " + permissionFlags.toString());
	}

	private Map<String,String> buildFlags(String data)
	{
		Map<String,String> result = new HashMap<String,String>();
		String[] dataList = data.split("\\s");

		for(int i = 0;i < dataList.length; i++)
		{
			String[] split = dataList[i].split(":",2);
			result.put(split[0], split[1]);
		}
		return result;
	}

}
