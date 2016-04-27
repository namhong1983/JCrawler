package com.youdao.dict.crawl;

import cn.edu.hfut.dmic.webcollector.model.Page;
import com.youdao.dict.util.Configuration;

/**
 * Created by zangyq on 2016/4/27.
 */
public class BBCPicsCrawler extends BaseCrawler {
    public BBCPicsCrawler(String propertiesFileName, String crawlPath) {
        super(propertiesFileName, crawlPath);
    }

    public BBCPicsCrawler(Configuration conf) {
        super(conf);
    }

    public BBCPicsCrawler(String crawlPath) {
        super(crawlPath);
    }

    @Override
    public BaseExtractor getBbcrssExtractor(Page page) {
        return new BBCPicsExtractor(page);
    }

    public static void main(String[]  args) throws Exception {

        BBCPicsCrawler crawler = new BBCPicsCrawler("data/BBCPics");
        crawler.setThreads(5);
        crawler.PCAgentMode();

//        Document doc = Jsoup.parse(new URL("http://www.bbc.com/news/10768686"), 180* 1000);//parse("<p>dd</p><img></img>");
//        Element body = doc.body();
//        Element group = body.select(".group").get(2);
//        for(Element e: group.select(".unit__link-wrapper")){
//            crawler.addSeed(e.attr("abs:href"));
//        }
//         doc = Jsoup.parse(new URL("http://www.bbc.com/news/in_pictures"), 180* 1000);//parse("<p>dd</p><img></img>");
//         body = doc.body();
//         group = body.select(".container-sparrow").get(0);
//        for(Element e: group.select("a")){
//            crawler.addSeed(e.attr("abs:href"));
//        }
//        group = body.select(".container-in-pictures-hawk").get(0);
//        for(Element e: group.select("a")){
//            crawler.addSeed(e.attr("abs:href"));
//        }

        crawler.addSeed("http://www.bbc.com/news/magazine-36074328");
//        group = body.select(".container-parakeet").get(0);
//        for(Element e: group.select("a")){
//            crawler.addSeed(e.attr("abs:href"));
//        }
//        System.out.println(body.html());
//        System.out.println(body.outerHtml());

        crawler.start(1);
    }


}
