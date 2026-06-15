package junit.suites;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import engine.model.BasicStuntTest;
import engine.model.EntityTest;
import engine.model.StuntNotificationTest;

@Suite
@SelectClasses({ EntityTest.class, BasicStuntTest.class, StuntNotificationTest.class })
public class EngineEntityTestSuite {
}