package com.SwearWord.VirtualShop;
import java.io.File;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.SwearWord.VirtualShop.Utilities.DatabaseManager;
import com.SwearWord.VirtualShop.Utilities.ItemDb;
import com.SwearWord.VirtualShop.Utilities.Response;
import com.SwearWord.VirtualShop.Utilities.VSproperties;
import com.iConomy.iConomy;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;


public class VirtualShop extends JavaPlugin
{
	public File folder = new File("plugins/VirtualShop");
	
	@Override
	public void onDisable() 
	{
		DatabaseManager.Close();
	}
	@Override
	public void onEnable() 
	{
		try
		{
			Response.Initialize(this.getServer());
			ItemDb.load(folder, "items.csv");
			if(VSproperties.Initialize() && DatabaseManager.Initialize())
			{
				Response.LogMessage("VirtualShop successfully loaded.");
			}
			else
			{
				Response.LogMessage("VirtualShop failed to load.");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,String label, String[] args) 
	{
		if(args.length<1) return false;
		if(args[0].equalsIgnoreCase("price"))
		{
			Shop.DisplayPrices(sender, args);
			return true;
		}
		if(args[0].equalsIgnoreCase("sell"))
		{
			Shop.SellItem(sender, args);
			return true;
		}
		if(args[0].equalsIgnoreCase("buy"))
		{
			Shop.BuyItem(sender, args);
			return true;
		}
		if(args[0].equalsIgnoreCase("log"))
		{
			Shop.ListTransactions(sender, args);
			return true;
		}
		if(args[0].equalsIgnoreCase("list"))
		{
			Shop.ListItems(sender, args);
			return true;
		}
		if(args[0].equalsIgnoreCase("remove"))
		{
			Shop.RemoveItems(sender, args);
			return true;
		}
		if(args[0].equalsIgnoreCase("rates"))
		{
			Shop.PrintRates(sender, args);
			return true;
		}
		if(args[0].equalsIgnoreCase("exchange"))
		{
			Shop.Exchange(sender);
			return true;
		}
		if(args[0].equalsIgnoreCase("invest"))
		{
			Shop.Invest(sender,args);
			return true;
		}
		return false;
	}
}

