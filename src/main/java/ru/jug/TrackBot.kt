package ru.jug

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.objects.Ability
import org.telegram.abilitybots.api.objects.Locality.ALL
import org.telegram.abilitybots.api.objects.Privacy.PUBLIC
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.User
import java.util.*

@Service
class TrackBot(
        @Value("\${track-bot.token}") botToken: String,
        @Value("\${track-bot.name}") botUsername: String
        //        , DefaultBotOptions botOptions
) : AbilityBot(botToken, botUsername) {

    private var tags: MutableMap<User, String> = HashMap()

    override fun creatorId(): Int {
        return 315210300
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
                    if (ctx.firstArg().isNullOrBlank()) {
                        silent.send("Пустой список тегов.", ctx.user().id!!.toLong())
                    } else {
                        val user = ctx.update().message.replyToMessage?.from ?: ctx.user()
                        tags[user] = ctx.update().message.text.replace("/reg ", "")

                        silent.send(
                                "Добавлены теги пользователю ${user.userName ?: user.firstName}",
                                ctx.chatId())
                    }
                }
                .build()
    }

    private fun tagsList(user: User) = tags[user]?.split(" ")

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
                            "Теги:" + (tags[id ?: ctx.user()] ?: "<нет>"),
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
                    val filters = ctx.firstArg().split("\n")[0]
                    val filterRole = filters.filter { it.isLetter() }
                    val filterTrack = filters.filter { it.isDigit() }

                    val filtered = tags.filterKeys { id ->
                        val utags = tags[id]!!
                        (filterRole.isBlank() || utags.contains(filterRole))
                                && (filterTrack.isBlank() || utags.contains(filterTrack))
                    }

                    if (filtered.isNotEmpty()) {
                        silent.execute(SendMessage().apply {
                            setChatId(ctx.chatId())
                            setParseMode(ParseMode.MARKDOWN)
                            replyToMessageId = ctx.update().message.messageId
                            text = filtered.keys.joinToString(", ") {
                                "[${it.userName ?: it.firstName}](tg://user?id=${it.id})"
                            }
                        })
                    }
                }
            }.build()
}
