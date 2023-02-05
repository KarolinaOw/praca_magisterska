FROM eclipse-temurin:17-jre-focal

COPY logoformatter-0.1.0-py3-none-any.whl /logoformatter-0.1.0-py3-none-any.whl

RUN apt-get update && \
    apt-get install -y inkscape python3 python3-pip
RUN pip install /logoformatter-0.1.0-py3-none-any.whl
