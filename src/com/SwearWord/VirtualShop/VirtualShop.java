package com.SwearWord.VirtualShop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

public class VirtualShop extends JavaPlugin{
	
	Logger log = Logger.getLogger("Minecraft");
	public String prefix = ChatColor.DARK_GREEN + "[Virtual Shop] ";
	public sqlCore db;
	public static HashMap exchanges = new HashMap(); 
	public File folder = new File("plugins/VirtualShop");
	public File config = new File("plugins/VirtualShop/config.txt");
	static Properties properties = new Properties();
	
	public void onEnable(){ 
		
		log.info(prefix+"VirtualShop loading.");
		
		try {
			ItemDb.load(folder, "items.csv");
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		if(!folder.exists()){
			folder.mkdir();
		}
		if(!config.exists()){
			try 
			{
				config.createNewFile();
				FileOutputStream out = new FileOutputStream(config);
				properties.put("items", "266;250,264;1000,339;10");
				properties.store(out, "/vs exhange items with price serparated by ,");
				out.flush();
				out.close();
				
			} 
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		loadconfig();
		
		db = new sqlCore(log,prefix, "VirtualShop",folder.getPath());
		db.initialize();
		if(!db.checkTable("stock"))
		{
			String query = "create table stock('id' integer primary key,'seller' varchar(80) not null,'item' integer not null, 'price' float not null,'amount' integer not null)";
			db.createTable(query);
		}
		 
	} 
	private void loadconfig() 
	{
		try {
			FileInputStream is = new FileInputStream(config);
			properties.load(is);
			String[] splits = properties.getProperty("items").split(",");
			for(int i=0;i<splits.length;i++)
			{
				String[] line = splits[i].split(";");
				int id = Integer.parseInt(line[0]);
				double price = Double.parseDouble(line[1]);
				exchanges.put(id, price);
			}
			is.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void onDisable()
	{ 
		db.close();
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)	{

		if(commandLabel.equalsIgnoreCase("vs"))
		{	
			if(args.length>0)
			{
				if(args[0].equalsIgnoreCase("exchange") && (sender instanceof Player))
				{
					Player p = (Player)sender;
					for(int i=0;i<exchanges.size();i++)
					{
						int id = (Integer)exchanges.keySet().toArray()[i];
						double price = (Double)exchanges.get(id);
						Exchange(new ItemStack(id),p,price);
					}
					return true;
					
				}
				if(args[0].equalsIgnoreCase("list"))
				{
					if(args.length == 1)
					{
						 ListItems(sender,"select * from stock limit 0,10");
						 return true;
					}
					if(args.length == 2)
					{
						String seller = args[1];
						try
						{
							int page = Integer.parseInt(seller);
							ListItems(sender,"select * from stock limit " + page + ",10");
							
							return true;
						}
						catch(Exception e)
						{
							if(seller.equalsIgnoreCase("me") && sender instanceof Player)
							{
								Player p = (Player)sender;
								seller = p.getName();
							}
							ListItems(sender,"select * from stock where seller like '%" + seller+"%'");
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
					 Material type = GetItem(item);
					 if(type==null)
					 {
					 	sender.sendMessage(prefix + "What is " + item + "?");
					    return true;
					 }
					 GetPrice(sender,type);
					 return true;
				}
				if(args[0].equalsIgnoreCase("remove") && args.length == 2)
				{
					String item = args[1].toUpperCase();
					Material type = GetItem(item);
					if(type==null)
					{
						sender.sendMessage(prefix + "What is " + item + "?");
						return true;
					}
					RemoveItem(sender,type);
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
					Material type = GetItem(item);
					if(type==null)
					{
						sender.sendMessage(prefix + "What is " + item + "?");
						return true;
					}
					BuyItem(sender, type, amount);
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
						double price = Double.parseDouble(args[3]);
						String item = args[2].toUpperCase();
						Material type;
						if(item.equalsIgnoreCase("hand"))
						{
							Player p = (Player)sender;
							type = p.getInventory().getItemInHand().getType();
						}
						else
						{
							type = GetItem(item);
						}
						if(type==null)
						{
							sender.sendMessage(prefix + "What is " + item + "?");
							return true;
						}
						SellItem(sender, type, price, amount);
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
	
	public void Exchange(ItemStack type, Player p, double price)
	{
		InventoryManager im = new InventoryManager(p);
		ItemStack items = im.quantify(type);
		double payment = items.getAmount() * price;
		iConomy.Accounts.get(p.getName()).getHoldings().add(payment);
		im.remove(items);
		if(items.getAmount() > 0)
		{
			p.sendMessage(prefix + "Exchanged " + items.getAmount()+ " " + type.getType().name() + " for " + iConomy.format(payment)+"." );
		}
	}
	
	public Material GetItem(String item)
	{

		Material type=  Material.getMaterial(ItemDb.get(item));
		if(type.equals(Material.AIR)) return null;
		return type;
	}
	
	public void GetPrice(CommandSender sender,Material item)
	{
		String query = "select * from stock where item=" + item.getId() + " order by price asc limit 0,10";
		ResultSet r = db.sqlQuery(query);
		int count=0;
		try {
			while(r.next())
			{
				//sender.sendMessage(item.name() + " for " + r.getFloat("price"));
				sender.sendMessage(prefix + r.getString("seller") + " selling " + r.getInt("amount") + " " + item.name() + " for " + iConomy.format(r.getFloat("price")));
				count++;
			}
		} catch (SQLException e) {
		}
		if(count==0)
		{
			sender.sendMessage(prefix + "No one is selling " + item.name());
		}
	}
	public void RemoveItem(CommandSender sender, Material item)
	{
		if(!(sender instanceof Player))
		{
			sender.sendMessage(prefix + "You are not in game.");
			return;
		}
		Player player = (Player)sender;
		String name = player.getName();
		String query = "select * from stock where seller = '" + name + "' and item =" + item.getId();
		ResultSet r = db.sqlQuery(query);
		int amount=0;
		try {
			while(r.next())
			{
				amount += r.getInt("amount");
			}
		} catch (SQLException e) {
			
		}
		if(amount == 0)
		{
			player.sendMessage(prefix + "You do not have any " + item.name() + " for sale.");
			return;
		}
		InventoryManager i = new InventoryManager(player);
		ItemStack stack = new ItemStack(item,amount);
		i.addItem(stack);
		query = "delete from stock where seller = '" + name + "' and item =" + item.getId();
		db.deleteQuery(query);
		player.sendMessage(prefix + amount + " " + item.name() + " was taken back.");
	}
	
	public void SellItem(CommandSender sender,Material item,double price, int amount)
	{
		if(!(sender instanceof Player))
		{
			sender.sendMessage(prefix + "You are not in game.");
			return;
		}
		Player player = (Player)sender;
		InventoryManager im = new InventoryManager(player);
		ItemStack stack = new ItemStack(item,amount);
		if(!im.contains(stack))
		{
			player.sendMessage(prefix + "You do not have " + amount + " " +item.name());
			return;
		}
		String query = "insert into stock(seller,item,amount,price) values('" +player.getName() +"',"+ item.getId() + ","+amount +","+price+")";
		db.insertQuery(query);
		im.remove(stack);

		player.getServer().broadcastMessage(prefix + player.getName() + " has put " + amount + " "+ item.name() + " for sale for " + iConomy.format(price) + " each.");
	}
	
	public void BuyItem(CommandSender sender, Material item, int amount)
	{
		if(!(sender instanceof Player))
		{
			sender.sendMessage(prefix + "You are not in game");
			return;
		}
		int original = amount;
		Player player = (Player)sender;
		Holdings money = iConomy.getAccount(player.getName()).getHoldings();
		float spent =0;
		String query = "select * from stock where item=" + item.getId()+ " order by price asc";
		ResultSet r = db.sqlQuery(query);
		InventoryManager im = new InventoryManager(player);
		try {
			while(r.next() && amount != 0)
			{
				int id = r.getInt("id");
				int quant = r.getInt("amount");
				float price = r.getFloat("price");
				float cost = quant*price;
				String seller = r.getString("seller");
				
				
				if(amount >= quant)
				{
					//Finds max that can be bought
					if(!(money.hasEnough(cost)))
					{
						quant = (int)(money.balance() / price);
						if(amount < 1)
						{
							player.sendMessage(prefix + "Ran out of money!");
							break;
						}
						cost = price * quant;
						
					}
					amount = amount-quant;
					money.subtract(cost);
					spent += cost;
					iConomy.Accounts.get(seller).getHoldings().add(cost);
					Player s = this.getServer().getPlayer(seller);
					if(s!=null)
					{
						s.sendMessage(prefix + player.getName() + " just bought " + quant + " " + item.name() + " for " + cost);
					}
					ItemStack stack = new ItemStack(item,quant);
					im.addItem(stack);
					query = "delete from stock where id="+id;
					db.deleteQuery(query);
				}
				else
				{
					cost = amount*price;
					if(!money.hasEnough(cost))
					{
						amount = (int)(money.balance() / price);
						if(amount < 1)
						{
							player.sendMessage(prefix + "Ran out of money!");
							break;
						}
						cost = price * amount;
					}
						int left = quant - amount;
						money.subtract(cost);
						spent += cost;
						iConomy.Accounts.get(seller).getHoldings().add(cost);
						Player s = this.getServer().getPlayer(seller);
						if(s!=null)
						{
							s.sendMessage(prefix + player.getName() + " just bought " + amount + " " + item.name() + " for " + cost);
						}
						ItemStack stack = new ItemStack(item,amount);
						im.addItem(stack);
						amount = 0;
						query = "update stock set amount="+left+" where id=" + id;
						db.updateQuery(query);
					
				}
				
			}
		} 
		catch (Exception e) 
		{
			
		}
		if(amount == original)
		{
			player.sendMessage(prefix + "There is no " + item.name()+ " for sale.");
		}
		else
		{
			player.sendMessage(prefix + "Managed to buy " + (original-amount) + " " + item.name() + " for " + iConomy.format(spent));
		}
	}
	
	public void ListItems(CommandSender sender, String query)
	{
		ResultSet r = db.sqlQuery(query);
		try {
			while(r.next())
			{
				String result = r.getString("seller")+" selling "+r.getInt("amount")+" "+Material.getMaterial(r.getInt("item")) + " for "+iConomy.format(r.getFloat("price"));
				//String result = r.getString("item") + " " + r.getFloat("price");
				sender.sendMessage(prefix + result);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			
		}
	}
}

