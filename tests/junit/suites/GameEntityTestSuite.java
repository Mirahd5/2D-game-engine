package junit.suites;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import engine.model.ViewPortTest;
import game.model.BossTest;
import game.model.EntityCollisionTest;
import game.model.GhostTest;
import game.model.GumTest;
import game.model.ObstacleTest;
import game.model.PacManTest;

@Suite
@SelectClasses({ PacManTest.class, GhostTest.class, GumTest.class, ObstacleTest.class, BossTest.class,
		EntityCollisionTest.class, ViewPortTest.class })
public class GameEntityTestSuite {
}