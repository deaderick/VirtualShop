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
	private PermissionHandler ph;

	public void onDisable() 
	{
		DatabaseManager.Close();
	}

	public void onEnable() 
	{
		try
		{
			Response.Initialize(this.getServer());
			ItemDb.load(folder, "items.csv");
			setupPermmissions();
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
		if(args[0].equalsIgnoreCase("sell") && !HasPermission(sender, "VirtualShop.nosell"))
		{
			if(HasPermission(sender, "VirtualShop.nosell"))
			{
				Response.NoPermissions(sender);
				return true;
			}
			Shop.SellItem(sender, args);
			return true;
		}
		if(args[0].equalsIgnoreCase("buy"))
		{
			if(HasPermission(sender, "VirtualShop.nobuy"))
			{
				Response.NoPermissions(sender);
				return true;
			}
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
			if(HasPermission(sender, "VirtualShop.noexchange"))
			{
				Response.NoPermissions(sender);
				return true;
			}
			Shop.Exchange(sender);
			return true;
		}
		if(args[0].equalsIgnoreCase("invest"))
		{
			if(HasPermission(sender, "VirtualShop.noinvest"))
			{
				Response.NoPermissions(sender);
				return true;
			}
			Shop.Invest(sender,args);
			return true;
		}
		return false;
	}

	private void setupPermmissions()
	{
	      Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");

	      if (this.ph == null) 
	      {
	          if (permissionsPlugin != null) {
	              this.ph = ((Permissions)permissionsPlugin).getHandler();
	          } else {
	              return;
	          }
	      }
	}
	
	private Boolean HasPermission(CommandSender sender, String permissions)
	{
		if(!(sender instanceof Player)) return true;
		Player p = (Player)sender;
		if(p.isOp()) return false;
		if(ph == null) return false;
		if(ph.has(p, "VirtualShop.hidden")) return false;
		if(ph.has(p, permissions)) return true;
		return false;
	}
}

