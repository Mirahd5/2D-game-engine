package junit.suites;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import engine.model.ViewPortTest;
import engine.view.ViewPaintTest;
import engine.view.ViewTest;

@Suite
@SelectClasses({ ViewPortTest.class, ViewTest.class, ViewPaintTest.class })
public class EngineViewTestSuite {
}
