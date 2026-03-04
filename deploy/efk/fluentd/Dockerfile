# =============================================================================
# Fluentd Custom Image — FTGO Platform
# =============================================================================
# Extends the official Fluentd image with Elasticsearch and Kubernetes plugins.
# =============================================================================

FROM fluent/fluentd:v1.16-1

USER root

# Install build dependencies for native gem extensions
RUN apk add --no-cache --virtual .build-deps \
        build-base \
        ruby-dev \
        linux-headers && \
    # Install Elasticsearch and Kubernetes metadata plugins
    gem install fluent-plugin-elasticsearch -v 5.4.3 --no-document && \
    gem install fluent-plugin-kubernetes_metadata_filter -v 3.4.0 --no-document && \
    gem install fluent-plugin-rewrite-tag-filter -v 2.4.0 --no-document && \
    # Clean up build dependencies to keep image small
    apk del .build-deps && \
    rm -rf /tmp/* /var/tmp/* /usr/lib/ruby/gems/*/cache/*.gem

# Copy Fluentd configuration
COPY fluent.conf /fluentd/etc/fluent.conf

# Create buffer directory
RUN mkdir -p /var/log/fluentd-buffers && \
    chown -R fluent:fluent /var/log/fluentd-buffers

USER fluent
