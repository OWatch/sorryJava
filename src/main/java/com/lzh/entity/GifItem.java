package com.lzh.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GifItem {
    private String imageUrl;
    private String content;
    private String template;
    private List<PlaceHolder> talks = new ArrayList<>();

    @Data
    public class PlaceHolder {
        private String name;
        private String placeholder;
    }
}
