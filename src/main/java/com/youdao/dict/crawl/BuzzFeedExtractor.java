package com.youdao.dict.crawl;

import cn.edu.hfut.dmic.webcollector.model.Page;
import com.rometools.rome.feed.synd.SyndEntry;
import com.youdao.dict.bean.ParserPage;
import com.youdao.dict.souplang.SoupLang;
import com.youdao.dict.util.OImageConfig;
import com.youdao.dict.util.OImageUploader;
import lombok.extern.apachecommons.CommonsLog;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pojava.datetime.DateTime;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.PriorityQueue;

//import org.joda.time.DateTime;

/**
 * Created by liuhl on 15-8-17.
 */
@CommonsLog
public class BuzzFeedExtractor extends BaseExtractor {

    public Page _page;

    public Elements resoveEs;

    public BuzzFeedExtractor(Page page) {
//        super(page, true);
        super(page);
        this._page = page;
    }

    public BuzzFeedExtractor(String url) {
        super(url);
    }

    public boolean init() {
        log.debug("*****init*****");
        try {
            SoupLang soupLang = new SoupLang(SoupLang.class.getClassLoader().getResourceAsStream("BuzzFeedRule.xml"));
            context = soupLang.extract(doc);
            content = (Element) context.output.get("content");


            Elements quiz = doc.select(".quiz");
            if(quiz != null && quiz.size() > 0){
                log.error("quiz, skipped, url: " + url);
                return false;
            }

            content.select(".hidden").remove();//去除隐藏的重复内容
//            if(doc.hasClass("quiz")){
//                log.error("quiz, skipped, url: " + url);
//                return false;
//            }
//            if(content != null){
//                getJsLoadedDoc(_page);
//                context = soupLang.extract(doc);
//                content = (Element) context.output.get("content");
//            }


            //为了可以选中视频，清除多余tag
//            resoveEs = content.select(".author-bio");
//            content.select(".author-bio").remove();
//            content.select(".share-buttons").remove();
//            content.select("hgroup").remove();
//            content.select(".Adsense").remove();
//            content.select(".title-divider").remove();
//            content.select("div[id=disqus_thread]").remove();

//            log.error("*****init  failed，isn't an article***** url:" + url);
            log.debug("init success");
            return true;
        } catch (Exception e) {
            log.info("*****init  failed***** url:" + url);
//            e.printStackTrace();
//            System.out.println(e);

            log.error(e.toString());
            return false;
        }
    }

    public boolean extractorTitle() {
        log.debug("*****extractorTitle*****");
//        String title = context.output.get("title").toString();
        Element elementTitle = (Element) context.output.get("title");
        if (elementTitle == null) {
            log.error("element time null, false");
            return false;
        }
        String title = elementTitle.attr("content");
        if (title == null || "".equals(title.trim())) {
            log.error("*****extractorTitle  failed***** url:" + url);
            return false;
        }
        title = title.replaceAll("\\\\s*|\\t|\\r|\\n", "");//去除换行符制表符/r,/n,/t
//        if (title.contains("-"))
//            p.setTitle(title.substring(0, title.lastIndexOf("-")).trim());
//        else
        p.setTitle(title.trim());
        log.debug("*****extractorTitle  success*****");
        return true;
    }

