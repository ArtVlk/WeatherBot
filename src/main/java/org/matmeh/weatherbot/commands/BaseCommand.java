package org.matmeh.weatherbot.commands;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Slf4j
public abstract class BaseCommand implements IBotCommand {
    public abstract BotApiMethodMessage answer(AbsSender bot, Message message);

    @SneakyThrows
    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        absSender.execute(answer(absSender, message));
        log.debug("@{}: /{} - successfully", message.getChat().getUserName(), getCommandIdentifier());
    }
}
