package org.misty.expose.core;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.misty.expose.MistyExposer;
import org.misty.expose.MistyExposerTest2;
import org.misty.expose._tool.AssertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

class MistyExposeDetectorTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    void findBySPIAndCheckDuplicate() {
        List<MistyExpose> list = MistyExposeDetector.findBySPIAndCheckDuplicate();
        print(list);

        Assertions.assertThat(list).contains(new MistyExposer(), new MistyExposerTest2());
    }

    @Test
    void findBySPI_withClassLoader() throws MalformedURLException {
        ClassLoader classLoader = buildTestClassLoader();
        List<MistyExpose> list = MistyExposeDetector.findBySPI(classLoader);
        print(list);

        Assertions.assertThat(list).contains(
                new MistyExposer(),
                new MistyExposerTest2(),
                new MistyExpose("misty-expose-test", "1.0.0")
        );
    }

    @Test
    void findBySPIAndCheckDuplicate_withClassLoader() throws MalformedURLException {
        ClassLoader classLoader = buildTestClassLoader();
        AssertException.print(() -> MistyExposeDetector.findBySPIAndCheckDuplicate(classLoader))
                .isInstanceOf(IllegalArgumentException.class);
    }

    ClassLoader buildTestClassLoader() throws MalformedURLException {
        String anchor = "misty-expose";
        URLClassLoader systemClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        URL[] systemUrls = systemClassLoader.getURLs();
        String pathSample = Arrays.stream(systemUrls)
                .filter(url -> url.toString().contains(anchor))
                .findAny().get().toString();

        int index = pathSample.lastIndexOf(anchor);
        String rootPath = pathSample.substring(0, index);
        String targetPath = rootPath + anchor + File.separator +
                "src" + File.separator +
                "test" + File.separator +
                "other" + File.separator;
        return new URLClassLoader(new URL[]{new URL(targetPath)});
    }

    private void print(List<MistyExpose> list) {
        list.forEach(mistyExpose -> {
            this.logger.info("------------------------------------------------");
            this.logger.info("name              : " + mistyExpose.getName());
            this.logger.info("version           : " + mistyExpose.getVersion());
            this.logger.info("description       : " + mistyExpose.getDescription());
            this.logger.info("fullName          : " + mistyExpose.getFullName());
            this.logger.info("fullNameWithClass : " + mistyExpose.getFullNameWithClass());
            this.logger.info("toString          : " + mistyExpose.toString());
        });
    }

}
