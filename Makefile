.DEFAULT_GOAL := all
.PHONY: build run test docker clean

all: build

build:
	@mvn clean package

run:
	@mvn spring-boot:run

test:
	@mvn test

docker:
	@mvn spring-boot:build-image

clean:
	@mvn clean
