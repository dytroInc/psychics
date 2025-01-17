package io.github.dytroInc.psychics.ability.butterfingers

import io.github.monun.psychics.AbilityConcept
import io.github.monun.psychics.ActiveAbility
import io.github.monun.psychics.Channel
import io.github.monun.psychics.TestResult
import io.github.monun.psychics.util.hostileFilter
import io.github.monun.tap.config.Name
import net.kyori.adventure.text.Component.text
import org.bukkit.FluidCollisionMode
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.LivingEntity
import org.bukkit.event.player.PlayerEvent
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory


// 지정한 대상의 들고 있는 아이템을 모두 떨어트리는 능력
@Name("butter-fingers")
class AbilityConceptButterFingers : AbilityConcept() {
    init {
        cooldownTime = 20000L
        cost = 35.0
        range = 16.0
        description =
            listOf(text("지정한 대상이 들고 있는 아이템들을 떨어트립니다."))
        displayName = "부주의"
        wand = ItemStack(Material.BLAZE_ROD)
    }
}

class AbilityButterFingers : ActiveAbility<AbilityConceptButterFingers>() {
    companion object {
        fun dropItem(living: LivingEntity, item: ItemStack?) {
            if (item == null) return
            living.world.dropItemNaturally(living.location, item) {
                it.pickupDelay = 60
            }
            item.amount = 0
        }
    }

    override fun onInitialize() {
        targeter = {
            val player = esper.player
            val start = player.eyeLocation
            val world = start.world

            world.rayTrace(
                start,
                start.direction,
                concept.range,
                FluidCollisionMode.NEVER,
                true,
                0.5,
                player.hostileFilter()
            )?.hitEntity
        }
    }

    override fun onChannel(channel: Channel) {
        val player = esper.player
        val location = player.location.apply { y += 3.0 }

        location.world.spawnParticle(
            Particle.FLAME,
            location,
            10,
            0.0,
            0.0,
            0.0,
            0.03
        )
    }

    override fun onCast(event: PlayerEvent, action: WandAction, target: Any?) {
        if (!(target is LivingEntity && target is InventoryHolder)) {
            return esper.player.sendActionBar(TestResult.FailedTarget.message(this))
        }
        val inventory = target.inventory
        if (inventory !is PlayerInventory) {
            return esper.player.sendActionBar(TestResult.FailedTarget.message(this))
        }
        if (!psychic.consumeMana(concept.cost)) return esper.player.sendActionBar(TestResult.FailedCost.message(this))
        cooldownTime = concept.cooldownTime
        esper.player.sendActionBar(text("${target.name}의 아이템을 떨어트렸습니다."))

        dropItem(target, inventory.itemInMainHand)
        dropItem(target, inventory.itemInOffHand)
    }

}