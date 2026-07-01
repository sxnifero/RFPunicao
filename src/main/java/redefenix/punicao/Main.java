package redefenix.punicao;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private PunishManager manager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        if (Bukkit.getPluginManager().getPlugin("RFBancoDeDados") == null) {
            getLogger().severe("§c[RFPunicao] RFBancoDeDados não encontrado!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.manager = new PunishManager();
        manager.criarTabela();

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        getServer().getPluginManager().registerEvents(new PunishListener(), this);
        getCommand("punir").setExecutor(new PunirCommand());
        getCommand("despunir").setExecutor(new DespunirCommand());
    }

    public static Main getInstance() { return instance; }
    public PunishManager getManager() { return manager; }
}
