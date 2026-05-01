ARG GRAALVM_TAG="25"

FROM ghcr.io/graalvm/native-image-community:$GRAALVM_TAG
ARG SOURCE_DATE_EPOCH="1776889382"
ENV SOURCE_DATE_EPOCH=$SOURCE_DATE_EPOCH
ENV LANG=C.UTF-8
ENV LC_CTYPE=en_US.UTF-8
COPY --chmod=0700 reproducible-builds/entrypoint.sh /usr/local/bin/entrypoint.sh
WORKDIR /signal-cli
ENTRYPOINT [ "/usr/local/bin/entrypoint.sh", "native" ]
