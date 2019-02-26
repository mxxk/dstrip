# dstrip

[![Build Status](https://travis-ci.org/mxxk/dstrip.svg?branch=master)](https://travis-ci.org/mxxk/dstrip)
[![](https://images.microbadger.com/badges/image/mxxk/dstrip.svg)](https://cloud.docker.com/repository/docker/mxxk/dstrip "Docker image mxxk/dstrip")

Removes watermarks from PDF files.

## Running

`dstrip` can be run in one of two ways: directly via the JAR, or through Docker.

### JAR

(Running the `dstrip` JAR requires Java 11.)

Download the most recent release JAR from the [releases tab](https://github.com/mxxk/dstrip/releases). Then, you can run it on a PDF file.

```
java -jar dstrip.jar /path/to/original-pdf-file.pdf /path/to/processed-pdf-file.pdf
```

### Docker

You can run the Docker image for this repository directly, and use bind mounts to make the local PDF files accessible to the Docker container. The Docker image is set up to assume a mount path of `/mnt`.

```
docker run --rm -v /path/to/pdf-dir:/mnt mxxk/dstrip \
    original-pdf-file.pdf \
    processed-pdf-file.pdf
```