    public boolean extractorType() {
//        Element elementType = (Element) context.output.get("type");
//        if (elementType == null)
//            return false;
//        String type = elementType.attr("content");
//        if (type == null || "".equals(type.trim())) {
//            log.info("*****extractorTitle  failed***** url:" + url);
//            return false;
//        }
//        if (type.contains("/")) {
//            type = type.substring(0, type.indexOf("/"));
//            type = type.replace("/", "");
//        }
////        type = type
//
//        if(type.contains("horoscope")){
//            log.info("horoscope, skipped");
//            return  false;
//        }
//        if(!TypeDictHelper.rightTheType(type)){
//            Map<String, String> map = new HashMap<String, String>();
//            map.put("orgType", type);
//            String moreinfo = new Gson().toJson(map);
//            p.setMoreinfo(moreinfo);
//        }
//        type = TypeDictHelper.getType(type, type);
        String type = BuzzFeedCrawler.url2Type.get(url);
        if(type != null && !type.equals("")) {
            p.setType(type);
        }else{
            log.error("cant get type, false");
            return false;
//            return true;
        }

//        Element elementLabel = (Element) context.output.get("label");
        Element elementLabel = (Element) context.output.get("label");
        if (elementLabel == null) {
            log.error("no keywords, continue");
            return false;
        }
//        Elements elementLabels = elementLabel.select("a");
//        List<String> keywords = new ArrayList<String>(elementLabels.size());
//        for (Element tag : elementLabels) {
//            keywords.add(tag.text());
//        }
        String label = elementLabel.attr("content");
////        String label = (String) context.output.get("label");
//        if (label == null || "".equals(label.trim())) {
//            log.error("*****extractorLabel  failed,continue***** url:" + url);
//            return true;
//        }
//        label = label.contains("China")?"China":label.contains("news")? "World": label;//news belong to World
        String[] keywords = label.split(",");
        PriorityQueue<String> pq = new PriorityQueue<String>(10, new Comparator<String>() {

            @Override
            public int compare(String o1, String o2) {
                int length1 = o1.split(" ").length, length2 = o2.split(" ").length;
                return length1 - length2;
            }
        });

        for (String keyword : keywords) {
            int wordCount = keyword.split(" ").length;
            if (wordCount <= 5) pq.add(keyword);
        }
        StringBuilder sb = new StringBuilder();
        while (!pq.isEmpty()) {
            sb.append(pq.poll());
            if (!pq.isEmpty()) sb.append(',');
        }
        p.setLabel(sb.toString());
        log.debug("*****extractorTitle  success*****");
        return true;
    }

    public boolean extractorTime() {
        log.debug("*****extractorTime*****");
        SyndEntry entry = BuzzFeedCrawler.url2SyndEntry.get(url);
        if(entry != null && entry.getPublishedDate() != null){
            p.setTime(new Timestamp(entry.getPublishedDate().getTime()).toString());
            return true;
        }
        Element elementTime = (Element) context.output.get("time");
        if (elementTime == null) {
            log.error("get Time Null, use CUENTTIME");
            p.setTime(CUENTTIME);
            return true;
        }
//        elementTime.select("a").remove();
        String time = elementTime.text();
        if (time == null || "".equals(time.trim())) {
            log.error("get Time Null22, use CUENTTIME");
            p.setTime(CUENTTIME);
            return true;
        }
        time = time.replaceAll("\\\\", "");
        time = time.trim();
        DateTime dt = new DateTime(time);
//2015-09-12 08:25
//        SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy 'at' -MM-dd'T'hh:mm:ssX", Locale.US);
//        Date date;
//        try {
//            date = format.parse(time.trim());
//        } catch (ParseException e) {
//            return false;
//        }
////        if (System.currentTimeMillis() - date.getTime() > 7 * 24 * 60 * 60 * 1000) {
////            log.debug("*****extractorTime  out of date*****");
////            return false;
////        }
//        p.setTime(new Timestamp(date.getTime() + 14 * 60 * 60 * 1000).toString());//-6 2 cst北京时间
        p.setTime(dt.toString());
        log.debug("*****extractorTime  success*****");
        return true;
    }


    public boolean extractorDescription() {
        log.debug("*****extractor Desc*****");
        Element elementTime = (Element) context.output.get("description");
        if (elementTime == null) {//business版head meta里没有时间
            log.error("can't extract desc, continue");
            return true;
        }
        String description = elementTime.attr("content");
        if (description == null || "".equals(description.trim())) {
            log.info("*****extractor Desc  failed***** url:" + url);
            return true;
        }

        p.setDescription(description);

        return true;
    }

