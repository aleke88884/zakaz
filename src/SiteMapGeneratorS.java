//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//import java.io.BufferedWriter;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.*;
//import java.util.concurrent.ForkJoinPool;
//import java.util.concurrent.RecursiveAction;
//
//public class SiteMapGeneratorS extends RecursiveAction {
//    private static final String BASE_URL = "https://skillbox.ru/";
//    private static final int MAX_DEPTH = 3;
//    private static final int MAX_LINKS = 100;
//    private static final Map<String, Integer> visited = Collections.synchronizedMap(new LinkedHashMap<>());
//    private final String url;
//    private final int depth;
//
//    public SiteMapGeneratorS(String url, int depth) {
//        this.url = url;
//        this.depth = depth;
//    }
//
//    @Override
//    protected void compute() {
//        if (depth > MAX_DEPTH || visited.size() >= MAX_LINKS) {
//            return;
//        }
//        try {
//            Document doc = Jsoup.connect(url).get();
//            Elements links = doc.select("a[href]");
//            List<SiteMapGeneratorS> tasks = new ArrayList<>();
//
//            for (Element link : links) {
//                String absUrl = link.attr("abs:href");
//                if (shouldVisit(absUrl)) {
//                    synchronized (visited) {
//                        if (visited.size() >= MAX_LINKS) break;
//                        visited.put(absUrl, depth);
//                    }
//                    SiteMapGeneratorS task = new SiteMapGeneratorS(absUrl, depth + 1);
//                    tasks.add(task);
//                }
//            }
//
//            for (SiteMapGeneratorS task : tasks) {
//                task.fork();
//            }
//            for (SiteMapGeneratorS task : tasks) {
//                task.join();
//            }
//
//        } catch (IOException e) {
//            System.err.println("Error fetching the URL: " + url);
//        }
//    }
//
//    private boolean shouldVisit(String url) {
//        return url.startsWith(BASE_URL) && visited.putIfAbsent(url, depth) == null;
//    }
//
//    public static void writeSiteMapToFile(String fileName, Map<String, Integer> links) {
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
//            for (String key : links.keySet()) {
//                writer.write(key + "\n");
//            }
//        } catch (IOException e) {
//            System.err.println("Error writing to file: " + fileName);
//        }
//    }
//
//    public static void main(String[] args) {
//        ForkJoinPool pool = new ForkJoinPool();
//        SiteMapGeneratorS task = new SiteMapGeneratorS(BASE_URL, 0);
//        pool.invoke(task);
//        writeSiteMapToFile("sitemap.txt", visited);
//
//
//        Path inputPath = Path.of("sitemap.txt"); // Укажите путь к вашему исходному файлу
//        Path outputPath = Path.of("output.txt"); // Укажите путь к файлу результату
//
//        try {
//            List<String> urls = Files.readAllLines(inputPath);
//            Collections.sort(urls);
//            List<String> siteMap = generateSiteMap(urls);
//            Files.write(outputPath, siteMap);
//            System.out.println("Site map has been generated and sorted successfully.");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
//    private static List<String> generateSiteMap(List<String> urls) {
//        List<String> siteMap = new ArrayList<>();
//        String rootUrl = "https://skillbox.ru/";
//        int rootDepth = getDepth(rootUrl);
//
//        for (String url : urls) {
//            int currentDepth = getDepth(url) - rootDepth; // Вычитаем глубину корня, чтобы начать с 0
//            String indentation = "\t".repeat(Math.max(0, currentDepth));
//            siteMap.add(indentation + url);
//        }
//
//        return siteMap;
//    }
//
//    private static int getDepth(String url) {
//        // Подсчет количества сегментов пути в URL
//        url = url.replaceFirst("https://skillbox.ru", "");
//        return (int) url.chars().filter(ch -> ch == '/').count();
//    }
//}
