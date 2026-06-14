package junit.suites;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import engine.brain.BotTest;
import engine.brain.BrainTest;
import engine.model.StuntBotNotificationTest;

@Suite
@SelectClasses({ BotTest.class, BrainTest.class, StuntBotNotificationTest.class })
public class EngineBrainTestSuite {

}
