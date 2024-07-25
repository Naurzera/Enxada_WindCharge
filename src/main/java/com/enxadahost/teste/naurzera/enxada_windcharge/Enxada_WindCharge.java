package com.enxadahost.teste.naurzera.enxada_windcharge;

import com.enxadahost.teste.naurzera.enxada_windcharge.commands.WindChargeCommand;
import com.enxadahost.teste.naurzera.enxada_windcharge.listeners.WindChargeListener;
import com.enxadahost.teste.naurzera.enxada_windcharge.runnables.WindChargeParticleRunnable;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.WindCharge;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;

public final class Enxada_WindCharge
    extends
    JavaPlugin
{
   // Executor para fazer as coisas em async
   public ExecutorService executor;

   // Fazer as coisas em async periodicamente
   public ScheduledExecutorService executor2;

   // Instanciando a Main
   private static Enxada_WindCharge instance;
   public static Enxada_WindCharge getInstance()
   {
      return instance;
   }

   // Lista de comandos
   private List<WindChargeCommand> wcm = new ArrayList<>();

   // Velocidade
   public double windChargeSpeed = 1;

   // Explosão
   public float windChargeExplosion = 1;

   // Se vamos ou não limitar os valores
   public boolean limitValues;

   // Lista de WindCharges criando partículas
   public List<WindCharge> windCharges;

   // Runnable que cria as particulas
   BukkitTask windChargeParticleRunnable;

   @Override
   public void onEnable()
   {
      // Definindo os objetos
      instance = this;
      executor = Executors.newSingleThreadExecutor();
      executor2 = Executors.newSingleThreadScheduledExecutor();
      saveDefaultConfig();
      windCharges = new ArrayList<>();
      loadConfigValues();

      // Definindo os comandos
      for (String cmdLabel : getConfig().getString("wind_charge_settings.command").split(","))
      {
         WindChargeCommand command = new WindChargeCommand(cmdLabel);
         wcm.add(command);
         registerCommand(command);
      }

      // Registrando o listener dos eventos
      getServer().getPluginManager().registerEvents(new WindChargeListener(), this);
   }

   // Lógica de reload do plugins
   public void reload()
   {
      windCharges.clear();
      reloadConfig();
      for (WindChargeCommand wcm : wcm) wcm.reload();
      loadConfigValues();
   }

   // Lógica para carregar os valores com base na config
   private void loadConfigValues()
   {
      if (windChargeParticleRunnable != null)
      {
         windChargeParticleRunnable.cancel();
      }
      boolean enable = getConfig().getBoolean("wind_charge_settings.projectile-particles.enable");
      if (enable)
      {
         long delay = getConfig().getLong("wind_charge_settings.projectile-particles.frequency");
         windChargeParticleRunnable = new WindChargeParticleRunnable().runTaskTimerAsynchronously(this, delay, delay);
      }
      try
      {
         limitValues = !getConfig().getBoolean("wind_charge_settings.allow-big-values");
      } catch (Exception | Error er)
      {
         limitValues = true;
      }
      try
      {
         windChargeSpeed = getConfig().getDouble("wind_charge_settings.projectile-speed-multiplier");
         if (windChargeSpeed > 10)
         {
            Bukkit.getLogger().log(Level.WARNING, "Note that using values larger than 10 for speed multiplier isn't recommended and can crash the server");
            if (limitValues)
            {
               Bukkit.getLogger().log(Level.INFO, "Big values disabled, setting speed multiplier to 10");
               windChargeSpeed = 10;
            }
         }
      }
      catch (Exception | Error er)
      {
         windChargeSpeed = 1;
      }
      try
      {
         windChargeExplosion = Float.parseFloat(getConfig().getString("wind_charge_settings.explosion-power"));
         if (windChargeExplosion > 50)
         {
            Bukkit.getLogger().log(Level.WARNING, "Note that using values larger than 50 for explosion power isn't recommended and can crash the server");
            if (limitValues)
            {
               Bukkit.getLogger().log(Level.INFO, "Big values disabled, setting explosion power to 50");
               windChargeExplosion = 50;
            }
         }
      } catch (Exception | Error er)
      {
         windChargeExplosion = 1;
      }
   }

   // Registrar comandos dinâmicos
   private void registerCommand(BukkitCommand bukkitCommand)
   {
      try
      {
         final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
         bukkitCommandMap.setAccessible(true);
         CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
         commandMap.register(bukkitCommand.getLabel(), bukkitCommand);
      }
      catch (Exception e)
      {
         setEnabled(false);
         throw new RuntimeException("Failed to register commands", e);
      }
   }
}
