#! /bin/bash -e

# The CircleCI `machine: true` executor ships with Java 8 as the default JDK, but the
# build now requires Java 10 source/target compatibility. OpenJDK 10 reached end of life
# and is not available from the Ubuntu apt repositories used by the machine image, so we
# install the archived OpenJDK 10.0.2 GA build published on download.java.net (the
# permanent artifact location behind jdk.java.net/archive).

JDK_VERSION=10.0.2
JDK_TARBALL=openjdk-${JDK_VERSION}_linux-x64_bin.tar.gz
JDK_URL=https://download.java.net/java/GA/jdk10/${JDK_VERSION}/19aef61b38124481863b1413dce1855f/13/${JDK_TARBALL}
JVM_DIR=/usr/lib/jvm
JAVA_HOME=${JVM_DIR}/jdk-${JDK_VERSION}

# If a previous (cached) run already installed the JDK, skip the download.
if [ ! -x "${JAVA_HOME}/bin/java" ] ; then
  # Prefer an apt-provided OpenJDK 10 if the base image happens to offer one
  # (a real candidate version, not just a virtual/unavailable package entry).
  sudo apt-get update -qq || true
  if apt-cache policy openjdk-10-jdk 2>/dev/null | grep -qE '^ *Candidate: [0-9]' ; then
    sudo apt-get install -y openjdk-10-jdk
    JAVA_HOME=$(dirname "$(dirname "$(readlink -f "$(command -v javac)")")")
  else
    curl -fsSL -o "/tmp/${JDK_TARBALL}" "${JDK_URL}"
    sudo mkdir -p "${JVM_DIR}"
    # The tarball unpacks into jdk-10.0.2/, matching JAVA_HOME above.
    sudo tar -xzf "/tmp/${JDK_TARBALL}" -C "${JVM_DIR}"
    rm -f "/tmp/${JDK_TARBALL}"
  fi
fi

# Make Java 10 the default for interactive/`update-alternatives`-based lookups too.
sudo update-alternatives --install /usr/bin/java java "${JAVA_HOME}/bin/java" 2000
sudo update-alternatives --install /usr/bin/javac javac "${JAVA_HOME}/bin/javac" 2000
sudo update-alternatives --set java "${JAVA_HOME}/bin/java"
sudo update-alternatives --set javac "${JAVA_HOME}/bin/javac"

# CircleCI 2.0 runs each step in a fresh shell, so persist the environment via $BASH_ENV
# to make JAVA_HOME/PATH visible to every subsequent step (including ./gradlew).
if [ -n "${BASH_ENV}" ] ; then
  echo "export JAVA_HOME=${JAVA_HOME}" >> "${BASH_ENV}"
  echo "export PATH=${JAVA_HOME}/bin:\$PATH" >> "${BASH_ENV}"
fi

export JAVA_HOME
export PATH=${JAVA_HOME}/bin:$PATH

java -version
javac -version
