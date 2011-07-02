package com.SwearWord.VirtualShop;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import com.SwearWord.VirtualShop.Utilities.DatabaseManager;
import com.SwearWord.VirtualShop.Utilities.InventoryManager;
import com.SwearWord.VirtualShop.Utilities.ItemDb;
import com.SwearWord.VirtualShop.Utilities.Response;
import com.SwearWord.VirtualShop.Utilities.VSproperties;
import com.alta189.sqlLibrary.MySQL.DatabaseHandler;
import com.iConomy.iConomy;
import com.iConomy.system.Holdings;

public class Shop 
{
	public static void DisplayPrices(CommandSender sender,String[] args)
	{
		if(args.length < 2)
		{
			Response.MsgPlayer(sender, "You need to specify the item.");
			return;
		}
		ItemStack item = ItemDb.get(args[1],0);
		if(item==null)
		{
			Response.WrongItem(sender, args[1]);
			return;
		}
		ResultSet r = DatabaseManager.GetPrices(item);
		int count=0;
		try 
		{
			while(r.next())
			{
				if(count == 9)
				{
					Response.PlainMsgPlayer(sender, "And others...");
					break;
				}
				Response.SendOffer(sender,r.getString("seller"), r.getInt("amount"),args[1],r.getFloat("price"));
				count++;
			}
		} 
		catch (SQLException e) 
		{
		}
		if(count==0)
		{
			Response.MsgPlayer(sender, "No one is selling " + args[1]);
		}
	}

	public static void SellItem(CommandSender sender, String[] args)
	{
		if(args.length < 4)
		{
			Response.MsgPlayer(sender, "Proper usage is /vs sell <amount> <item> <price>");
			return;
		}
		if(!(sender instanceof Player))
		{
			Response.DenyConsole(sender);
			return;
		}
		float price = ParseFloat(args[3]);
		int amount = ParseInteger(args[1]);
		if(amount < 0 || price < 0)
		{
			Response.NumberFormat(sender);
			return;
		}
		
		Player player = (Player)sender;
		ItemStack item = ItemDb.get(args[2],amount);
		if(args[2].equalsIgnoreCase("hand")) 
		{
			item=player.getItemInHand();
			item.setAmount(amount);
			args[2] = ItemDb.reverseLookup(item);
		}
		if(item==null)
		{
			Response.WrongItem(sender, args[2]);
			return;
		}
		InventoryManager im = new InventoryManager(player);
		if(!im.contains(item,true,true))
		{
			Response.MsgPlayer(sender,"You do not have " + Response.FormatAmount(item.getAmount()) + " " + Response.FormatItem(args[2]));
			return;
		}
		DatabaseManager.AddItem(player, item, price);
		im.remove(item, true, true);
		if(VSproperties.BroadcastOffers()) 
		{
			Response.BroadcastOffer(player.getDisplayName(), amount, args[2], price);
			return;
		}
		Response.SendOffer(player, "Your shop", amount, args[2], price);
		
		
	}
	
