# Detect OS
ifeq ($(OS),Windows_NT)
    PLATFORM = windows
    LIB_PATH = .\lib\native\windows
    PATH_SEPARATOR = ;
    CLASSPATH_SEPARATOR = ;
    EXE_SUFFIX = .exe
else
    UNAME_S := $(shell uname -s)
    ifeq ($(UNAME_S),Linux)
        PLATFORM = linux
    endif
    ifeq ($(UNAME_S),Darwin)
        PLATFORM = mac
    endif
    LIB_PATH = ./lib/native/$(PLATFORM)
    PATH_SEPARATOR = :
    CLASSPATH_SEPARATOR = :
    EXE_SUFFIX =
endif

# Directories
LIB_DIR = ./lib/java
SRC_DIR = ./src
BIN_DIR = ./bin

# JAR Files
DPJAR = dpuareu.jar
CODEC_JAR = commons-codec-1.15.jar
JSON_JAR = json-20230618.jar

# Classpath
CLASSPATH = $(LIB_DIR)/$(DPJAR)$(CLASSPATH_SEPARATOR)$(LIB_DIR)/$(CODEC_JAR)$(CLASSPATH_SEPARATOR)$(LIB_DIR)/$(JSON_JAR)

# Main Class
MAIN_CLASS = Main

# Java Version
JAVA_VERSION = 11

.PHONY: all clean run

all: $(BIN_DIR) Mirakas.jar

$(BIN_DIR):
	mkdir -p $(BIN_DIR)

Mirakas.jar: $(BIN_DIR) $(SRC_DIR)/*.java manifest.txt
	javac -source $(JAVA_VERSION) -target $(JAVA_VERSION) -classpath "$(CLASSPATH)" -d "$(BIN_DIR)" $(SRC_DIR)/*.java
	jar -cvfm Mirakas.jar manifest.txt -C "$(BIN_DIR)" .

clean:
	rm -rf $(BIN_DIR)
	rm -f Mirakas.jar
	rm -rf ./fingerprints

run:
ifeq ($(OS),Windows_NT)
	set PATH=%PATH%;$(LIB_PATH) && java -cp "Mirakas.jar;$(LIB_DIR)\*" $(MAIN_CLASS)
else
	export LD_LIBRARY_PATH=$(LIB_PATH):$$LD_LIBRARY_PATH && java -cp "Mirakas.jar:$(LIB_DIR)/*" $(MAIN_CLASS)
endif
