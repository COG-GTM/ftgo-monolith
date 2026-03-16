FROM fluent/fluentd:v1.14-1

USER root

# Install Elasticsearch plugin for Fluentd
RUN gem install fluent-plugin-elasticsearch --no-document --version 5.2.4 \
    && gem install fluent-plugin-concat --no-document --version 2.5.0 \
    && gem install fluent-plugin-multi-format-parser --no-document --version 1.0.0

USER fluent
