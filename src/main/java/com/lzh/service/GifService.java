package com.lzh.service;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.lzh.entity.Subtitles;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Paths;
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
    private Map<String, String> TEMPLATE_MAP = new HashMap<>(7);
    private final List<String> TEMPLATE_NAME = ImmutableList.of("wangjingze", "sorry", "kongming", "yalidaye", "zengxiaoxian", "marmot", "woquandouyao", "heiren");

    @PostConstruct
    private void init() throws Exception {
        loadSorryTemplate();
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


}
