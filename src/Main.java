import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.concurrent.ForkJoinPool;

public class Main {
    public static void main(String[] args) {
        ForkJoinPool pool = new ForkJoinPool();
        SiteMapGeneratorTask task = new SiteMapGeneratorTask(SiteMapGeneratorTask.BASE_URL, 0);
        pool.invoke(task);
        SiteMapGeneratorTask.writeSiteMapToFile("sitemap.txt", SiteMapGeneratorTask.visited);
        Path inputPath = Path.of("sitemap.txt"); // Укажите путь к вашему исходному файлу
        Path outputPath = Path.of("output.txt"); // Укажите путь к файлу результату

        try {
            List<String> urls = Files.readAllLines(inputPath);
            Collections.sort(urls);
            List<String> siteMap = SiteMapGeneratorTask.generateSiteMap(urls);
            Files.write(outputPath, siteMap);
            System.out.println("Site map has been generated and sorted successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
