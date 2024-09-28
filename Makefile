# Makefile for DigitalPersona Java Sample with API Integration

# Directories
LIB_DIR = ./lib/java
SRC_DIR = ./src
BIN_DIR = ./bin

# JAR Files
DPJAR = dpuareu.jar
CODEC_JAR = commons-codec-1.15.jar
JSON_JAR = json-20230618.jar

# Classpath
CLASSPATH = $(LIB_DIR)/$(DPJAR):$(LIB_DIR)/$(CODEC_JAR):$(LIB_DIR)/$(JSON_JAR)

# Main Class
MAIN_CLASS = Main

# Java Version
JAVA_VERSION = 11

.PHONY: all clean run

all: $(BIN_DIR) Mirakas.jar

$(BIN_DIR):
	mkdir -p $(BIN_DIR)

Mirakas.jar: $(BIN_DIR) $(SRC_DIR)/*.java manifest.txt
	javac -source $(JAVA_VERSION) -target $(JAVA_VERSION) -classpath $(CLASSPATH) -d $(BIN_DIR) $(SRC_DIR)/*.java
	jar -cvfm Mirakas.jar manifest.txt -C $(BIN_DIR) .

clean:
	rm -rf $(BIN_DIR)
	rm -f Mirakas.jar

run:
	LD_LIBRARY_PATH=./lib/x64 java -cp "Mirakas.jar:./lib/java/*" Main
