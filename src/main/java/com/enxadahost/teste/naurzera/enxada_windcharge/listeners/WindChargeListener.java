package com.enxadahost.teste.naurzera.enxada_windcharge.listeners;

import com.enxadahost.teste.naurzera.enxada_windcharge.Enxada_WindCharge;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class WindChargeListener
    implements Listener
{
   /*
   Lista de jogadores que estão imunes a danos de explosão (temporariamente)
    */
   List<Player> playersImmunes = new ArrayList<>();

   /*
   Quando uma entidade explode...
    */
   @EventHandler
   public void onExplosion(EntityExplodeEvent event)
   {
      /*
      Vendo qual é a entidade
       */
      Entity entity = event.getEntity();
      /*
      Verificando se essa entidade já foi verificada por este mesmo método (porque o método
      cria outra explosão, e causaria um loop)
       */
      if (entity.hasMetadata("poweredWindCharge")) return;

      /*
      Verificando se a entidade é um WindCharge
       */
      if (entity instanceof WindCharge)
      {
         WindCharge windCharge = (WindCharge) entity;
         Location location = event.getLocation();
         World world = location.getWorld();

         // Buscando o tamanho de explosão requerido
         float explosionSize = Enxada_WindCharge.getInstance().windChargeExplosion;

         // Aqui vamos configurar para os jogadores nesse raio de explosão não tomarem o dano.
         // Adicionamos +1 em cada direção pois o cálculo de área e esferas do minecraft pode
         // divergir em alguns casos. (Claro, o jogo é quadrado hehe)
         for (Entity entity1 : location.getWorld()
             .getNearbyEntities(location, explosionSize+1.0, explosionSize+1.0, explosionSize+1.0))
         {
            if (entity1 instanceof Player) playersImmunes.add((Player) entity1);
         }

         // Tirar os jogadores depois de 1 tick
         Enxada_WindCharge.getInstance().executor2.schedule(() -> {
            playersImmunes.clear();
         },50, TimeUnit.MILLISECONDS);

         // Criando a explosão
         world.createExplosion(location, explosionSize,false,false, windCharge);

         // Definindo a metadata pra saber que já alteramos esse evento
         entity.setMetadata("poweredWindCharge",new FixedMetadataValue(Enxada_WindCharge.getInstance(), true));

         // Removendo a WindCharge da lista de WindCharges que estão emitindo particulas
         Enxada_WindCharge.getInstance().windCharges.remove(entity);
      }
   }

   /*
   Verificando os danos de explosão
    */
   @EventHandler
   public void onExplosionDamage(EntityDamageEvent event)
   {
      // Se a causa for uma entidadade explodindo
      if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)
      {
         // Se quem sofreu dano foi um player
         if (event.getEntity() instanceof Player)
         {
            Player player = (Player) event.getEntity();

            // Se ele estiver imune (um WindCharge explodiu nele nesse último 1 tick)
            if (playersImmunes.contains(player))
            {
               // Cancelar o dano
               event.setDamage(0);
            }
         }
      }
   }

   /*
   Ao lançar um projétil...
    */
   @EventHandler
   public void onProjectileLaunch(ProjectileLaunchEvent event)
   {
      Projectile projectile = event.getEntity();
      // Se esse projétil for um WindCharge
      if (event.getEntity() instanceof WindCharge)
      {
         WindCharge windCharge = (WindCharge) projectile;
         double x = windCharge.getDirection().getX();
         double y = windCharge.getDirection().getY();
         double z = windCharge.getDirection().getZ();

         // Aplicando o multiplicador de velocidade conforme o solicitado
         double velocityMultiplier = Enxada_WindCharge.getInstance().windChargeSpeed;
         Vector newDirection = new Vector(x*velocityMultiplier,y*velocityMultiplier,z*velocityMultiplier);
         windCharge.setVelocity(newDirection);

         // Adicionando a WindCharge na lista de WindCharges emitindo partículas
         Enxada_WindCharge.getInstance().windCharges.add(windCharge);
      }
   }
}
