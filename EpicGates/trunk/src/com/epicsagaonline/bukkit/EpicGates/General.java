/*

        This file is part of EpicGates

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

package com.epicsagaonline.bukkit.EpicGates;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.epicsagaonline.bukkit.EpicGates.objects.EpicGate;
import com.epicsagaonline.bukkit.EpicGates.objects.EpicGatesPlayer;

public class General
{

	public static Map<String, EpicGate> myGates = new HashMap<String, EpicGate>();
	public static ArrayList<String> myGateTags = new ArrayList<String>();
	public static Map<String, EpicGatesPlayer> myPlayers = new HashMap<String, EpicGatesPlayer>();

	public static Config config;
	public static final String NO_PERM_ENTER = "You do not have permission to enter ";
	public static final String NO_PERM_BORDER = "You have reached the border of the map.";
	public static EpicGates plugin;

	private static final String GATE_FILE = "gates.txt";

	public static void loadGates()
	{
		String line;
		File file = new File(plugin.getDataFolder() + File.separator + GATE_FILE);

		try
		{
			Scanner scanner = new Scanner(file);
			myGates.clear();
			myGateTags.clear();
			try
			{
				while (scanner.hasNext())
				{
					EpicGate newGate;
					line = scanner.nextLine().trim();
					if (line.startsWith("#") || line.isEmpty())
					{
						continue;
					}
					newGate = new EpicGate(line);
					;
					General.myGates.put(newGate.getTag(), newGate);
					General.myGateTags.add(newGate.getTag());
				}

			}
			finally
			{
				scanner.close();
			}
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}

		LinkGates();

	}

	private static void LinkGates()
	{

		for (String gateTag : myGateTags)
		{
			EpicGate gate = myGates.get(gateTag);
			if (gate.getTargetTag().length() > 0)
			{
				if (myGates.get(gate.getTargetTag()) != null)
				{
					myGates.get(gateTag).setTarget(myGates.get(gate.getTargetTag()));
				}
				else
				{
					myGates.get(gateTag).setTarget(null);
				}
			}
		}
	}

	public static void saveGates()
	{
		File file = new File(plugin.getDataFolder() + File.separator + GATE_FILE);

		try
		{
			String data = BuildGateData();
			Writer output = new BufferedWriter(new FileWriter(file, false));
			try
			{
				output.write(data);
			}
			finally
			{
				output.close();
			}
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
	}

	private static String BuildGateData()
	{
		String result = "#Gate Tag|World|X|Y|Z|Target Tag\n";
		String line = "";

		for (String gateTag : myGateTags)
		{
			EpicGate gate = myGates.get(gateTag);
			line = gate.getTag() + ",";
			line = line + gate.getLocation().getWorld().getName() + ",";
			line = line + gate.getLocation().getBlockX() + ",";
			line = line + gate.getLocation().getBlockY() + ",";
			line = line + gate.getLocation().getBlockZ() + ",";
			line = line + gate.getTargetTag() + ",";
			line = line + gate.getDirection() + ",";
			line = line + BuildAllowed(gate) + ",";
			line = line + BuildNotAllowed(gate) + "\n";
			result = result + line;
		}
		return result;
	}

	private static String BuildAllowed(EpicGate gate)
	{
		String result = "";

		if (gate.getAllowed().size() > 0)
		{
			for (String value : gate.getAllowed())
			{
				result = result + value + " ";
			}
		}

		return result.trim();
	}

	private static String BuildNotAllowed(EpicGate gate)
	{
		String result = "";

		if (gate.getNotAllowed().size() > 0)
		{
			for (String value : gate.getNotAllowed())
			{
				result = result + value + " ";
			}
		}

		return result.trim();
	}

	public static void addPlayer(String name)
	{
		myPlayers.put(name, new EpicGatesPlayer());
	}

	public static void removePlayer(String name)
	{
		myPlayers.remove(name);
	}

}
