# Makefile for PCDashboard Android App

GRADLEW = ./gradlew
RELEASE_OPTS = -Pandroid.packageSigningConfigs.release.signingConfig=debug
PACKAGE_NAME = com.noosxe.pc_dashboard
MAIN_ACTIVITY = .MainActivity

.PHONY: all build install launch run clean help

all: build

## build: Build the app in release mode with all optimizations
build:
	$(GRADLEW) assembleRelease $(RELEASE_OPTS)

## install: Build and install the release app on the connected device
install:
	$(GRADLEW) installRelease $(RELEASE_OPTS)

## launch: Launch the app on the connected device
launch:
	adb shell am start -n $(PACKAGE_NAME)/$(MAIN_ACTIVITY)

## run: Build, install, and launch the app
run: install launch

## clean: Clean the build directory
clean:
	$(GRADLEW) clean

## help: Show this help message
help:
	@echo "Usage: make [target]"
	@echo ""
	@echo "Targets:"
	@grep -E '^##' Makefile | sed -e 's/## //' | column -t -s ':'
