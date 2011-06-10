package com.SwearWord.VirtualShop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Logger;
import org.bukkit.inventory.ItemStack;


public class ItemDb
{
	private final static Logger logger = Logger.getLogger("Minecraft");
	private static Map<String, Integer> items = new HashMap<String, Integer>();
	private static Map<String, Short> durabilities = new HashMap<String, Short>();

	@SuppressWarnings("LoggerStringConcat")
	public static void load(File folder, String fname) throws IOException
	{
		folder.mkdirs();
		File file = new File(folder, fname);

		if (!file.exists())
		{
			file.createNewFile();
			InputStream res = ItemDb.class.getResourceAsStream("/items.csv");
			FileWriter tx = new FileWriter(file);
			try
			{
				for (int i = 0; (i = res.read()) > 0;) tx.write(i);
			}
			finally
			{
				try
				{
					tx.flush();
					tx.close();
					res.close();
				}
				catch (Exception ex)
				{
				}
			}
		}

		BufferedReader rx = new BufferedReader(new FileReader(file));
		try
		{
			items.clear();

			for (int i = 0; rx.ready(); i++)
			{
				try
				{
					String line = rx.readLine().trim().toLowerCase();
					if (line.startsWith("#"))
						continue;
					
					String[] parts = line.split("[^a-z0-9]");
					if (parts.length < 2)
						continue;
					
					int numeric = Integer.parseInt(parts[1]);
					
					durabilities.put(parts[0], parts.length > 2 && !parts[2].equals("0") ? Short.parseShort(parts[2]) : 0);
					items.put(parts[0], numeric);
				}
				catch (Exception ex)
				{
					logger.warning("Error parsing " + fname + " on line " + i);
				}
			}
		}
		finally
		{
			rx.close();
		}
	}
	
	
	public static ItemStack get(String id, int quantity) throws Exception {
		ItemStack retval = get(id);
		retval.setAmount(quantity);
		return retval;
	}

	public static ItemStack get(String id) throws Exception
	{
		ItemStack retval = new ItemStack(getUnsafe(id));
		retval.setDurability(durabilities.containsKey(id) ? durabilities.get(id) : 0);
		if (items.containsValue(retval.getTypeId()) || true) return retval;
		throw new Exception("Unknown item numeric: " + retval);
	}

	private static int getUnsafe(String id) throws Exception
	{
		id = id.toLowerCase();
		try
		{
			return Integer.parseInt(id);
		}
		catch (NumberFormatException ex)
		{
			if (items.containsKey(id)) return items.get(id);
			throw new Exception("Unknown item name: " + id);
		}
	}
}