    public boolean extractorContent(boolean paging) {
        log.debug("*****extractorContent*****");
        if (content == null || p == null || (!paging && content.text().length() < MINSIZE)) {
            log.error("extractorContent failed return false");

            return false;
        }

//        Elements navgs = content.select(".article-pagination");
//        content.select(".article-pagination").remove();

        HideSomeHypLink(content);

        Elements hypLinks;
        hypLinks = content.select("noscript");
        for (Element a : hypLinks) {
            a.unwrap();
//            System.out.println(a);
        }

//        if(paging){
//            //删除作者信息
//            content.select("author-bio").remove();
//        }


        content.select(".meta-time").remove();

        Elements embed = content.select(".embed");
        Elements jses = embed.select("script");
        jses.attr("data-height", "100%");
        jses.attr("data-width", "100%");

        content.select(".embed").remove();

        content.select(".reuseble-id").remove();
        content.select(".print").remove();

        replaceFrame();
        removeComments(content);

        String contentHtml = content.html();

        contentHtml = contentHtml.replaceAll("&gt;", ">").replaceAll("&lt;", "<");//替换转义字符

        contentHtml = contentHtml.replaceAll("(?i)(<SCRIPT)[\\s\\S]*?((</SCRIPT>)|(/>))", "");//去除script
        contentHtml = contentHtml.replaceAll("(?i)(<NOSCRIPT)[\\s\\S]*?((</NOSCRIPT>)|(/>))", "");//去除NOSCRIPT
        contentHtml = contentHtml.replaceAll("(?i)(<STYLE)[\\s\\S]*?((</STYLE>)|(/>))", "");//去除style
        contentHtml = contentHtml.replaceAll("<(?!img|a|br|p[ >]|/p).*?>", "");//去除所有标签，只剩img,br,p
        contentHtml = contentHtml.replaceAll("\\\\s*|\\t|\\r|\\n", "");//去除换行符制表符/r,/n,/t /n
//        contentHtml = contentHtml.replaceAll("(\\n[\\s]*?)+", "\n");//多个换行符 保留一个----意义不大，本来也显示不出来，还是加<p>达到换行效果


        if (contentHtml.length() < 384){
            log.error("content after extracted too short, false");

            return false;//太短
        }

        contentHtml = resumeFrame(contentHtml);

        p.setContent(embed.html() + contentHtml);
        if (!paging && isPaging()) {
            mergePage(p);
        }else{//不需要merge
            //把保留的作者信息加上
            if(resoveEs != null){
                p.setContent(p.getContent() + resoveEs.html());
            }
        }
        log.debug("*****extractorContent  success*****");
        return true;
    }



    //dt,, 为了防止导航栏出现在正文，需要抽出来，
    public boolean isPaging(Elements navgs){
        if(navgs == null || navgs.size() < 1){
            return false;
        }
        Elements a = navgs.select("a");
        if (a == null || a.size() < 0) {
            return false;
        }

        for(Element ea: a){
            if(ea.hasClass("next") && ea.hasClass("active")){
                log.info("last page, url: " + url);

                return false;
            }
        }
        return true;
    }

    public boolean isPaging() {
        Elements div = doc.select(".article-pagination");
        if (div == null || div.size() < 1) {
            return false;
        }
        Elements a = div.select("a");
        if (a == null || a.size() < 1) {
            return false;
        }

        for(Element ea: a){
            if(ea.hasClass("next") && ea.hasClass("active"));{
                log.info("last page, url: " + url);
                return false;
            }
        }

//        Elements isActive = a.select(".active");
//
//        if (isActive != null && isActive.size() > 0) {
//            log.info("last page, url: " + url);
//            return false;
//        }

/*        if (url.equals(a.get(0).attr("href"))) {
            return false;
        }*/
        return true;
    }

