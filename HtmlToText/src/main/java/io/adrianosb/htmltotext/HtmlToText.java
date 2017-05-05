package io.adrianosb.htmltotext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author adriano
 */
public class HtmlToText {

    public void start(final File path) {

        if (!path.exists()) {
            System.out.println("Path or file doesn't exist!");
            return;
        }

        if (path.isFile()) {
            getHtmlAndSaveNewFileTxt(path);
        }
        
        try {
            findAllHtmls(path).forEach(p -> getHtmlAndSaveNewFileTxt(p.toFile()));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Get HTML and save new file txt 
     * @param path 
     */
    private void getHtmlAndSaveNewFileTxt(final File path) {
        try {
            // file HTML to object Document
            Document document = Jsoup.parse(path, "utf-8");
            //get contents of the book
            Element element = document.getElementById("i_apologize_for_the_soup");
            if (element != null) {
                String titulo = getTitulo(document);
                if(StringUtils.isBlank(titulo)){
                    return;
                }
                
                String capitulo = getCapitulo(document, titulo);
                if(StringUtils.isBlank(capitulo)){
                    return;
                }
                
                if (!element.getElementsByTag("audio").isEmpty()) {
                    //remove audio
                    element.getElementsByTag("audio").forEach(e -> e.remove());
                }

                //create new file txt
                File fileTxt = new File("./result_books/"+titulo +"/"+capitulo+ ".txt");
                //save clean content in txt
                FileUtils.writeByteArrayToFile(fileTxt, element.text().getBytes());
                
                System.out.println("OK -> " + fileTxt.getAbsolutePath());
            }
        } catch (IOException ex) {
            System.out.println(ex.toString());
        }
    }

    private String getCapitulo(Document document, String titulo) {
        String capitulo = document.select("#page_content > header > h4").text();
        capitulo = capitulo.replaceAll("[\\W]", " ");
        capitulo = capitulo.trim().replace(" ", "_");
        return capitulo;
    }

    private String getTitulo(Document document) {
        String titulo = document.select("#page_content > header > h2 > a").text();
        titulo = titulo.replaceAll("[\\W]", " ");
        titulo = titulo.trim().replace(" ", "_");
        return titulo;
    }

    /**
     * Find all HTMLs
     * @param path
     * @return
     */
    private Stream<Path> findAllHtmls(final File path) throws IOException {
        return Files.find(path.toPath(),
                Integer.MAX_VALUE,
                (filePath, fileAttr) -> fileAttr.isRegularFile()
                && (filePath.getFileName().toString().toLowerCase().endsWith(".html")
                || filePath.getFileName().toString().toLowerCase().endsWith(".htm")));
    }

}
