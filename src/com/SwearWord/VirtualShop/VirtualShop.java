package com.SwearWord.VirtualShop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.alta189.sqllitelib.sqlCore;

import com.iConomy.*;
import com.iConomy.system.Holdings;

public class VirtualShop extends JavaPlugin
{
	
	Logger log = Logger.getLogger("Minecraft");
	public String prefix = ChatColor.DARK_GREEN + "[Virtual Shop] " + ChatColor.WHITE;
	public File folder = new File("plugins/VirtualShop");
	public File config = new File("plugins/VirtualShop/config.txt");
	private Shop Shop;
	
	public void onEnable()
	{ 
		log.info(prefix+"VirtualShop loading.");
		Shop = new Shop();
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
				if(args[0].equalsIgnoreCase("exchange") && (sender instanceof Player))
				{
					Player p = (Player)sender;
					for(int i=0;i<Shop.exchanges.size();i++)
					{
						int id = (Integer)Shop.exchanges.keySet().toArray()[i];
						double price = (Double)Shop.exchanges.get(id);
						Shop.Exchange(new ItemStack(id),p,price);
					}
					return true;
					
				}
				if(args[0].equalsIgnoreCase("log"))
				{
					if(args.length == 1)
					{
						Shop.ListTransactions(sender, "select * from transactions");
						return true;
					}
					if(args.length == 2)
					{
						String target = args[1];
						Shop.ListTransactions(sender, "select * from transactions where seller like '%" + target+"%' OR buyer like '%" + target +"%'");
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

