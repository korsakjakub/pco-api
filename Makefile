.DEFAULT_GOAL := all
.PHONY: build run test docker clean

all: build

build:
	@mvn clean package -Dmaven.test.skip

run:
	@mvn spring-boot:run

test:
	@mvn test

docker:
	@mvn spring-boot:build-image -Dmaven.test.skip

clean:
	@mvn clean
