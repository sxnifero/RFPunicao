package redefenix.punicao;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;

public class PunishListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView().getTitle().startsWith("§8Punir: ")) {
            e.setCancelled(true);
            if (e.getCurrentItem() == null) return;

            Player p = (Player) e.getWhoClicked();
            String target = e.getView().getTitle().replace("§8Punir: ", "");

            if (e.getSlot() == 26) { p.closeInventory(); return; }

            if (e.getSlot() == 12) {
                abrirMenuTempo(p, target, true);
            } else if (e.getSlot() == 14) {
                abrirMenuTempo(p, target, false);
            }
        }
        else if (e.getView().getTitle().startsWith("§8Tempo: ")) {
            e.setCancelled(true);
            if (e.getCurrentItem() == null) return;

            Player p = (Player) e.getWhoClicked();
            String[] parts = e.getView().getTitle().replace("§8Tempo: ", "").split("-");
            String target = parts[0];
            boolean isBan = parts[1].equals("BAN");

            p.closeInventory();

            if (e.getSlot() == 11) {
                Main.getInstance().getManager().activeSessions.put(p.getUniqueId(), new PunishSession(target, isBan, true));
            } else if (e.getSlot() == 15) {
                Main.getInstance().getManager().activeSessions.put(p.getUniqueId(), new PunishSession(target, isBan, false));
            } else { return; }

            Messages.getList("ask-reason", List.of(
                    "&e&lREDE FENIX &8- &fPor qual motivo voce esta punindo esse jogador?",
                    "&7(Digite &ccancelar &7no chat para cancelar a operacao)"
            )).forEach(p::sendMessage);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChatStaff(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        PunishSession session = Main.getInstance().getManager().activeSessions.get(p.getUniqueId());

        if (session != null) {
            e.setCancelled(true);
            String input = e.getMessage();

            if (input.equalsIgnoreCase("cancelar")) {
                Main.getInstance().getManager().activeSessions.remove(p.getUniqueId());
                p.sendMessage(Messages.get("punish-cancelled", "&cPunicao cancelada."));
                return;
            }

            if (session.getState() == PunishSession.State.AWAITING_REASON) {
                session.setReason(input);
                if (session.isPermanent()) {
                    Main.getInstance().getManager().aplicarPunicao(p.getName(), session, 0);
                    Main.getInstance().getManager().activeSessions.remove(p.getUniqueId());
                } else {
                    session.setState(PunishSession.State.AWAITING_TIME);
                    p.sendMessage(Messages.get("ask-time", "&e&lREDE FENIX &8- &fQual o tempo da punicao? &7(Ex: 1s, 1m, 1h, 1d)"));
                }
            } else if (session.getState() == PunishSession.State.AWAITING_TIME) {
                long ms = parseTime(input);
                if (ms == 0) {
                    p.sendMessage(Messages.get("invalid-time", "&cFormato de tempo invalido! Use: 1s, 1m, 1h, 1d."));
                    return;
                }
                Main.getInstance().getManager().aplicarPunicao(p.getName(), session, ms);
                Main.getInstance().getManager().activeSessions.remove(p.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onLogin(AsyncPlayerPreLoginEvent e) {
        String query = "SELECT * FROM punicoes WHERE alvo = ? AND tipo = 'BAN' AND ativo = TRUE ORDER BY id DESC LIMIT 1";

        try (java.sql.Connection conn = redefenix.bd.Main.getInstance().getDatabaseManager().getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, e.getName());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long exp = rs.getLong("tempo_fim");
                    if (exp != -1 && System.currentTimeMillis() > exp) {
                        redefenix.bd.Main.getInstance().getDatabaseManager().runUpdateAsync("UPDATE punicoes SET ativo = FALSE WHERE id = ?", rs.getInt("id"));
                        return;
                    }

                    String motivo = rs.getString("motivo");
                    String tempoStr = exp == -1 ? Messages.get("time-never", "&cNunca (Permanente)") : "§e" + formatTime(exp - System.currentTimeMillis());
                    String kickMsg = String.join("\n", Messages.getList("ban-screen", List.of(
                                    "&c&lREDE FENIX",
                                    "",
                                    "&cVoce foi banido!",
                                    "&7Motivo: &f{reason}",
                                    "&7Expira em: {time}",
                                    "",
                                    "&7Faca sua revisao em: &b{appeal_url}"),
                            "{reason}", motivo,
                            "{time}", tempoStr,
                            "{appeal_url}", Messages.get("appeal-url", "https://discord.gg/")));

                    e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, kickMsg);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        if (isMuted(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent e) {
        List<String> blockedCmds = Main.getInstance().getConfig().getStringList("blocked-muted-commands");
        String cmd = e.getMessage().split(" ")[0].toLowerCase();

        if (blockedCmds.contains(cmd) && isMuted(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    private boolean isMuted(Player p) {
        String query = "SELECT * FROM punicoes WHERE alvo = ? AND tipo = 'MUTE' AND ativo = TRUE ORDER BY id DESC LIMIT 1";

        try (java.sql.Connection conn = redefenix.bd.Main.getInstance().getDatabaseManager().getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, p.getName());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long exp = rs.getLong("tempo_fim");
                    if (exp != -1 && System.currentTimeMillis() > exp) {
                        redefenix.bd.Main.getInstance().getDatabaseManager().runUpdateAsync("UPDATE punicoes SET ativo = FALSE WHERE id = ?", rs.getInt("id"));
                        return false;
                    }

                    String tempoStr = exp == -1 ? "Permanente" : formatTime(exp - System.currentTimeMillis());
                    p.sendMessage(Messages.get("muted", "&cVoce esta mutado por: &e{time}&c. Motivo: &f{reason}",
                            "{time}", tempoStr,
                            "{reason}", rs.getString("motivo")));
                    return true;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static String formatTime(long millis) {
        if (millis <= 0) return "Expirado";

        long segundos = millis / 1000 % 60;
        long minutos = millis / (60 * 1000) % 60;
        long horas = millis / (60 * 60 * 1000) % 24;
        long dias = millis / (24 * 60 * 60 * 1000);

        StringBuilder sb = new StringBuilder();
        if (dias > 0) sb.append(dias).append(" dias, ");
        if (horas > 0) sb.append(horas).append(" horas, ");
        if (minutos > 0) sb.append(minutos).append(" minutos, ");
        sb.append(segundos).append(" segundos");

        return sb.toString();
    }

    public static void abrirMenuTempo(Player p, String target, boolean isBan) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8Tempo: " + target + "-" + (isBan ? "BAN" : "MUTE"));
        inv.setItem(11, criarItem(Material.BARRIER, "§cPermanentemente", ""));
        inv.setItem(15, criarItem(Material.CLOCK, "§eTemporariamente", ""));
        p.openInventory(inv);
    }

    public static ItemStack criarItem(Material mat, String name, String lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (!lore.isEmpty()) meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    private long parseTime(String time) {
        try {
            long val = Long.parseLong(time.substring(0, time.length() - 1));
            if (time.endsWith("s")) return val * 1000L;
            if (time.endsWith("m")) return val * 60000L;
            if (time.endsWith("h")) return val * 3600000L;
            if (time.endsWith("d")) return val * 86400000L;
        } catch (Exception ignored) {}
        return 0;
    }
}
