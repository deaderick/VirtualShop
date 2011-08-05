package com.SwearWord.VirtualShop.Utilities;

import java.io.File;
import java.io.IOException;

import org.bukkit.util.config.Configuration;

public class VSproperties 
{
	private static Configuration config;
	private static File file = new File("plugins/VirtualShop/config.yml");
	public enum Option
	{
		DATABASE_TYPE,
		MYSQL_USERNAME,
		MYSQL_PASSWORD,
		MYSQL_HOST,
		MYSQL_PORT,
		MYSQL_DATABASE,
		BROADCAST,
		EXCHANGE_ITEM
		
	}
	
	public static Boolean Initialize()
	{
		if(!file.exists())
		{
			Response.LogMessage("Generating configuration.");
			try 
			{
				GenerateConfig();
			} 
			catch (IOException e) 
			{
				Response.LogMessage("Config generation failed!");
				return false;
			}
		}
		else
		{
			config = new Configuration(file);
		}
		config.load();
		
		return true;
	}
	
	private static void GenerateConfig() throws IOException
	{
		file.createNewFile();
		config = new Configuration(file);
		config.setProperty("broadcast-offers", true);
		config.setProperty("Exchange.exchange-item", 266);
		config.getDouble("Exchange.exchange-base-price", 250.0d);
		config.setProperty("Exchange.multiplier", 0.05);
		config.setProperty("Exchange.dynamic", true);
		config.setProperty("using-MySQL", false);
		config.setProperty("MySQL.username", "root");
		config.setProperty("MySQL.password", "password");
		config.setProperty("MySQL.host", "localhost");
		config.setProperty("MySQL.database", "minecraft");
		config.setProperty("MySQL.port", 3306);
		config.save();
	}


	public static void SaveConfig()
	{
		config.save();
	}

    public static Boolean isDynamic()
    {
		return config.getBoolean("Exchange.dynamic", true);
    }
	
	public static Integer getExchangeItem()
	{
		return config.getInt("Exchange.exchange-item", 266);
	}

	public static Double getExchangeMultipier()
	{
		return config.getDouble("Exchange.exchange-multiplier", 0.05d);
	}

	public static Double getExchangeBase()
	{
		return config.getDouble("Exchange.exchange-base-price", 250.0d);
	}
	
	public static Boolean BroadcastOffers()
	{
		return config.getBoolean("broadcast-offers", true);
	}
	public static Boolean UsingMySQL()
	{
		return config.getBoolean("using-MySQL", false);
	}
	
	public static String MySQLUserName()
	{
		return config.getString("MySQL.username", "root");
	}
	
	public static String MySQLPassword()
	{
		return config.getString("MySQL.password", "password");
	}
	
	public static String MySQLHost()
	{
		return config.getString("MySQL.host", "localhost");
	}
	
	public static String MySQLdatabase()
	{
		return config.getString("MySQL.database", "minecraft");
	}
	
	public static Integer MySQLport()
	{
		return config.getInt("MySQL.port", 3306);
	}

}
