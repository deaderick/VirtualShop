package com.SwearWord.VirtualShop.Listeners;

import com.nijikokun.register.payment.Method;
import com.nijikokun.register.payment.Methods;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;

public class EconomyManager extends ServerListener
{
	private static Methods Methods = new Methods();

	@Override
	public void onPluginEnable(PluginEnableEvent event)
	{
		if (!Methods.hasMethod())
		{
			if(Methods.setMethod(event.getPlugin())) System.out.println("[Towny] using " + Methods.getMethod().getName() + " for economy.");
		}
	}

	public static Method getMethod()
	{
		return Methods.getMethod();
	}
}
