package com.llewkcor.ares.humbug;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@AllArgsConstructor
@CommandAlias("humbug|hb")
public final class HumbugCommand extends BaseCommand {
    @Getter public final Humbug plugin;

    @Subcommand("reload")
    @CommandPermission("humbug.reload")
    @Description("Reload Humbug Configuration")
    public void onReload(CommandSender sender) {
        plugin.getModManager().reload();
        sender.sendMessage(ChatColor.GREEN + "All Humbug Mods have been reloaded");
    }

    @HelpCommand
    public void onHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
        sender.sendMessage(ChatColor.YELLOW + "Type " + ChatColor.GOLD + "/" + help.getCommandName() + " help " + (help.getPage() + 1) + ChatColor.YELLOW + " to see the next page");
    }
}