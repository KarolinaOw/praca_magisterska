#!/bin/bash

SPARK_VERSION="3.0.0"
HADOOP_VERSION="2.7"
JUPYTERLAB_VERSION="2.1.5"

# -- Building the Images

docker build \
  -f Dockerfile.cluster \
  -t cluster-base .

docker build \
  --build-arg spark_version="${SPARK_VERSION}" \
  --build-arg hadoop_version="${HADOOP_VERSION}" \
  -f Dockerfile.spark-base \
  -t spark-base .

docker build \
  -f Dockerfile.spark-master \
  -t spark-master .

docker build \
  -f Dockerfile.spark-worker \
  -t spark-worker .

docker build \
  --build-arg spark_version="${SPARK_VERSION}" \
  --build-arg jupyterlab_version="${JUPYTERLAB_VERSION}" \
  -f Dockerfile.jupyter \
  -t jupyterlab .
