package io.adrianosb.srttotext;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.apache.tika.detect.AutoDetectReader;
import org.apache.tika.exception.TikaException;
import org.jsoup.Jsoup;

/**
 *
 * @author adriano
 */
public class SrtToText {

    public void start(final File path) {

        if (!path.exists()) {
            System.out.println("Path or file doesn't exist!");
            return;
        }

        if (path.isFile()) {
            getSrtAndSaveNewFileTxt(path);
        }

        try {
            findAllSrts(path).forEach(p -> getSrtAndSaveNewFileTxt(p.toFile()));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Get HTML and save new file text
     *
     * @param path
     */
    private void getSrtAndSaveNewFileTxt(final File path) {
        try {

            Charset charset = new AutoDetectReader(FileUtils.openInputStream(path)).getCharset();

            List<String> filtered = Files.lines(path.toPath(), charset)
                    .filter(line -> line.matches(".*[a-zA-Z].*")) //contains alpha
                    .map(line -> html2text(line))
                    .collect(Collectors.toList());

            if (!filtered.isEmpty()) {
                //create new file txt
                File fileTxt = new File("./result_series/" + path.toPath().getName(path.toPath().getNameCount()-2) + "/" + path.toPath().getName(path.toPath().getNameCount()-1) + ".txt");
//                File fileTxt = new File(path.getAbsolutePath() + ".txt");

                FileUtils.writeLines(fileTxt, filtered);
            }
        } catch (IOException | TikaException ex) {
            System.out.println(ex.toString());
        }
    }

    /**
     * Find all SRTs
     *
     * @param path
     * @return
     */
    private Stream<Path> findAllSrts(final File path) throws IOException {
        return Files.find(path.toPath(),
                Integer.MAX_VALUE,
                (filePath, fileAttr) -> fileAttr.isRegularFile()
                && filePath.getFileName().toString().toLowerCase().endsWith(".srt"));
    }

    public static String html2text(String html) {
        return Jsoup.parse(html).text();
    }

}
