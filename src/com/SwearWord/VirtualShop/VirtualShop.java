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

import com.iConomy.iConomy;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;


public class VirtualShop extends JavaPlugin
{
	
	Logger log = Logger.getLogger("Minecraft");
	public String prefix = ChatColor.DARK_GREEN + "[Virtual Shop] " + ChatColor.WHITE;
	public File folder = new File("plugins/VirtualShop");
	public File config = new File("plugins/VirtualShop/config.txt");
	private Shop Shop;
	PermissionHandler ph;
	
	public void onEnable()
	{ 
		log.info(prefix+"VirtualShop loading.");
		Shop = new Shop();
		setupPermissions();
	} 
	private void setupPermissions() 
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

	
	public void onDisable()
	{ 
		Shop.Unload();
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)	{

		if(commandLabel.equalsIgnoreCase("vs") || commandLabel.equalsIgnoreCase("shop"))
		{	
			if(args.length>0)
			{
				if(args[0].equalsIgnoreCase("rates"))
				{
					for(int i=0;i<Shop.exchanges.size();i++)
					{
						String s = "";
						if(sender instanceof Player)
						{
							s = ((Player)(sender)).getName();
						}
						int id = (Integer)Shop.exchanges.keySet().toArray()[i];
						Material m = Material.getMaterial(id);
						double price = Shop.CalculatePrice(id, s,1);
						sender.sendMessage(prefix + "Sell " + m.name() + ": " + iConomy.format(price));
						sender.sendMessage(prefix + "Buy " + m.name() + ": " + iConomy.format(price + Shop.multiplier));
					}
					return true;
					
				}
				if(args[0].equalsIgnoreCase("invest"))
				{
					if(args.length < 2)
					{
						sender.sendMessage(prefix + "You must specify amount of money to invest");
						return true;
					}
					String p = args[1];
					int amount = 0;
					try
					{
						amount = Integer.parseInt(p);
					}
					catch (Exception ex)
					{
						sender.sendMessage(prefix + "That is not a number.");
						return true;
					}
					Shop.Invest(amount, sender);
					return true;
				}
				if(args[0].equalsIgnoreCase("exchange") && (sender instanceof Player))
				{
					Player p = (Player)sender;
					for(int i=0;i<Shop.exchanges.size();i++)
					{
						int id = (Integer)Shop.exchanges.keySet().toArray()[i];
						Shop.Exchange(new ItemStack(id),p);
					}
					return true;
					
				}
				if(args[0].equalsIgnoreCase("log"))
				{
					if(args.length == 1)
					{
						Shop.ListTransactions(sender, "select * from transactions order by id desc limit 0,10");
						return true;
					}
					if(args.length == 2)
					{
						String target = args[1];
						Shop.ListTransactions(sender, "select * from transactions where seller like '%" + target+"%' OR buyer like '%" + target +"%' order by id desc limit 0,10");
						return true;
					}
					
				}
				if(args[0].equalsIgnoreCase("list"))
				{
					if(args.length == 1)
					{
						 Shop.ListItems(sender,"select * from stock limit 0,10");
						 return true;
					}
					if(args.length == 2)
					{
						String seller = args[1];
						try
						{
							int page = Integer.parseInt(seller);
							Shop.ListItems(sender,"select * from stock limit " + page + ",10");
							
							return true;
						}
						catch(Exception e)
						{
							if(seller.equalsIgnoreCase("me") && sender instanceof Player)
							{
								Player p = (Player)sender;
								seller = p.getName();
							}
							Shop.ListItems(sender,"select * from stock where seller like '%" + seller+"%'");
							sender.sendMessage(seller);
							return true;
						}
					}
				}
				if(args[0].equalsIgnoreCase("price"))
				{
					 if(args.length != 2)
					 {
						 sender.sendMessage(prefix + "Proper usage is /vs price <item>");
						 return true;
					 }
					 String item = args[1].toUpperCase();
					 ItemStack type = Shop.GetItem(item);
					 if(type==null)
					 {
					 	sender.sendMessage(prefix + "What is " + item + "?");
					    return true;
					 }
					 Shop.GetPrice(sender,type.getType());
					 return true;
				}
				if(args[0].equalsIgnoreCase("remove") && args.length == 2)
				{
					String item = args[1].toUpperCase();
					ItemStack type = Shop.GetItem(item);
					if(type==null)
					{
						sender.sendMessage(prefix + "What is " + item + "?");
						return true;
					}
					Shop.RemoveItem(sender,type);
					return true;
					
				}
				if(args[0].equalsIgnoreCase("buy"))
				{
					if(ph != null && (sender instanceof Player) && ph.has((Player)sender, "VirtualShop.nobuy"))
					{
						sender.sendMessage(prefix + "You may not purchase items.");
						return true;
					}
					if(args.length != 3)
					{
						sender.sendMessage(prefix + "Proper usage is /vs buy <amount> <item>");
						return true;
					}
					String item = args[2].toUpperCase();
					int amount =0;
					try
					{
						amount = Integer.parseInt(args[1]);
					}
					catch(Exception e)
					{
						sender.sendMessage(prefix + "The amount goes first.");
						return true;
					}
					if(amount < 1)
					{
						sender.sendMessage(prefix + "Stop trying to cheat. Positive numbers, idiot.");
						return true;
					}
					ItemStack type = Shop.GetItem(item,amount);
					if(type==null)
					{
						sender.sendMessage(prefix + "What is " + item + "?");
						return true;
					}
					Shop.BuyItem(sender, type);
					return true;
					
				}
				if(args[0].equalsIgnoreCase("sell")&& sender instanceof Player)
				{
					if(ph != null && (sender instanceof Player) && ph.has((Player)sender, "VirtualShop.nosell"))
					{
						sender.sendMessage(prefix + "You may not sell items.");
						return true;
					}
					if(args.length != 4)
					{
						sender.sendMessage(prefix + "Proper usage is /vs sell <amount> <item> <price>");
						return true;
					}
						try
						{
						int amount = Integer.parseInt(args[1]);
						if(amount < 1)
						{
							sender.sendMessage(prefix + "Stop trying to cheat. Positive numbers, idiot.");
							return true;
						}
						double price = Double.parseDouble(args[3]);
						String item = args[2].toUpperCase();
						ItemStack type;
						/*
						if(item.equalsIgnoreCase("hand"))
						{
							Player p = (Player)sender;
							type = p.getInventory().getItemInHand().getType();
						}
						*/
						
						type = Shop.GetItem(item,amount);
							
						if(type==null)
						{
							sender.sendMessage(prefix + "What is " + item + "?");
							return true;
						}
						Shop.SellItem(sender, type, price);
						return true;
					
					}
					catch(Exception e)
					{
						return false;
					}
				}
			}
		

		}
		return false;
		}
	
	
	
	
	

	
	
	
	
}

