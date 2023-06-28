.DEFAULT_GOAL := all
.PHONY: build run docker clean

all: build

build:
	@mvn clean package

run:
	@mvn spring-boot:run

docker:
	@mvn spring-boot:build-image

clean:
	@mvn clean
