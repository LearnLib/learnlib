import de.learnlib.setting.sources.LearnLibLocalPropertiesAutomataLibSettingsSource;
import de.learnlib.setting.sources.LearnLibLocalPropertiesSource;
import de.learnlib.setting.sources.LearnLibPropertiesAutomataLibSettingsSource;
import de.learnlib.setting.sources.LearnLibPropertiesSource;
import de.learnlib.setting.sources.LearnLibSystemPropertiesAutomataLibSettingsSource;
import de.learnlib.setting.sources.LearnLibSystemPropertiesSource;
import net.automatalib.common.util.setting.SettingsSource;

open module de.learnlib.setting {

    requires de.learnlib.api;
    requires net.automatalib.common.util;
    requires net.automatalib.api;
    requires org.checkerframework.checker.qual;
    requires org.slf4j;

    requires static org.kohsuke.metainf_services;

    exports de.learnlib.setting;
    exports de.learnlib.setting.sources;

    provides SettingsSource with LearnLibLocalPropertiesAutomataLibSettingsSource, LearnLibPropertiesAutomataLibSettingsSource, LearnLibSystemPropertiesAutomataLibSettingsSource, LearnLibLocalPropertiesSource, LearnLibPropertiesSource, LearnLibSystemPropertiesSource;
}