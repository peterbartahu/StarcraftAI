package org.peterbarta.scai;

import org.peterbarta.scai.bot.Bot;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(final String[] args) {
        try (final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ContextConfiguration.class)) {
            context.getBean(Bot.class).run();
        }
    }
}
