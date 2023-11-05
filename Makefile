.DEFAULT_GOAL := all
.PHONY: build run test docker clean

all: build

mvn-flags: -B --no-transfer-progress -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn 

build:
	@mvn clean package -Dmaven.test.skip

run:
	@mvn spring-boot:run $(mvn-flags)

test:
	@mvn test $(mvn-flags)

docker:
	@mvn spring-boot:build-image -Dmaven.test.skip $(mvn-flags)

clean:
	@mvn clean
