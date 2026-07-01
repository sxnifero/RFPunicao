package redefenix.punicao;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class PunirCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.hasPermission("rf.staff")) return true;

        if (args.length != 1) {
            p.sendMessage(Messages.get("punish-usage", "&cUse: /punir <nick>"));
            return true;
        }

        String target = args[0];
        Inventory inv = Bukkit.createInventory(null, 27, "§8Punir: " + target);

        inv.setItem(12, PunishListener.criarItem(Material.BARRIER, "§cBanimento", "§7Clique aqui para banir o jogador"));
        inv.setItem(14, PunishListener.criarItem(Material.PAPER, "§aMute", "§7Clique aqui para silenciar o jogador"));
        inv.setItem(26, PunishListener.criarItem(Material.ARROW, "§cVoltar", "§7Fecha o menu e a requisição"));

        p.openInventory(inv);
        return true;
    }
}
