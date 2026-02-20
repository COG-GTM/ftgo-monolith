package com.ftgo.logging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ftgo.logging")
public class LoggingProperties {

    private boolean enabled = true;
    private boolean jsonEnabled = true;
    private boolean traceCorrelationEnabled = true;
    private String serviceName;
    private Logstash logstash = new Logstash();
    private Console console = new Console();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isJsonEnabled() {
        return jsonEnabled;
    }

    public void setJsonEnabled(boolean jsonEnabled) {
        this.jsonEnabled = jsonEnabled;
    }

    public boolean isTraceCorrelationEnabled() {
        return traceCorrelationEnabled;
    }

    public void setTraceCorrelationEnabled(boolean traceCorrelationEnabled) {
        this.traceCorrelationEnabled = traceCorrelationEnabled;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Logstash getLogstash() {
        return logstash;
    }

    public void setLogstash(Logstash logstash) {
        this.logstash = logstash;
    }

    public Console getConsole() {
        return console;
    }

    public void setConsole(Console console) {
        this.console = console;
    }

    public static class Logstash {

        private boolean enabled = false;
        private String host = "localhost";
        private int port = 5000;
        private int queueSize = 512;
        private boolean includeCallerData = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public int getQueueSize() {
            return queueSize;
        }

        public void setQueueSize(int queueSize) {
            this.queueSize = queueSize;
        }

        public boolean isIncludeCallerData() {
            return includeCallerData;
        }

        public void setIncludeCallerData(boolean includeCallerData) {
            this.includeCallerData = includeCallerData;
        }
    }

    public static class Console {

        private boolean enabled = true;
        private boolean prettyPrint = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isPrettyPrint() {
            return prettyPrint;
        }

        public void setPrettyPrint(boolean prettyPrint) {
            this.prettyPrint = prettyPrint;
        }
    }
}
