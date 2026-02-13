package baitap3;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrawlNews {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("===== CRAWL TIN TUC (REGEX) =====");
        System.out.print("Nhap URL (enter de dung mac dinh): ");
        String input = sc.nextLine().trim();

        String url = input.isEmpty() ? "https://dantri.com.vn/the-gioi.htm" : input;

        try {
            String html = fetchHtml(url);

            // Xoa xuong dong + gom khoang trang cho regex de match de hon
            String content = html.replaceAll("\\R+", " ").replaceAll("\\s{2,}", " ");

            // Thu regex theo cau truc pho bien cua Dantri: h3.article-title > a
            Pattern p1 = Pattern.compile(
                    "<h3[^>]*class\\s*=\\s*\"[^\"]*article-title[^\"]*\"[^>]*>\\s*<a[^>]*href\\s*=\\s*\"([^\"]+)\"[^>]*>(.*?)</a>",
                    Pattern.CASE_INSENSITIVE
            );

            // Fallback: neu trang khong co article-title thi lay cac link co the la bai viet
            Pattern p2 = Pattern.compile(
                    "<a[^>]*href\\s*=\\s*\"([^\"]+)\"[^>]*>(.*?)</a>",
                    Pattern.CASE_INSENSITIVE
            );

            Map<String, String> results = new LinkedHashMap<>(); // url -> title (giu thu tu, tu dong loai trung)

            extractWithPattern(url, content, p1, results);

            // Neu it qua thi fallback quet them (nhung loc bot rac)
            if (results.size() < 5) {
                extractWithPatternFallback(url, content, p2, results);
            }

            if (results.isEmpty()) {
                System.out.println("Khong loc duoc tin nao (co the trang doi HTML / chan bot).");
                return;
            }

            System.out.println("\n----- DANH SACH BAN TIN (toi da 20) -----");
            int count = 0;
            for (Map.Entry<String, String> e : results.entrySet()) {
                System.out.println((count + 1) + ". " + e.getValue());
                System.out.println("   " + e.getKey());
                count++;
                if (count >= 20) break;
            }

            System.out.println("\nTong loc duoc: " + results.size() + " (hien thi toi da 20).");

        } catch (Exception e) {
            System.out.println("Loi: " + e.getMessage());
        }
    }

    private static String fetchHtml(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(15000);

        // Nhiều trang yêu cầu User-Agent, không có dễ bị 403/blocked
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Java Crawl)");
        conn.setRequestProperty("Accept", "text/html,*/*");

        int code = conn.getResponseCode();
        if (code >= 300 && code < 400) {
            String location = conn.getHeaderField("Location");
            if (location != null && !location.isEmpty()) {
                return fetchHtml(location);
            }
        }

        if (code != 200) {
            throw new RuntimeException("HTTP " + code);
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append("\n");
        }
        return sb.toString();
    }

    private static void extractWithPattern(String pageUrl, String content, Pattern pattern, Map<String, String> out) {
        Matcher m = pattern.matcher(content);
        while (m.find()) {
            String href = m.group(1);
            String titleRaw = m.group(2);

            String title = cleanText(titleRaw);
            if (title.isEmpty()) continue;

            String fullUrl = resolveUrl(pageUrl, href);
            if (fullUrl == null) continue;

            // Loai trung theo URL
            out.putIfAbsent(fullUrl, title);
        }
    }

    private static void extractWithPatternFallback(String pageUrl, String content, Pattern pattern, Map<String, String> out) {
        Matcher m = pattern.matcher(content);
        while (m.find()) {
            String href = m.group(1);
            String titleRaw = m.group(2);

            String title = cleanText(titleRaw);
            if (title.isEmpty()) continue;

            String fullUrl = resolveUrl(pageUrl, href);
            if (fullUrl == null) continue;

            // Loc bot link rac
            // - bo javascript/mailto/#/dang nhap/.../tag/.../anh/...
            String low = fullUrl.toLowerCase();
            if (low.contains("javascript:") || low.contains("mailto:")) continue;
            if (low.endsWith(".jpg") || low.endsWith(".png") || low.endsWith(".webp")) continue;
            if (low.contains("/tag/") || low.contains("/video") || low.contains("/photo")) continue;

            // Tieu de qua ngan thuong la menu / nut bam
            if (title.length() < 6) continue;

            out.putIfAbsent(fullUrl, title);
            if (out.size() >= 200) break; // chan qua nhieu rac
        }
    }

    private static String resolveUrl(String pageUrl, String href) {
        try {
            URI base = new URI(pageUrl);
            URI resolved = base.resolve(href);
            return resolved.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private static String cleanText(String s) {
        if (s == null) return "";
        // Bo tag con ben trong <a>...</a>
        String t = s.replaceAll("<[^>]+>", "");
        // Unescape don gian (du cho bai tap)
        t = t.replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&nbsp;", " ");
        // Gon khoang trang
        t = t.replaceAll("\\s{2,}", " ").trim();
        return t;
    }
}