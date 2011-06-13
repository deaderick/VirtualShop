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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.alta189.sqllitelib.sqlCore;
import com.iConomy.iConomy;
import com.iConomy.system.Holdings;

public class Shop 
{
	Logger log = Logger.getLogger("Minecraft");
	static String prefix = ChatColor.DARK_GREEN + "[Virtual Shop] " + ChatColor.WHITE;
	private sqlCore db;
	private Properties properties = new Properties();
	public HashMap exchanges = new HashMap(); 
	public File config = new File("plugins/VirtualShop/config.txt");
	public File folder = new File("plugins/VirtualShop");
	
	public Shop()
	{
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
			String query = "create table stock('id' integer primary key,'damage' integer,'seller' varchar(80) not null,'item' integer not null, 'price' float not null,'amount' integer not null)";
			db.createTable(query);
		}
		if(!db.checkTable("transactions"))
		{
			String query = "create table transactions('id' integer primary key,'buyer' varchar(80) not null,'seller' varchar(80) not null,'item' integer not null, 'cost' float not null,'amount' integer not null)";
			db.createTable(query);
		}
	}
	
	public void Unload()
	{
		db.close();
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

		db = new sqlCore(log,prefix, "VirtualShop",folder.getPath());
		db.initialize();
		
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
			String query = "insert into transactions(buyer,seller,item,amount,cost) values('Exchange','"+ p.getName() + "'," + items.getType().getId() + ","+ items.getAmount() +","+payment+")";
			db.insertQuery(query);
		}
	}

	public void RemoveItem(CommandSender sender, ItemStack item)
	{
		if(!(sender instanceof Player))
		{
			sender.sendMessage(prefix + "You are not in game.");
			return;
		}
		Player player = (Player)sender;
		String name = player.getName();
		String query = "select * from stock where seller = '" + name + "' and item =" + item.getTypeId();
		ResultSet r = db.sqlQuery(query);
		int total = 0;
		InventoryManager i = new InventoryManager(player);
		try 
		{
			while(r.next())
			{
				int amount = r.getInt("amount");
				total += amount;
				item.setAmount(amount);
				item.setDurability((short)r.getInt("damage"));
				i.addItem(item);
			}
		} 
		catch (SQLException e) 
		{
			
		}
		if(total == 0)
		{
			player.sendMessage(prefix + "You do not have any " + item.getType().name() + " for sale.");
			return;
		}
		query = "delete from stock where seller = '" + name + "' and item =" + item.getTypeId();
		db.deleteQuery(query);
		player.sendMessage(prefix + total + " " + item.getType().name() + " was taken back.");
	}
	
	public void SellItem(CommandSender sender,ItemStack item,double price)
	{
		if(!(sender instanceof Player))
		{
			sender.sendMessage(prefix + "You are not in game.");
			return;
		}
		Player player = (Player)sender;
		InventoryManager im = new InventoryManager(player);
		if(!im.contains(item,true,true))
		{
			player.sendMessage(prefix + "You do not have " + item.getAmount() + " " +item.getType().name());
			return;
		}
		String query = "insert into stock(seller,item,amount,price,damage) values('" +player.getName() +"',"+ item.getType().getId() + ","+item.getAmount() +","+price+"," + item.getDurability()+")";
		db.insertQuery(query);
		im.remove(item);
		player.getServer().broadcastMessage(prefix + player.getName() + " has put " + item.getAmount() + " "+ item.getType().name() + " for sale for " + iConomy.format(price) + " each.");
	}
	
	public void BuyItem(CommandSender sender, ItemStack item)
	{
		if(!(sender instanceof Player))
		{
			sender.sendMessage(prefix + "You are not in game");
			return;
		}
		int amount = item.getAmount();
		int original = amount;
		Player player = (Player)sender;
		Holdings money = iConomy.getAccount(player.getName()).getHoldings();
		float spent =0;
		String query = "select * from stock where item=" + item.getTypeId()+ " order by price asc";
		ResultSet r = db.sqlQuery(query);
		InventoryManager im = new InventoryManager(player);
		int rows =0;
		try {
			while(r.next() && amount != 0)
			{
				rows++;
				int id = r.getInt("id");
				int damage = r.getInt("damage");
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
							player.sendMessage(prefix + "Ran out of money!");
							break;
						}
						cost = price * canbuy;
						
					}
					amount = amount-quant;
					money.subtract(cost);
					spent += cost;
					iConomy.Accounts.get(seller).getHoldings().add(cost);
					Player s = player.getServer().getPlayer(seller);
					if(s!=null)
					{
						s.sendMessage(prefix + player.getName() + " just bought " + canbuy + " " + item.getType().name() + " for " + cost);
					}
					ItemStack stack = new ItemStack(item.getType(),quant);
					stack.setDurability((short)damage);
					im.addItem(stack);
					int left = quant-canbuy;
					if(left == 0)
					{
						query = "delete from stock where id="+id;
						db.deleteQuery(query);
					}
					else
					{
						query = "update stock set amount="+left+" where id=" + id;
						db.updateQuery(query);
						
					}
					query = "insert into transactions(seller,buyer,item,amount,cost) values('" +s.getName() +"','"+ player.getName() + "'," + item.getType().getId() + ","+ canbuy +","+cost+")";
					db.insertQuery(query);

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
							player.sendMessage(prefix + "Ran out of money!");
							
							break;
						}
						cost = price * canbuy;
					}
						int left = quant - canbuy;
						money.subtract(cost);
						spent += cost;
						iConomy.Accounts.get(seller).getHoldings().add(cost);
						Player s = player.getServer().getPlayer(seller);
						if(s!=null)
						{
							s.sendMessage(prefix + player.getName() + " just bought " + canbuy + " " + item.getType().name() + " for " + cost);
						}
						ItemStack stack = new ItemStack(item.getType(),canbuy);
						im.addItem(stack);
						amount = 0;
						query = "update stock set amount="+left+" where id=" + id;
						db.updateQuery(query);
						query = "insert into transactions(seller,buyer,item,amount,cost) values('" +s.getName() +"','"+ player.getName() + "'," + item.getType().getId() + ","+ canbuy +","+cost+")";
						db.insertQuery(query);
					
				}
				
			}
		} 
		catch (Exception e) 
		{
			
		}
		if(rows == 0)
		{
			player.sendMessage(prefix + "There is no " + item.getType().name()+ " for sale.");
		}
		else
		{
			player.sendMessage(prefix + "Managed to buy " + (original-amount) + " " + item.getType().name() + " for " + iConomy.format(spent));
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
	
	public void ListTransactions(CommandSender sender, String query)
	{
		ResultSet r = db.sqlQuery(query);
		try {
			while(r.next())
			{
				String result = r.getString("seller")+" sold "+r.getInt("amount")+" "+Material.getMaterial(r.getInt("item")) + " for "+iConomy.format(r.getFloat("cost")) + " to " + r.getString("buyer");
				//String result = r.getString("item") + " " + r.getFloat("price");
				sender.sendMessage(prefix + result);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			
		}
	}
	
	public ItemStack GetItem(String item)
	{
		return GetItem(item,0);
		
	}
	
	public ItemStack GetItem(String item, int amount)
	{
		ItemStack type;
		try {
			type = ItemDb.get(item,amount);
			return type;
		} catch (Exception e) {
			return null;
		}
	}
	
}
