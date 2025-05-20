package util;

import com.fbytes.llmka.tools.FileUtil;
import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

public class FileUtilTest {

    private Path tempDir;

    @BeforeEach
    public void setup() throws IOException {
        tempDir = Files.createTempDirectory("fileutiltest");
    }

    @AfterEach
    public void teardown() throws IOException {
        Files.walk(tempDir)
                .map(Path::toFile)
                .sorted((a, b) -> -a.compareTo(b))
                .forEach(File::delete);
    }

    @Test
    public void testMoveToBackup_noExistingBackup() throws IOException {
        Path original = tempDir.resolve("file.txt");
        Files.writeString(original, "test data");
        FileUtil.moveToBackup(original.toString());
        assertFalse(Files.exists(original));
        assertTrue(Files.exists(Path.of(original + ".bak")));
    }

    @Test
    public void testMoveToBackup_withExistingBackup() throws IOException {
        Path original = tempDir.resolve("file.txt");
        Path backup = Path.of(original + ".bak");
        Files.writeString(original, "original");
        Files.writeString(backup, "backup");

        FileUtil.moveToBackup(original.toString());

        assertFalse(Files.exists(original));
        assertTrue(Files.exists(Path.of(original + ".bak")));
        assertFalse(Files.exists(Path.of(original + ".bak.temp")));
    }

    @Test
    public void testMoveToBackup_withExistingBackupAndTemp() throws IOException {
        Path original = tempDir.resolve("file.txt");
        Path backup = Path.of(original + ".bak");
        Path backupTemp = Path.of(original + ".bak.temp");

        Files.writeString(original, "original");
        Files.writeString(backup, "backup");
        Files.writeString(backupTemp, "old temp");

        FileUtil.moveToBackup(original.toString());

        assertFalse(Files.exists(original));
        assertTrue(Files.exists(Path.of(original + ".bak")));
        assertFalse(Files.exists(Path.of(original + ".bak.temp")));
    }
}

