package junit.suites;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import game.view.GameAvatarTest;

@Suite
@SelectClasses({ EngineCoreTestSuite.class, EngineEntityTestSuite.class, EngineGeometryTestSuite.class,
		EngineShapeTestSuite.class, GameEntityTestSuite.class, EngineBrainTestSuite.class, EngineViewTestSuite.class, GameAvatarTest.class })
public class AllTestSuite {
}