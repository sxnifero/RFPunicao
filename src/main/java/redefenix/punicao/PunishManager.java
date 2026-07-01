package redefenix.punicao;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PunishManager {
    public final Map<UUID, PunishSession> activeSessions = new HashMap<>();

    public void criarTabela() {
        String sql = "CREATE TABLE IF NOT EXISTS punicoes (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "alvo VARCHAR(16), " +
                "tipo VARCHAR(10), " +
                "motivo TEXT, " +
                "tempo_fim BIGINT, " +
                "staff VARCHAR(16), " +
                "ativo BOOLEAN DEFAULT TRUE)";
        redefenix.bd.Main.getInstance().getDatabaseManager().runUpdateAsync(sql);
    }

    public void aplicarPunicao(String staffName, PunishSession session, long durationMs) {
        long tempoFim = session.isPermanent() ? -1 : System.currentTimeMillis() + durationMs;
        String tipoCurto = session.isBan() ? "BAN" : "MUTE";

        String sql = "INSERT INTO punicoes (alvo, tipo, motivo, tempo_fim, staff) VALUES (?, ?, ?, ?, ?)";
        redefenix.bd.Main.getInstance().getDatabaseManager().runUpdateAsync(sql, session.getTargetName(), tipoCurto, session.getReason(), tempoFim, staffName)
                .thenRun(() -> {
                    String tempoAnuncio = session.isPermanent()
                            ? Messages.get("time-permanent", "&cPermanente")
                            : "§e" + PunishListener.formatTime(durationMs);
                    String tipoAcao = session.isBan()
                            ? Messages.get("action-banned", "BANIDO")
                            : Messages.get("action-muted", "MUTADO");
                    String staffMsg = Messages.get("staff-punish", "&8[&c&lPUNICAO&8] &f{target} &7foi {action} por &f{staff}&7. &8| &7Motivo: &f{reason} &8| &7Tempo: {time}",
                            "{target}", session.getTargetName(),
                            "{action}", tipoAcao,
                            "{staff}", staffName,
                            "{reason}", session.getReason(),
                            "{time}", tempoAnuncio);

                    Bukkit.broadcast(staffMsg, "rf.staff");

                    if (session.isBan()) {
                        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                            Player staffPlayer = Bukkit.getPlayerExact(staffName);
                            if (staffPlayer != null) {
                                String tempoKick = session.isPermanent()
                                        ? Messages.get("time-never", "&cNunca (Permanente)")
                                        : "§e" + PunishListener.formatTime(tempoFim - System.currentTimeMillis());
                                String kickMsg = String.join("\n", Messages.getList("ban-screen", List.of(
                                                "&c&lREDE FENIX",
                                                "",
                                                "&cVoce foi banido!",
                                                "&7Motivo: &f{reason}",
                                                "&7Expira em: {time}",
                                                "",
                                                "&7Faca sua revisao em: &b{appeal_url}"),
                                        "{reason}", session.getReason(),
                                        "{time}", tempoKick,
                                        "{appeal_url}", Messages.get("appeal-url", "https://discord.gg/")));

                                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                                out.writeUTF("KickPlayer");
                                out.writeUTF(session.getTargetName());
                                out.writeUTF(kickMsg);

                                staffPlayer.sendPluginMessage(Main.getInstance(), "BungeeCord", out.toByteArray());
                            }
                        });
                    }
                });
    }
}
