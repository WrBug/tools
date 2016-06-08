package hduLogin;


import common.MD5;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by wangtao on 2016-06-08.
 */
public class JwcManager {
    private String cookie;
    private List<Node> nodes;
    private List<Element> elements;
    private String user;
    private String password;

    public JwcManager(String user, String password) {
        this.user = user;
        this.password = password;
    }
    public String getLT() throws IOException {
        Document document = Jsoup.connect("http://cas.hdu.edu.cn/cas/login").get();
        Elements elements = document.select("[name=lt]");
        String lt = elements.get(0).attr("value");
        System.out.println("lt:" + lt);
        return lt;
    }

    public String login() throws IOException {
        String baseUrl = "http://cas.hdu.edu.cn/cas/login";
        String requestData = "?lt=" + getLT() + "&encodedService=http%3a%2f%2fjxgl.hdu.edu.cn%2findex.aspx" + "&service=http://jxgl.hdu.edu.cn/index.aspx" + "&serviceName=null"
                + "&loginErrCnt=0" + "&username=" + user + "&password=" + MD5.encode(password);
        Document document = Jsoup.connect(baseUrl + requestData).get();
        Elements elements = document.select("a[href*=http://jxgl.hdu.edu.cn/index.aspx?ticket=]");
        if (elements.size() == 0) {
            System.out.print("密码错误");
            System.exit(0);
        }
        String url = elements.get(0).attr("href");
        System.out.println("url:" + url);
        return url;
    }

    public String getCookies() throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(login()).build();
        Response response = client.newCall(request).execute();
        List<String> list = response.headers("set-cookie");
        cookie = list.get(0).substring(0, list.get(0).indexOf(';') + 1) + list.get(1).substring(0, list.get(1).indexOf(';'));
        System.out.println("cookie:" + cookie);
        return cookie;
    }

    public String getMenu() throws IOException {
        String url = "http://jxgl.hdu.edu.cn/xs_main.aspx?xh=" + user;
        Document document = Jsoup.connect(url).header("Cookie", getCookies()).header("Referer", url).post();
        Elements ele = document.select("a[href]");
        int i = 0;
        elements = new ArrayList<>();
        for (Element element : ele) {
            if (element.childNode(0).attr("text") != null && element.childNode(0).attr("text").trim().length() != 0) {
                System.out.println("[" + i + "]\t" + element.childNode(0).attr("text"));
                i++;
                elements.add(element);
            }
        }
        return "";
    }

    public void getInfo(int index) throws IOException {
        Element element = elements.get(index);
        String url = element.absUrl("href");
        Document document = Jsoup.connect(url).header("Cookie", cookie).header("Referer", url).post();
        System.out.println(document.body().text().replace(" ", "\n"));
    }

    public static void main(String args[]) throws IOException {
        JwcManager jwcManager = new JwcManager("12946137", "123456");
        jwcManager.getMenu();
        System.out.println("输入编号：\n");
        Scanner sc = new Scanner(System.in);
        while (sc.hasNext()) {
            int index = sc.nextInt();
            jwcManager.getInfo(index);
        }

    }
}
