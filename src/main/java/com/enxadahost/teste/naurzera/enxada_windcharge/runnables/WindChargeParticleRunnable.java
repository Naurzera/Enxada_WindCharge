package com.enxadahost.teste.naurzera.enxada_windcharge.runnables;

import com.enxadahost.teste.naurzera.enxada_windcharge.Enxada_WindCharge;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.WindCharge;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;

public class WindChargeParticleRunnable
    extends
    BukkitRunnable
{
   // Verificação se será limitada a quantidade de particulas de uma WindCharge
   boolean allowLimit;

   // Qual será esse limite
   long limit;

   // Qual será a particula
   Particle particle;

   // Quantas partículas serão
   int amount;

   public WindChargeParticleRunnable()
   {
      // Buscando as informações na config
      allowLimit = Enxada_WindCharge.getInstance()
          .getConfig()
          .getLong("wind_charge_settings.projectile-particles.max-particles") != -1;
      limit = Enxada_WindCharge.getInstance()
          .getConfig()
          .getLong("wind_charge_settings.projectile-particles.max-particles");
      particle = Particle.valueOf(Enxada_WindCharge.getInstance()
          .getConfig()
          .getString("wind_charge_settings.projectile-particles.particle-type"));
      amount = Enxada_WindCharge.getInstance()
          .getConfig()
          .getInt("wind_charge_settings.projectile-particles.particle-amount");
      if (amount > 20)
      {
         amount = 20;
         Bukkit.getLogger()
             .log(Level.WARNING, "The amount of particles was defined to 20 to prevent lag");
      }
   }

   // Quantas partículas já foram criadas por uma WindCharge
   LinkedHashMap<WindCharge, Integer> particlesMade = new LinkedHashMap<>();
   List<WindCharge> toRemove = new ArrayList<>();

   @Override
   public void run()
   {
      // Sincronizando pra manter a ordem
      synchronized ("windcharge_particles")
      {
         for (WindCharge wc : toRemove)
         {
            Enxada_WindCharge.getInstance().windCharges.remove(wc);
         }

         // Dando loop nas WindCharges que estão emitindo partículas
         List<WindCharge> subList = Enxada_WindCharge.getInstance().windCharges;
         for (WindCharge wc : subList)
         {
            boolean applyParticle = true;

            // Vendo quantas já foram emitidas, caso o limite esteja ativo
            if (allowLimit)
            {
               // Buscando a informações de quantas partículas já foram criadas por esse WindCharge
               int count = particlesMade.getOrDefault(wc, 0);

               // Somando 1 a contagem de partículas criadas
               if (particlesMade.containsKey(wc))
               {
                  particlesMade.replace(wc, count + 1);
               }
               else
               {
                  particlesMade.put(wc, 1);
               }

               // Caso a contagem tenha excedido o limite...
               if (count > limit)
               {
                  toRemove.add(wc);
                  applyParticle = false;
               }
            }

            if (applyParticle)
            {
               // Fazendo a particula dentro de um try catch, pois algumas partículas precisam de tratamento diferente
               try
               {
                  wc.getLocation()
                      .getWorld()
                      .spawnParticle(particle, wc.getLocation(), 1, 0, 0, 0);
               } catch (Exception | Error ex)
               {
                  Bukkit.getLogger()
                      .log(Level.WARNING, "[Enxada_WindCharge] Invalid particle!");
               }
            }
         }
      }
   }
}
