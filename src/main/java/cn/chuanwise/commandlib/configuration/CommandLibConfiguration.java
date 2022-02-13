package cn.chuanwise.commandlib.configuration;

import lombok.Data;

@Data
public class CommandLibConfiguration {

    @Data
    public static class Option {

        protected String prefix = "--";
        protected String splitter = "=";
    }
    protected Option option = new Option();
}
