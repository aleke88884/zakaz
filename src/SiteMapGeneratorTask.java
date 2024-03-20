import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class SiteMapGeneratorTask extends RecursiveTask<Set<String>> {
    public static final String BASE_URL = "https://skillbox.ru/";
    private static final int MAX_DEPTH = 10;
    private static final int MAX_LINKS = 10;
    // В SiteMapGeneratorTask.java
    static final Map<String, Integer> visited = new ConcurrentHashMap<>();

    private final String url;
    private final int depth;

    public SiteMapGeneratorTask(String url, int depth) {
        this.url = url;
        this.depth = depth;
    }

    @Override
    protected Set<String> compute() {
        Set<String> localLinks = new HashSet<>();
        if (depth > MAX_DEPTH || visited.size() >= MAX_LINKS) {
            return localLinks;
        }
        try {
            Thread.sleep(100); // To prevent rate limits
            Document doc = Jsoup.connect(url).get();
            Elements links = doc.select("a[href]");
            List<SiteMapGeneratorTask> tasks = new ArrayList<>();

            for (Element link : links) {
                String absUrl = link.attr("abs:href");
                if (shouldVisit(absUrl)) {
                    localLinks.add(absUrl);
                    SiteMapGeneratorTask task = new SiteMapGeneratorTask(absUrl, depth + 1);
                    task.fork();
                    tasks.add(task);
                }
            }

            for (SiteMapGeneratorTask task : tasks) {
                localLinks.addAll(task.join());
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Error fetching the URL: " + url);
        }
        return localLinks;
    }

    private boolean shouldVisit(String url) {
        synchronized (visited) {
            return url.startsWith(BASE_URL) && !url.contains("#") && visited.putIfAbsent(url, depth) == null;
        }
    }

    public static void main(String[] args) {
        ForkJoinPool pool = new ForkJoinPool();
        SiteMapGeneratorTask task = new SiteMapGeneratorTask(BASE_URL, 0);
        Set<String> links = pool.invoke(task);

        writeSiteMapToFile("sitemaps.txt", visited);
    }

    public static void writeSiteMapToFile(String fileName, Map<String, Integer> links) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (String key : links.keySet()) {
                writer.write(key + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error writing to file: " + fileName);
        }
    }
    public static List<String> generateSiteMap(List<String> urls) {
        List<String> siteMap = new ArrayList<>();
        String rootUrl = "https://skillbox.ru/";
        int rootDepth = getDepth(rootUrl);

        for (String url : urls) {
            int currentDepth = getDepth(url) - rootDepth; // Вычитаем глубину корня, чтобы начать с 0
            String indentation = "\t".repeat(Math.max(0, currentDepth));
            siteMap.add(indentation + url);
        }

        return siteMap;
    }
    private static int getDepth(String url) {
        // Подсчет количества сегментов пути в URL
        url = url.replaceFirst("https://skillbox.ru", "");
        return (int) url.chars().filter(ch -> ch == '/').count();
    }
}
