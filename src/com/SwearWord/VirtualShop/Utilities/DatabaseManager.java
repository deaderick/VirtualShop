package com.SwearWord.VirtualShop.Utilities;

import java.net.MalformedURLException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.SwearWord.VirtualShop.VirtualShop;
import com.alta189.sqlLibrary.MySQL.DatabaseHandler;
import com.alta189.sqlLibrary.MySQL.mysqlCore;
import com.alta189.sqlLibrary.SQLite.sqlCore;
import com.iConomy.iConomy;



public class DatabaseManager 
{
	private static sqlCore db;
	private static mysqlCore mysqldb;
	private static Boolean usingMySQL = false;
	
	
	public static Boolean Initialize() throws Exception
	{
		if(VSproperties.UsingMySQL())
		{
			Response.LogMessage("Using MySQL.");
			mysqldb = new mysqlCore(Response.getLogger(), Response.getPrefix(),VSproperties.MySQLHost(), VSproperties.MySQLdatabase(), VSproperties.MySQLUserName(), VSproperties.MySQLPassword());
			mysqldb.initialize();
			if(mysqldb.checkConnection())
			{
				Response.LogMessage("Successfully connected to MySQL Database");
				CheckTables(mysqldb);
				usingMySQL = true;
				return true;
			}
			Response.LogMessage("Could not connect to MySQL Database. Check settings.");
		}
		db = new sqlCore(Response.getLogger(), Response.getPrefix(), "VirtualShop", "plugins/VirtualShop/");
		db.initialize();
		if(!db.checkConnection())
		{
			Response.LogMessage("FlatFile creation failed!");
			return false;
		}
		Response.LogMessage("Using flat files.");
		CheckTables(db);
		return true;
	}
	
	public static void Close()
	{
		if(usingMySQL) mysqldb.close();
		else db.close();
	}
	
	private static void CheckTables(sqlCore Database)
	{
		if(!Database.checkTable("stock"))
		{
			String query = "create table stock('id' integer primary key,'damage' integer,'seller' varchar(80) not null,'item' integer not null, 'price' float not null,'amount' integer not null)";
			Database.createTable(query);
			Response.LogMessage("Created stock table.");
		}
		if(!Database.checkTable("transactions"))
		{
			String query = "create table transactions('id' integer primary key,'damage' integer not null,'buyer' varchar(80) not null,'seller' varchar(80) not null,'item' integer not null, 'cost' float not null,'amount' integer not null)";
			Database.createTable(query);
			Response.LogMessage("Created transaction table.");
		}
	}
	
	private static void CheckTables(mysqlCore Database) throws Exception
	{
		if(!Database.checkTable("stock"))
		{
			String query = "create table stock(`id` integer primary key auto_increment,`damage` integer,`seller` varchar(80) not null,`item` integer not null, `price` float not null,`amount` integer not null)";
			Response.LogMessage("Created stock table.");
			Database.createTable(query);
		}
		if(!Database.checkTable("transactions"))
		{
			String query = "create table transactions(`id` integer primary key auto_increment,`damage` integer not null, `buyer` varchar(80) not null,`seller` varchar(80) not null,`item` integer not null, `cost` float not null,`amount` integer not null)";
			mysqldb.createTable(query);
			Response.LogMessage("Created transaction table.");
		}
	}

	public static ResultSet SelectQuery(String query)
	{
		if(usingMySQL)
		{
			try 
			{
				return mysqldb.sqlQuery(query);
			} 
			catch (Exception e) 
			{
				Response.LogMessage("MySQL error, check connection.");
				return null;
			}
		}
		return db.sqlQuery(query);
	}
	
	public static void DeleteQuery(String query)
	{
		if(usingMySQL)
		{
			try 
			{
				mysqldb.deleteQuery(query);
				return;
			} 
			catch (Exception e) 
			{
				Response.LogMessage("MySQL error, check connection.");
				return;
			}
		}
		db.deleteQuery(query);
	}
	
	public static void UpdateQuery(String query)
	{
		if(usingMySQL)
		{
			try 
			{
				mysqldb.updateQuery(query);
				return;
			} 
			catch (Exception e) 
			{
				Response.LogMessage("MySQL error, check connection.");
				return;
			}
		}
		db.updateQuery(query);
	}
	
	public static void InsertQuery(String query)
	{
		if(usingMySQL)
		{
			try 
			{
				mysqldb.insertQuery(query);
				return;
			} 
			catch (Exception e) 
			{
				Response.LogMessage("MySQL error, check connection.");
				return;
			}
		}
		db.insertQuery(query);
	}

	public static ResultSet SelectItem(ItemStack item)
	{
		String query = "select * from stock where item=" + item.getTypeId()+ " and damage=" + item.getDurability() + " order by price asc";
		return SelectQuery(query);
	}
	
	public static ResultSet GetPrices(ItemStack item)
	{
		String query = "select * from stock where item=" + item.getTypeId() + " AND damage=" + item.getDurability() + " order by price asc limit 0,10";
		return SelectQuery(query);
	}
	
	public static void AddItem(Player player, ItemStack item, float price)
	{
			String query = "insert into stock(seller,item,amount,price,damage) values('" +player.getName() +"',"+ item.getType().getId() + ","+item.getAmount() +","+price+"," + item.getDurability()+")";
			InsertQuery(query);
	}

	public static void DeleteItem(int id)
	{
		String query = "delete from stock where id="+id;
		DeleteQuery(query);
	}
	
	public static void UpdateQuantity(int id, int quantity)
	{
		String query = "update stock set amount="+quantity+" where id=" + id;
		UpdateQuery(query);
	}

	public static void LogTransaction(String seller, String buyer, Integer id, Integer quantity, Float cost, Short damage)
	{
		String query = "insert into transactions(seller,buyer,item,amount,cost,damage) values('" +seller +"','"+ buyer + "'," + id + ","+ quantity +","+cost+","+damage+")";
		InsertQuery(query);
	}
	
	public static ResultSet GetTransactions()
	{
		return SelectQuery("select * from transactions order by id desc limit 0,10");
	}
	
	public static ResultSet GetTransactions(String search)
	{
		return SelectQuery("select * from transactions where seller like '%" + search +"%' OR buyer like '%" + search +"%' order by id desc limit 0,10");
	}

	public static ResultSet GetCheapest()
	{
		return SelectQuery("select f.* from (select item,min(price) as minprice from stock group by item) as x inner join stock as f on f.item = x.item and f.price = x.minprice");
	}
	
	public static ResultSet GetCheapest(String s)
	{
		return SelectQuery("select f.* from (select item,min(price) as minprice from stock group by item) as x inner join stock as f on f.item = x.item and f.price = x.minprice");
	}

	public static ResultSet SelectSellerItem(Player player, ItemStack item)
	{
		String query = "select * from stock where seller = '" + player.getName() + "' and item =" + item.getTypeId() + " and damage=" + item.getDurability();
		return SelectQuery(query);
	}
	
	public static void RemoveSellerItem(Player player, ItemStack item)
	{
		String query = "delete from stock where seller = '" + player.getName() + "' and item =" + item.getTypeId() + " and damage = " + item.getDurability();
		DeleteQuery(query);
	}

}
