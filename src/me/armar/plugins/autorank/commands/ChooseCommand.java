package me.armar.plugins.autorank.commands;

import java.util.ArrayList;
import java.util.List;

import me.armar.plugins.autorank.Autorank;
import me.armar.plugins.autorank.commands.manager.AutorankCommand;
import me.armar.plugins.autorank.language.Lang;
import me.armar.plugins.autorank.rankbuilder.ChangeGroup;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChooseCommand extends AutorankCommand {

	private final Autorank plugin;

	public ChooseCommand(final Autorank instance) {
		this.setUsage("/ar choose <path name>");
		this.setDesc("Choose a certain ranking path");
		this.setPermission("autorank.choose");

		plugin = instance;
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd,
			final String label, final String[] args) {

		// This command will give a preview of a certain path of ranking.
		if (!plugin.getCommandsManager()
				.hasPermission("autorank.choose", sender)) {
			return true;
		}

		if (args.length < 2) {
			sender.sendMessage(Lang.INVALID_FORMAT.getConfigValue("/ar choose <path name>"));
			return true;
		}
		
		if (!(sender instanceof Player)) {
			sender.sendMessage(Lang.YOU_ARE_A_ROBOT.getConfigValue("you can't choose ranking paths, silly.."));
			return true;
		}
		
		Player player = (Player) sender;
		
		String pathName = args[1];
		
		String groupName = plugin.getAPI().getPrimaryGroup(player);
		
		List<ChangeGroup> changeGroups = plugin.getPlayerChecker().getChangeGroupManager().getChangeGroups(groupName);
		
		if (changeGroups == null || changeGroups.size() == 1) {
			sender.sendMessage(ChatColor.RED + "You don't have to choose a path for this rank, there is only one.");
			return true;
		}
		
		if (pathName.equalsIgnoreCase(plugin.getPlayerDataHandler().getChosenPath(player.getUniqueId()))) {
			sender.sendMessage(ChatColor.RED + "You're already on this path!");
			return true;
		}
		
		ChangeGroup changeGroup = plugin.getPlayerChecker().getChangeGroupManager().matchChangeGroupFromDisplayName(groupName, pathName.toLowerCase());
		
		if (changeGroup == null) {
			sender.sendMessage(ChatColor.RED + "There was no ranking path found with that name.");
			return true;
		}
		
		plugin.getPlayerDataHandler().setChosenPath(player.getUniqueId(), changeGroup.getInternalGroup());

		// Reset progress
		plugin.getPlayerDataHandler().setPlayerProgress(player.getUniqueId(), new ArrayList<Integer>());
		
		sender.sendMessage(ChatColor.GREEN + "You have chosen '" + ChatColor.GRAY + changeGroup.getDisplayName() + ChatColor.GREEN + "'.");
		sender.sendMessage(ChatColor.YELLOW + "Your progress for the rank is reset.");
		

		return true;
	}
	

}
