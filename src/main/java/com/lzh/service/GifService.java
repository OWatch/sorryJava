package com.lzh.service;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.lzh.entity.GifItem;
import com.lzh.entity.Subtitles;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Getter
@Setter
@Slf4j
@ConfigurationProperties(prefix = "cache.template")
public class GifService {
    private String tempPath;
    private String HOST = "https://xcx.ps502.top";
    private Map<String, String> TEMPLATE_MAP = new HashMap<>(7);
    private Map<String, GifItem> MORE_GIF_CONTENT = new HashMap<>();
    private final List<String> TEMPLATE_NAME = ImmutableList.of("wangjingze", "sorry", "kongming", "yalidaye", "zengxiaoxian", "marmot", "woquandouyao", "heiren",
    "nihaosaoa", "dongshu");


    @Getter
    private List<String> fileNames = new ArrayList<>();


    @PostConstruct
    private void init() throws Exception {
        loadSorryTemplate();
        loadFileNames();
        loadMoreGifContent();
    }

    public String renderGif(Subtitles subtitles) throws Exception {
        String assPath = renderAss(subtitles);
        String gifPath = Paths.get(tempPath).resolve(UUID.randomUUID() + ".gif").toString();
        String videoPath = Paths.get(tempPath).resolve(subtitles.getTemplateName()+"/template.mp4").toString();
        String cmd = String.format("ffmpeg -i %s -r 6 -vf ass=%s,scale=300:-1 -y %s", videoPath, assPath, gifPath);
        if ("simple".equals(subtitles.getMode())) {
//            cmd = String.format("ffmpeg -i %s -r 2 -vf ass=%s,scale=250:-1 -f gif - |gifsicle --optimize=3 --delay=20 > %s ", videoPath, assPath, gifPath);
            cmd = String.format("ffmpeg -i %s -r 5 -vf ass=%s,scale=250:-1 -y %s ", videoPath, assPath, gifPath);
        }
        log.info("cmd: {}", cmd);
        try {
            Process exec = Runtime.getRuntime().exec(cmd);
            exec.waitFor();
//            logger.info("输出:{}",IOUtils.toString(exec.getErrorStream()));
        } catch (Exception e) {
            log.error("生成gif报错：{}", e);
        }
        return gifPath;
    }

    private String renderAss(Subtitles subtitles) throws Exception {
        List<String> list = Splitter.on("&&&").splitToList(subtitles.getSentence());
        String targetPath = Paths.get(tempPath).resolve(UUID.randomUUID().toString().replace("-", "") + ".ass").toString();
        String templateStr = TEMPLATE_MAP.get(subtitles.getTemplateName());
        for (int i = 0; i < list.size(); i++) {
            String srcStr = "<%= sentences[" + i + "] %>";
            templateStr = templateStr.replace(srcStr, list.get(i));
        }
        FileOutputStream fos = new FileOutputStream(targetPath);
        fos.write(templateStr.getBytes());
        fos.close();
        return targetPath;
    }

    public List<GifItem> moreGif(int page) {
        List<GifItem> res = new ArrayList<>();
        for (int i = 8 + page * 8; i < TEMPLATE_NAME.size(); i++) {
            GifItem item = MORE_GIF_CONTENT.get(TEMPLATE_NAME.get(i));
            res.add(item);
        }
        return res;
    }

    private void loadSorryTemplate()  throws Exception  {
        for (String templateName : TEMPLATE_NAME) {
            String originPath = Paths.get(tempPath).resolve(templateName + "/template.ass").toString();
            File file = new File(originPath);
            FileReader in = new FileReader(file);
            BufferedReader bufIn = new BufferedReader(in);
            CharArrayWriter tempStream = new CharArrayWriter();
            String line = null;
            while ((line = bufIn.readLine()) != null) {
                tempStream.write(line);
                tempStream.append(System.getProperty("line.separator"));
            }
            bufIn.close();
            TEMPLATE_MAP.put(templateName, tempStream.toString());
        }
    }

    private void loadFileNames() {
        String path = "/opt/site/cache/discover";
        File dir = new File(path);
        File[] files = dir.listFiles();
        for (File f : files) {
            fileNames.add(f.getName());
        }
    }

    private void loadMoreGifContent() {
        GifItem nihaosaoaItem = new GifItem();
        nihaosaoaItem.setImageUrl(HOST + "/nihaosaoa/sample.gif");
        nihaosaoaItem.setContent("你好骚啊");
        nihaosaoaItem.setTemplate("nihaosaoa");

        GifItem.PlaceHolder s_placeHolder1 = nihaosaoaItem.new PlaceHolder();
        s_placeHolder1.setName("第一句");
        s_placeHolder1.setPlaceholder("xx");
        GifItem.PlaceHolder s_placeHolder2 = nihaosaoaItem.new PlaceHolder();
        s_placeHolder2.setName("第二句");
        s_placeHolder2.setPlaceholder("你好骚啊");
        nihaosaoaItem.getTalks().add(s_placeHolder1);
        nihaosaoaItem.getTalks().add(s_placeHolder2);

        MORE_GIF_CONTENT.put("nihaosaoa", nihaosaoaItem);

        GifItem dongshu = new GifItem();
        dongshu.setImageUrl(HOST + "/dongshu/sample.gif");
        dongshu.setContent("东叔不喜欢");
        dongshu.setTemplate("dongshu");

        GifItem.PlaceHolder d_ph1 = dongshu.new PlaceHolder();
        d_ph1.setName("第一句");
        d_ph1.setPlaceholder("你能给东叔打这个电话");
        GifItem.PlaceHolder d_ph2 = dongshu.new PlaceHolder();
        d_ph2.setName("第二句");
        d_ph2.setPlaceholder("东叔很高兴");
        GifItem.PlaceHolder d_ph3 = dongshu.new PlaceHolder();
        d_ph3.setName("第三句");
        d_ph3.setPlaceholder("但是你刚才说话的语气");
        GifItem.PlaceHolder d_ph4 = dongshu.new PlaceHolder();
        d_ph4.setName("第四句");
        d_ph4.setPlaceholder("东叔不喜欢");
        dongshu.getTalks().add(d_ph1);
        dongshu.getTalks().add(d_ph2);
        dongshu.getTalks().add(d_ph3);
        dongshu.getTalks().add(d_ph4);

        MORE_GIF_CONTENT.put("dongshu", dongshu);
    }

}
