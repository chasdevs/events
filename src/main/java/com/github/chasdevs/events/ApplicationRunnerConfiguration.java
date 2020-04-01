package com.github.chasdevs.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeExceptionMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.shell.ExitRequest;
import org.springframework.shell.Input;
import org.springframework.shell.InputProvider;
import org.springframework.shell.Shell;
import org.springframework.shell.jline.InteractiveShellApplicationRunner;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Configuration class that specifies a {@link CommandLineRunner} bean that essentially assumes any runtime arguments
 * can map directly to interactive shell commands. This allows us to provide the same functionality within
 * a non-interactive CLI as we do in the interactive shell.
 * <p>
 * For example, the non-interactive CLI will be used in our post-build actions on the master branch for syncing with
 * the schema registry. Whereas we could allow the interactive shell for developers to test compatibility before they
 * create a PR.
 */
@Configuration
public class ApplicationRunnerConfiguration {

    @Autowired
    private Shell shell;

    @Bean
    public CommandLineRunner exampleCommandLineRunner(ConfigurableEnvironment environment) {
        return new MapToShellCommandLineRunner(shell, environment);
    }

    @Bean
    public ExitCodeExceptionMapper exitCodeExceptionMapper() {
        return exception -> {
            Throwable e = exception;
            while (e != null && !(e instanceof ExitRequest)) {
                e = e.getCause();
            }
            return e == null ? 1 : ((ExitRequest) e).status();
        };
    }
}

@Order(InteractiveShellApplicationRunner.PRECEDENCE - 2)
class MapToShellCommandLineRunner implements CommandLineRunner {

    private Shell shell;

    private final ConfigurableEnvironment environment;

    public MapToShellCommandLineRunner(Shell shell, ConfigurableEnvironment environment) {
        this.shell = shell;
        this.environment = environment;
    }

    @Override
    public void run(String... args) throws Exception {
        List<String> commandsToRun = Arrays.stream(args)
                .filter(w -> !w.startsWith("@"))
                .collect(Collectors.toList());
        if (!commandsToRun.isEmpty()) {
            InteractiveShellApplicationRunner.disable(environment);
            shell.run(new StringInputProvider(commandsToRun));
        }
    }
}

class StringInputProvider implements InputProvider {

    private final List<String> words;

    private boolean done;

    public StringInputProvider(List<String> words) {
        this.words = words;
    }

    @Override
    public Input readInput() {
        if (!done) {
            done = true;
            return new Input() {
                @Override
                public List<String> words() {
                    return words;
                }

                @Override
                public String rawText() {
                    return StringUtils.collectionToDelimitedString(words, " ");
                }
            };
        }
        else {
            return null;
        }
    }
}