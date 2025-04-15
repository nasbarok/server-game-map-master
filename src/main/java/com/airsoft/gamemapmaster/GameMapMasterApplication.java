package com.airsoft.gamemapmaster;

import com.airsoft.gamemapmaster.scenario.treasurehunt.TreasureHuntModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(TreasureHuntModule.class)
public class GameMapMasterApplication {

    public static void main(String[] args) {
        SpringApplication.run(GameMapMasterApplication.class, args);
    }
}
