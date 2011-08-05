package com.SwearWord.VirtualShop.Utilities;

import java.util.logging.Logger;

import com.SwearWord.VirtualShop.Listeners.EconomyManager;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class Response 
{
	private static Logger log = Logger.getLogger("minecraft");
	private static Server server;
	private static String prefix = ChatColor.DARK_GREEN + "[Virtual Shop] " + ChatColor.WHITE;
	private static String plainprefix = "[Virtual Shop] ";
	
	public static void Initialize(Server s)
	{
		server = s;
	}
	
	public static void LogMessage(String msg)
	{
		log.info(plainprefix + msg);

	}
	
	public static void MsgPlayers(String msg)
	{
		server.broadcastMessage(prefix + msg);
	}
	
	public static Boolean MsgPlayer(String p, String msg)
	{
		Player player = server.getPlayer(p);
		if(player == null) return false;
		MsgPlayer(player,msg);
		return true;
	}
	
	public static void MsgPlayer(Player p, String msg)
	{
		PlainMsgPlayer(p,prefix + msg);
	}
	
	public static void MsgPlayer(CommandSender s, String msg)
	{
		PlainMsgPlayer(s,prefix + msg);
	}
	
	public static void PlainMsgPlayer(Player p, String msg)
	{
		p.sendMessage(msg);
	}
	
	public static void PlainMsgPlayer(CommandSender s, String msg)
	{
		s.sendMessage(msg);
	}
	
	public static Logger getLogger()
	{
		return log;
	}
	
	public static String getPrefix()
	{
		return prefix;
	}

	public static void SendOffer(Player p,String seller, Integer amount, String item, float price)
	{
		Response.SendOffer((CommandSender)p, seller,amount,item,price);
	}
	
	
	public static void SendOffer(CommandSender s,String seller, Integer amount, String item, float price)
	{
		Response.PlainMsgPlayer(s, FormatSeller(seller)  + " is selling " + FormatAmount(amount) + " " + FormatItem(item) + " for " + FormatPrice(price) + " each.");
	}
	
	public static void SendLogEvent(CommandSender sender, String seller, Integer amount, String item, float price, String buyer)
	{
		sender.sendMessage(FormatSeller(seller)+ " sold "+ FormatAmount(amount)+" " + FormatItem(item) + " for "+ FormatPrice(price) + " to " + FormatBuyer(buyer));

	}
	
	public static void BroadcastOffer(String seller, Integer amount, String item, float price)
	{
		Response.MsgPlayers(FormatSeller(seller)  + " is selling " + FormatAmount(amount) + " " + FormatItem(item) + " for " + FormatPrice(price) + " each.");
	}

	public static void WrongItem(CommandSender sender, String item)
	{
		
		Response.MsgPlayer(sender, "What is " + item + "?");
	}
	
	public static void DenyConsole(CommandSender sender)
	{
		Response.MsgPlayer(sender, "You must be in-game to do this.");
	}
	
	public static void NumberFormat(CommandSender sender)
	{
		Response.MsgPlayer(sender, "That is not a proper number.");
	}
	
	public static String FormatSeller(String seller)
	{
		return ChatColor.RED + seller + ChatColor.WHITE;
	}
	
	public static String FormatAmount(Integer amount)
	{
		return ChatColor.GOLD + amount.toString() + ChatColor.WHITE;
	}
	
	public static String FormatItem(String item)
	{
		return ChatColor.BLUE + item.toLowerCase() + ChatColor.WHITE;
	}

	public static String FormatPrice(Float price)
	{
		return ChatColor.YELLOW + EconomyManager.getMethod().format(price) + ChatColor.WHITE;
	}
	
	public static String FormatBuyer(String buyer)
	{
		return ChatColor.AQUA + buyer.toString() + ChatColor.WHITE;
	}
	
	public static void NoPermissions(CommandSender sender)
	{
		MsgPlayer(sender, "You do not have permission to do this");
	}
}
