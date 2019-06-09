package com.lzh.controller;

import com.google.common.collect.Maps;
import com.lzh.entity.Subtitles;
import com.lzh.service.GifService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

@RestController
@RequestMapping(path = "/", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
public class GifController {

    @Autowired
    private GifService gifService;

    @RequestMapping(path = "/gif/filePath", method = RequestMethod.POST)
    public Map renderGifPath(@RequestBody Subtitles subtitles){
        ConcurrentMap<String, String> map = Maps.newConcurrentMap();
        try{
            String file = gifService.renderGif(subtitles);
            String filePath = Paths.get(file).getFileName().toString();
            map.put("code", "0");
            map.put("result", filePath);
        } catch (Exception e) {
            map.put("code", "1");
            map.put("result", e.getMessage());
        }
        return map;
    }

    @RequestMapping(path = "/{path}", method = RequestMethod.GET)
    public ResponseEntity<Resource> renderGif(@PathVariable String path) throws Exception {
        String gifPath = Paths.get("/opt/site/cache/").resolve(path).toString();
        Resource resource = new FileSystemResource(gifPath);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=txtx.gif").body(resource);
    }
}
