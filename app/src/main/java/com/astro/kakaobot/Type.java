package com.astro.kakaobot;

public class Type {
    public enum ProjectType {
        JS("js");

        private String type;

        ProjectType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }
    }

    public static class Message {
        public String message;
        public String room;
        public String sender;
    }

    public static class Project {
        public boolean enable;
        @Deprecated
        public String isError;
        public String subtitle;
        public String title;
        public ProjectType type;
    }
}
