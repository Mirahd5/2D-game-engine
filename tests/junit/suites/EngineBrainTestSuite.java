package junit.suites;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import engine.brain.BotTest;
import engine.brain.BrainTest;
import engine.brain.BrainScenarioTest;
import engine.model.StuntBotNotificationTest;

@Suite
@SelectClasses({ BotTest.class, BrainTest.class, BrainScenarioTest.class, StuntBotNotificationTest.class })
public class EngineBrainTestSuite {

}