	public static void BuyItem(CommandSender sender, String args[])
	{
		if(!(sender instanceof Player))
		{
			Response.DenyConsole(sender);
			return;
		}
		if(args.length < 3)
		{
			Response.MsgPlayer(sender, "Proper usage is /vs buy <amount> <item>");
			return;
		}
		int amount = ParseInteger(args[1]);
		if(amount < 0)
		{
			Response.NumberFormat(sender);
		}
		
		ItemStack item = ItemDb.get(args[2], 0);
		if(item==null)
		{
			Response.WrongItem(sender, args[2]);
			return;
		}
		
		int original = amount;
		Player player = (Player)sender;
		Holdings money = iConomy.getAccount(player.getName()).getHoldings();
		float spent =0;
		ResultSet r = DatabaseManager.SelectItem(item);
		InventoryManager im = new InventoryManager(player);
		int rows =0;
		try {
			while(r.next() && amount != 0)
			{
				rows++;
				int id = r.getInt("id");
				int quant = r.getInt("amount");
				float price = r.getFloat("price");
				float cost = quant*price;
				String seller = r.getString("seller");			
				if(amount >= quant)
				{
					//Finds max that can be bought
					int canbuy = quant;
					if(!(money.hasEnough(cost)))
					{
						canbuy = (int)(money.balance() / price);
						if(canbuy < 1)
						{
							Response.MsgPlayer(sender,"Ran out of money!");
							break;
						}
						cost = price * canbuy;
						
					}
					amount = amount-quant;
					money.subtract(cost);
					spent += cost;
					iConomy.Accounts.get(seller).getHoldings().add(cost);
					Response.MsgPlayer(seller, Response.FormatSeller(player.getName()) + " just bought " + Response.FormatAmount(canbuy) + " " + Response.FormatItem(args[2]) + " for " + Response.FormatPrice(cost));
					item.setAmount(canbuy);
					im.addItem(item);
					int left = quant-canbuy;
					if(left == 0) DatabaseManager.DeleteItem(id);
					else DatabaseManager.UpdateQuantity(id, left);
					DatabaseManager.LogTransaction(seller, player.getName(), item.getTypeId(), canbuy, cost,item.getDurability());
				}
				else
				{
					cost = amount*price;
					int canbuy = amount;
					if(!money.hasEnough(cost))
					{
						canbuy = (int)(money.balance() / price);
						if(canbuy < 1)
						{
							Response.MsgPlayer(sender,"Ran out of money!");
							break;
						}
						cost = price * canbuy;
					}
						int left = quant - canbuy;
						money.subtract(cost);
						spent += cost;
						iConomy.Accounts.get(seller).getHoldings().add(cost);
						Response.MsgPlayer(seller, Response.FormatSeller(player.getDisplayName()) + " just bought " + Response.FormatAmount(canbuy) + " " + Response.FormatItem(args[2]) + " for " + Response.FormatPrice(cost));
						
						item.setAmount(canbuy);
						im.addItem(item);
						amount = 0;
						DatabaseManager.UpdateQuantity(id, left);
						DatabaseManager.LogTransaction(seller, player.getName(), item.getTypeId(), canbuy, cost,item.getDurability());
					
				}
				
			}
		} 
		catch (Exception e) 
		{
			
		}
		if(rows == 0)
		{
			Response.MsgPlayer(sender,"There is no " + Response.FormatItem(args[2])+ " for sale.");
		}
		else
		{
			Response.MsgPlayer(sender,"Managed to buy " + Response.FormatAmount((original-amount)) + " " + Response.FormatItem(args[2]) + " for " + Response.FormatPrice(spent));
		}
	}

