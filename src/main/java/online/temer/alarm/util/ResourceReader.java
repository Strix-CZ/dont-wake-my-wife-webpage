package online.temer.alarm.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class ResourceReader {
    public String readResourceTextFile(String name) {
        try (var inputStream = getClass().getClassLoader().getResourceAsStream(name)) {

            if (inputStream == null)
                throw new RuntimeException("Resource file " + name + " was not found or can't be read.");

            return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                         .lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
