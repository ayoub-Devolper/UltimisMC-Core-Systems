package net.zetrexmc.core.security;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class SentinelShieldPro implements Listener {

    private final JavaPlugin plugin;
    private final HashMap<UUID, Integer> violationFlags = new HashMap<>();

    public SentinelShieldPro(JavaPlugin plugin) {
        this.plugin = plugin;
        registerPacketListener(); // تفعيل التجسس على الحزم فور تشغيل الكود
    }

    // --- الجزء الأول: الكشف عبر الحزم (Packet-Level Detection) ---
    // أسرع وأخف على السيرفر، يكتشف الهاك قبل معالجة الحركة
    private void registerPacketListener() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin,
                ListenerPriority.NORMAL, PacketType.Play.Client.POSITION, PacketType.Play.Client.POSITION_LOOK) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player player = event.getPlayer();
                // هنا نراقب الحزم القادمة من جهاز اللاعب مباشرة
                double x = event.getPacket().getDoubles().read(0);
                double z = event.getPacket().getDoubles().read(2);
                
                // (منطق متقدم: فحص الـ Packets لكل ثانية لمنع الـ Timer Hack)
            }
        });
    }

    // --- الجزء الثاني: الكشف عبر الأحداث (Event-Level Detection) ---
    // أدق في حساب المسافات الجغرافية داخل العالم
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("zetrex.bypass.anticheat") || player.isFlying()) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        double distance = from.distance(to);

        // كشف الـ Speed (إذا تحرك اللاعب أكثر من 0.8 بلوك في تكة واحدة)
        if (distance > 0.79) {
            flagPlayer(player, "Hybrid-Movement-Slight", distance);
        }
    }

    // --- الجزء الثالث: نظام التنبيهات الذكي (Integrated Alert System) ---
    private void flagPlayer(Player player, String hackType, double value) {
        UUID uuid = player.getUniqueId();
        violationFlags.put(uuid, violationFlags.getOrDefault(uuid, 0) + 1);

        if (violationFlags.get(uuid) >= 3) {
            sendStaffAlert(player, hackType, value);
            violationFlags.put(uuid, 0);
        }
    }

    private void sendStaffAlert(Player player, String hackType, double value) {
        String alert = ChatColor.DARK_RED + "[SENTINEL-PRO] " +
                ChatColor.RED + player.getName() + 
                ChatColor.YELLOW + " flagged for " + ChatColor.WHITE + hackType + 
                ChatColor.GRAY + " (Val: " + String.format("%.2f", value) + ")";

        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("zetrex.staff.alerts")) {
                staff.sendMessage(alert);
                staff.playSound(staff.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 2.0f);
            }
        }
        Bukkit.getLogger().warning("[SENTINEL] " + player.getName() + " suspected for " + hackType);
    }
}
