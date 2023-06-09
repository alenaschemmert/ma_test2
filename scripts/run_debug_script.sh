#!/usr/bin/env bash

#./scripts/run_debug_script.sh /c/Users/Alena/source/repos/demo2/build/libs/demo2-1.0-SNAPSHOT.jar
# argument checking
if (( $# != 1 )); then
  echo "Needs exactly 1 argument: Jar Path"
  exit 1
fi

# argument renaming
JAR_PATH="$1"

function log() {
  echo "$1" | tee -a "$LOGFILE"
}


PLOT_DATA_SAVE_DIR="./plot_data"
LOGFILE="./executor.log"
EXEC_DIR="$(date +"%Y-%m-%d_%H-%M-%S")"

# method declaration
function savePlotData() {
  # if plot data directory does not exist
  if [ ! -d "$PLOT_DATA_SAVE_DIR" ]; then
    mkdir -p "$PLOT_DATA_SAVE_DIR"
  fi
  # copy passed executions plot data and rename it meaningful
  cp "$1" "$PLOT_DATA_SAVE_DIR/plot_data.csv"
}

function executeTest() {
    # generate dir names
    # BASEDIR="./${CURRENT_METHOD}"
    # FAIL_DIR="$BASEDIR/fail"
    # WORKING_DIR="$BASEDIR/work"
    # TEST_DIR="$BASEDIR/test"

    # create execution dirs
    # mkdir -p "$BASEDIR" "$FAIL_DIR" "$WORKING_DIR" "$TEST_DIR"

    #core execution
    log ""
    # log "===== Executing $CURRENT_METHOD  ====="
    log "===== Executing with debug ====="
    java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005 --illegal-access=permit -Xmx4G -jar $JAR_PATH | tee -a $LOGFILE 2>/dev/null

    # copy plot data
    #log "Saving Plot data..."
    #savePlotData "plot_data"

   # log "Archiving working directory..."
   # zip -r "$BASEDIR/work.zip" "$WORKING_DIR" && rm -r "$WORKING_DIR"
}

# execution preparation
mkdir -p "$EXEC_DIR" || return 1
cd "$EXEC_DIR" || return 1

executeTest

echo ""
echo "===== DONE ====="