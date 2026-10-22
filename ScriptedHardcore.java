package de.scriptedhardcore;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.ban.ProfileBanList;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;

public final class ScriptedHardcore extends JavaPlugin implements Listener, CommandExecutor {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (getCommand("hardcore") != null) {
            getCommand("hardcore").setExecutor(this);
        }
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("ScriptedHardcore 1.1.0 aktiviert. Keine LifeSteal-Mechanik aktiv.");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player dead = event.getEntity();
        Location deathLocation = dead.getLocation();

        // Vanilla-Todesnachricht unterdruecken, damit sie nicht global gesendet wird.
        event.deathMessage(null);

        double radius = Math.max(0.0, getConfig().getDouble("death-radius", 80.0));
        double radiusSquared = radius * radius;
        String template = getConfig().getString(
                "local-death-message", "&7{player} &cist gestorben.");
        String message = color(template.replace("{player}", dead.getName()));

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (!viewer.getWorld().equals(deathLocation.getWorld())) {
                continue;
            }
            if (viewer.getLocation().distanceSquared(deathLocation) <= radiusSquared) {
                viewer.sendMessage(message);
            }
        }

        if (getConfig().getBoolean("death-ban.enabled", true)) {
            applyDeathBan(dead);
        }
    }

    private void applyDeathBan(Player player) {
        String mode = getConfig().getString("death-ban.mode", "TEMPORARY")
                .toUpperCase(Locale.ROOT);
        String reason = getConfig().getString(
                "death-ban.reason", "Du bist auf diesem Scripted-Hardcore-Server gestorben.");
        String kickMessage = color(getConfig().getString(
                "death-ban.kick-message", "&cDu bist gestorben und kannst vorerst nicht mehr beitreten."));

        if ("PERMANENT".equals(mode)) {
            Bukkit.getBanList(BanList.Type.NAME).addBan(
                    player.getName(), reason, null, "ScriptedHardcore");
            player.kickPlayer(kickMessage);
            return;
        }

        long minutes = Math.max(1L,
                getConfig().getLong("death-ban.temporary-ban-minutes", 60L));
        Date expires = Date.from(Instant.now().plus(Duration.ofMinutes(minutes)));
        Bukkit.getBanList(BanList.Type.NAME).addBan(
                player.getName(), reason, expires, "ScriptedHardcore");
        player.kickPlayer(kickMessage);
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        // Bukkit/Paper prueft die Banliste beim Login automatisch.
        // Der Handler ist bewusst ohne Zusatzaktion; die Methode dokumentiert die Regel.
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(color("&6&lScriptedHardcore &7Befehle"));
        sender.sendMessage(color("&e/hardcore help &7- Hilfe anzeigen"));
        sender.sendMessage(color("&e/hardcore radius <Bloecke> &7- Radius setzen"));
        sender.sendMessage(color("&e/hardcore deathban <temporary|permanent|off> &7- Todesstrafe einstellen"));
        sender.sendMessage(color("&e/hardcore unban <Spieler> &7- Sperre aufheben"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        if (!sender.hasPermission("scriptedhardcore.admin")) {
            sender.sendMessage(color("&cDafuer hast du keine Berechtigung."));
            return true;
        }

        if (args[0].equalsIgnoreCase("radius")) {
            if (args.length != 2) {
                sender.sendMessage(color("&cVerwendung: /hardcore radius <Bloecke>"));
                return true;
            }
            try {
                double radius = Double.parseDouble(args[1]);
                if (radius < 0 || radius > 10000) throw new NumberFormatException();
                getConfig().set("death-radius", radius);
                saveConfig();
                sender.sendMessage(color("&aTodesradius auf " + radius + " Bloecke gesetzt."));
            } catch (NumberFormatException ex) {
                sender.sendMessage(color("&cRadius muss eine Zahl zwischen 0 und 10000 sein."));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("deathban")) {
            if (args.length != 2) {
                sender.sendMessage(color("&cVerwendung: /hardcore deathban <temporary|permanent|off>"));
                return true;
            }
            String value = args[1].toLowerCase(Locale.ROOT);
            switch (value) {
                case "temporary" -> {
                    getConfig().set("death-ban.enabled", true);
                    getConfig().set("death-ban.mode", "TEMPORARY");
                    saveConfig();
                    sender.sendMessage(color("&aTodessperre aktiviert: temporaer (" +
                            getConfig().getLong("death-ban.temporary-ban-minutes", 60) + " Minuten)."));
                }
                case "permanent" -> {
                    getConfig().set("death-ban.enabled", true);
                    getConfig().set("death-ban.mode", "PERMANENT");
                    saveConfig();
                    sender.sendMessage(color("&aTodessperre aktiviert: permanent."));
                }
                case "off" -> {
                    getConfig().set("death-ban.enabled", false);
                    saveConfig();
                    sender.sendMessage(color("&eTodessperre deaktiviert."));
                }
                default -> sender.sendMessage(color("&cVerwendung: /hardcore deathban <temporary|permanent|off>"));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("unban")) {
            if (args.length != 2) {
                sender.sendMessage(color("&cVerwendung: /hardcore unban <Spieler>"));
                return true;
            }
            String playerName = args[1];
            Bukkit.getBanList(BanList.Type.NAME).pardon(playerName);
            sender.sendMessage(color("&aSperre fuer " + playerName + " aufgehoben (falls vorhanden)."));
            return true;
        }

        sender.sendMessage(color("&cUnbekannter Unterbefehl. Nutze /hardcore help"));
        return true;
    }
}
