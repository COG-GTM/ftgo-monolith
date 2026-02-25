# ============================================================
# FTGO Platform - Fluentd Custom Image
# ============================================================
# Extends the official Fluentd image with the Elasticsearch
# output plugin for forwarding logs to the EFK stack.
# ============================================================

FROM fluent/fluentd:v1.16-1

# Switch to root to install plugins
USER root

# Install the Elasticsearch output plugin
RUN gem install fluent-plugin-elasticsearch --no-document --version 5.4.3

# Switch back to fluent user
USER fluent

# Copy the configuration file
COPY fluent.conf /fluentd/etc/fluent.conf

# Expose ports
EXPOSE 24224 24224/udp 9880
