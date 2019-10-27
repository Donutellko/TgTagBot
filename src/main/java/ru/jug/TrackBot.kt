package ru.jug

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.bot.BaseAbilityBot
import org.telegram.abilitybots.api.objects.Ability
import org.telegram.abilitybots.api.objects.Locality.ALL
import org.telegram.abilitybots.api.objects.Privacy.PUBLIC
import java.util.*

@Service
class TrackBot(
        @Value("\${track-bot.token}") botToken: String,
        @Value("\${track-bot.name}") botUsername: String
        //        , DefaultBotOptions botOptions
) : AbilityBot(botToken, botUsername) {

    internal var tags: MutableMap<Int, Array<String>> = HashMap()

    override fun creatorId(): Int {
        return 315210300
    }

    fun sayHelloWorld(): Ability {
        return Ability
                .builder()
                .name(BaseAbilityBot.DEFAULT)
                .info("says hello world!")
                .locality(ALL)
                .privacy(PUBLIC)
                .action { ctx -> silent.send("Hello world!", ctx.chatId()!!) }
                .post { ctx -> silent.send("Anything else!", ctx.chatId()!!) }
                .build()
    }

    /**
     * Adds user and his tags to a storage, to receive notifications
     */
    fun register(): Ability {
        return Ability.builder()
                .name("reg")
                .input(0)
                .locality(ALL)
                .privacy(PUBLIC)
                .action { ctx ->
                    tags[ctx.user().id] = ctx.arguments()
                    silent.send(
                            "Вам добавлены теги " +
                                    ctx.arguments().joinToString("', '", "'", "'"),
                            ctx.user().id!!.toLong())
                }
                .build()
    }

    /**
     * Get info for user with
     */
    fun info(): Ability = Ability.builder()
            .name("info")
            .input(0)
            .locality(ALL)
            .privacy(PUBLIC)
            .action { ctx ->
                run {
                    val id = ctx.update().message.replyToMessage.from.id
                    silent.send(
                            tags[id]?.joinToString("', '", "Теги: ''", "'")
                                    ?: "Нет тегов",
                            ctx.chatId()!!)
                }
            }
            .build()

    /**
     * Tag all people with matching tags
     */
    fun tag(): Ability = Ability.builder()
            .name("tag")
            .input(0)
            .locality(ALL)
            .privacy(PUBLIC)
            .action { ctx ->
                run {
                    val a = ctx.firstArg()
                    val text = ctx.update().message.text
                    val filterRole = a.filter { it.isLetter() }
                    val filterTrack = a.filter { it.isDigit() }

                    val filtered = tags.filterKeys { id ->
                        tags[id]!!.any {
                            (filterRole.isBlank() || it.contains(filterRole))
                                    && (filterTrack.isBlank() || it.contains(filterTrack))
                        }
                    }
                    silent.send(filtered.keys.joinToString(", tg://user?id=", "tg://user?id="), ctx.chatId()!!)
                }
            }.build()
}
