#! /bin/bash -e

# The CircleCI `machine` executor image ships Java 8 as its default JDK, so the
# archived OpenJDK 10.0.2 GA build is installed here and selected explicitly.
# Java 10 is EOL and is not available from any apt repository, hence the tarball.

JDK_VERSION=10.0.2
JDK_DIR=${HOME?}/jdk-${JDK_VERSION?}
JDK_URL=https://download.java.net/java/GA/jdk10/${JDK_VERSION}/19aef61b38124481863b1413dce1855f/13/openjdk-${JDK_VERSION}_linux-x64_bin.tar.gz

if [ ! -x "${JDK_DIR}/bin/java" ] ; then
  echo Installing OpenJDK ${JDK_VERSION?}
  curl -fL "${JDK_URL?}" -o /tmp/openjdk.tar.gz
  rm -rf "${JDK_DIR?}"
  mkdir -p "${JDK_DIR?}"
  # The tarball has a single top-level jdk-10.0.2 directory, which is stripped.
  tar -xzf /tmp/openjdk.tar.gz -C "${JDK_DIR?}" --strip-components=1
  rm -f /tmp/openjdk.tar.gz
else
  echo OpenJDK ${JDK_VERSION?} already installed - restored from cache
fi

# Refresh the EOL JDK's 2018 trust store so Gradle can validate current TLS certificates.
if [ -f /etc/ssl/certs/java/cacerts ] ; then
  cp /etc/ssl/certs/java/cacerts "${JDK_DIR?}/lib/security/cacerts"
fi

# Each CircleCI `run` step is a fresh shell, so JAVA_HOME/PATH have to be
# appended to $BASH_ENV, which is sourced by every subsequent step.
if [ ! -z "$BASH_ENV" ] ; then
  echo "export JAVA_HOME=${JDK_DIR?}" >> "${BASH_ENV?}"
  echo "export PATH=${JDK_DIR?}/bin:\$PATH" >> "${BASH_ENV?}"
fi

export JAVA_HOME=${JDK_DIR?}
export PATH=${JDK_DIR?}/bin:$PATH

java -version
javac -version
