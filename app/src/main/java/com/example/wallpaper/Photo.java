package com.example.wallpaper;

import com.google.gson.annotations.SerializedName;

public class Photo {
    @SerializedName("src")
    private Src src;

    public Src getSrc() {
        return src;
    }

    public class Src {
        @SerializedName("original")
        private String original;

        @SerializedName("large")
        private String large;

        @SerializedName("medium")
        private String medium;

        public String getOriginal() {
            return original;
        }

        public String getLarge() {
            return large;
        }

        public String getMedium() {
            return medium;
        }
    }
}
