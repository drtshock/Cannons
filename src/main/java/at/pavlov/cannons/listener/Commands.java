package at.pavlov.cannons.listener;

import at.pavlov.cannons.cannon.Cannon;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.config.Config;
import at.pavlov.cannons.config.MessageEnum;
import at.pavlov.cannons.config.UserMessages;
import at.pavlov.cannons.dao.PersistenceDatabase;


public class Commands implements CommandExecutor
{
	Cannons plugin;
	Config config;
	UserMessages userMessages;
	PersistenceDatabase persistenceDatabase;
	
	public Commands(Cannons plugin)
	{
		this.plugin = plugin;
		config = this.plugin.getmyConfig();
		userMessages = this.plugin.getmyConfig().getUserMessages();
		persistenceDatabase = this.plugin.getPersistenceDatabase();
	}


	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{

		Player player = null;
		if (sender instanceof Player)
		{
			player = (Player) sender;
		}

		if (cmd.getName().equalsIgnoreCase("cannons"))
		{
            if (args.length >= 1)
			{
                //############## console and player commands ######################
                //cannons reload
                if (args[0].equalsIgnoreCase("reload") && (player == null || player.hasPermission("cannons.admin.reload")))
                {
                    // reload config
                    config.loadConfig();
                    sendMessage(sender, ChatColor.GREEN + "Cannons config loaded");
                }
                //cannons save
                else if (args[0].equalsIgnoreCase("save") && (player == null || player.hasPermission("cannons.admin.reload")))
                {
                    // save database
                    persistenceDatabase.saveAllCannons();
                    sendMessage(sender, ChatColor.GREEN + "Cannons database saved ");
                }
                //cannons load
                else if (args[0].equalsIgnoreCase("load") && (player == null || player.hasPermission("cannons.admin.reload")))
                {
                    // load database
                    persistenceDatabase.loadCannons();
                    sendMessage(sender, ChatColor.GREEN + "Cannons database loaded ");
                }
                //cannons reset
                else if(args[0].equalsIgnoreCase("reset") && (player == null || player.hasPermission("cannons.admin.reset")))
                {
                    if (args.length >= 2 && args[1] != null)
                    {
                        // delete all cannon entries for this player
                        if (persistenceDatabase.deleteCannons(args[1]) || plugin.getCannonManager().deleteCannons(args[1]))
                        {
                            //there was an entry in the list
                            sendMessage(sender, ChatColor.GREEN + userMessages.getMessage(MessageEnum.CannonsReseted));
                        }
                        else
                        {
                            sendMessage(sender, ChatColor.GREEN + "Player " + ChatColor.GOLD + args[1] + ChatColor.GREEN + " not found in the storage");
                        }
                    }
                    else
                    {
                        sendMessage(sender, ChatColor.GREEN + "Missing player name " + ChatColor.GOLD + "/cannons reset NAME");
                    }
                }
                //cannons list
                else if(args[0].equalsIgnoreCase("list") && (player == null || player.hasPermission("cannons.admin.list")) )
                {
                    if (args.length >= 2)
                    {
                        //additional player name
                        for (Cannon cannon : plugin.getCannonManager().getCannonList())
                        {
                            sendMessage(sender, ChatColor.GREEN + "Cannon list for " + ChatColor.GOLD + args[1]);
                            if (cannon.getOwner().equalsIgnoreCase(args[1]))
                                sendMessage(sender, ChatColor.GREEN + "Name:" + ChatColor.GOLD + cannon.getCannonName() + ChatColor.GREEN + " design:" + ChatColor.GOLD + cannon.getCannonDesign().getDesignName() +  ChatColor.GREEN +" location:" + ChatColor.GOLD + cannon.getOffset().toString());
                        }
                    }
                    else
                    {
                        //plot all cannons
                        sendMessage(sender, ChatColor.GREEN + "List of all cannons:");
                        for (Cannon cannon : plugin.getCannonManager().getCannonList())
                        {
                            sendMessage(sender, ChatColor.GREEN + "Name:" + ChatColor.GOLD + cannon.getCannonName() + ChatColor.GREEN + " owner:" + ChatColor.GOLD + cannon.getOwner() +  ChatColor.GREEN +" location:" + ChatColor.GOLD + cannon.getOffset().toString());
                        }
                    }
                }



                //################### Player only commands #####################
                else if (player != null)
                {
                    //cannons build
                    if (args[0].equalsIgnoreCase("build") && player.hasPermission("cannons.player.command"))
                    {
                        // how to build a cannon
                        userMessages.displayMessage(player, MessageEnum.HelpBuild);
                    }
                    //cannons fire
                    else if (args[0].equalsIgnoreCase("fire") && player.hasPermission("cannons.player.command"))
                    {
                        // how to fire
                        userMessages.displayMessage(player, MessageEnum.HelpFire);
                    }
                    //cannons adjust
                    else if (args[0].equalsIgnoreCase("adjust") && player.hasPermission("cannons.player.command"))
                    {
                        // how to adjust
                        userMessages.displayMessage(player, MessageEnum.HelpAdjust);
                    }
                    //list cannons of this player name
                    else if(args[0].equalsIgnoreCase("list") && player.hasPermission("cannons.player.list"))
                    {
                        for (Cannon cannon : plugin.getCannonManager().getCannonList())
                        {
                            player.sendMessage(ChatColor.GREEN +"Cannon list for " + args[1] + ":");
                            if (cannon.getOwner().equalsIgnoreCase(player.getName()))
                                player.sendMessage(ChatColor.GREEN + "Name:" + ChatColor.GOLD + cannon.getCannonName() + ChatColor.GREEN + " design:" +
                                        ChatColor.GOLD + cannon.getCannonDesign().getDesignName() + ChatColor.GREEN + " loc:" + ChatColor.GOLD + cannon.getOffset().toString());
                        }
                    }

                    //cannons reset
                    else if(args[0].equalsIgnoreCase("reset") && player.hasPermission("cannons.player.reset"))
                    {
                        // delete all cannon entries for this player
                        persistenceDatabase.deleteCannons(player.getName());
                        plugin.getCannonManager().deleteCannons(player.getName());
                        userMessages.displayMessage(player, MessageEnum.CannonsReseted);
                    }
                    else
                    {
                        // display help
                        userMessages.displayMessage(player, MessageEnum.HelpText);
                    }
                }
                else
                {
                    plugin.logDebug("This command can only be used by a player");
                    return false;
                }



			}
			else
			{
					// display help
					userMessages.displayMessage(player, MessageEnum.HelpText);
			}
			return true;
		}
		return false;
	}


    private void sendMessage(CommandSender sender, String str)
    {
        //strip color of console messages
        if (!(sender instanceof Player))
            str = ChatColor.stripColor(str);

        sender.sendMessage(str);
    }


}