	public static void ListTransactions(CommandSender sender, String[] args)
	{
		ResultSet r;
		if(args.length > 1) r = DatabaseManager.GetTransactions(args[1]);
		else r = DatabaseManager.GetTransactions();
		try 
		{
			while(r.next())
			{
				Response.LogMessage("Parsing row.");
				String name = ItemDb.reverseLookup(new ItemStack(r.getInt("item"), 0, (short)r.getInt("damage")));
				Response.SendLogEvent(sender, r.getString("seller"), r.getInt("amount"), name, r.getFloat("cost"),r.getString("buyer"));
			}
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
	}
	
	public static void ListItems(CommandSender sender, String[] args)
	{
		ResultSet r = DatabaseManager.GetCheapest();
		int start =1;
		if(args.length >1) start =ParseInteger(args[1]);
		if(start < 0) start = 1;
		start = (start-1) * 9;
		try 
		{
			int count =0;
			while(r.next())
			{
				if(count==start+9)
				{
					Response.PlainMsgPlayer(sender, "And more...");
					break;
				}
				if(count >= start)
				{
					String name = ItemDb.reverseLookup(new ItemStack(r.getInt("item"),0,(short)r.getInt("damage")));
					Response.SendOffer(sender, r.getString("seller"), r.getInt("amount"), name, r.getFloat("price"));
				}
				count++;
			}

		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
	}
	
	public static void RemoveItems(CommandSender sender, String args[])
	{
		if(!(sender instanceof Player))
		{
			Response.DenyConsole(sender);
			return;
		}
		if(args.length < 2)
		{
			Response.MsgPlayer(sender, "You must specify an item.");
			return;
		}
		
		ItemStack item = ItemDb.get(args[1], 0);
		if(item==null)
		{
			Response.WrongItem(sender, args[1]);
			return;
		}
		
		Player player = (Player)sender;
		ResultSet r = DatabaseManager.SelectSellerItem(player, item);
		int total = 0;
		InventoryManager im = new InventoryManager(player);
		try 
		{
			while(r.next())
			{
				int amount = r.getInt("amount");
				total += amount;
			}
		}
		catch (Exception ex)
		{
			
		}
		if(total == 0)
		{
			Response.MsgPlayer(sender,"You do not have any " + args[1] + " for sale.");
			return;
		}
		item.setAmount(total);
		im.addItem(item);
		DatabaseManager.RemoveSellerItem(player, item);
		Response.MsgPlayer(sender, "Removed " + Response.FormatAmount(total) + " " + Response.FormatItem(args[1]));
		
	}
	
	public static void PrintRates(CommandSender sender, String args[])
	{
		Material m = Material.getMaterial(VSproperties.getExchangeItem());
		try
		{
			double price = CalculateExchangeRates(1);
			Response.MsgPlayer(sender, "Sell " + Response.FormatItem(m.name()) + ": " + Response.FormatPrice((float)price));
			Response.MsgPlayer(sender, "Buy " + Response.FormatItem(m.name()) + ": " + Response.FormatPrice((float)(price + VSproperties.getExchangeMultipier())));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return;
		}
	
	}
	
	private static double CalculateExchangeRates(int trans) throws Exception
	{
		int id = VSproperties.getExchangeItem();
		double multiplier = VSproperties.getExchangeMultipier();
		double base = VSproperties.getExchangeBase();
		String query = "select amount from transactions where item = " + id + " and buyer = 'Exchange'";
		ResultSet rs = DatabaseManager.SelectQuery(query);
		int sold = 0;  
		while ( rs.next() )  
		{  
		    sold += rs.getInt("amount");  
		}
		
		query = "select amount from transactions where item = " + id + " and seller = 'Exchange'";
		rs = DatabaseManager.SelectQuery(query);
		int bought = 0;  
		while ( rs.next() )  
		{  
			    bought += rs.getInt("amount"); 
		}
		int ratio = bought - sold;
		float sign = Math.signum(trans);
		trans = Math.abs(trans);
		base = base + ratio*multiplier;
		return (base*trans + sign*multiplier*(trans * (trans-1)/2))/trans;
			
	}
	
	public static void Exchange(CommandSender sender)
	{
		if(!(sender instanceof Player))
		{
			Response.DenyConsole(sender);
			return;
		}
		ItemStack type = new ItemStack(VSproperties.getExchangeItem());
		
		try {
			ExchangeItem(type,(Player)sender);
		} catch (Exception e) {
			sender.sendMessage("An error ocurred.");
		}
	}
	
	public static void Invest(CommandSender sender, String args[])
	{
		if(!(sender instanceof Player))
		{
			Response.DenyConsole(sender);
			return;
		}
		if(args.length<2)
		{
			Response.MsgPlayer(sender, "Proper usage is /vs invest <money>");
			return;
		}
		Player p = (Player)sender;
		float amount = ParseFloat(args[1]);
		if(amount < 0)
		{
			Response.NumberFormat(sender);
		}
		Holdings h = iConomy.getAccount(p.getName()).getHoldings();
		if(!(h.hasEnough(amount)))
		{
			Response.MsgPlayer(sender,"You can't invest that much!");
			return;
		}
		try
		{
			InventoryManager im = new InventoryManager(p);
			double price = VSproperties.getExchangeBase();
			int id = VSproperties.getExchangeItem();
			double multiplier = VSproperties.getExchangeMultipier();
			price = CalculateExchangeRates(1) + multiplier;
			int count = (int)((Math.sqrt(4*price*price - 4*price*multiplier + 8 * amount * multiplier + multiplier * multiplier) - 2*price + multiplier)/(2*multiplier));
			if(count > 0)
			{
				double cost = count*(CalculateExchangeRates(count)+multiplier);
				amount -= cost;
				h.subtract(cost);
				ItemStack is = new ItemStack(id,count);
				im.addItem(is);
				Response.MsgPlayer(sender,"Invested " + Response.FormatAmount((int)cost) + " in " + Response.FormatAmount(count) + " " + Response.FormatItem(is.getType().name()));
				DatabaseManager.LogTransaction("Exchange", p.getName(), is.getTypeId(), is.getAmount(), (float)cost, (short)0);
			}
		}
		catch (Exception e)
		{
			Response.MsgPlayer(sender, "An error occurred. Ask owner to check config");
		}
		
	}
	
	private static void ExchangeItem(ItemStack type, Player p) throws Exception
	{
		
		InventoryManager im = new InventoryManager(p);
		ItemStack items = im.quantify(type);
		double price = (Double)CalculateExchangeRates(-items.getAmount());
		double payment = items.getAmount() * price;
		iConomy.Accounts.get(p.getName()).getHoldings().add(payment);
		im.remove(items);
		if(items.getAmount() > 0)
		{
			Response.MsgPlayer(p,"Exchanged " + Response.FormatAmount(items.getAmount())+ " " + Response.FormatItem(type.getType().name()) + " for " + Response.FormatPrice((float)payment));
			DatabaseManager.LogTransaction(p.getName(),"Exchange", type.getTypeId(), items.getAmount(), (float)payment, (short)0);
		}
	}
	
	private static Integer ParseInteger(String s)
	{
		try
		{
			Integer i = Integer.parseInt(s);
			if(i > 0) return i;
		}
		catch(NumberFormatException ex)
		{
			return -1;
		}
		
		return -1;
	}

	private static Float ParseFloat(String s)
	{
		try
		{
			Float i = Float.parseFloat(s);
			if(i > 0) return i;
		}
		catch(NumberFormatException ex)
		{
			return -1f;
		}
		
		return -1f;
	}
}
