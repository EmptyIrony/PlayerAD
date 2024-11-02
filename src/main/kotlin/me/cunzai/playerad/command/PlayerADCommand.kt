package me.cunzai.playerad.command

import me.cunzai.playerad.config.ConfigLoader
import me.cunzai.playerad.ui.AdUI
import me.cunzai.playerad.ui.CheckUI
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.*
import taboolib.expansion.createHelper

@CommandHeader("playerAd", permissionDefault = PermissionDefault.TRUE)
object PlayerADCommand {

    @CommandBody(permissionDefault = PermissionDefault.TRUE)
    val send = subCommand {
        execute<Player> { sender, _, _ ->
            AdUI.open(sender)
        }
    }

    @CommandBody(permission = "ad.admin")
    val check = subCommand {
        execute<Player> { sender, _, _ ->
            CheckUI.open(sender)
        }
    }

    @CommandBody(permission = "ad.admin")
    val reload = subCommand {
        execute<CommandSender> { sender, _, _ ->
            ConfigLoader.config.reload()
            ConfigLoader.sets.clear()
            ConfigLoader.i()

            AdUI.config.reload()

            sender.sendMessage("ok")
        }
    }

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

}