    public void mergePage(ParserPage p, Elements navgs) {
        log.debug("*****mergePage*****");
        Elements aa = navgs.select("a");

        Element next = aa.select(".next").get(0);
        String nextUrl = next.attr("href");
        try {
            nextUrl = new URL(new URL(url), nextUrl).toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        BuzzFeedExtractor extractor = new BuzzFeedExtractor(nextUrl);
        extractor.init();
        extractor.extractorAndUploadImg();
        extractor.extractorContent(false);
        p.setContent(p.getContent() + extractor.getParserPage().getContent());

        log.info("*****mergePage end*****");
    }

    public void mergePage(ParserPage p) {
        log.debug("*****mergePage*****");
        Elements aa = doc.select(".article-pagination").select("a");

        Element next = aa.select(".next").get(0);
        String nextUrl = next.attr("href");
        try {
            nextUrl = new URL(new URL(url), nextUrl).toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        BuzzFeedExtractor extractor = new BuzzFeedExtractor(nextUrl);
        extractor.init();
        extractor.extractorAndUploadImg();
        extractor.extractorContent(true);
        p.setContent(p.getContent() + extractor.getParserPage().getContent());

        log.info("*****mergePage end*****");
    }


    public boolean extractorAndUploadImg(String host, String port) {
        log.debug("*****extractorAndUploadImg*****");
        if (content == null || p == null) {
            log.error("upload image, content or p null, false");

            return false;
        }
       /* if (host.equals(port)) return true;*/

        Elements imgs = content.select("img");
        String mainImage = null;
        int width = 0;
        for (Element img : imgs) {
            try {
                String imageUrl = img.attr("rel:bf_image_src");
                //                if ("".equals(imageUrl) || !"".equals(img.attr("data-src-small")) || !"".equals(img.attr("itemprop"))) {
                if ("".equals(imageUrl)) {
                    imageUrl = img.attr("src");
                    if("".equals(imageUrl)) {
                        img.remove();
                        continue;
                    }
                }
                img.removeAttr("width");
                img.removeAttr("WIDTH");
                img.removeAttr("height");
                img.removeAttr("HEIGHT");
                img.removeAttr("srcset");
                //                img.removeAttr("srcset");
//                img.attr("style", "width:100%;");
                OImageUploader uploader = new OImageUploader();
                if (!"".equals(host) && !"".equals(port))
                    uploader.setProxy(host, port);
                long id = 0;
                try {
                    id = uploader.deal(imageUrl);
                }catch (Exception e){
                    img.attr("src", imageUrl);
                    log.error("use org img url, continue");
                    continue;
                }
                //                long id = 0;
                URL newUrl = new OImageConfig().getImageSrc(id, "dict-consult");
                int twidth = uploader.getWidth();
                if (twidth >= 300)
                    img.attr("style", "width:100%;");
                img.attr("src", newUrl.toString());
                if (mainImage == null) {
                    width = uploader.getWidth();
                    mainImage = newUrl.toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//         if(mainImage == null) {
        Element elementImg = (Element) context.output.get("mainimage");
        if (elementImg != null) {
            String tmpMainImage = elementImg.attr("content");
            OImageUploader uploader = new OImageUploader();
            if (!"".equals(host) && !"".equals(port))
                uploader.setProxy(host, port);
            long id = 0;
            try {
                id = uploader.deal(tmpMainImage);

//                long id = 0;
                URL newUrl = new OImageConfig().getImageSrc(id, "dict-consult");
                width = uploader.getWidth();
                mainImage = newUrl.toString();
            } catch (Exception e1) {
//                        e1.printStackTrace();

            }
        }
        p.setMainimage(mainImage);
        if (width == 0) {
            p.setStyle("no-image");
        } else if (width >= 300) {
            p.setStyle("large-image");
        } else {
            p.setStyle("no-image");
        }

//        } catch (Exception e) {
//            p.setStyle("no-image");
//        }
        return true;

    }
}