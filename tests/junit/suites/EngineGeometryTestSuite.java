package junit.suites;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import engine.model.AxisTest;
import engine.model.GridTest;
import engine.model.ISUTest;

@Suite
@SelectClasses({ AxisTest.class, GridTest.class, ISUTest.class })
public class EngineGeometryTestSuite {
}