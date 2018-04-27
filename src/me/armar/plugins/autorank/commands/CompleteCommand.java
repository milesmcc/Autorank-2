package me.armar.plugins.autorank.commands;

import me.armar.plugins.autorank.Autorank;
import me.armar.plugins.autorank.commands.manager.AutorankCommand;
import me.armar.plugins.autorank.language.Lang;
import me.armar.plugins.autorank.pathbuilder.Path;
import me.armar.plugins.autorank.pathbuilder.holders.CompositeRequirement;
import me.armar.plugins.autorank.permissions.AutorankPermission;
import me.armar.plugins.autorank.util.AutorankTools;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * The command delegator for the '/ar complete' command.
 */
public class CompleteCommand extends AutorankCommand {

    private final Autorank plugin;

    public CompleteCommand(final Autorank instance) {
        plugin = instance;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(Lang.YOU_ARE_A_ROBOT.getConfigValue("you can't rank up, silly.."));
            return true;
        }

        if (!plugin.getSettingsConfig().usePartialCompletion()) {
            sender.sendMessage(Lang.PARTIAL_COMPLETION_NOT_ENABLED.getConfigValue());
            return true;
        }

        if (!this.hasPermission(AutorankPermission.COMPLETE_REQUIREMENT, sender))
            return true;

        final Player player = (Player) sender;

        String reqIdString;
        String pathName;

        if (args.length < 3) {
            // If no path is given, but there is only one active path, it's not a problem to forget the path.
            if (plugin.getPathManager().getActivePaths(player.getUniqueId()).size() == 1) {
                pathName = plugin.getPathManager().getActivePaths(player.getUniqueId()).get(0).getDisplayName();
            } else {
                // No active path, so then we need a path.
                sender.sendMessage(Lang.INVALID_FORMAT.getConfigValue(this.getUsage()));
                return true;
            }
        } else {
            pathName = AutorankTools.getStringFromArgs(args, 2);
        }

        reqIdString = args[1];

        int completionID = 0;

        try {
            completionID = Integer.parseInt(reqIdString);

            if (completionID < 1) {
                completionID = 1;
            }
        } catch (final Exception e) {
            player.sendMessage(ChatColor.RED + Lang.INVALID_NUMBER.getConfigValue(reqIdString));
            return true;
        }

        Path targetPath = plugin.getPathManager().findPathByDisplayName(pathName, false);

        if (targetPath == null) {
            sender.sendMessage(Lang.NO_PATH_FOUND_WITH_THAT_NAME.getConfigValue());
            return true;
        }

        // CHeck if path is active for the player.
        if (!targetPath.isActive(player.getUniqueId())) {
            sender.sendMessage(Lang.PATH_IS_NOT_ACTIVE.getConfigValue(targetPath.getDisplayName()));
            return true;
        }

        // Rank player as he has fulfilled all requirements
        if (targetPath.getFailedRequirements(player, true).size() == 0) {
            player.sendMessage(ChatColor.GREEN + "You don't have any requirements left.");
            return true;
        }

        List<CompositeRequirement> requirements = targetPath.getRequirements();

        // Get the specified requirement
        if (completionID > requirements.size()) {
            completionID = requirements.size();
        }

        // Human logic = first number is 1 not 0.
        final CompositeRequirement holder = requirements.get((completionID - 1));

        if (targetPath.hasCompletedRequirement(player.getUniqueId(), completionID - 1)) {
            player.sendMessage(ChatColor.RED + Lang.ALREADY_COMPLETED_REQUIREMENT.getConfigValue());
            return true;
        }

        if (holder.meetsRequirement(player)) {
            targetPath.completeRequirement(player, holder.getRequirementId());
        } else {
            // player does not meet requirements
            player.sendMessage(ChatColor.RED + Lang.DO_NOT_MEET_REQUIREMENTS_FOR.getConfigValue(completionID + ""));
            player.sendMessage(ChatColor.AQUA + holder.getDescription());
            player.sendMessage(ChatColor.GREEN + "Current: " + ChatColor.GOLD + holder.getProgress(player));
        }

        return true;
    }

    @Override
    public String getDescription() {
        return "Complete a requirement at this moment";
    }

    @Override
    public String getPermission() {
        return AutorankPermission.COMPLETE_REQUIREMENT;
    }

    @Override
    public String getUsage() {
        return "/ar complete <req id> <path>";
    }
}
