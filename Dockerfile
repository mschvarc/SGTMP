FROM openjdk:8
RUN apt-get update \
  && apt-get install -y python3-pip python3-dev maven python3-lxml \
  && cd /usr/local/bin \
  && ln -s /usr/bin/python3 python \
  && pip3 install --upgrade pip \
  && pip3 install mosaik==2.3.1 \
  && pip3 install networkx==1.11

RUN mkdir /home/thesis
COPY . /home/thesis/SGTMP

ENV testlauncher_mosaikEnvDir /usr/bin/python3
ENV testlauncher_pythonPlatformFile /home/thesis/SGTMP/testframework-mosaik-api/framework_simulator.py
ENV testlauncher_globalConfigPath /home/thesis/SGTMP/configfiles/global_config.xml 
ENV testlauncher_testsDirectory /home/thesis/SGTMP/configfiles/tests

RUN echo export testlauncher_mosaikEnvDir=/usr/bin/python3 >> ~/.bashrc
RUN echo export testlauncher_pythonPlatformFile=/home/thesis/SGTMP/testframework-mosaik-api/framework_simulator.py >> ~/.bashrc
RUN echo export testlauncher_globalConfigPath=/home/thesis/SGTMP/configfiles/global_config.xml >> ~/.bashrc
RUN echo export testlauncher_testsDirectory=/home/thesis/SGTMP/configfiles/tests >> ~/.bashrc

RUN cd /home/thesis/SGTMP/mosaik-java-api && mvn clean install package
RUN cd /home/thesis/SGTMP && mvn clean install package
RUN cd /home/thesis/SGTMP/testframework-api && mvn test -Dtest=ArduinoTestLauncher
RUN cd /home/thesis/SGTMP/testframework-api && mvn test -Dtest=TestLauncher

EXPOSE 8080

WORKDIR /home/thesis/SGTMP/
CMD cd /home/thesis/SGTMP/testframework-web && mvn spring-boot:run

#run from console: docker run -p 8080:8080 -it {container id}
