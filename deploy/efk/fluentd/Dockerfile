# =============================================================================
# Fluentd Custom Image — FTGO Platform
# =============================================================================
# Extends the official Fluentd image with Elasticsearch and Kubernetes plugins.
# =============================================================================

FROM fluent/fluentd:v1.16-1

USER root

# Install Elasticsearch and Kubernetes metadata plugins
RUN gem install fluent-plugin-elasticsearch -v 5.4.3 --no-document && \
    gem install fluent-plugin-kubernetes_metadata_filter -v 3.4.0 --no-document && \
    gem install fluent-plugin-rewrite-tag-filter -v 2.4.0 --no-document

# Copy Fluentd configuration
COPY fluent.conf /fluentd/etc/fluent.conf

# Create buffer directory
RUN mkdir -p /var/log/fluentd-buffers && \
    chown -R fluent:fluent /var/log/fluentd-buffers

USER fluent
