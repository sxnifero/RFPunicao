package redefenix.punicao;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DespunirCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("rf.admin")) {
            sender.sendMessage(Messages.get("no-permission", "&cVoce nao tem permissao para usar este comando."));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(Messages.get("unpunish-usage", "&cUse: /despunir <nick>"));
            return true;
        }

        String target = args[0];
        String sql = "UPDATE punicoes SET ativo = FALSE WHERE alvo = ? AND ativo = TRUE";

        redefenix.bd.Main.getInstance().getDatabaseManager().runUpdateAsync(sql, target)
                .thenRun(() -> {
                    String staffMsg = Messages.get("staff-unpunish", "&8[&a&lDESPUNICAO&8] &f{target} &7foi despunido(a) por &f{staff}&7.",
                            "{target}", target,
                            "{staff}", sender.getName());
                    Bukkit.broadcast(staffMsg, "rf.staff");
                });

        sender.sendMessage(Messages.get("unpunish-success", "&aVoce removeu as punicoes de {target} com sucesso.",
                "{target}", target));
        return true;
    }
}
