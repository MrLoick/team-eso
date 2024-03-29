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

package com.epicsagaonline.bukkit.ChallengeMaps.commands;

import org.bukkit.command.CommandSender;

public class CM implements CommandHandler
{
	public boolean onCommand(String command, CommandSender sender, String[] args)
	{
		if (args.length > 0)
		{
			String subCommand = args[0].toLowerCase().trim();
			if (subCommand.equals("leave"))
			{
				new CMLeave(args, sender);
			}
			else if (subCommand.equals("reset"))
			{
				new CMReset(args, sender);
			}
			else if (subCommand.startsWith("obj"))
			{
				new CMObjectives(args, sender);
			}
			else if (subCommand.startsWith("score"))
			{
				new CMScore(args, sender);
			}
			else
			{
				new CMHelp(sender);
			}
		}
		else
		{
			if (command.equals("leave"))
			{
				new CMLeave(args, sender);
			}
			else
			{
				new CMHelp(sender);
			}
		}
		return true;
	}
}
