package dev.alphaserpentis.bots.zeroextheta.handlers.bot

import com.google.gson.reflect.TypeToken
import dev.alphaserpentis.bots.zeroextheta.data.bot.NotifyTrigger
import dev.alphaserpentis.bots.zeroextheta.data.bot.ThetaGuildData
import dev.alphaserpentis.bots.zeroextheta.data.bot.ThetaUserData
import dev.alphaserpentis.coffeecore.data.entity.EntityData
import dev.alphaserpentis.coffeecore.data.entity.EntityType
import dev.alphaserpentis.coffeecore.handler.api.discord.entities.DataHandler
import dev.alphaserpentis.coffeecore.serialization.EntityDataDeserializer
import java.nio.file.Path

class ThetaDataHandler<T : EntityData>(
    private val cachePath: Path,
    mainPath: Path,
    jsonDeserializer: EntityDataDeserializer<T>
) : DataHandler<T>(
    mainPath,
    object : TypeToken<MutableMap<String, MutableMap<Long, T>>>() {},
    jsonDeserializer,
    listOf(
        EntityType("guild", ThetaGuildData::class.java),
        EntityType("user", ThetaUserData::class.java),
        EntityType("notify", NotifyTrigger::class.java)
    )
) {

    @Suppress("UNCHECKED_CAST")
    override fun createNewEntityData(entityType: String): T {
        val type = getEntityTypes()
            .stream()
            .filter { entityType == it.id }
            .findFirst()
            .get().id ?: ""

        return when (type) {
            "guild" -> ThetaGuildData() as T
            "user" -> ThetaUserData() as T
            "notify" -> NotifyTrigger() as T
            else -> throw IllegalArgumentException("Entity type $entityType does not exist")
        }
    }

    override fun handleEntityDataException(e: Exception) {

    }
}