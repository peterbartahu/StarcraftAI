package org.peterbarta.scai;

import org.peterbarta.scai.bot.Bot;
import org.peterbarta.scai.bot.MassMarineBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan("org.peterbarta.scai")
@PropertySource("application.yaml")
public class ContextConfiguration {
    @Bean
    public Bot bot() {
        return new MassMarineBot();
    }
}
