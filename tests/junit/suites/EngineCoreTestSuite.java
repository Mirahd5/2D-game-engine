package junit.suites;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import engine.model.GameTest;
import engine.model.ModelTest;

@Suite
@SelectClasses({ GameTest.class, ModelTest.class})
public class EngineCoreTestSuite {
}