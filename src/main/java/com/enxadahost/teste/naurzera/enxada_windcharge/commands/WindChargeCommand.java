package com.enxadahost.teste.naurzera.enxada_windcharge.commands;

import com.enxadahost.teste.naurzera.enxada_windcharge.Enxada_WindCharge;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

public class WindChargeCommand
    extends
    BukkitCommand
{
   /*
   Buscando a permissão e a mensagem da config
    */
   String permission;
   String permMessage;

   /*
   Criando o comando dinâmico
    */
   public WindChargeCommand(String cmd)
   {
      super(cmd);
      permission = Enxada_WindCharge.getInstance()
          .getConfig()
          .getString("wind_charge_settings.staff-permission");
      permMessage = ChatColor.translateAlternateColorCodes('&', Enxada_WindCharge.getInstance()
          .getConfig()
          .getString("wind_charge_settings.no-permission-message"));
   }

   /*
   Recarregando o comando
    */
   public void reload()
   {
      permission = Enxada_WindCharge.getInstance()
          .getConfig()
          .getString("wind_charge_settings.staff-permission");
      permMessage = ChatColor.translateAlternateColorCodes('&', Enxada_WindCharge.getInstance()
          .getConfig()
          .getString("wind_charge_settings.no-permission-message"));
   }

   @Override
   public boolean execute(CommandSender sender, String commandLabel, String[] args)
   {
      // Verificando se o jogador tem a permissão
      if (!sender.hasPermission(permission))
      {
         sender.sendMessage(permMessage);
         return false;
      }

      // Verificando se há argumentos
      if (args.length < 1)
      {
         sender.sendMessage("");
         sender.sendMessage(ChatColor.YELLOW + " < Enxada Wind Charge Help >");
         sender.sendMessage(ChatColor.YELLOW + " /" + commandLabel + " reload");
         sender.sendMessage(ChatColor.YELLOW + " /" + commandLabel + " setspeed");
         sender.sendMessage(ChatColor.YELLOW + " /" + commandLabel + " setpower");
         sender.sendMessage("");
         return true;
      }

      // Comando de reload
      if (args[0].equalsIgnoreCase("reload"))
      {
         Enxada_WindCharge.getInstance()
             .reload();
         sender.sendMessage(ChatColor.GREEN + " Suceesso! O plugin foi recarregado.");
         return true;
      }

      // Comando para definir a velocidade do WindCharge
      if (args[0].equalsIgnoreCase("setspeed"))
      {
         if (args.length < 2)
         {
            sender.sendMessage(ChatColor.RED + " Opa! Utilize /" + commandLabel + " setspeed (número)");
            return false;
         }
         try
         {
            float newSpeed = Float.parseFloat(args[1]);
            if (Enxada_WindCharge.getInstance().limitValues)
            {
               if (newSpeed > 10)
               {
                  sender.sendMessage(ChatColor.RED + " Erro! Velocidade maior que a permitida (verifique a opção §f'allow-big-values' §cna §fconfig.yml");
                  return false;
               }
            }
            Enxada_WindCharge.getInstance().windChargeSpeed = newSpeed;
            Enxada_WindCharge.getInstance()
                .getConfig()
                .set("wind_charge_settings.projectile-speed-multiplier", newSpeed);
            Enxada_WindCharge.getInstance()
                .saveConfig();
            sender.sendMessage(ChatColor.GREEN + " Sucesso! Nova velocidade definida para " + ChatColor.AQUA + newSpeed + ChatColor.GREEN + "!");
            return true;
         } catch (NumberFormatException ex)
         {
            sender.sendMessage(ChatColor.RED + " Opa! Utilize /" + commandLabel + " setspeed (número)");
            return false;
         }
      }

      // Comando para definir o poder da explosão
      if (args[0].equalsIgnoreCase("setpower"))
      {
         if (args.length < 2)
         {
            sender.sendMessage(ChatColor.RED + " Opa! Utilize /" + commandLabel + " setpower (número)");
            return false;
         }
         try
         {
            float newPower = Float.parseFloat(args[1]);
            if (Enxada_WindCharge.getInstance().limitValues)
            {
               if (newPower > 50)
               {
                  sender.sendMessage(ChatColor.RED + " Erro! Força maior que a permitida (verifique a opção " + ChatColor.WHITE + "'allow-big-values' " + ChatColor.RED + "na " + ChatColor.WHITE + "config.yml");
                  return false;
               }
            }
            Enxada_WindCharge.getInstance().windChargeExplosion = newPower;
            Enxada_WindCharge.getInstance()
                .getConfig()
                .set("wind_charge_settings.explosion-power", newPower);
            Enxada_WindCharge.getInstance()
                .saveConfig();
            sender.sendMessage(ChatColor.GREEN + " Sucesso! Nova força de explosão definida para "+ChatColor.AQUA+"" + newPower +ChatColor.GREEN+"!");
            return true;
         } catch (NumberFormatException ex)
         {
            sender.sendMessage(ChatColor.RED + " Opa! Utilize /" + commandLabel + " setpower (número)");
            return false;
         }
      }
      return false;
   }
}
